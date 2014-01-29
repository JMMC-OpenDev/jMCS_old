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
package fr.jmmc.jmcs.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.filechooser.FileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about.
 *
 * Extensions are of the type "foo", which is typically found on Windows and
 * Unix boxes, but not on older Macintosh which use ResourceForks (case ignored).
 *
 * Example - create a new filter that filters out all files but GIF and JPG files:
 *     GenericFileFilter filter = new GenericFileFilter(
 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
 *
 * Strongly inspired of ExampleFileFilter class from FileChooserDemo under the
 * demo/JFC directory in the JDK.
 *
 * @author Sylvain LAFRASSE, Laurent BOURGES, Guillaume MELLA.
 */
public final class GenericFileFilter extends FileFilter implements FilenameFilter {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(GenericFileFilter.class.getName());
    /** Hold each file extensions */
    private final HashMap<String, String> _fileExtensions = new HashMap<String, String>(4);
    /** Filter description */
    private final String _description;
    /** Flag to indicate that one extension contains '.' char */
    private final boolean _extensionWithDot;

    /**
     * Creates a new GenericFileFilter object.
     *
     * @param fileExtensions an array of file extensions associated to the mime type.
     * @param description the humanly readable description for the mime type.
     */
    public GenericFileFilter(final String[] fileExtensions, final String description) {
        super();

        if (_logger.isDebugEnabled()) {
            _logger.debug("GenericFileFilter(fileExtensions = '{}', description = '{}')",
                    Arrays.toString(fileExtensions), description);
        }

        final int nbOfFileExtensions = fileExtensions.length;

        boolean hasDot = false;

        for (int i = 0; i < nbOfFileExtensions; i++) {
            // Add filters one by one
            final String fileExtension = fileExtensions[i].toLowerCase();

            hasDot |= fileExtension.contains(".");

            _fileExtensions.put(fileExtension, description);

            if (_logger.isTraceEnabled()) {
                _logger.trace("GenericFileFilter(...) - Added fileExtensions[{}]/{}] = '{}'.",
                        (i + 1), nbOfFileExtensions, fileExtension);
            }
        }

        _extensionWithDot = hasDot;
        _description = description;
    }

    /**
     * Return whether the given file is accepted by this filter, or not.
     *
     * @param currentFile the file to test
     *
     * @return true if file is accepted, false otherwise.
     */
    @Override
    public boolean accept(final File currentFile) {
        return checkExtensions(currentFile, true);
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return accept(new File(dir, name));
    }

    /**
     * Return whether the given file is accepted by this filter, or not.
     *
     * @param currentFile the file to test
     * @param checkRead true to check if the given file exists and is really a file (not directory or ...)
     *
     * @return true if file is accepted, false otherwise.
     */
    public boolean checkExtensions(final File currentFile, final boolean checkRead) {
        if (currentFile != null) {
            final String fileName = currentFile.getName();

            // If current file is not regular (e.g directory, links, ...)
            if (checkRead && !currentFile.isFile()) {
                _logger.trace("Accepting non-regular file '{}'.", fileName);

                // Accept it to ensure navigation through directory and so
                return true;
            }

            // If the file has no extension
            final String fileExtension = FileUtils.getExtension(fileName);

            if (fileExtension == null) {
                return false; // Discard it
            }

            // If corresponding mime-type is handled
            String fileType = _fileExtensions.get(fileExtension);

            if (fileType != null) {
                _logger.debug("Accepting file '{}' of type '{}'.", fileName, fileType);

                return true; // Accept it
            }

            if (_extensionWithDot) {
                // retry with extension with dot:
                final String fileExtWithDot = FileUtils.getExtension(fileName, 2);

                if (fileExtWithDot == null) {
                    return false; // Discard it
                }

                // If corresponding mime-type is handled
                fileType = _fileExtensions.get(fileExtWithDot);

                if (fileType != null) {
                    _logger.debug("Accepting file '{}' of type '{}'.", fileName, fileType);

                    return true; // Accept it
                }
            }
        }

        return false;
    }

    /**
     * Return the description of this filter.
     *
     * @return the description of this filter.
     */
    @Override
    public String getDescription() {
        return _description;
    }

    /**
     * Return the content of the object as a String for output.
     *
     * @return the content of the object as a String for output.
     */
    @Override
    public String toString() {
        final String fileExtensions;

        if (_fileExtensions != null) {
            fileExtensions = _fileExtensions.toString();
        } else {
            fileExtensions = "NONE";
        }

        return "File extensions registered for '" + _description + "' : " + fileExtensions;
    }
}