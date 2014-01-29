/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.network.interop;

import ch.qos.logback.classic.Level;
import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.logging.LoggingService;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.astrogrid.samp.Client;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.Subscriptions;
import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.client.SampException;
import org.astrogrid.samp.gui.GuiHubConnector;
import org.astrogrid.samp.gui.SubscribedClientListModel;
import org.astrogrid.samp.gui.SysTray;
import org.astrogrid.samp.hub.Hub;
import org.astrogrid.samp.hub.HubServiceMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SampManager singleton class.
 * 
 * Note: JSamp 1.3.3 required
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES, Guillaume MELLA.
 */
public final class SampManager {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(SampManager.class.getName());
    /** flag to stop running hub at shutdown explicitely i.e. do not rely on JVM shutdown hooks (buggy) */
    private static final boolean stopHubOnShutdown = true;
    /** Singleton instance */
    private static volatile SampManager _instance = null;
    /** Hook to the "Interop" menu */
    private static volatile JMenu _menu = null;
    /** JMenu to Action relations */
    private static final Map<SampCapabilityAction, JMenu> _map = Collections.synchronizedMap(new HashMap<SampCapabilityAction, JMenu>(8));

    /* members */
    /** GUI hub connector */
    private final GuiHubConnector _connector;

    /**
     * Return the singleton instance
     * @return singleton instance
     */
    public static synchronized SampManager getInstance() {
        // DO NOT MODIFY !!!
        if (_instance == null) {
            _instance = new SampManager();
        }

        return _instance;

        // DO NOT MODIFY !!!
    }

    /**
     * @return true if SAMP hub should not prevent quitting, false otherwise.
     */
    public synchronized boolean allowHubKilling() {

        // If the application wants to bypass SAMP hub killing warning message
        if (App.getInstance().shouldSilentlyKillSampHubOnQuit()) {
            // Let the hub die without prompting confirmation
            return true;
        }

        // If no one else is registered to the hub
        int nbOfConnectedClient = _connector.getClientListModel().getSize();
        if (nbOfConnectedClient <= 2) { // at least 1 for the hub, and possibly 1 for us
            _logger.info("No one else but us is registered to SAMP hub, letting application quits.");
            // Let the hub die without prompting confirmation
            return true;
        }

        // If we did not launch the hub ourself
        if (getRunningHub() == null) {
            _logger.info("Application has not launched the SAMP hub internally, letting application quits.");
            // Let the hub die without prompting confirmation
            return true;
        }

        _logger.info("Application has launched the SAMP hub internally, asking user if it should be killed or not.");

        // Ask the user to confirm hub killing
        boolean shouldWeQuit = MessagePane.showConfirmKillHub();
        if (!shouldWeQuit) {
            _logger.info("User dissmissed SAMP hub termination, preventing application from quitting.");
            // Prevent hub dying
            return false;
        }

        _logger.info("User allowed SAMP hub termination, proceeding with application quitting.");
        // Let the hub die after all
        return true;
    }

    /**
     * Explicitely shut down the hub connector
     */
    public static synchronized void shutdown() {
        if (_instance != null) {
            _instance.shutdownNow();
            _instance = null;
        }
    }

    /**
     * Hidden constructor
     */
    protected SampManager() {

        // @TODO : init JSamp env.
        final ClientProfile profile = DefaultClientProfile.getProfile();

        _connector = new GuiHubConnector(profile);

        // Build application metadata
        final ApplicationDescription applicationDescription = ApplicationDescription.getInstance();
        final String applicationName = applicationDescription.getProgramName();
        Metadata metaData = forgeSampMetaDataFromApplicationDescription(applicationName, applicationDescription);
        _connector.declareMetadata(metaData);

        // Monitor hub connections
        _connector.addConnectionListener(new SampConnectionChangeListener());

        // Try to connect
        _connector.setActive(true);
        if (!_connector.isConnected()) {
            // Try to start an internal SAMP hub if none available (JNLP do not support external hub) :
            try {
                Hub.runHub(getInternalHubMode());
            } catch (IOException ioe) {
                _logger.debug("unable to start internal hub (probably another hub is already running)", ioe);
            }

            // Retry connection
            _connector.setActive(true);
        }
        if (_connector.isConnected()) {
            _logger.info("Application ['{}'] connected to the SAMP Hub.", applicationName);
        } else {
            StatusBar.show("Could not connect to an existing hub or start an internal SAMP hub.");
        }

        // Keep a look out for hubs if initial one shuts down
        _connector.setAutoconnect(5);

        // This step required even if no message handlers added.
        _connector.declareSubscriptions(_connector.computeSubscriptions());
    }

    /**
     * Create a new SAMP metadata instance using the given application name and its application data model instance
     * @param applicationName application name
     * @param applicationDataModel application data model instance
     * @return new SAMP Metadata instance
     */
    private Metadata forgeSampMetaDataFromApplicationDescription(final String applicationName, final ApplicationDescription applicationDataModel) {

        final Metadata meta = new Metadata();

        meta.setName(applicationName);

        final String sampDescription = applicationDataModel.getSampDescription();
        if (sampDescription != null) {
            meta.setDescriptionText(sampDescription);
        }

        final String documentationUrl = applicationDataModel.getDocumetationUrl();
        if (documentationUrl != null) {
            meta.setDocumentationUrl(documentationUrl);
        }

        // @TODO : embbed the icon in each application JAR file
        // meta.setIconUrl("http://apps.jmmc.fr/~sclws/SearchCal/AppIcon.png");

        // Non-standard meatadata
        meta.put(SampMetaData.AFFILIATION_NAME.id(), applicationDataModel.getShortCompanyName() + " (" + applicationDataModel.getLegalCompanyName() + ")");

        meta.put(SampMetaData.AFFILIATION_URL.id(), applicationDataModel.getMainWebPageURL());

        final String jnlpUrl = applicationDataModel.getJnlpUrl();
        if (jnlpUrl != null) {
            meta.put(SampMetaData.JNLP_URL.id(), jnlpUrl);
        }

        final String userSupportUrl = applicationDataModel.getUserSupportURL();
        if (userSupportUrl != null) {
            meta.put(SampMetaData.AFFILIATION_CONTACT.id(), userSupportUrl);
        }

        String authors = applicationDataModel.getAuthors();
        if (authors != null) {
            meta.put(SampMetaData.AUTHORS.id(), "Brought to you by " + authors);
        }

        meta.put(SampMetaData.HOMEPAGE_URL.id(), applicationDataModel.getLinkValue());

        meta.put(SampMetaData.RELEASE_VERSION.id(), applicationDataModel.getProgramVersion());

        meta.put(SampMetaData.RELEASE_DATE.id(), applicationDataModel.getCompilationDate());

        final String newsUrl = applicationDataModel.getHotNewsRSSFeedLinkValue();
        if (newsUrl != null) {
            meta.put(SampMetaData.RSS_URL.id(), newsUrl);
        }

        final String faq = applicationDataModel.getFaqLinkValue();
        if (faq != null) {
            meta.put(SampMetaData.FAQ_URL.id(), faq);
        }

        return meta;
    }

    /**
     * Shutdown operations
     */
    private void shutdownNow() {
        // It is good practice to call setActive(false) when this object is finished with;
        // however if it is not called explicitly, any open connection will unregister itself
        // on object finalisation or JVM termination, as long as the JVM shuts down cleanly.

        // Disconnect from hub:
        _connector.setActive(false);

        _logger.info("SAMP Hub connection closed.");

        // Perform manual JSamp Hub shutdown as its JVM shudown hook seems not working well ...
        if (stopHubOnShutdown) {
            LoggingService.setLoggerLevel("org.astrogrid.samp", Level.DEBUG);
            LoggingService.setLoggerLevel("org.astrogrid.samp.hub.Hub", Level.DEBUG);

            final Hub hub = getRunningHub();

            if (hub != null) {
                _logger.info("Stopping SAMP Hub ...");

                hub.shutdown();
                _logger.info("SAMP Hub shutdown.");
            }
            
            // Note: JVM ShutdownHook can not use j.u.l.Logger (log lost) because LogManager adds itself a Cleaner hook
            // to free all log handlers => lost logs !
        }
    }

    /**
     * Return the JSamp GUI hub connector providing swing actions
     * @return JSamp GUI hub connector providing swing actions
     */
    private GuiHubConnector getHubConnector() {
        return _connector;
    }

    /* --- STATIC METHODS --------------------------------------------------- */
    /**
     * Return the first running Hub or null
     * @return first running Hub or null
     */
    private static Hub getRunningHub() {
        final Hub[] hubs = Hub.getRunningHubs();

        // TODO: should test its profiles (standard ...) to check if it is a normal Hub ???
        if (hubs.length > 0) {
            // use first one only:
            return hubs[0];
        }
        return null;
    }

    /**
     * Return the hub service mode for the internal Hub (CLIENT_GUI if system tray is supported)
     * @return hub mode
     */
    private static HubServiceMode getInternalHubMode() {
        final HubServiceMode internalMode = SysTray.getInstance().isSupported()
                ? HubServiceMode.CLIENT_GUI
                : HubServiceMode.NO_GUI;
        return internalMode;
    }

    /**
     * Return the JSamp Gui hub connector providing swing actions
     * @return JSamp Gui hub connector providing swing actions
     */
    private static GuiHubConnector getGuiHubConnector() {
        return SampManager.getInstance().getHubConnector();
    }

    /**
     * Indicates whether this connector is currently registered with a
     * running hub.
     *
     * @return true if currently connected to a hub
     */
    public static boolean isConnected() {
        return SampManager.getInstance().getHubConnector().isConnected();
    }

    /**
     * Create a list model for the registered clients of the given message type
     * @param mType samp message type
     * @return list model for the registered clients
     */
    public static SubscribedClientListModel createSubscribedClientListModel(final String mType) {
        return new SubscribedClientListModel(SampManager.getGuiHubConnector(), mType);
    }

    /**
     * Create a list model for the registered clients of given message types
     * @param mTypes samp message types
     * @return list model for the registered clients
     */
    public static SubscribedClientListModel createSubscribedClientListModel(final String[] mTypes) {
        return new SubscribedClientListModel(SampManager.getGuiHubConnector(), mTypes);
    }

    /**
     * Returns an action which toggles hub registration.
     *
     * @return registration toggle action
     */
    public static Action createToggleRegisterAction() {
        final GuiHubConnector connector = getGuiHubConnector();

        final Action[] hubStartActions = new Action[]{
            connector.createHubAction(false, getInternalHubMode())
        };

        return connector.createRegisterOrHubAction(App.getFrame(), hubStartActions);
    }

    /**
     * Returns an action which will display a SAMP hub monitor window.
     *
     * @return monitor window action
     */
    public static Action createShowMonitorAction() {
        return new HubMonitorAction();
    }

    /**
     * Register an app-specific capability
     * @param handler message handler
     */
    public static void registerCapability(final SampMessageHandler handler) {
        final GuiHubConnector connector = getGuiHubConnector();

        connector.addMessageHandler(handler);

        // This step required even if no custom message handlers added.
        connector.declareSubscriptions(connector.computeSubscriptions());

        _logger.info("Registered SAMP capability for mType '{}'.", handler.handledMType());
    }

    /**
     * Link SampManager instance to the "Interop" menu
     * @param menu interop menu container
     */
    public static synchronized void hookMenu(final JMenu menu) {

        if (_menu != null) {
            throw new IllegalStateException("the interoperability menu is already hooked by SampManager : \n" + _menu + "\n" + menu);
        }

        _menu = menu;

        // If some capabilities are registered
        if (!_map.isEmpty()) {
            // Make the "Interop" menu visible
            _menu.setVisible(true);
        }
    }

    /**
     * Link a menu entry to its action
     * @param menu menu entry
     * @param action samp capability action
     */
    public static void addMenu(final JMenu menu, final SampCapabilityAction action) {
        _map.put(action, menu);
    }

    /**
     * Get a menu entry from its action
     * @param action samp capability action
     * @return menu menu entry
     */
    public static JMenu getMenu(final SampCapabilityAction action) {
        return _map.get(action);
    }

    /**
     * Return the client map known by the hub
     * @return client map
     */
    public static Map<?, ?> getClientMap() {
        return getGuiHubConnector().getClientMap();
    }

    /**
     * Return the client corresponding to the given client Id known by the hub
     * @param clientId client id
     * @return client or null
     */
    public static Client getClient(final String clientId) {
        return (Client) getClientMap().get(clientId);
    }

    /**
     * Return the meta data corresponding to the given client Id known by the hub
     * @param clientId client id
     * @return meta data or null
     */
    public static Metadata getMetaData(final String clientId) {
        final Client client = getClient(clientId);
        if (client != null) {
            return client.getMetadata();
        }
        return null;
    }

    /**
     * Return the subscriptions corresponding to the given client Id known by the hub
     * @param clientId client id
     * @return subscriptions or null
     */
    public static Subscriptions getSubscriptions(final String clientId) {
        final Client client = getClient(clientId);
        if (client != null) {
            return client.getSubscriptions();
        }
        return null;
    }

    /**
     * Return the list of id for a given SAMP client name
     * @param name client name
     * @return list of id
     */
    public static List<String> getClientIdsForName(final String name) {
        final List<String> clientIdList = new ArrayList<String>(1);

        for (Iterator<?> it = getClientMap().values().iterator(); it.hasNext();) {
            final Client client = (Client) it.next();
            if (client.getMetadata().getName().matches(name)) {
                clientIdList.add(client.getId());
            }
        }
        return clientIdList;
    }

    /**
     * Send the given message to a client
     * @param mType samp message type
     * @param recipient public-id of client to receive message
     * @param parameters message parameters
     *
     * @throws SampException if any Samp exception occurred
     */
    public static void sendMessageTo(final String mType, final String recipient, final Map<?, ?> parameters) throws SampException {
        final GuiHubConnector connector = getGuiHubConnector();

        final long start = System.nanoTime();

        connector.getConnection().notify(recipient, new Message(mType, parameters));

        if (_logger.isInfoEnabled()) {
            _logger.info("Sent '{}' SAMP message to '{}' client ({} ms)",
                    mType, recipient, 1e-6d * (System.nanoTime() - start));
        }
    }

    /**
     * Send the given message to all clients supporting the given message type
     * @param mType samp message type
     * @param parameters message parameters
     *
     * @throws SampException if any Samp exception occurred
     */
    public static void broadcastMessage(final String mType, final Map<?, ?> parameters) throws SampException {
        final GuiHubConnector connector = getGuiHubConnector();

        final long start = System.nanoTime();

        connector.getConnection().notifyAll(new Message(mType, parameters));

        if (_logger.isInfoEnabled()) {
            _logger.info("Broadcasted SAMP message to '{}' capable clients ({} ms)",
                    mType, 1e-6d * (System.nanoTime() - start));
        }
    }

    /**
     * Samp Hub Connection Change listener
     */
    private final static class SampConnectionChangeListener implements ChangeListener {

        /**
         * Invoked when the hub connection has changed its state i.e.
         * when this connector registers or unregisters with a hub.
         *
         * @param e  a ChangeEvent object
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            final GuiHubConnector connector = (GuiHubConnector) e.getSource();

            _logger.info("SAMP Hub connection status : {}", ((connector.isConnected()) ? "registered" : "unregistered"));
        }
    }

    /**
     * Action subclass for popping up a monitor or the hub window.
     */
    private static class HubMonitorAction extends AbstractAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1L;
        /* members */
        /** client monitor action */
        private final Action clientMonitorAction;

        /**
         * Constructor.
         */
        HubMonitorAction() {
            super("Show Hub Status");
            putValue(SHORT_DESCRIPTION, "Display a window showing client applications registered with the SAMP hub");

            clientMonitorAction = getGuiHubConnector().createShowMonitorAction();
        }

        /**
         * Display Hub window
         * @param ae action event
         */
        @Override
        public void actionPerformed(final ActionEvent ae) {
            final Hub hub = getRunningHub();

            boolean show = false;
            if (hub != null) {
                final JFrame hubWindow = hub.getWindow();

                if (hubWindow != null) {
                    // ensure window is visible (not iconified):
                    if (hubWindow.getState() == Frame.ICONIFIED) {
                        hubWindow.setState(Frame.NORMAL);
                    }

                    // force the frame to be visible and bring it to front
                    hubWindow.setVisible(true);
                    hubWindow.toFront();

                    show = true;
                }
            }
            if (!show) {
                clientMonitorAction.actionPerformed(ae);
            }
        }
    }
}
