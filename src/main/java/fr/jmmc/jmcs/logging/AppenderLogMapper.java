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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;

/**
 * Simple mapping between logger and our custom ByteArrayOutputStreamAppender
 * 
 * Note: this class is only used in this package (inaccessible from outside)
 *
 * @author Laurent BOURGES.
 */
final class AppenderLogMapper {

    /** display name */
    private final String _displayName;
    /** logger path */
    private final String _loggerPath;
    /** logger */
    private final org.slf4j.Logger _logger;
    /** Logback appender which keeps log content */
    private final ByteArrayOutputStreamAppender _logAppender;

    /**
     * Constructor
     * @param displayName display name
     * @param loggerPath logger path
     * @param appenderName appender name
     */
    AppenderLogMapper(final String displayName, final String loggerPath, final String appenderName) {
        _displayName = displayName;
        _loggerPath = loggerPath;

        // Try to get the root logger (logback):
        ch.qos.logback.classic.Logger loggerImpl = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(_loggerPath);

        LoggerFactory.getLogger(LoggingService.JMMC_STATUS_LOG);

        // Get the root logger's appender 'APPLOG':
        final Appender<ILoggingEvent> appender = loggerImpl.getAppender(appenderName);
        // Check if this appender has the correct type (ByteArrayOutputStreamAppender):
        if ((appender != null) && !(appender instanceof ByteArrayOutputStreamAppender)) {
            throw new IllegalStateException("Bad class type [" + appender.getClass() + " - "
                    + ByteArrayOutputStreamAppender.class + "expected] for appender [" + appenderName + "] attached to the " + _loggerPath + " logger !");
        }
        if (appender != null) {
            // use this appender:
            _logAppender = (ByteArrayOutputStreamAppender) appender;
        } else {
            throw new IllegalStateException("Missing appender [" + appenderName + "] attached to the " + _loggerPath + " logger !");
        }

        _logger = loggerImpl;
    }

    /**
     * Return the display name
     * @return display name
     */
    String getDisplayName() {
        return _displayName;
    }

    /**
     * Return the logger path
     * @return logger path
     */
    String getLoggerPath() {
        return _loggerPath;
    }

    /**
     * Return the logger
     * @return logger
     */
    org.slf4j.Logger getLogger() {
        return _logger;
    }

    /**
     * Return the Logback appender which keeps log content
     * @return Logback appender which keeps log content
     */
    ByteArrayOutputStreamAppender getLogAppender() {
        return _logAppender;
    }
}
