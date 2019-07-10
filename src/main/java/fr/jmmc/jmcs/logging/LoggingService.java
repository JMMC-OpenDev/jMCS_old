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
package fr.jmmc.jmcs.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import fr.jmmc.jmcs.util.ResourceUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This class keeps into memory the full application log using the LogBack root logger 
 * and a custom appender to store log messages into one ByteBuffer (memory).
 * 
 * Usage: automatically initialized by the Bootstrapper
 * 
 * @author Laurent BOURGES, Sylvain LAFRASSE.
 */
public final class LoggingService {

    /** Singleton instance */
    private volatile static LoggingService _instance = null;
    /** JMMC status log */
    public final static String JMMC_STATUS_LOG = "fr.jmmc.jmcs.status";
    /** JMMC main logger */
    private final static String JMMC_LOGGER = "fr.jmmc";
    /** JMMC LogBack configuration file as one resource file (in class path) */
    private final static String JMMC_LOGBACK_CONFIG_RESOURCE = "fr/jmmc/jmcs/resource/DefaultLogbackConfiguration.xml";
    /** JMMC application log */
    public final static String JMMC_APP_LOG = Logger.ROOT_LOGGER_NAME;

    /**
     * Get the singleton instance or create a new one if needed
     * @return singleton instance
     */
    public static LoggingService getInstance() {
        return getInstance(true);
    }

    /**
     * Get the singleton instance or create a new one if needed
     * @param createMappers true to create log mappers
     * @return singleton instance
     */
    public static synchronized LoggingService getInstance(final boolean createMappers) {
        if (_instance == null) {
            init();
            _instance = new LoggingService(createMappers);
        }
        return _instance;
    }

    /** @return the the JMMC logger (top level). */
    public static Logger getJmmcLogger() {
        return LoggerFactory.getLogger(JMMC_LOGGER);
    }

    /**
     * Set the logger's level (if it is a logback logger) given its name
     * @param loggerName logger name
     * @param level Logback level
     */
    public static void setLoggerLevel(final String loggerName, ch.qos.logback.classic.Level level) {
        setLoggerLevel(LoggerFactory.getLogger(loggerName), level);
    }

    /**
     * Set given logger's level (if it is a logback logger)
     * @param logger slf4j logger
     * @param level Logback level
     */
    public static void setLoggerLevel(Logger logger, ch.qos.logback.classic.Level level) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) logger).setLevel(level);
        }
    }
    
    public static ch.qos.logback.classic.Level getLoggerEffectiveLevel(Logger logger) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            return ((ch.qos.logback.classic.Logger) logger).getEffectiveLevel();
        }
        return null;
    }

    /** slf4j / Logback initialization */
    private static void init() throws SecurityException, IllegalStateException {
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final URL logConf = ResourceUtils.getResource(JMMC_LOGBACK_CONFIG_RESOURCE);
            try {
                final JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(loggerContext);
                loggerContext.reset();
                configurator.doConfigure(logConf.openStream());
            } catch (IOException ioe) {
                throw new IllegalStateException("IO Exception occured", ioe);
            } catch (JoranException je) {
                StatusPrinter.printInCaseOfErrorsOrWarnings((LoggerContext) LoggerFactory.getILoggerFactory());
            }
        }

        // Remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();
    }
    // Members
    /** Mapper collection keyed by logger path */
    private final Map<String, AppenderLogMapper> _mappers = new LinkedHashMap<String, AppenderLogMapper>(8);

    /**
     * Private constructor that gets one ByteArrayOutputStreamAppender:
     * - from appenders attached to the root logger (joran configuration already done / logback.xml)
     * 
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     * <configuration> 
     * 
     * ...
     * 
     *   <!-- Appenders -->
     *     <appender name="APPLOG" class="fr.jmmc.jmcs.util.logging.ByteArrayOutputStreamAppender">
     *         <encoder>
     *             <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] %logger{60} - %msg%n</pattern>
     *         </encoder>
     *     </appender>
     * ...  
     * 
     *   <!-- Loggers -->
     * ...
     * 
     *   <!-- Root Logger -->
     *   <root level="INFO">
     *         <appender-ref ref="APPLOG" />
     *         ...
     *   </root>
     *  
     * </configuration>
     * 
     * - created here and attached to the root logger
     * @param createMappers true to create log mappers
     */
    private LoggingService(final boolean createMappers) {
        super();

        if (createMappers) {
            // define Log Mappers:
            addLogMapper("Status history", JMMC_STATUS_LOG, "STATUSLOG");
            addLogMapper("Execution log", JMMC_APP_LOG, "APPLOG");
        }
    }

    /**
     * Return the complete application log as string (THREAD SAFE)
     * @return complete log output
     */
    public LogOutput getLogOutput() {
        return getLogOutput(0);
    }

    /**
     * Return the partial application log as string starting at the given argument from (THREAD SAFE)
     * @param from gives the position in the buffer to copy from
     * @return partial log output
     */
    public LogOutput getLogOutput(final int from) {
        return getLogOutput(JMMC_APP_LOG, from);
    }

    /**
     * Return the partial log for the given logger path as string starting at the given argument from (THREAD SAFE)
     * @param loggerPath logger path
     * @param from gives the position in the buffer to copy from
     * @return partial log output
     */
    public LogOutput getLogOutput(final String loggerPath, final int from) {
        return getLogMapper(loggerPath).getLogAppender().getLogOutput(from);
    }

    /**
     * Return the logger for the given logger path
     * @param loggerPath logger path
     * @return logger for the given logger path
     */
    public Logger getLogger(final String loggerPath) {
        return getLogMapper(loggerPath).getLogger();
    }

    /**
     * Register a new log given its attributes (logger path, appender name)
     * @param displayName display name
     * @param loggerPath logger path
     * @param appenderName appender name
     */
    public void addLogMapper(final String displayName, final String loggerPath, final String appenderName) {
        final AppenderLogMapper appenderLogMapper = new AppenderLogMapper(displayName, loggerPath, appenderName);
        _mappers.put(loggerPath, appenderLogMapper);
    }

    /**
     * Return the ordered collection of Log mappers
     * @return collection of Log mappers
     */
    Collection<AppenderLogMapper> getLogMappers() {
        return _mappers.values();
    }

    /**
     * Return the mapper associated to the given logger path
     * @param loggerPath logger path
     * @return mapper associated to the given logger path or null
     * @throws IllegalStateException if the logger path is not present in the mappers collection
     */
    private AppenderLogMapper getLogMapper(final String loggerPath) throws IllegalStateException {
        final AppenderLogMapper mapper = _mappers.get(loggerPath);
        if (mapper == null) {
            throw new IllegalStateException("Unsupported logger [" + loggerPath + "]");
        }
        return mapper;
    }
}
