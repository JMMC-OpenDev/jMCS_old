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
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.util.FileUtils;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods to create file and directory choosers
 * 
 * @author Laurent BOURGES, Sylvain LAFRASSE, Guillaume MELLA.
 */
public final class FileChooser {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(FileChooser.class.getName());
    /** Apple specific property to force AWT FileDialog work on directories only */
    public final static String MAC_FILE_DIALOG_DIRECTORY = "apple.awt.fileDialogForDirectories";
    /** Use native file chooser i.e. AWT FileDialog (Mac OS X) */
    private final static boolean USE_DIALOG_FOR_FILE_CHOOSER = SystemUtils.IS_OS_MAC_OSX;
    /** File Dialog key to remember file dialog dimensions */
    private final static String FILE_DIALOG_DIMENSION_KEY = "___JMCS_INTERNAL_FILE_DIALOG_DIMENSION";

    /**
     * Show the directory chooser using following properties:
     * @param title dialog title
     * @param givenDirectory optional current directory as file (last one used for given mime type if null)
     * @param mimeType optional file mime type used to get file extension(s), file filter and last directory used
     * @return File instance or null if dialog was discarded
     */
    public static File showDirectoryChooser(final String title, final File givenDirectory, final MimeType mimeType) {

        final File preselectedDirectory = retrieveLastDirectoryForMimeType(givenDirectory, mimeType);

        File selectedDirectory = null;

        // If running under Mac OS X
        if (SystemUtils.IS_OS_MAC_OSX) {
            final FileDialog fileDialog = new FileDialog(App.getFrame(), title);
            if (preselectedDirectory != null) {
                fileDialog.setDirectory(preselectedDirectory.getParent());
                fileDialog.setFile(preselectedDirectory.getName());
            }

            hookFileDialog(fileDialog);

            // force the file dialog to use directories only:
            System.setProperty(MAC_FILE_DIALOG_DIRECTORY, "true");

            try {
                // waits for dialog inputs:
                fileDialog.setVisible(true);
            } finally {
                // restore system property:
                System.setProperty(MAC_FILE_DIALOG_DIRECTORY, "false");
            }

            // note: this avoid to choose the root folder '/':
            if (fileDialog.getFile() != null && fileDialog.getDirectory() != null) {
                selectedDirectory = new File(fileDialog.getDirectory(), fileDialog.getFile());
            }
        } else {
            final JFileChooser fileChooser = new JmcsFileChooser();
            if (preselectedDirectory != null) {
                fileChooser.setCurrentDirectory(preselectedDirectory);
            }

            // select one directory:
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle(title);

            final int returnVal = fileChooser.showSaveDialog(App.getFrame());

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selectedDirectory = fileChooser.getSelectedFile();
            }
        }
        if (selectedDirectory != null) {
            if (!selectedDirectory.isDirectory()) {
                _logger.warn("Expected directory: {}", selectedDirectory);
                selectedDirectory = null;
            } else {
                if (!selectedDirectory.exists()) {
                    if (MessagePane.showConfirmDirectoryCreation(selectedDirectory.getAbsolutePath())) {
                        selectedDirectory.mkdirs();
                    } else {
                        StatusBar.show("directory creation cancelled.");
                        selectedDirectory = null;
                    }
                }
            }
        }
        if (selectedDirectory != null) {
            updateDirectoryForMimeType(mimeType, selectedDirectory.getPath());
        }
        return selectedDirectory;
    }

    /**
     * Show the Open File Dialog using following properties:
     * @param title dialog title
     * @param givenDirectory optional current directory as file (last one used for given mime type if null)
     * @param mimeType optional file mime type used to get file extension(s), file filter and last directory used
     * @return File instance or null if dialog was discarded
     */
    public static File showOpenFileChooser(final String title, final File givenDirectory, final MimeType mimeType) {
        final File[] files = showOpenFileChooser(title, givenDirectory, mimeType, null, false);

        return (files != null) ? files[0] : null;
    }

    /**
     * Show the Open File Dialog using following properties:
     * @param title dialog title
     * @param givenDirectory optional current directory as file (last one used for given mime type if null)
     * @param mimeType optional file mime type used to get file extension(s), file filter and last directory used
     * @param defaultFileName optional default file name
     * @return File instance or null if dialog was discarded
     */
    public static File showOpenFileChooser(final String title, final File givenDirectory, final MimeType mimeType, final String defaultFileName) {
        final File[] files = showOpenFileChooser(title, givenDirectory, mimeType, defaultFileName, false);

        return (files != null) ? files[0] : null;
    }

    /**
     * Show the Open File Dialog accepting multiple file selection using following properties:
     * @param title dialog title
     * @param givenDirectory optional current directory as file (last one used for given mime type if null)
     * @param mimeType optional file mime type used to get file extension(s), file filter and last directory used
     * @return File instance or null if dialog was discarded
     */
    public static File[] showOpenFilesChooser(final String title, final File givenDirectory, final MimeType mimeType) {
        return showOpenFileChooser(title, givenDirectory, mimeType, null, true);
    }

    /**
     * Show the Open Files Dialog. 
     * Note: Multiple selection is not available on MacOsX machines.
     * @param title dialog title
     * @param givenDirectory optional current directory as file (last one used for given mime type if null)
     * @param mimeType optional file mime type used to get file extension(s), file filter and last directory used
     * @param defaultFileName optional default file name     
     * @param multiSelectionFlag allow multiple file selection or not
     * @return File array or null if dialog was discarded
     */
    private static File[] showOpenFileChooser(final String title, final File givenDirectory, final MimeType mimeType, final String defaultFileName,
                                              final boolean multiSelectionFlag) {

        final File preselectedDirectory = retrieveLastDirectoryForMimeType(givenDirectory, mimeType);

        File[] selectedFiles = null;

        if (USE_DIALOG_FOR_FILE_CHOOSER && !multiSelectionFlag) {
            final FileDialog fileDialog = new FileDialog(App.getFrame(), title, FileDialog.LOAD);

            if (preselectedDirectory != null) {
                fileDialog.setDirectory(preselectedDirectory.getAbsolutePath());
            }
            if (mimeType != null) {
                fileDialog.setFilenameFilter(mimeType.getFileFilter());
            }
            if (defaultFileName != null) {
                fileDialog.setFile(defaultFileName);
            }

            hookFileDialog(fileDialog);

            // waits for dialog inputs:
            fileDialog.setVisible(true);

            if (fileDialog.getFile() != null && fileDialog.getDirectory() != null) {
                selectedFiles = new File[]{new File(fileDialog.getDirectory(), fileDialog.getFile())};
            }

        } else {
            final JFileChooser fileChooser = new JmcsFileChooser();
            fileChooser.setMultiSelectionEnabled(multiSelectionFlag);
            if (preselectedDirectory != null) {
                fileChooser.setCurrentDirectory(preselectedDirectory);
            }

            // select one file:
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (mimeType != null) {
                fileChooser.setFileFilter(mimeType.getFileFilter());
            }

            if (defaultFileName != null) {
                fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), defaultFileName));
            }

            fileChooser.setDialogTitle(title);

            final int returnVal = fileChooser.showOpenDialog(App.getFrame());

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                if (multiSelectionFlag) {
                    selectedFiles = fileChooser.getSelectedFiles();
                    if (selectedFiles.length == 0) {
                        selectedFiles = null;
                    }
                } else {
                    selectedFiles = new File[]{fileChooser.getSelectedFile()};
                }
            }
        }

        if (selectedFiles != null) {
            final int length = selectedFiles.length;
            final List<File> validatedFiles = new ArrayList<File>(length);

            for (int i = 0; i < length; i++) {
                File selectedFile = selectedFiles[i];

                // Mac OS X can return application packages:
                if (SystemUtils.IS_OS_MAC_OSX && selectedFile.isDirectory()) {
                    _logger.warn("Selected file is an application package: {}", selectedFile);
                    selectedFile = null;
                } else {
                    // note: this may not work with multi selection:
                    if (!selectedFile.exists()) {
                        _logger.warn("Selected file does not exist: {}", selectedFile);

                        // try using the same file name with extension:
                        if (mimeType == null || FileUtils.getExtension(selectedFile) != null) {
                            selectedFile = null;
                        } else {
                            // try using the same file name with extension :
                            selectedFile = mimeType.checkFileExtension(selectedFile);
                            // check again if that file exists :
                            if (!selectedFile.exists()) {
                                selectedFile = null;
                            }
                        }
                    }
                }

                if (selectedFile != null) {
                    validatedFiles.add(selectedFile);
                }
            }

            final int valid = validatedFiles.size();

            // update selected file to have only valid files:
            selectedFiles = (valid != 0) ? validatedFiles.toArray(new File[valid]) : null;
        }

        if (selectedFiles != null) {
            updateDirectoryForMimeType(mimeType, selectedFiles[0].getParent());
        }

        return selectedFiles;
    }

    /**
     * Show the Save File Dialog using following properties:
     * @param title dialog title
     * @param givenDirectory optional current directory as file (last one used for given mime type if null)
     * @param mimeType optional file mime type used to get file extension(s), file filter and last directory used
     * @param defaultFileName optional default file name
     * @return File instance or null if dialog was discarded
     */
    public static File showSaveFileChooser(final String title, final File givenDirectory, final MimeType mimeType, final String defaultFileName) {

        final File preselectedDirectory = retrieveLastDirectoryForMimeType(givenDirectory, mimeType);

        File selectedFile = null;

        if (USE_DIALOG_FOR_FILE_CHOOSER) {
            final FileDialog fileDialog = new FileDialog(App.getFrame(), title, FileDialog.SAVE);
            if (preselectedDirectory != null) {
                fileDialog.setDirectory(preselectedDirectory.getAbsolutePath());
            }
            if (mimeType != null) {
                fileDialog.setFilenameFilter(mimeType.getFileFilter());
            }
            if (defaultFileName != null) {
                fileDialog.setFile(defaultFileName);
            }

            hookFileDialog(fileDialog);

            // waits for dialog inputs:
            fileDialog.setVisible(true);

            if (fileDialog.getFile() != null && fileDialog.getDirectory() != null) {
                selectedFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
            }

        } else {
            final JFileChooser fileChooser = new JmcsFileChooser();
            if (preselectedDirectory != null) {
                fileChooser.setCurrentDirectory(preselectedDirectory);
            }

            // select one file:
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (mimeType != null) {
                fileChooser.setFileFilter(mimeType.getFileFilter());
            }

            if (defaultFileName != null) {
                fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), defaultFileName));
            }

            fileChooser.setDialogTitle(title);

            final int returnVal = fileChooser.showSaveDialog(App.getFrame());

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
            }
        }
        if (selectedFile != null) {
            if (mimeType != null) {
                selectedFile = mimeType.checkFileExtension(selectedFile);
            }

            // Mac OS X already handles file overwrite confirmation:
            if (!SystemUtils.IS_OS_MAC_OSX && selectedFile.exists()) {
                if (!MessagePane.showConfirmFileOverwrite(selectedFile.getName())) {
                    StatusBar.show("overwritting cancelled.");
                    selectedFile = null;
                }
            }
        }
        if (selectedFile != null) {
            updateDirectoryForMimeType(mimeType, selectedFile.getParent());
        }
        return selectedFile;
    }

    /**
     * Return the last directory associated to the given mime type only if the given directory is undefined
     * @param givenDirectory optional proposed directory
     * @param mimeType optional file mime type used to get the last directory associated to the given mime type
     * @return directory or null
     */
    private static File retrieveLastDirectoryForMimeType(final File givenDirectory, final MimeType mimeType) {
        File preselectedDirectory = givenDirectory;
        if (preselectedDirectory == null && mimeType != null) {
            preselectedDirectory = SessionSettingsPreferences.getLastDirectoryForMimeTypeAsFile(mimeType);
        }
        return preselectedDirectory;
    }

    /**
     * Define the last directory used for files having this mime type
     * @param mimeType optional file mime type used to set the last directory associated to the given mime type
     * @param path file path to an existing directory
     */
    private static void updateDirectoryForMimeType(final MimeType mimeType, final String path) {
        if (path != null && mimeType != null) {
            SessionSettingsPreferences.setCurrentDirectoryForMimeType(mimeType, path);
        }
    }

    /**
     * Forbidden constructor
     */
    private FileChooser() {
        super();
    }

    private static void hookFileDialog(final Window fileDialog) {
        // Restore, then automatically save window size changes:
        WindowUtils.rememberWindowSize(fileDialog, FILE_DIALOG_DIMENSION_KEY);
        // Center dialog on window:
        WindowUtils.centerOnMainScreen(fileDialog);
    }

    private static final class JmcsFileChooser extends JFileChooser {

        private static final long serialVersionUID = 1L;

        JmcsFileChooser() {
            super();
        }

        @Override
        protected JDialog createDialog(final Component parent) throws HeadlessException {
            final JDialog fileDialog = super.createDialog(parent);

            hookFileDialog(fileDialog);

            return fileDialog;
        }
    }
}
