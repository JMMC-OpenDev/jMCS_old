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

import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAXBFactory is an utility class to configure JAXB context &amp; properties
 *
 * @author Laurent BOURGES.
 */
public final class JAXBFactory {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(JAXBFactory.class.getName());
    /** all factories */
    private static final ConcurrentHashMap<String, JAXBFactory> managedInstances = new ConcurrentHashMap<String, JAXBFactory>(4);

    /** JAXB implementation 2.3.0 provided in JMCS libraries */
    public static final String JAXB_CONTEXT_FACTORY_IMPLEMENTATION = "com.sun.xml.bind.v2.ContextFactory";

    static {
        // Define the system property to define which JAXB implementation to use:
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, JAXB_CONTEXT_FACTORY_IMPLEMENTATION);

        logger.info("JAXB ContextFactory: {}", System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY));
    }

    // Members
    /** JAXB context path : used to find a factory */
    private final String _jaxbPath;
    /** JAXB Context for the given JAXB context path */
    private JAXBContext _jaxbContext = null;

    /**
     * Creates a new JPAFactory object
     *
     * @param pJaxbPath JAXB context path
     */
    private JAXBFactory(final String pJaxbPath) {
        _jaxbPath = pJaxbPath;
    }

    //~ Methods ----------------------------------------------------------------------------------------------------------
    /**
     * Factory singleton per JAXB-context-path pattern
     *
     * @param jaxbPath JAXB context path
     *
     * @return JAXBFactory initialized
     *
     * @throws XmlBindException if a JAXBException was caught
     */
    public static JAXBFactory getInstance(final String jaxbPath) throws XmlBindException {
        JAXBFactory factory = managedInstances.get(jaxbPath);

        if (factory == null) {
            factory = new JAXBFactory(jaxbPath);

            factory.initialize();

            managedInstances.putIfAbsent(jaxbPath, factory);
            // to be sure to return the singleton :
            factory = managedInstances.get(jaxbPath);
        }

        return factory;
    }

    /**
     * Initializes the JAXB Context
     *
     * @throws XmlBindException if a JAXBException was caught
     */
    private void initialize() throws XmlBindException {
        try {
            _jaxbContext = getContext(_jaxbPath);
        } catch (XmlBindException xbe) {
            logger.error("JAXBFactory.initialize: JAXB failure : ", xbe);
            throw xbe;
        }
    }

    /**
     * JAXBContext factory for a given path
     *
     * @param path given path
     * @return JAXBContext instance
     * @throws XmlBindException if a JAXBException was caught
     */
    private JAXBContext getContext(final String path) throws XmlBindException {
        JAXBContext context = null;

        try {
            // create a JAXBContext capable of handling classes generated into
            // package given by path:
            context = JAXBContext.newInstance(path, JAXBFactory.class.getClassLoader());

            if (logger.isInfoEnabled()) {
                if (context instanceof com.sun.xml.bind.v2.runtime.JAXBContextImpl) {
                    logger.info("JAXBContext[{}] Version: {}", path, ((com.sun.xml.bind.v2.runtime.JAXBContextImpl) context).getBuildId());
                } else {
                    logger.info("JAXBContext[{}] unknown implementation: {}", path, context.getClass().getName());
                }
            }

        } catch (JAXBException je) {
            // Can happen if either JAXB library can not be loaded (classloader) or if ObjectFactory can not be loaded (classloader)
            // put stack trace in netbeans IDE logs:
            je.printStackTrace(System.err);
            System.err.println("ClassLoader (Jmcs): " + JAXBFactory.class.getClassLoader());
            System.err.println("ClassLoader (thread): " + Thread.currentThread().getContextClassLoader());

            throw new XmlBindException("JAXBFactory.getContext: Unable to create JAXBContext : " + path, je);
        }

        return context;
    }

    /**
     * Returns JAXB Context for the given JAXB context path
     *
     * @return JAXB Context for the given JAXB context path
     */
    private JAXBContext getJAXBContext() {
        return _jaxbContext;
    }

    /**
     * Creates a JAXB Unmarshaller
     *
     * @return JAXB Unmarshaller
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    public Unmarshaller createUnMarshaller() throws XmlBindException {
        Unmarshaller unmarshaller = null;

        try {
            // create an Unmarshaller
            unmarshaller = getJAXBContext().createUnmarshaller();

        } catch (JAXBException je) {
            throw new XmlBindException("JAXBFactory.createUnMarshaller: JAXB Failure", je);
        }

        return unmarshaller;
    }

    /**
     * Creates a JAXB Marshaller
     *
     * @return JAXB Marshaller
     * @throws XmlBindException if a JAXBException was caught while creating an marshaller
     */
    public Marshaller createMarshaller() throws XmlBindException {
        Marshaller marshaller = null;

        try {
            // create an Unmarshaller
            marshaller = getJAXBContext().createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (JAXBException je) {
            throw new XmlBindException("JAXBFactory.createMarshaller: JAXB Failure", je);
        }

        return marshaller;
    }
}
