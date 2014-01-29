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

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.StatusBar;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.astrogrid.samp.Client;
import org.astrogrid.samp.client.SampException;
import org.astrogrid.samp.gui.SubscribedClientListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic action dedicated to SAMP capability handling.
 *
 * Your action should extends SampCapabilityAction, implementing composeMessage() to forge the message to be sent by SAMP.
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES, Guillaume MELLA.
 */
public abstract class SampCapabilityAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(SampCapabilityAction.class.getName());
    /** Flag to enable/disable broadcast action */
    public final static boolean BROADCAST_ENABLED = false;
    /** Label for broadcast to all */
    private final static String BROADCAST_MENU_LABEL = "All Applications";
    // Members
    /** SAMP capability to send */
    private final SampCapability _capability;
    /** mType of SAMP capability to send */
    private final String _mType;
    /** Capable clients for the registered capability */
    private SubscribedClientListModel _capableClients = null;
    /** Store whether the action should be enabled once SAMP clients are registered for the given capability */
    private boolean _couldBeEnabled = true;

    /**
     * Constructor.
     *
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     * @param capability the SAMP mType to be sent.
     */
    public SampCapabilityAction(final String classPath, final String fieldName, final SampCapability capability) {
        // use defered initialization:
        super(classPath, fieldName, true);

        _capability = capability;
        _mType = _capability.mType();

        // always prepare menu entries anyway
        updateMenuAndActionAfterSubscribedClientChange();
    }

    /**
     * Perform deferred initialization i.e. executed after the application startup.
     * This method must be override in sub classes
     */
    @Override
    protected void performDeferedInitialization() {
        // Get a dynamic list of SAMP clients able to respond to the specified capability.
        _capableClients = SampManager.createSubscribedClientListModel(_mType);

        // Monitor any modification to the capable clients list
        _capableClients.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(final ListDataEvent e) {
                updateMenuAndActionAfterSubscribedClientChange();
            }

            @Override
            public void intervalAdded(final ListDataEvent e) {
                updateMenuAndActionAfterSubscribedClientChange();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                updateMenuAndActionAfterSubscribedClientChange();
            }
        });

        // but do one first test if one registered app already handle such capability
        updateMenuAndActionAfterSubscribedClientChange();
    }

    /**
     * (dis)Allow the menu entry to be enabled once SAMP clients are registered.
     *
     * @param flag allow enabling if true, disabling otherwise.
     */
    public void couldBeEnabled(final boolean flag) {
        _logger.trace("couldBeEnabled({})", flag);

        // Update menus only if needed
        if (flag != _couldBeEnabled) {
            _couldBeEnabled = flag;
            updateMenuAndActionAfterSubscribedClientChange();
        }
    }

    /**
     * Set action text.
     * @param text
     */
    public void setText(String text) {
        _logger.trace("setText('{}')", text);
        putValue(Action.NAME, text);
        updateMenuAndActionAfterSubscribedClientChange();
    }

    /**
     * Updates linked JMenu entry to offer all capable clients, plus broadcast.
     */
    private void updateMenuAndActionAfterSubscribedClientChange() {

        // Disabled until a client for the given capabily registers to the hub
        setEnabled(false);

        // Retrieve the JMenu entry for the current capablity
        // or build a new one if it does not already exists
        JMenu menu = SampManager.getMenu(this);
        if (menu == null) {
            _logger.debug("Could not get back menu entry for action '{}'.", this);

            menu = new JMenu(this);
            SampManager.addMenu(menu, this);
        }

        // First remove all sub-menus
        menu.removeAll();

        // If no client is able to handle specified capability
        final int nbOfClients = (_capableClients != null) ? _capableClients.getSize() : 0;
        if (nbOfClients <= 0) {
            _logger.debug("No SAMP client available for capability '{}'.", _mType);

            // Leave the menu disable with no sub-menus
            return;
        }

        // Otherwise, enable menu (if needed)
        setEnabled(_couldBeEnabled);

        if (BROADCAST_ENABLED) {
            // Add generic "All" entry to broadcast message to all capable clients at once
            final JMenuItem broadcastMenuItem = new JMenuItem(this);
            broadcastMenuItem.setText(BROADCAST_MENU_LABEL);
            menu.add(broadcastMenuItem);

            _logger.trace("Added '{}' broadcast menu entry for capability '{}'.", BROADCAST_MENU_LABEL, _mType);

            menu.addSeparator();
        }

        // Add each individual client
        for (int i = 0; i < nbOfClients; i++) {
            final Client client = (Client) _capableClients.getElementAt(i);
            final String clientName = client.toString();
            final String clientId = client.getId();

            final JMenuItem individualMenuItem = new JMenuItem(this);
            individualMenuItem.setText(clientName);
            individualMenuItem.setActionCommand(clientId);

            menu.add(individualMenuItem);

            if (_logger.isTraceEnabled()) {
                _logger.trace("Added '{}' ({}) menu entry for capability '{}'.", clientName, clientId, _mType);
            }
        }
    }

    /** 
     * Should return the message you want to send
     * @return Samp message parameters as a map
     */
    public abstract Map<?, ?> composeMessage();

    /**
     * This method automatically sends the message returned by composeMessage()
     * to user selected client(s). Children classes should not overwrite this
     * method or must call super implementation to keep SAMP message management.
     *
     * @param e actionEvent coming from SWING objects. It contains in its
     * command the name of the destination.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        composeAndSendMessage(e.getActionCommand());
    }

    /**
     * Create a new Samp message returned by composeMessage() and send it to
     * user selected client(s).
     * @param command action command. It contains in its
     * command the name of the destination.
     * @return true if success; false otherwise
     */
    protected final boolean composeAndSendMessage(final String command) {
        boolean ok = true;

        // Delegate message forging to app-specific code
        final Map<?, ?> parameters = composeMessage();

        if (parameters != null) {
            StatusBar.show("Sending data through SAMP ...");

            try {
                ok = false;
                // Get the user clicked menu label
                // If the 'All' menu was used
                if (BROADCAST_MENU_LABEL.equals(command)) {
                    // Broadcast the forged message to all capable clients
                    SampManager.broadcastMessage(_mType, parameters);

                } else {
                    // Otherwise only send forged message to the selected client
                    SampManager.sendMessageTo(_mType, command, parameters);
                }

                ok = true;

            } catch (SampException se) {
                _logger.error("Samp message send failure", se);
            } finally {
                StatusBar.show(
                        (ok) ? "Sending data through SAMP ... done."
                        : "Sending data through SAMP ... failed.");
            }
        }
        return ok;
    }
}
