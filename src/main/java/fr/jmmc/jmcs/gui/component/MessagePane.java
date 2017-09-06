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
package fr.jmmc.jmcs.gui.component;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods to create message panes (message, error) with/without exceptions
 *
 * @author Laurent BOURGES, Sylvain LAFRASSE, Guillaume MELLA.
 */
public final class MessagePane {

    // Constants
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(MessagePane.class.getName());
    /** Minimum width for message component of dialog frames */
    private static final int MINIMUM_WIDTH = 450;
    /** Maximum width for message component of dialog frames */
    private static final int MAXIMUM_WIDTH = 600;
    /** Margin trick used to get better layout */
    private static final int MARGIN = 35;
    /** Maximum height for message component of dialog frames */
    private static final int MAXIMUM_HEIGHT = 9 * MARGIN;
    /** default title for error messages */
    private final static String TITLE_ERROR = "Error";
    /** default title for warning messages */
    private final static String TITLE_WARNING = "Warning";
    /** default title for information messages */
    private final static String TITLE_INFO = "Information";
    /** create directory dialog options */
    private final static Object[] DIRECTORY_CREATE_OPTIONS = {"Cancel", "Create"};
    /** overwrite file dialog options */
    private final static Object[] FILE_OVERWRITE_OPTIONS = {"Cancel", "Replace"};
    /** save changes dialog options */
    private final static Object[] SAVE_CHANGES_OPTIONS = {"Save", "Cancel", "Don't Save"};
    /** save changes dialog options */
    private final static Object[] KILL_HUB_OPTIONS = {"Cancel", "Quit"};

    /** Save changes before closing results */
    public enum ConfirmSaveChanges {

        /** Save */
        Save,
        /** Cancel */
        Cancel,
        /** Ignore */
        Ignore
    }

    /**
     * Forbidden constructor
     */
    private MessagePane() {
        super();
    }

    // --- ERROR MESSAGES --------------------------------------------------------
    /**
     * Show an error with the given message using EDT if needed
     * @param message message to display
     */
    public static void showErrorMessage(final String message) {
        showErrorMessage(message, TITLE_ERROR, null);
    }

    /**
     * Show an error with the given message plus the exception message (if any) using EDT if needed
     * and log the exception
     * @param message message to display
     * @param th exception to use
     */
    public static void showErrorMessage(final String message, final Throwable th) {
        showErrorMessage(message, TITLE_ERROR, th);
    }

    /**
     * Show an error with the given message and window title using EDT if needed
     * @param message message to display
     * @param title window title to use
     */
    public static void showErrorMessage(final String message, final String title) {
        showErrorMessage(message, title, null);
    }

    /**
     * Show an error with the given message plus the exception message (if any) using EDT if needed
     * and window title and log the exception
     * @param message message to display
     * @param title window title to use
     * @param th exception to use
     */
    public static void showErrorMessage(final String message, final String title, final Throwable th) {
        if (th != null) {
            _logger.error("An exception occured: {}", message, th);
        } else {
            _logger.error("A problem occured: {}", message);
        }

        final String msg;
        if (th != null && th.getMessage() != null) {

            // try to get cause if possible
            String cause = "";

            Throwable thCause = th.getCause();

            // process all nested exceptions:
            while (thCause != null) {
                if (thCause.getMessage() != null) {
                    cause += "\n\nCause: " + thCause.getMessage();
                }

                thCause = thCause.getCause();
            }

            // Add exception name to improve given information e.g. ArrayOutOfBound just returned a number as message...
            msg = message + "\n\nExplanation (" + th.getClass().getName() + "): " + th.getMessage() + cause + "\n\n";
        } else {
            msg = message;
        }

        // display the message within EDT :
        showMessageDialog(msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show the given message.
     * The frame size is limited so long messages appear in a scroll pane.
     * @param message message to display
     * @param title window title to use
     * @param messageType the type of message to be displayed:
     *          <code>ERROR_MESSAGE</code>,
     *			<code>INFORMATION_MESSAGE</code>,
     *			<code>WARNING_MESSAGE</code>,
     *          <code>QUESTION_MESSAGE</code>,
     *			or <code>PLAIN_MESSAGE</code>
     */
    private static void showMessageDialog(final String message, final String title, final int messageType) {
        if (Bootstrapper.isHeadless()) {
            _logger.info("[Headless] Message: {}", message);
        } else {
            // display the message within EDT :
            SwingUtils.invokeAndWaitEDT(new Runnable() {
                @Override
                public void run() {
                    // ensure window is visible (not iconified):
                    App.showFrameToFront();

                    JOptionPane.showMessageDialog(getApplicationFrame(), getMessageComponent(message), title, messageType);
                }
            });
        }
    }

    // --- WARNING MESSAGES ---------------------------------------------------------
    /**
     * Show an information with the given message
     * @param message message to display
     */
    public static void showWarning(final String message) {
        showWarning(message, TITLE_WARNING);
    }

    /**
     * Show an information with the given message and window title
     * @param message message to display
     * @param title window title to use
     */
    public static void showWarning(final String message, final String title) {
        showMessageDialog(message, title, JOptionPane.WARNING_MESSAGE);
    }

    // --- INFO MESSAGES ---------------------------------------------------------
    /**
     * Show an information with the given message
     * @param message message to display
     */
    public static void showMessage(final String message) {
        showMessage(message, TITLE_INFO);
    }

    /**
     * Show an information with the given message and window title
     * @param message message to display
     * @param title window title to use
     */
    public static void showMessage(final String message, final String title) {
        showMessageDialog(message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // --- CONFIRM MESSAGES ------------------------------------------------------
    /**
     * Show a confirmation dialog to ask if the user wants to overwrite the file with the same name
     * @param fileName file name
     * @return true if the user wants the file replaced, false otherwise.
     */
    public static boolean showConfirmFileOverwrite(final String fileName) {
        final String message = "\"" + fileName + "\" already exists. Do you want to replace it ?\n\n"
                + "A file or folder with the same name already exists in the current folder.\n"
                + "Replacing it will overwrite its current contents.";

        // ensure window is visible (not iconified):
        App.showFrameToFront();

        final int result = JOptionPane.showOptionDialog(getApplicationFrame(), getMessageComponent(message),
                null, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, FILE_OVERWRITE_OPTIONS, FILE_OVERWRITE_OPTIONS[0]);

        // If the user clicked the "Replace" button
        if (result == 1) {
            return true;
        }

        return false;
    }

    /**
     * Show a confirmation dialog to ask if the user wants to create the directory
     * @param directoryPath directory path
     * @return true if the user wants the directory created, false otherwise.
     */
    public static boolean showConfirmDirectoryCreation(final String directoryPath) {
        final String message = "\"" + directoryPath + "\" does not exists. Do you want to create it ?\n\n";

        // ensure window is visible (not iconified):
        App.showFrameToFront();

        final int result = JOptionPane.showOptionDialog(getApplicationFrame(), getMessageComponent(message),
                null, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, DIRECTORY_CREATE_OPTIONS, DIRECTORY_CREATE_OPTIONS[0]);

        // If the user clicked the "Create" button
        if (result == 1) {
            return true;
        }

        return false;
    }

    /**
     * Show a confirmation dialog to ask the given question
     * @param message message to ask
     * @return true if the user answers yes
     */
    public static boolean showConfirmMessage(final String message) {
        return showConfirmMessage(getApplicationFrame(), message);
    }

    /**
     * Show a confirmation dialog to ask if the user wants to save changes before closing the application
     * @return Save if the user wants to save changes, Cancel or Ignore otherwise.
     */
    public static ConfirmSaveChanges showConfirmSaveChangesBeforeClosing() {
        return showConfirmSaveChanges("closing");
    }

    /**
     * Show a confirmation dialog to ask if the user wants to save changes before doing any operation.
     *
     * @param beforeMessage part of the message inserted after 'before ' ?
     * @return Save if the user wants to save changes, Cancel or Ignore otherwise.
     */
    public static ConfirmSaveChanges showConfirmSaveChanges(final String beforeMessage) {
        final String message = "Do you want to save changes to this document before "
                + beforeMessage
                + "?\nIf you don't save, your changes will be definitively lost.\n\n";

        // ensure window is visible (not iconified):
        App.showFrameToFront();

        // If the data are NOT saved, handle it before loosing any results !!!
        // Ask the user if he wants to save modifications
        final int result = JOptionPane.showOptionDialog(getApplicationFrame(),
                getMessageComponent(message),
                null, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, SAVE_CHANGES_OPTIONS, SAVE_CHANGES_OPTIONS[0]);

        // Handle user choice
        switch (result) {
            // If the user clicked the "Save" button
            case 0: // options[0] = "Save" button
                // Save the current data if no cancel occured
                return ConfirmSaveChanges.Save;

            // If the user clicked the "Don't Save" button
            case 2: // options[2] = "Don't Save" button
                // Exit
                return ConfirmSaveChanges.Ignore;

            // If the user clicked the "Cancel" button or pressed 'esc' key
            case 1: // options[1] = "Cancel" button
            case JOptionPane.CLOSED_OPTION: // 'esc' key
            default: // Any other case
                // Cancel the exit
                return ConfirmSaveChanges.Cancel;
        }
    }

    /**
     * Show a confirmation dialog to ask if the user wants to kill SAMP hub while quitting.
     *
     * @return true if the user wants the quit nevertheless, false otherwise.
     */
    public static boolean showConfirmKillHub() {
        final String applicationName = ApplicationDescription.getInstance().getProgramName();
        final String message = "Quitting '"
                + applicationName
                + "' will also terminate the shared SAMP hub,\npotentially preventing other applications interoperability until\nanother hub is started elsewhere.\n\nProceed with quitting nevertheless ?";

        // ensure window is visible (not iconified):
        App.showFrameToFront();

        // Ask the user if he wants to kill hub
        final int result = JOptionPane.showOptionDialog(getApplicationFrame(),
                getMessageComponent(message),
                null, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, KILL_HUB_OPTIONS, KILL_HUB_OPTIONS[0]);

        // Handle user choice
        switch (result) {
            // If the user clicked the "Quit" button
            case 1: // options[0] = "Quit" button
                // Proceed whith quit
                return true;

            // If the user clicked the "Cancel" button or pressed 'esc' key
            case 0: // options[0] = "Cancel" button
            case JOptionPane.CLOSED_OPTION: // 'esc' key
            default: // Any other case
                // Cancel quitting
                return false;
        }
    }

    /**
     * Show a confirmation dialog to ask the given question
     * @param parentComponent Parent component or null
     * @param message message to ask
     * @return true if the user answers yes
     */
    public static boolean showConfirmMessage(final Component parentComponent, final String message) {

        // ensure window is visible (not iconified):
        App.showFrameToFront();

        final int answer = JOptionPane.showConfirmDialog(getParent(parentComponent), getMessageComponent(message));

        return answer == JOptionPane.YES_OPTION;
    }

    /**
     * Show an input message (with title) returning a string (using EDT if needed)
     *
     * @param message message to display
     * @param title window title to use
     *
     * @return the string given by the user, or null otherwise.
     */
    public static String showInputMessage(final String message, final String title) {

        final FutureTask<String> future = new FutureTask<String>(
                new Callable<String>() {
            @Override
            public String call() {
                // ensure window is visible (not iconified):
                App.showFrameToFront();
                return JOptionPane.showInputDialog(getApplicationFrame(), getMessageComponent(message), title, JOptionPane.INFORMATION_MESSAGE);
            }
        });

        SwingUtils.invokeEDT(future);

        // Wait for call result:
        String receivedValue = null;
        try {
            receivedValue = future.get();
        } catch (InterruptedException ie) {
            _logger.error("Could not read user input", ie);
        } catch (ExecutionException ee) {
            _logger.error("Could not read user input", ee);
        }

        return receivedValue;
    }

    /**
     * Return a parent component / owner for a dialog window
     * @param com component argument
     * @return given component argument or the application frame if the given component is null
     */
    public static Component getParent(final Component com) {
        Component owner = com;
        if (owner == null) {
            owner = getApplicationFrame();
        }
        _logger.debug("dialog owner = {}", owner);
        return owner;
    }

    /**
     * Return the shared application frame
     * @return application frame
     */
    private static JFrame getApplicationFrame() {
        return App.getFrame();
    }

    /**
     * Return the smallest component that would display given message in a dialog frame.
     * If the message is too big, one limited size scroll pane is used for display.
     * @param message string that will be wrapped if too long
     * @return the component which can be given to JOptionPane methods.
     */
    private static Component getMessageComponent(String message) {
        final JTextArea textArea = new JTextArea(message);

        // Sizing : add MARGIN to the textArea to avoid scrollPane borders when the size is reaching the limit
        final int textAreaWidth = textArea.getMinimumSize().width + MARGIN;
        final int textAreaHeight = textArea.getMinimumSize().height;
        final int finalHeight = Math.min(textAreaHeight, MAXIMUM_HEIGHT);
        final int finalWidth = Math.max(MINIMUM_WIDTH, Math.min(textAreaWidth, MAXIMUM_WIDTH));
        final Dimension dims = new Dimension(finalWidth, finalHeight);
        final JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setMaximumSize(dims);
        scrollPane.setPreferredSize(dims);

        // Show scrollpane only when needed
        final boolean textAreaBackgroundShouldBeOpaque = (textAreaWidth > finalWidth) || (textAreaHeight > finalHeight);
        textArea.setOpaque(textAreaBackgroundShouldBeOpaque);
        scrollPane.setOpaque(textAreaBackgroundShouldBeOpaque);
        scrollPane.getViewport().setOpaque(textAreaBackgroundShouldBeOpaque);
        if (textAreaBackgroundShouldBeOpaque) {
            // Accomodate more vertical space for visible scroll pane
            dims.setSize(finalWidth, MAXIMUM_HEIGHT);
            scrollPane.setMaximumSize(dims);
            scrollPane.setPreferredSize(dims);
        } else {
            scrollPane.setBorder(null);
        }

        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);

        return scrollPane;
    }

    public static void main(String[] args) {

        final String applicationName = "SAOImage DS9";
        final String title = "Please give '" + applicationName + "' command-line path ?";
        String message = "AppLauncher tries to start the '" + applicationName + "' native application.\n"
                + "As it is a user-installed application, AppLauncher does not know where to find it.\n\n"
                + "Please enter a straight command-line to launch it (arguments not supported) :";
        final String showInputMessage = showInputMessage(message, title);
        System.out.println("showInputMessage = '" + showInputMessage + "'.");

        message = "";
        for (int i = 1; i < 10; i++) {
            message += "Blah blah... " + i;
            MessagePane.showMessage(message, "Title");
            message += "\n";
        }

        message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum congue tincidunt justo. Etiam massa arcu, vestibulum pulvinar accumsan ut, ullamcorper sed sapien. Quisque ullamcorper felis eget turpis mattis vestibulum. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras et turpis justo, sed lacinia libero. Sed in tellus eget libero posuere euismod. In nulla mi, semper a condimentum quis, tincidunt eget magna. Etiam tristique venenatis ante eu interdum. Phasellus ultrices rhoncus urna, ac pretium ante ultricies condimentum. Vestibulum et turpis ac felis pulvinar rhoncus nec a nulla. Proin eu ante eu leo fringilla ornare in a massa. Morbi varius porttitor nibh ac elementum. Cras sed neque massa, sed vulputate magna. Ut viverra velit magna, sagittis tempor nibh.";
        MessagePane.showMessage(message, "Lorem Ipsum");
        System.exit(0);
    }
}
