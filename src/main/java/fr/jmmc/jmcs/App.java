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
package fr.jmmc.jmcs;

import fr.jmmc.jmcs.data.ArgumentDefinition;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.internal.InternalActionFactory;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.util.CommandLineUtils;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton that formalize application's life-cycle.
 *
 * In order to use functionalities provided by jMCS,
 * extend your application from this class and use:
 * @see Bootstrapper
 *
 * @author Brice COLUCCI, Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public abstract class App {

    /** Class Logger */
    private static final Logger _logger = LoggerFactory.getLogger(App.class.getName());
    /** Singleton reference */
    private static App _instance;
    /** Main frame of the application (singleton) */
    private static JFrame _applicationFrame = null;

    /**
     * Execution mode (GUI or TTY mode)
     */
    public enum ExecMode {

        /** GUI */
        GUI,
        /** TTY Mode (command-line) */
        TTY;
    }

    // Members
    /** Command-line arguments */
    protected final String[] _args;
    /** Command-line argument meta data (insertion order) */
    private final Map<String, ArgumentDefinition> _customArgumentsDefinition = new LinkedHashMap<String, ArgumentDefinition>();
    /** Store the custom command line argument values (keyed by name) */
    private Map<String, String> _customArgumentValues = null;

    /**
     * Static jMCS environment startup.
     */
    static {
        Bootstrapper.bootstrap();
    }

    /**
     * Creates a new App object.
     * @param args command-line arguments.
     * @warning mandatory call to super(...) in your App derived class.
     */
    protected App(String[] args) {
        _args = args;
        _logger.debug("App object instantiated and logger created.");
    }

    final void ___internalSingletonInitialization() {
        // Set shared instance
        _instance = this;
    }

    /**
     * @return App shared instance if available, null otherwise.
     */
    public static App getInstance() {
        return _instance;
    }

    final void ___internalStart() {
        defineCustomCommandLineArgumentsAndHelp();

        // Interpret arguments
        _customArgumentValues = CommandLineUtils.interpretArguments(_args, _customArgumentsDefinition);

        // Force headless mode if shell action:
        if (isShellAction()) {
            Bootstrapper.forceHeadless();
        }
    }

    private final boolean isShellAction() {
        if (_customArgumentValues != null) {
            for (String argumentName : _customArgumentValues.keySet()) {
                final ArgumentDefinition def = _customArgumentsDefinition.get(argumentName);
                if (def != null && def.getMode() == ExecMode.TTY) {
                    return true;
                }
            }
        }
        return false;
    }

    final void ___internalProcessCommandLine() {
        if (_customArgumentValues != null) {
            _logger.debug("___internalProcessCommandLine: {}", _customArgumentValues);

            // If shell action:
            if (isShellAction()) {

                // Using thread main: must block until asynchronous task finishes !
                try {
                    processShellCommandLine();
                } catch (IllegalArgumentException iae) {
                    _logger.error("processShellCommandLine failed: {}", iae.getMessage());
                    showArgumentsHelp();
                } catch (Throwable th) {
                    // unexpected errors:
                    _logger.error("processShellCommandLine failed:", th);
                } finally {
                    // Exit the application
                    Bootstrapper.stopApp(0);
                }
            }
        }
    }

    /**
     * Optional hook to override in your App, to perform command-line actions:
     * Validate the arguments given by the command line (TTY mode)
     * and performs the corresponding action ...
     *
     * Note: executed by the thread [main]: must block until asynchronous task finishes !
     * @throws IllegalArgumentException if any argument is missing or is invalid
     */
    protected void processShellCommandLine() throws IllegalArgumentException {
        //noop
    }

    /**
     * @return command line arguments hash map (argument value keyed by argument name).
     * @warning may be empty if no argument provided by user at launch.
     */
    protected final Map<String, String> getCommandLineArguments() {
        return _customArgumentValues;
    }

    /**
     * Optional hook to override in your App, to add support for custom command-line argument(s) and help using:
     * @see #addCustomCommandLineArgument(java.lang.String, boolean)
     * @see #addCustomArgumentsHelp(java.lang.String)
     */
    protected void defineCustomCommandLineArgumentsAndHelp() {
        // noop
    }

    /**
     * Show command-line arguments help.
     */
    protected final void showArgumentsHelp() {
        CommandLineUtils.showArgumentsHelp(_customArgumentsDefinition);
    }

    /**
     * Add a custom command line argument.
     * @param name argument's name.
     * @param hasArgument true if an argument value is required, false otherwise.
     * @param help argument's description displayed in the command-line help.
     */
    protected final void addCustomCommandLineArgument(final String name,
                                                      final boolean hasArgument, final String help) {
        addCustomCommandLineArgument(name, hasArgument, help, ExecMode.GUI);
    }

    /**
     * Add a custom command line argument.
     * @param name argument's name.
     * @param hasArgument true if an argument value is required, false otherwise.
     * @param help argument's description displayed in the command-line help.
     * @param mode execution mode (GUI or TTY mode).
     */
    protected final void addCustomCommandLineArgument(final String name,
                                                      final boolean hasArgument, final String help, final ExecMode mode) {
        if ((name == null) || (name.isEmpty())) {
            return;
        }
        _customArgumentsDefinition.put(name, new ArgumentDefinition(name, hasArgument, mode, help));
    }

    /**
     * Mandatory hook to override in your App, to initialize services before the GUI.
     */
    protected abstract void initServices();

    /**
     * Mandatory hook to override in your App, to properly initialize user interface elements in EDT.
     * @warning : The actions which are present in menu bar must be instantiated in this method.
     */
    protected abstract void setupGui();

    /**
     * Optional hook to override in your App, to declare SAMP capabilities (if any).
     */
    protected void declareInteroperability() {
        _logger.debug("Empty App.declareInteroperability() handler called.");
    }

    /**
     * Open the file given by the user as a command-line argument (-open file)
     */
    public final void openCommandLineFile() {

        if ((_customArgumentValues == null) || (_customArgumentValues.isEmpty())) {
            return;
        }

        // If any file argument exists, open that file using the registered open action
        final String fileArgument = _customArgumentValues.get(CommandLineUtils.CLI_OPEN_KEY);
        if (fileArgument == null) {
            return;
        }

        SwingUtils.invokeLaterEDT(new Runnable() {
            /**
             * Open the file using EDT :
             */
            @Override
            public void run() {
                final ActionRegistrar actionRegistrar = ActionRegistrar.getInstance();
                final AbstractAction openAction = actionRegistrar.getOpenAction();
                if (openAction != null) {
                    openAction.actionPerformed(new ActionEvent(actionRegistrar, 0, fileArgument));
                }
            }
        });
    }

    /**
     * Mandatory hook to override in your App, to execute application body.
     */
    protected abstract void execute();

    /**
     * Optional hook to override in your App, to log or return the application state when submitting a feedback report
     *
     * @return application state as String
     */
    public String getStateForFeedbackReport() {
        // nothing by default
        return null;
    }

    /**
     * Define the application frame (singleton).
     *
     * Note: applications can also get directly the main GUI frame using getFrame().
     * @see #getFrame()
     *
     * @param frame application frame.
     */
    public static void setFrame(final JFrame frame) {
        // avoid reentrance:
        if (_applicationFrame != frame) {

            if (_applicationFrame != null) {
                ___internalDisposeFrame();
            }

            _applicationFrame = frame;
            _applicationFrame.setLocationByPlatform(true);

            // previous adapter manages the windowClosing(event) :
            _applicationFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            // Properly quit the application when main window close button is clicked
            _applicationFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    // Callback on exit
                    InternalActionFactory.quitAction().actionPerformed(null);
                }
            });
        }
    }

    /**
     * @return the application frame (singleton) if defined; null otherwise
     */
    public static JFrame getExistingFrame() {
        return _applicationFrame;
    }

    /**
     * @return the application frame (singleton).
     */
    public static JFrame getFrame() {
        if (_applicationFrame == null) {
            /* try getting application name */
            String appName = "Application";
            try {
                appName = ApplicationDescription.getInstance().getProgramName();
            } catch (IllegalStateException ise) {
                _logger.debug("Unable to get application description: ", ise);
            }
            /* always create a frame with a title and default icon (displayed in the OS toolbar) */
            final JFrame frame = new JFrame(appName);

            final Image jmmcFavImage = ResourceImage.JMMC_FAVICON.icon().getImage();
            frame.setIconImage(jmmcFavImage);

            /* define the minimal size to see the frame empty and not an ugly cropped box */
            final Dimension dim = new Dimension(300, 100);
            frame.setMinimumSize(dim);
            frame.setPreferredSize(dim);

            // Set application frame ideal size
            frame.pack();

            setFrame(frame);

            /* center the empty frame on screen */
            WindowUtils.centerOnMainScreen(frame);
        }
        return _applicationFrame;
    }

    /**
     * @return the application frame panel.
     */
    public static Container getFramePanel() {
        return getFrame().getContentPane();
    }

    /**
     * Show the application frame and bring it to front.
     */
    public static void showFrameToFront() {
        // may create a new JFrame when displaying messages during application startup
        final JFrame frame = getFrame();

        if (frame != null) {
            // Ensure window is visible (not iconified)
            if (frame.getState() == Frame.ICONIFIED) {
                frame.setState(Frame.NORMAL);
            }

            // Force the frame to be visible and bring it to front
            frame.setVisible(true);
            frame.toFront();
        }
    }

    /**
     * Optional hook to override in your App, to return whether the application can be terminated or not.
     *
     * This method is automatically triggered when the application "Quit" menu is used.
     * Thus, you have a chance to do things like saves before the application dies.
     *
     * The default implementation lets the application silently quit without further ado.
     *
     * @warning This method should be overridden to handle quit as you intend to. In its default
     * behavior, all changes that occurred during application life will be lost.
     *
     * @return should return true if the application can exit, or false to cancel exit.
     */
    public boolean canBeTerminatedNow() {
        _logger.info("Default App.canBeTerminatedNow() handler called.");
        return true;
    }

    /**
     * Optional hook to override in your App, to handle SAMP hub destiny before closing application.
     *
     * This method is automatically triggered when the application "Quit" menu is used.
     * Thus, you have a chance to bypass SAMP warning message if needed.
     *
     * The default implementation asks the user if he really wants to kill the hub.
     *
     * @warning This method should be overridden to handle SAMP hub death as you intend to.
     * In its default behavior, the SAMP warning message will be shown to get user's advice.
     *
     * @return should return true if the SAMP hub should be silently killed, false otherwise
     * to ask for user permission.
     */
    public boolean shouldSilentlyKillSampHubOnQuit() {
        _logger.info("Default App.shouldSilentlyKillSampHubOnQuit() handler called.");
        return false;
    }

    /**
     * Mandatory hook to override in your App, to handle operations before exit time.
     */
    protected abstract void cleanup();

    static void ___internalSingletonCleanup() {
        ___internalDisposeFrame();
        _instance = null;
    }

    private static void ___internalDisposeFrame() {
        if (_applicationFrame != null) {
            // Hide and dispose the former application frame:
            final JFrame frame = _applicationFrame;
            // Free pointer now in case an exception occurs.
            _applicationFrame = null;
            frame.setVisible(false);
            frame.dispose();
        }
    }

    /**
     * Describe current application state in its life-cycle.
     */
    public enum ApplicationState {

        APP_BROKEN(-1),
        JAVA_LIMB(0),
        ENV_BOOTSTRAP(1),
        ENV_INIT(2),
        APP_INIT(3),
        GUI_SETUP(4),
        APP_READY(5),
        APP_STOP(6),
        APP_CLEANUP(7),
        APP_CLEANUP_FAIL(8),
        ENV_CLEANUP(9),
        APP_DEAD(10);
        // Members
        /** the numerical order of the internal progress */
        private final int _step;

        /**
         * Constructor
         * @param step the numerical order of the internal progress
         */
        ApplicationState(final int step) {
            _step = step;
        }

        /**
         * @return the internal numerical progression.
         */
        public int step() {
            return _step;
        }

        /**
         * Return true if this state is after the given state
         * @param state state to compare with
         * @return true if this state is after the given state
         */
        public boolean after(final ApplicationState state) {
            return _step > state.step();
        }

        /**
         * Return true if this state is before the given state
         * @param state state to compare with
         * @return true if this state is before the given state
         */
        public boolean before(final ApplicationState state) {
            return _step < state.step();
        }

        /**
         * For unit testing purpose only.
         * @param args CLI options and parameters
         */
        public static void main(String[] args) {
            for (ApplicationState s : ApplicationState.values()) {
                System.out.println("State '" + s.step() + "' = [" + s.name() + "'].");
            }
        }
    }
}
/*___oOo___*/
