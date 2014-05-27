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
package fr.jmmc.jmcs.service;

import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.jmcs.util.StringUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for XSL transformations.
 *
 * @author Laurent BOURGES.
 */
public final class XslTransform {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(XslTransform.class.getName());
    /** Encoding used for XML and XSL documents */
    public static final String ENCODING = "UTF-8";
    /** Default buffer size for XSLT result document */
    public static final int DEFAULT_BUFFER_SIZE = 16384;
    /** Inner XSLT factory */
    private static TransformerFactory _transformerFactory = null;
    /** Cache for XSL templates */
    private static final Map<String, Templates> _cachedTemplates = new HashMap<String, Templates>(32);

    /** Forbidden constructor */
    private XslTransform() {
        /* no-op */
    }

    /**
     * Returns a TransformerFactory (JAXP)
     *
     * @return TransformerFactory (JAXP)
     *
     * @throws IllegalStateException if TransformerFactory initialization failed
     */
    private static TransformerFactory getTransformerFactory()
            throws IllegalStateException {

        if (_transformerFactory == null) {
            try {
                _transformerFactory = TransformerFactory.newInstance();
            } catch (TransformerFactoryConfigurationError tfce) {
                throw new IllegalStateException("XmlFactory.getTransformerFactory : failure on TransformerFactory initialisation : ", tfce);
            }
        }

        return _transformerFactory;
    }

    /**
     * Returns a new XSLT template (precompiled XSLT script) for the given XSLT source.
     *
     * @param source stream source for XSLT script.
     *
     * @return new XSLT template.
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed.
     */
    private static Templates newTemplate(final StreamSource source)
            throws IllegalStateException {

        try {
            return getTransformerFactory().newTemplates(source);
        } catch (TransformerConfigurationException tce) {
            throw new IllegalStateException("XmlFactory.newTransformer : failure on creating new template : " + source, tce);
        }
    }

    /**
     * Returns a transformer for the given XSLT template (precompiled XSLT script).
     *
     * @param tmp XSLT template (precompiled XSLT script).
     *
     * @return transformer for the given XSLT template.
     *
     * @throws IllegalStateException if transformer creation failed.
     */
    private static Transformer newTransformer(final Templates tmp)
            throws IllegalStateException {

        try {
            return getOutTransformer(tmp.newTransformer());
        } catch (TransformerConfigurationException tce) {
            throw new IllegalStateException("XmlFactory.newTransformer : failure on creating new Transformer for template : " + tmp, tce);
        }
    }

    /**
     * Returns a transformer for the given XSLT source
     *
     * @param source stream source for XSLT script
     *
     * @return transformer for the given XSLT source
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or transformer creation failed
     */
    private static Transformer newTransformer(final StreamSource source)
            throws IllegalStateException {

        try {
            return getOutTransformer(getTransformerFactory().newTransformer(source));
        } catch (TransformerConfigurationException tce) {
            throw new IllegalStateException("XmlFactory.newTransformer : failure on creating new Transformer for source : " + source, tce);
        }
    }

    /**
     * Sets the encoding and indentation parameters for the given transformer
     *
     * @param tf transformer
     *
     * @return tf transformer
     */
    private static Transformer getOutTransformer(final Transformer tf) {
        tf.setOutputProperty(OutputKeys.ENCODING, ENCODING);
        tf.setOutputProperty(OutputKeys.INDENT, "yes");

        return tf;
    }

    /**
     * Process XSLT on XML document (using XSLT cache)
     *
     * @param xmlSource XML content to transform
     * @param xslFilePath XSL file to use (XSLT)
     *
     * @return result document as string
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or IO failure
     */
    public static String transform(final String xmlSource, final String xslFilePath)
            throws IllegalStateException, IllegalArgumentException {

        return transform(xmlSource, xslFilePath, null, true);
    }

    /**
     * Process XSLT on XML document with parameters (using XSLT cache)
     *
     * @param xmlSource XML content to transform
     * @param xslFilePath XSL file to use (XSLT)
     * @param params parameters for transformation
     *
     * @return result document as string
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or IO failure
     */
    public static String transform(final String xmlSource, final String xslFilePath, final Map<String, Object> params)
            throws IllegalStateException, IllegalArgumentException {

        return transform(xmlSource, xslFilePath, params, true);
    }

    /**
     * Process XSLT on XML document.
     *
     * @param xmlSource XML content to transform
     * @param xslFilePath XSL file to use (XSLT)
     * @param doCacheXsl true indicates that XSLT can be keep in permanent cache for reuse (avoid a lot of wasted time
     *        (compiling XSLT) for many transformations)
     *
     * @return result document as string
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or IO failure
     */
    public static String transform(final String xmlSource, final String xslFilePath, final boolean doCacheXsl)
            throws IllegalStateException, IllegalArgumentException {

        final StringWriter out = new StringWriter(DEFAULT_BUFFER_SIZE);

        transform(xmlSource, xslFilePath, null, doCacheXsl, out);

        return out.toString();
    }

    /**
     * Process XSLT on XML document with parameters
     *
     * @param xmlSource XML content to transform
     * @param xslFilePath XSL file to use (XSLT)
     * @param params optional parameters for transformation
     * @param doCacheXsl true indicates that XSLT can be keep in permanent cache for reuse (avoid a lot of wasted time
     *        (compiling XSLT) for many transformations)
     *
     * @return result document as string
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or IO failure
     */
    public static String transform(final String xmlSource, final String xslFilePath, final Map<String, Object> params, final boolean doCacheXsl)
            throws IllegalStateException, IllegalArgumentException {

        final StringWriter out = new StringWriter(DEFAULT_BUFFER_SIZE);

        transform(xmlSource, xslFilePath, params, doCacheXsl, out);

        return out.toString();
    }

    /**
     * Process XSLT on XML document (using XSLT cache)
     *
     * @param sourceStream XML source to transform as stream
     * @param xslFilePath XSL file to use (XSLT)
     * @param resultStream transform result as stream
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    public static void transform(final InputStream sourceStream, final String xslFilePath,
            final OutputStream resultStream)
            throws IllegalStateException, IllegalArgumentException {

        transform(new StreamSource(sourceStream), xslFilePath, null, true, new StreamResult(resultStream));
    }

    /**
     * Process XSLT on XML document with parameters (using XSLT cache)
     *
     * @param sourceStream XML source to transform as stream
     * @param xslFilePath XSL file to use (XSLT)
     * @param params optional parameters for transformation 
     * @param resultStream transform result as stream
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    public static void transform(final InputStream sourceStream, final String xslFilePath, final Map<String, Object> params,
            final OutputStream resultStream)
            throws IllegalStateException, IllegalArgumentException {

        transform(new StreamSource(sourceStream), xslFilePath, params, true, new StreamResult(resultStream));
    }

    /**
     * Process XSLT on XML document
     *
     * @param sourceStream XML source to transform as stream
     * @param xslFilePath XSL file to use (XSLT)
     * @param doCacheXsl true indicates that XSLT can be keep in permanent cache for reuse (avoid a lot of wasted time
     *        (compiling XSLT) for many transformations)
     * @param resultStream transform result as stream
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    public static void transform(final InputStream sourceStream, final String xslFilePath,
            final OutputStream resultStream, final boolean doCacheXsl)
            throws IllegalStateException, IllegalArgumentException {

        transform(new StreamSource(sourceStream), xslFilePath, null, doCacheXsl, new StreamResult(resultStream));
    }

    /**
     * Process XSLT on XML document  with parameters
     *
     * @param sourceStream XML source to transform as stream
     * @param xslFilePath XSL file to use (XSLT)
     * @param params optional parameters for transformation
     * @param doCacheXsl true indicates that XSLT can be keep in permanent cache for reuse (avoid a lot of wasted time
     *        (compiling XSLT) for many transformations)
     * @param resultStream transform result as stream
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    public static void transform(final InputStream sourceStream, final String xslFilePath, final Map<String, Object> params,
            final OutputStream resultStream, final boolean doCacheXsl)
            throws IllegalStateException, IllegalArgumentException {

        transform(new StreamSource(sourceStream), xslFilePath, params, doCacheXsl, new StreamResult(resultStream));
    }

    /**
     * Process XSLT on XML document
     *
     * @param xmlSource XML content to transform
     * @param xslFilePath XSL file to use (XSLT)
     * @param params optional parameters for transformation 
     * @param doCacheXsl true indicates that XSLT can be keep in permanent cache for reuse (avoid a lot of wasted time
     *        (compiling XSLT) for many transformations)
     * @param out buffer (should be cleared before method invocation)
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    private static void transform(final String xmlSource, final String xslFilePath, final Map<String, Object> params,
            final boolean doCacheXsl, final Writer out)
            throws IllegalStateException, IllegalArgumentException {

        logger.debug("XmlFactory.transform : enter : xslFilePath : {}", xslFilePath);

        if ((xmlSource != null) && (xslFilePath != null)) {
            final Transformer tf;

            if (doCacheXsl) {
                tf = loadXsl(xslFilePath);
            } else {
                tf = newTransformer(resolveXSLTPath(xslFilePath));
            }

            // Handle params
            addParams(tf, params);

            logger.debug("XmlFactory.transform : XML Source : {}", xmlSource);

            asString(tf, new StreamSource(new StringReader(xmlSource)), out);
        }

        logger.debug("XmlFactory.transform : exit : {}", out);
    }

    /**
     * Process XSLT on XML document
     *
     * @param source XML source to transform
     * @param xslFilePath XSL file to use (XSLT)
     * @param params optional parameters for transformation 
     * @param doCacheXsl true indicates that XSLT can be keep in permanent cache for reuse (avoid a lot of wasted time
     *        (compiling XSLT) for many transformations)
     * @param result transform result
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if transformation failure or the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    private static void transform(final Source source, final String xslFilePath, final Map<String, Object> params,
            final boolean doCacheXsl, final Result result)
            throws IllegalStateException, IllegalArgumentException {

        logger.debug("XmlFactory.transform : enter : xslFilePath : {}", xslFilePath);

        if ((source != null) && (xslFilePath != null)) {
            final Transformer tf;

            if (doCacheXsl) {
                tf = loadXsl(xslFilePath);
            } else {
                tf = newTransformer(resolveXSLTPath(xslFilePath));
            }

            // Handle params
            addParams(tf, params);

            logger.debug("XmlFactory.transform : XML Source : {}", source);

            try {
                tf.transform(source, result);
            } catch (TransformerException te) {
                throw new IllegalArgumentException("XmlFactory.transform : transformer failure :", te);
            }
        }

        logger.debug("XmlFactory.transform : exit : {}", result);
    }

    /**
     * Load an XSLT using template cache
     *
     * @param xslFilePath XSL file to use (XSLT)
     *
     * @return transformer or null if file does not exist
     *
     * @throws IllegalStateException if TransformerFactory initialization failed or template creation failed or transformer creation failed
     * @throws IllegalArgumentException if the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    private static Transformer loadXsl(final String xslFilePath)
            throws IllegalStateException, IllegalArgumentException {

        if (StringUtils.isEmpty(xslFilePath)) {
            throw new IllegalArgumentException("XmlFactory.resolvePath : unable to load XSLT : empty file path !");
        }

        Templates tmp = _cachedTemplates.get(xslFilePath);

        if (tmp == null) {
            tmp = newTemplate(resolveXSLTPath(xslFilePath));

            _cachedTemplates.put(xslFilePath, tmp);

            if (logger.isDebugEnabled()) {
                logger.debug("XmlFactory.loadXsl : template: {}", Integer.toHexString(tmp.hashCode()));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("XmlFactory.loadXsl : template in cache: {}", Integer.toHexString(tmp.hashCode()));
        }

        final Transformer tf = newTransformer(tmp);

        logger.debug("XmlFactory.loadXsl : XSLT : {}", tf);

        return tf;
    }

    /**
     * Resolve the XSL file path using the class loader
     *
     * @param xslFilePath XSL file to use (XSLT)
     *
     * @return StreamSource instance
     *
     * @throws IllegalStateException if the file is not found
     * @throws IllegalArgumentException if the XSL file path is empty or I/O exception occurs while reading XSLT
     */
    private static StreamSource resolveXSLTPath(final String xslFilePath)
            throws IllegalStateException, IllegalArgumentException {

        if (StringUtils.isEmpty(xslFilePath)) {
            throw new IllegalArgumentException("XmlFactory.resolveXSLTPath : unable to load XSLT : empty file path !");
        }

        final URL url = ResourceUtils.getResource(xslFilePath);

        logger.debug("XmlFactory.resolveXSLTPath : url : {}", url);

        try {
            return new StreamSource(new BufferedInputStream(url.openStream()));
        } catch (IOException ioe) {
            throw new IllegalArgumentException("XmlFactory.resolveXSLTPath : unable to load the XSLT file : " + xslFilePath, ioe);
        }
    }

    /**
     * Converts source XML document into the out writer with given transformer
     *
     * @param transformer XSL transformer to use
     * @param source XML document
     * @param out buffer (should be cleared before method invocation)
     *
     * @throws IllegalArgumentException if transformation failure
     */
    private static void asString(final Transformer transformer, final Source source, final Writer out)
            throws IllegalArgumentException {
        try {
            transformer.transform(source, new StreamResult(out));
        } catch (TransformerException te) {
            throw new IllegalArgumentException("XmlFactory.asString : transformer failure :", te);
        }
    }

    /**
     * Add parameters to the transformer if any
     * @param transformer the transformer
     * @param params the optional parameter map     
     */
    private static void addParams(final Transformer transformer, final Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
                transformer.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }
    }
}
