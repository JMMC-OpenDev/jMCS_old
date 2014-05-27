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
import java.io.IOException;
import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test file for FileUtils methods
 * @author mellag
 */
public class TestFileUtils {

    public TestFileUtils() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getDirectory method, of class FileUtils.
     */
    @Test
    public void testGetDirectory() {
        System.out.println("getDirectory");
        String path = "";
        File expResult = null;
        File result = FileUtils.getDirectory(path);
        Assert.assertEquals(expResult, result);

        path = SystemUtils.USER_HOME;
        expResult = new File(SystemUtils.USER_HOME);
        result = FileUtils.getDirectory(path);
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getFile method, of class FileUtils.
     */
    @Test
    public void testGetFile() {
        /*
         System.out.println("getFile");
         String path = "";
         File expResult = null;
         File result = FileUtils.getFile(path);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of getName method, of class FileUtils.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        String fileName = "";
        String expResult = "";
        String result = FileUtils.getName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "http://www.av.com/toto";
        expResult = "toto";
        result = FileUtils.getName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "http://www.av.com/toto/";
        expResult = "";
        result = FileUtils.getName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "/var/toto";
        expResult = "toto";
        result = FileUtils.getName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "/var/toto/";
        expResult = "";
        result = FileUtils.getName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = null;
        expResult = "";
        result = FileUtils.getName(fileName);
        Assert.assertEquals(expResult, result);

    }

    /**
     * Test of getFileNameWithoutExtension method, of class FileUtils.
     */
    @Test
    public void testGetFileNameWithoutExtension_File() {
        System.out.println("getFileNameWithoutExtension");
        File file = null;
        String expResult = null;
        String result = FileUtils.getFileNameWithoutExtension(file);
        Assert.assertEquals(expResult, result);

    }

    /**
     * Test of getFileNameWithoutExtension method, of class FileUtils.
     */
    @Test
    public void testGetFileNameWithoutExtension_String() {
        System.out.println("getFileNameWithoutExtension");
        String fileName = null;
        String expResult = null;
        String result = FileUtils.getFileNameWithoutExtension(fileName);
        Assert.assertEquals(expResult, result);
        /* TODO */
    }

    /**
     * Test of getExtension method, of class FileUtils.
     */
    @Test
    public void testGetExtension_File() {
        System.out.println("getExtension");
        File file = null;
        String expResult = null;
        String result = FileUtils.getExtension(file);
        Assert.assertEquals(expResult, result);
        /* TODO */
    }

    /**
     * Test of getExtension method, of class FileUtils.
     */
    @Test
    public void testGetExtension_String() {
        System.out.println("getExtension");
        String fileName = "";
        String expResult = null;
        String result = FileUtils.getExtension(fileName);
        Assert.assertEquals(expResult, result);
        /* TODO */
    }

    /**
     * Test of getExtension method, of class FileUtils.
     */
    @Test
    public void testGetExtension_File_int() {
        System.out.println("getExtension");
        File file = null;
        int nDots = 0;
        String expResult = null;
        String result = FileUtils.getExtension(file, nDots);
        Assert.assertEquals(expResult, result);
        /* TODO */
    }

    /**
     * Test of getExtension method, of class FileUtils.
     */
    @Test
    public void testGetExtension_String_int() {
        System.out.println("getExtension");
        int nDots = 0;
        String fileName = "";
        String expResult = null;
        String result = FileUtils.getExtension(fileName, nDots);
        Assert.assertEquals(expResult, result);

        // with one dot
        nDots = 1;
        fileName = "toto.tutu.fits";
        expResult = "fits";
        result = FileUtils.getExtension(fileName, nDots);
        Assert.assertEquals(expResult, result);

        fileName = "tutu.fits";
        expResult = "fits";
        result = FileUtils.getExtension(fileName, nDots);
        Assert.assertEquals(expResult, result);

        // with mutliple dots now
        nDots = 2;
        fileName = "toto.tutu.fits";
        expResult = "tutu.fits";
        result = FileUtils.getExtension(fileName, nDots);
        Assert.assertEquals(expResult, result);

        fileName = "tutu.fits";
        expResult = null;
        result = FileUtils.getExtension(fileName, nDots);
        Assert.assertEquals(expResult, result);

    }

    /**
     * Test of readFile method, of class FileUtils.
     */
    @Test
    public void testReadFile() throws Exception {
        System.out.println("readFile");
        /* TODO
         File file = null;
         String expResult = "";
         String result = FileUtils.readFile(file);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of readStream method, of class FileUtils.
     */
    @Test
    public void testReadStream_InputStream() throws Exception {
        System.out.println("readStream");
        /* TODO
         InputStream inputStream = null;
         String expResult = "";
         String result = FileUtils.readStream(inputStream);
         Assert.assertEquals(expResult, result);
         */

    }

    /**
     * Test of readStream method, of class FileUtils.
     */
    @Test
    public void testReadStream_InputStream_int() throws Exception {
        System.out.println("readStream");
        /* TODO
         InputStream inputStream = null;
         int bufferCapacity = 0;
         String expResult = "";
         String result = FileUtils.readStream(inputStream, bufferCapacity);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of writeFile method, of class FileUtils.
     */
    @Test
    public void testWriteFile() throws Exception {
        System.out.println("writeFile");
        /* TODO
         File file = null;
         String content = "";
         FileUtils.writeFile(file, content);
         */

    }

    /**
     * Test of openFile method, of class FileUtils.
     */
    @Test
    public void testOpenFile_String() {
        System.out.println("openFile");
        /* TODO
         String absoluteFilePath = "";
         Writer expResult = null;
         Writer result = FileUtils.openFile(absoluteFilePath);
         Assert.assertEquals(expResult, result);
         */

    }

    /**
     * Test of openFile method, of class FileUtils.
     */
    @Test
    public void testOpenFile_String_int() {
        System.out.println("openFile");
        /* TODO
         String absoluteFilePath = "";
         int bufferSize = 0;
         Writer expResult = null;
         Writer result = FileUtils.openFile(absoluteFilePath, bufferSize);
         Assert.assertEquals(expResult, result);
         */

    }

    /**
     * Test of openFile method, of class FileUtils.
     */
    @Test
    public void testOpenFile_File() {
        System.out.println("openFile");
        /* TODO
         File file = null;
         Writer expResult = null;
         Writer result = FileUtils.openFile(file);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of openFile method, of class FileUtils.
     */
    @Test
    public void testOpenFile_File_int() {
        System.out.println("openFile");
        /* TODO
         File file = null;
         int bufferSize = 0;
         Writer expResult = null;
         Writer result = FileUtils.openFile(file, bufferSize);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of closeFile method, of class FileUtils.
     */
    @Test
    public void testCloseFile_Reader() {
        System.out.println("closeFile");
        /* TODO
         Reader r = null;
         Reader expResult = null;
         Reader result = FileUtils.closeFile(r);
         Assert.assertEquals(expResult, result);
         */

    }

    /**
     * Test of closeFile method, of class FileUtils.
     */
    @Test
    public void testCloseFile_Writer() {
        System.out.println("closeFile");
        /* TODO
         Writer w = null;
         Writer expResult = null;
         Writer result = FileUtils.closeFile(w);
         Assert.assertEquals(expResult, result);
         */

    }

    /**
     * Test of closeStream method, of class FileUtils.
     */
    @Test
    public void testCloseStream_InputStream() {
        System.out.println("closeStream");
        /* TODO
         InputStream in = null;
         FileUtils.closeStream(in);
         */

    }

    /**
     * Test of closeStream method, of class FileUtils.
     */
    @Test
    public void testCloseStream_OutputStream() {
        System.out.println("closeStream");
        /* TODO
         OutputStream out = null;
         FileUtils.closeStream(out);
         */

    }

    /**
     * Test of copy method, of class FileUtils.
     */
    @Test
    public void testCopy() throws Exception {
        System.out.println("copy");
        /* TODO
         File src = null;
         File dst = null;
         FileUtils.copy(src, dst);
         */
    }

    /**
     * Test of saveStream method, of class FileUtils.
     */
    @Test
    public void testSaveStream() throws Exception {
        System.out.println("saveStream");
        /* TODO
         InputStream in = null;
         File dst = null;
         FileUtils.saveStream(in, dst);
         */
    }

    /**
     * Test of zip method, of class FileUtils.
     */
    @Test
    public void testZip() throws Exception {
        System.out.println("zip");
        /* TODO
         File src = null;
         File dst = null;
         FileUtils.zip(src, dst);
         */
    }

    /**
     * Test of copyFile method, of class FileUtils.
     */
    @Test
    public void testCopyFile() throws Exception {
        System.out.println("copyFile");
        /* TODO
         File in = null;
         File out = null;
         FileUtils.copyFile(in, out);
         */
    }

    /**
     * Test of renameFile method, of class FileUtils.
     */
    @Test
    public void testRenameFile() throws Exception {
        System.out.println("renameFile");
        /* TODO
         String oldPath = "";
         String newPath = "";
         boolean overwrite = false;
         FileUtils.renameFile(oldPath, newPath, overwrite);
         */
    }

    /**
     * Test of checksum method, of class FileUtils.
     */
    @Test
    public void testChecksum() throws Exception {
        System.out.println("checksum TBD");
        /* TODO
         InputStream in = null;
         byte[] expResult = null;
         byte[] result = FileUtils.checksum(in);
         assertArrayEquals(expResult, result);
         */
    }

    /**
     * Test of getTempFile method, of class FileUtils.
     */
    @Test
    public void testGetTempFile_String_String() {
        System.out.println("getTempFile");
        /* TODO
         String prefix = "";
         String suffix = "";
         File expResult = null;
         File result = FileUtils.getTempFile(prefix, suffix);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of getTempFile method, of class FileUtils.
     */
    @Test
    public void testGetTempFile_String() {
        System.out.println("getTempFile");
        /* TODO
         String filename = "";
         File expResult = null;
         File result = FileUtils.getTempFile(filename);
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of getTempDirPath method, of class FileUtils.
     */
    @Test
    public void testGetTempDirPath() {
        System.out.println("getTempDirPath");
        /* TODO

         String expResult = "";
         String result = FileUtils.getTempDirPath();
         Assert.assertEquals(expResult, result);
         */
    }

    /**
     * Test of cleanupFileName method, of class FileUtils.
     */
    @Test
    public void testCleanupFileName() {
        System.out.println("cleanupFileName");

        String fileName = "";
        String expResult = "";
        String result = FileUtils.cleanupFileName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "aZeRtY/uiop";
        expResult = "aZeRtY_uiop";
        result = FileUtils.cleanupFileName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "This>is some(string,with $invalid*-chars).jpg";
        expResult = "This_is_some_string_with__invalid_-chars_.jpg";
        result = FileUtils.cleanupFileName(fileName);
        Assert.assertEquals(expResult, result);

        fileName = "aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰ-_*$€\\[]";
        expResult = "aaeeiioooouuuu_AAEEIIOOOOUUUU-_______";
        result = FileUtils.cleanupFileName(fileName);
        Assert.assertEquals(expResult, result);

    }

    /**
     * Test of isRemote method, of class FileUtils.
     */
    @Test
    public void testIsRemote() {
        System.out.println("isRemote");
        String fileLocation = "";
        boolean expResult = false;
        boolean result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = " ";
        expResult = false;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "http://www.jmmc.fr";
        expResult = true;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "http://www.jmmc.fr/file";
        expResult = true;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "http://www.jmmc.fr/file.ext";
        expResult = true;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "/var/file";
        expResult = false;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "/var/file.ext";
        expResult = false;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "file:///var/file";
        expResult = false;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

        fileLocation = "file:///var/file.ext";
        expResult = false;
        result = FileUtils.isRemote(fileLocation);
        Assert.assertEquals(expResult, result);

    }

    /**
     * Test of retrieveRemoteFile method, of class FileUtils.
     */
    @Test
    public void testRetrieveRemoteFile() throws Exception {
        System.out.println("retrieveRemoteFile");
        /* TODO

         String remoteLocation = "";
         String parentDir = "";
         MimeType mimeType = null;
         File expResult = null;
         File result = FileUtils.retrieveRemoteFile(remoteLocation, parentDir, mimeType);
         Assert.assertEquals(expResult, result);
         */

    }

    public static void main(String[] args) {
        try {
            File f1 = FileUtils.getTempFile("toto", "txt");
            FileUtils.writeFile(f1, "ABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGH");
            File f2 = FileUtils.getTempFile("toto", ".txt.gz");
            FileUtils.zip(f1, f2);
            System.out.println("f1 = " + f1);
            System.out.println("f1.length() = " + f1.length());
            System.out.println("f2 = " + f2);
            System.out.println("f2.length() = " + f2.length());
            System.out.println("f2.read() = " + FileUtils.readFile(f2));

        } catch (IOException ioe) {
            System.out.println("exception:" + ioe);
        }

    }
}