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
package fr.jmmc.jmcs.util.jaxb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used to perform generic marshalling and unmarshalling operations.
 * @author Launrent BOURGES, Guillaume MELLA.
 */
public class JAXBUtils {

    // Members
    /** Logger */
    private final static Logger _logger = LoggerFactory.getLogger(JAXBUtils.class);

    /** Private constructor for utility class */
    private JAXBUtils() {
    }

    /**
     * Load on object from url.
     * @param inputUrl File to load
     * @param jbf JAXBFactory
     * @return unmarshalled object
     *
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    public static Object loadObject(final URL inputUrl, final JAXBFactory jbf) throws IOException, IllegalStateException, XmlBindException {
        Object result = null;

        _logger.debug("JAXBUtils.loadObject() from url : {}", inputUrl);

        try {
            result = jbf.createUnMarshaller().unmarshal(new BufferedInputStream(inputUrl.openStream()));
        } catch (JAXBException ex) {
            handleException("Loading object from " + inputUrl, ex);
        }

        return result;
    }

    /**
     * Protected load method
     * @param inputFile File to load
     * @param jbf jaxb factory instance
     * @return unmarshalled object
     *
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    public static Object loadObject(final File inputFile, final JAXBFactory jbf)
            throws IOException, IllegalStateException, XmlBindException {

        _logger.debug("JAXBUtils.loadObject() from file : {}", inputFile);

        Object result = null;
        try {
            final Unmarshaller u = jbf.createUnMarshaller();

            result = u.unmarshal(inputFile);

        } catch (JAXBException je) {
            handleException("Load failure on " + inputFile, je);
        }
        return result;
    }

    /**
     * Protected load method
     * @param reader any reader
     * @param jbf jaxb factory instance
     * @return unmarshalled object
     * 
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    protected static Object loadObject(final Reader reader, final JAXBFactory jbf)
            throws IOException, IllegalStateException, XmlBindException {

        Object result = null;
        try {
            final Unmarshaller u = jbf.createUnMarshaller();

            result = u.unmarshal(reader);

        } catch (JAXBException je) {
            handleException("Load failure on " + reader, je);
        }
        return result;
    }

    /**
     * Protected save method
     * @param outputFile File to save
     * @param object to marshall
     * @param jbf jaxb factory instance
     *
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     */
    public static void saveObject(final File outputFile, final Object object, final JAXBFactory jbf)
            throws IOException, IllegalStateException {
        try {
            jbf.createMarshaller().marshal(object, outputFile);

        } catch (JAXBException je) {
            handleException("Save failure on " + outputFile, je);
        }
    }

    /**
     * Public save method
     * @param writer writer to use
     * @param object to marshall
     * @param jbf jaxb factory instance
     *
     * @throws IllegalStateException if an unexpected exception occurred
     */
    public static void saveObject(final Writer writer, final Object object, JAXBFactory jbf)
            throws IllegalStateException {
        try {
            jbf.createMarshaller().marshal(object, writer);
        } catch (JAXBException je) {
            throw new IllegalStateException("Serialization failure", je);
        }
    }

    /**
     * Handle JAXB Exception to extract IO Exception or unexpected exceptions
     * @param message message
     * @param je jaxb exception
     * 
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws IOException if an I/O exception occurred
     */
    protected static void handleException(final String message, final JAXBException je) throws IllegalStateException, IOException {
        final Throwable cause = je.getCause();
        if (cause != null) {
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
        }
        if (je instanceof UnmarshalException) {
            throw new IllegalArgumentException("The loaded file does not correspond to a valid file", je);
        }
        throw new IllegalStateException(message, je);
    }
}
