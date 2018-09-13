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

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.network.http.Http;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import static java.io.File.separatorChar;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Several File utility methods.
 *
 * @author Guillaume MELLA, Laurent BOURGES, Sylvain LAFRASSE.
 */
public final class FileUtils {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(FileUtils.class.getName());
    /** Platform dependent line separator */
    public static final String LINE_SEPARATOR = SystemUtils.LINE_SEPARATOR;
    /** File encoding use UTF-8 */
    public static final String FILE_ENCODING = "UTF-8";
    /** Default read buffer capacity: 8K */
    public static final int DEFAULT_BUFFER_CAPACITY = 8192;

    /**
     * Returns an existing File for the given path
     *
     * @param path file path
     * @return File or null
     */
    private static File getExistingFile(final String path) {
        if (!StringUtils.isEmpty(path)) {
            final File file = new File(path);

            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    /**
     * Returns an existing directory for the given path
     *
     * @param path directory path
     * @return directory or null
     */
    public static File getDirectory(final String path) {
        final File dir = getExistingFile(path);

        if (dir != null && dir.isDirectory()) {
            return dir;
        }

        return null;
    }

    /**
     * Returns an existing File for the given path
     *
     * @param path file path
     * @return File or null
     */
    public static File getFile(final String path) {
        final File file = getExistingFile(path);

        if (file != null && file.isFile()) {
            return file;
        }

        return null;
    }

    /**
     * Returns the name of the file or directory denoted by this abstract
     * pathname.  This is just the last name in the pathname's name
     * sequence.  If the pathname's name sequence is empty, then the empty
     * string is returned.
     *
     * @param fileName long file name (local or remote)
     *
     * @return  The name of the file or directory denoted by this abstract
     *          pathname, or the empty string if this pathname's name sequence
     *          is empty
     */
    public static String getName(final String fileName) {
        if (fileName == null) {
            return "";
        }

        int index = fileName.lastIndexOf(separatorChar);
        if (index < 0) {
            return fileName;
        }
        return fileName.substring(index + 1);
    }

    /**
     * Get the file name part without extension
     *
     * @param file file as File
     * @return the file name part without extension or null
     */
    public static String getFileNameWithoutExtension(final File file) {
        if (file != null) {
            return getFileNameWithoutExtension(file.getName());
        }
        return null;
    }

    /**
     * Get the file name part without extension
     *
     * @param fileName file name as String
     * @return the file name part without extension or null
     */
    public static String getFileNameWithoutExtension(final String fileName) {
        if (fileName != null) {
            final int pos = fileName.lastIndexOf('.');
            if (pos == -1) {
                return fileName;
            }
            if (pos > 0) {
                return fileName.substring(0, pos);
            }
        }
        return null;
    }

    /**
     * Get the extension of a file in lower case
     *
     * @param file file as File
     * @return the extension of the file (without the dot char) or null
     */
    public static String getExtension(final File file) {
        if (file != null) {
            return getExtension(file.getName());
        }
        return null;
    }

    /**
     * Get the extension of a file in lower case
     *
     * @param fileName file name as String
     * @return the extension of the file (without the dot char) or null
     */
    public static String getExtension(final String fileName) {
        if (fileName != null) {
            final int i = fileName.lastIndexOf('.');

            if (i > 0 && i < fileName.length() - 1) {
                return fileName.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    /**
     * Get the extension of a file in lower case
     *
     * @param file file as File
     * @param nDots number of dots in extension
     * @return the extension of the file (without first dot char) or null
     */
    public static String getExtension(final File file, final int nDots) {
        if (file != null) {
            return getExtension(file.getName(), nDots);
        }
        return null;
    }

    /**
     * Get the extension of a file in lower case
     *
     * @param fileName file name as String
     * @param nDots number of dots in extension
     * @return the extension of the file (without first dot char) or null
     */
    public static String getExtension(final String fileName, final int nDots) {
        if (fileName != null) {
            final int end = fileName.length() - 1;
            int from = end;
            int nDot = 0;

            for (;;) {
                int i = fileName.lastIndexOf('.', from);

                if (i > 0 && i < end) {
                    nDot++;
                    from = i - 1;

                    if (nDot == nDots) {
                        return fileName.substring(i + 1).toLowerCase();
                    }

                } else {
                    break;
                }
            }
        }
        return null;
    }

    /**
     * Read a text file from the given file
     *
     * @param file local file
     * @return text file content
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String readFile(final File file) throws IOException {
        return readStream(new FileInputStream(file), (int) file.length());
    }

    /**
     * Read a text file from the given input stream into a string
     *
     * @param inputStream stream to load
     * @return text file content
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String readStream(final InputStream inputStream) throws IOException {
        return readStream(inputStream, DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Read a text file from the given input stream into a string
     *
     * @param inputStream stream to load
     * @param bufferCapacity initial buffer capacity (chars)
     * @return text file content
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String readStream(final InputStream inputStream, final int bufferCapacity) throws IOException {

        String result = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, FILE_ENCODING));

            // Use one string buffer with the best guessed initial capacity:
            final StringBuilder sb = new StringBuilder(bufferCapacity);

            // Use a char buffer to consume reader using DEFAULT_BUFFER_CAPACITY:
            final char[] cbuf = new char[DEFAULT_BUFFER_CAPACITY];

            int len;
            while ((len = reader.read(cbuf)) > 0) {
                sb.append(cbuf, 0, len);
            }

            result = sb.toString();

        } finally {
            closeFile(reader);
        }
        return result;
    }

    /**
     * Write the given string into the given file
     *
     * @param file file to write
     * @param content content to write
     *
     * @throws IOException if an I/O exception occurred
     */
    public static void writeFile(final File file, final String content) throws IOException {
        final Writer w = openFile(file);
        try {
            w.write(content);
        } finally {
            closeFile(w);
        }
    }

    /**
     * Returns a Writer for the given file path and use the default writer
     * buffer capacity
     *
     * @param absoluteFilePath absolute file path
     * @return Writer (buffered) or null
     */
    public static Writer openFile(final String absoluteFilePath) {
        return openFile(absoluteFilePath, DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Returns a Writer for the given file path and use the given buffer
     * capacity
     *
     * @param absoluteFilePath absolute file path
     * @param bufferSize write buffer capacity
     * @return Writer (buffered) or null
     */
    public static Writer openFile(final String absoluteFilePath, final int bufferSize) {
        if (!StringUtils.isEmpty(absoluteFilePath)) {
            return openFile(new File(absoluteFilePath), bufferSize);
        }

        return null;
    }

    /**
     * Returns a Writer for the given file and use the default writer buffer
     * capacity
     *
     * @param file file to write
     * @return Writer (buffered) or null
     */
    public static Writer openFile(final File file) {
        return openFile(file, DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Returns a Writer for the given file and use the given buffer capacity
     *
     * @param file file to write
     * @param bufferSize write buffer capacity
     * @return Writer (buffered) or null
     */
    public static Writer openFile(final File file, final int bufferSize) {
        try {
            // Should define UTF-8 encoding for cross platform compatibility
            // but we must stay compatible with existing files (windows vs unix)
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), FILE_ENCODING), bufferSize);
        } catch (final IOException ioe) {
            _logger.error("IO failure : ", ioe);
        }

        return null;
    }

    /**
     * Close the given reader
     *
     * @param r reader to close
     *
     * @return null to optionally reset variable (fluent API)
     */
    public static Reader closeFile(final Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (IOException ioe) {
                _logger.debug("IO close failure.", ioe);
            }
        }
        return null;
    }

    /**
     * Close the given writer
     *
     * @param w writer to close
     *
     * @return null to optionally reset variable (fluent API)
     */
    public static Writer closeFile(final Writer w) {
        if (w != null) {
            try {
                w.close();
            } catch (IOException ioe) {
                _logger.debug("IO close failure.", ioe);
            }
        }
        return null;
    }

    /**
     * Close the given input stream
     *
     * @param in input stream to close
     */
    public static void closeStream(final InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                _logger.debug("IO close failure.", ioe);
            }
        }
    }

    /**
     * Close the given output stream
     *
     * @param out output stream to close
     */
    public static void closeStream(final OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                _logger.debug("IO close failure.", ioe);
            }
        }
    }

    /**
     * Copy file
     *
     * @param src source file
     * @param dst destination file
     * @throws IOException if an I/O exception occurred
     * @throws FileNotFoundException if input file is not found
     */
    public static void copy(final File src, final File dst) throws IOException, FileNotFoundException {
        final InputStream in = new BufferedInputStream(new FileInputStream(src));

        saveStream(in, dst);
    }

    /**
     * Save the given input stream as file.
     *
     * @param in input stream to save as file
     * @param dst destination file
     * @throws IOException if an I/O exception occurred
     */
    public static void saveStream(final InputStream in, final File dst) throws IOException {
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(dst), 64 * 1024);

        // Transfer bytes from in to out
        try {
            final byte[] buf = new byte[64 * 1024];

            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            closeStream(in);
            closeStream(out);
        }
    }

    /**
     * Save the input file to the given output stream.
     *
     * @param in input file
     * @param dst destination outputstream
     * @throws IOException if an I/O exception occurred
     * @throws FileNotFoundException if the input file is not found
     */
    public static void saveFile(final File in, final OutputStream dst) throws IOException, FileNotFoundException {
        final OutputStream out = new BufferedOutputStream(dst, 64 * 1024);
        final InputStream is = new FileInputStream(in);
        // Transfer bytes from in to out
        try {
            final byte[] buf = new byte[64 * 1024];

            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            closeStream(is);
            closeStream(out);
        }
    }

    /**
     * Zip source file into destination one.
     *
     * @param src source file to be zipped
     * @param dst destination file corresponding to the zipped source file
     * @throws IOException if an I/O exception occurred
     * @throws FileNotFoundException if input file is not found
     */
    public static void zip(final File src, final File dst) throws IOException, FileNotFoundException {
        final InputStream in = new BufferedInputStream(new FileInputStream(src));
        final OutputStream out = new GZIPOutputStream(new FileOutputStream(dst), 64 * 1024);

        // Transfer bytes from in to out
        try {
            final byte[] buf = new byte[8 * 1024];

            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            closeStream(in);
            closeStream(out);
        }
    }

    /**
     * Unzip source file into destination one.
     *
     * @param src source file to be unzipped
     * @param dst destination file corresponding to the unzipped source file
     * @throws IOException if an I/O exception occurred
     * @throws FileNotFoundException if input file is not found
     */
    public static void unzip(final File src, final File dst) throws IOException, FileNotFoundException {
        final InputStream in = new GZIPInputStream(new FileInputStream(src));
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(dst), 64 * 1024);

        // Transfer bytes from in to out
        try {
            final byte[] buf = new byte[8 * 1024];

            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            closeStream(in);
            closeStream(out);
        }
    }

    /**
     * Copy the input file to output file
     *
     * @param in input file
     * @param out output file
     * @throws IOException if an I/O exception occurred
     */
    public static void copyFile(final File in, final File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * Rename a file from an old path to a new path.
     *
     * @param oldPath original file path.
     * @param newPath destination path.
     * @param overwrite true to overwrite destination path if needed, false otherwise.
     * @throws IOException if an I/O exception occurred
     */
    public static void renameFile(String oldPath, String newPath, boolean overwrite) throws IOException {

        boolean done = false;
        File source = new File(oldPath);
        File destination = new File(newPath);

        try {
            if (destination.exists()) {
                if (!overwrite) {
                    throw new IOException("'" + newPath + "' already exists");
                }
                if (!destination.delete()) {
                    throw new IOException("Could not delete '" + newPath + "'");
                }
            }

            if (!source.renameTo(destination)) {
                throw new IOException("'" + oldPath + "' could not be renamed to '" + newPath + "'");
            }
            done = true;
        } finally {
            if (done) {
                source.delete();
            }
        }
    }

    /**
     * Return the MD5 checksum of the given input stream
     * @param in input file
     * @return MD5 checksum as byte[]
     * @throws IOException if an I/O exception occurred
     */
    public static byte[] checksum(final InputStream in) throws IOException {
        try {
            return org.apache.commons.codec.digest.DigestUtils.md5(in);
        } finally {
            closeStream(in);
        }
    }

    /**
     * Creates an empty file in the default temporary-file directory, using the
     * given prefix and suffix to generate its name. The file will be deleted on
     * program exit.
     *
     * @param prefix The prefix string to be used in generating the file's name;
     * must be at least three characters long
     *
     * @param suffix The suffix string to be used in generating the file's name;
     * may be
     * <code>null</code>, in which case the suffix
     * <code>".tmp"</code> will be used
     *
     * @return An abstract pathname denoting a newly-created empty file
     *
     * @throws IllegalStateException If a file could not be created
     *
     * @throws SecurityException If a security manager exists and its
     * <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code> method
     * does not allow a file to be created
     */
    public static File getTempFile(final String prefix, final String suffix) {
        // Prevent exception thrown by createTempFile that requires one prefix and
        // suffix longer than 3 chars.
        final String p;
        if (prefix.length() < 3) {
            p = prefix + "___";
        } else {
            p = prefix;
        }
        final String s;
        if (suffix.length() < 3) {
            s = "___" + suffix;
        } else {
            s = suffix;
        }

        File file = null;
        try {
            file = File.createTempFile(p, s);
            file.deleteOnExit();
        } catch (IOException ioe) {
            throw new IllegalStateException("unable to create a temporary file", ioe);
        }
        return file;
    }

    /**
     * Return an temporary filename using temp directory and given filename. The
     * caller must consider that this file may already be present. The file will
     * be deleted on program exit.
     *
     * @param filename the short name to use in the computation of the temporary
     * filename
     * @return the temporary filename
     */
    public static File getTempFile(final String filename) {
        final File file = new File(getTempDirPath(), filename);
        file.deleteOnExit();
        return file;
    }

    /**
     * Return the temporary directory where temporary file can be saved into.
     *
     * @return the temporary directory name
     */
    public static String getTempDirPath() {
        return SystemUtils.JAVA_IO_TMPDIR;
    }

    /**
     * Remove accents from characters and replace wild chars with '_'.
     * @param fileName the string to clean up
     * @return cleaned up file name
     */
    public static String cleanupFileName(final String fileName) {
        // Remove accent from characters (if any) (Java 1.6)
        final String removed = StringUtils.removeAccents(fileName);

        // Replace wild characters with '_'
        final String cleaned = StringUtils.replaceNonFileNameCharsByUnderscore(removed);

        if (_logger.isDebugEnabled() && !cleaned.equals(fileName)) {
            _logger.debug("Had to clean up file name (was '{}', became '{}').", fileName, cleaned);
        }

        return cleaned;
    }

    /**
     * Test if given file is remote.
     *
     * @param fileLocation file location path to test (must not be null).
     * @return true if file is remote else false
     */
    public static boolean isRemote(final String fileLocation) {

        // If the given file is an URL :
        if (fileLocation.contains(":/")) {

            try {
                final URI uri = new URI(fileLocation);
                //TODO check for other local resources ?? jar is local or remote ?
                return !"file".equalsIgnoreCase(uri.getScheme());
            } catch (URISyntaxException ue) {
                _logger.error("bad URI", ue);
            }

        }

        return false;
    }

    /**
     * Retrieve a remote file onto local disk.
     * For now: limited to HTTP and HTTPS.
     *
     * Warning: calling this method may block the current thread for long time (slow transfer or big file or timeout)
     * Please take care of using it properly using a cancellable SwingWorker (Cancellable background task)
     *
     * @see fr.jmmc.jmcs.network.http.Http
     * @param remoteLocation remote location
     * @param parentDir destination directory
     * @param mimeType mime type to fix missing file extension
     * @return a copy of the remote file
     * @throws IOException if any I/O operation fails (HTTP or file)
     * @throws URISyntaxException if given fileLocation  is invalid
     */
    public static File retrieveRemoteFile(final String remoteLocation,
            final String parentDir,
            final MimeType mimeType) throws IOException, URISyntaxException {

        // TODO improve handling of existing files (do we have to warn the user ?)
        // TODO add other remote file scheme (ftp, ssh?)
        // assert that parentDir exist
        new File(parentDir).mkdirs();

        String fileName = FileUtils.getName(remoteLocation);

        if (fileName.isEmpty()) {
            fileName = StringUtils.replaceNonAlphaNumericCharsByUnderscore(remoteLocation);
        }

        // fix file extension if missing:
        final File name = mimeType.checkFileExtension(new File(fileName));

        final File localFile = new File(parentDir, name.getName());

        if (!localFile.exists()) {
            StatusBar.show("downloading file: " + remoteLocation + " ...");

            if (!Http.download(new URI(remoteLocation), localFile, false)) {
                // http status != 200
                return null;
            }
        } else {
            // TODO: use HEAD HTTP method to check remote file date / checksum ...
            _logger.info("Use local copy '{}', skip downloading '{}'", localFile, remoteLocation);
        }

        return localFile;
    }

    /**
     * Returns the path of folder containing preferences files, as this varies
     * across different execution platforms.
     *
     * @return a string containing the full folder path to the preference file,
     * according to execution platform.
     */
    static public String getPlatformPreferencesPath() {
        // [USER_HOME]/
        String fullPreferencesPath = SystemUtils.USER_HOME + File.separatorChar;

        // Under Mac OS X
        if (SystemUtils.IS_OS_MAC_OSX) {
            // [USER_HOME]/Library/Preferences/
            fullPreferencesPath += ("Library" + File.separatorChar + "Preferences" + File.separatorChar);
        } // Under Windows
        else if (SystemUtils.IS_OS_WINDOWS) {
            // [USER_HOME]/Local Settings/Application Data/
            fullPreferencesPath += ("Local Settings" + File.separatorChar + "Application Data" + File.separatorChar);
        } // Under Linux, and anything else
        else {
            // [USER_HOME]/.
            fullPreferencesPath += ".";
        }

        // Mac OS X : [USER_HOME]/Library/Preferences/
        // Windows : [USER_HOME]/Local Settings/Application Data/
        // Linux (and anything else) : [USER_HOME]/.
        _logger.debug("Computed preferences folder path = '{}'.", fullPreferencesPath);
        return fullPreferencesPath;
    }

    /**
     * Returns the preferred path for cache files across different execution platforms.
     *
     * @return a string containing the full file path for caches,
     * according to the execution platform.
     */
    static public String getPlatformCachesPath() {
        // [USER_HOME]/
        String fullCachesPath = SystemUtils.USER_HOME + File.separatorChar;

        // Under Mac OS X
        if (SystemUtils.IS_OS_MAC_OSX) {
            // [USER_HOME]/Library/Caches/
            fullCachesPath += ("Library" + File.separatorChar + "Caches" + File.separatorChar);
        } // Under Windows
        else if (SystemUtils.IS_OS_WINDOWS) {
            // [USER_HOME]/AppData/Local/
            fullCachesPath += ("AppData" + File.separatorChar + "Local" + File.separatorChar);
        } // Under Linux, and anything else
        else {
            // [USER_HOME]/.cache/
            fullCachesPath += ".cache" + File.separatorChar;
        }

        // Mac OS X : [USER_HOME]/Library/Caches/
        // Windows : [USER_HOME]/AppData/Local/
        // Linux (and anything else) : [USER_HOME]/.cache/
        _logger.debug("Computed caches folder path = '{}'.", fullCachesPath);
        return fullCachesPath;
    }

    /**
     * Returns the preferred path for documents files across different execution platforms.
     *
     * @return a string containing the full file path for documents,
     * according to the execution platform.
     */
    static public String getPlatformDocumentsPath() {
        // [USER_HOME]/
        String fullDocumentsPath = SystemUtils.USER_HOME + File.separatorChar;

        // Under Mac OS X or Windows
        if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_WINDOWS) {
            // [USER_HOME]/Documents/
            fullDocumentsPath += ("Documents" + File.separatorChar);
        }
        // Under Linux, and anything else just use [USER_DIR]

        // Mac OS X or Windows : [USER_HOME]/Documents/
        // Linux (and anything else) : [USER_HOME]/
        _logger.debug("Computed documents folder path = '{}'.", fullDocumentsPath);
        return fullDocumentsPath;
    }

    /** Forbidden constructor */
    private FileUtils() {
        // no-op
    }

    public static void main(String[] args) {
        String[] table = {"aZeRtY/uiop", "This>is some(string,with $invalid*-chars).jpg", "aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰ-_*$€\\[]"};
        for (String string : table) {
            final String cleanupFileName = cleanupFileName(string);
            System.out.println("cleanupFileName(" + string + ") = " + cleanupFileName);
        }
        System.out.println("getPlatformPreferencesPath() = " + getPlatformPreferencesPath());
        System.out.println("getPlatformCachesPath() = " + getPlatformCachesPath());
        System.out.println("getPlatformDocumentsPath() = " + getPlatformDocumentsPath());
        /*
         cleanupFileName(aZeRtY/uiop) = aZeRtY_uiop
         cleanupFileName(This>is some(string,with $invalid*-chars).jpg) = This_is_some_string_with__invalid_-chars_.jpg
         cleanupFileName(aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰ-_*$€\[]) = a_e_i_o___u____A_E_I_O___U___-_______
         */
    }
}
