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

import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a function to open a web page with the default web browser of the user system.
 *
 * It uses <b>BrowserLauncher</b>.
 * 
 * @author Brice COLUCCI, Laurent BOURGES.
 */
public final class BrowserLauncher {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(BrowserLauncher.class.getName());
    /** system property "mrj.version" */
    private static final String PROP_MRJ_VERSION = "mrj.version";
    /** launcher instance */
    private static edu.stanford.ejalbert.BrowserLauncher _launcher = null;

    /**
     * Return the BrowserLauncher instance
     * @return BrowserLauncher instance
     */
    private static edu.stanford.ejalbert.BrowserLauncher getLauncher() {
        if (_launcher == null) {
            boolean hackMRJ = false;
            if (SystemUtils.IS_OS_MAC_OSX) {
                // Circumvent BrowserLauncher NPE when running under Oracle 1.7u4 (and later ???) release for Mac OS X
                if (System.getProperty(PROP_MRJ_VERSION) == null) {
                    hackMRJ = true;
                    System.setProperty(PROP_MRJ_VERSION, "9999.999");
                    _logger.debug("Probably running Oracle JVM under Mac OS X, faking missing 'mrj.version' system property.");
                }
            }
            try {
                _launcher = new edu.stanford.ejalbert.BrowserLauncher();
            } catch (UnsupportedOperatingSystemException uose) {
                _logger.warn("Cannot initialize browser launcher : ", uose);
            } catch (BrowserLaunchingInitializingException bie) {
                _logger.warn("Cannot initialize browser launcher : ", bie);
            } finally {
                if (hackMRJ) {
                    // Circumvent BrowserLauncher NPE when running under Oracle 1.7u4 (and later ???) release for Mac OS X
                    System.clearProperty(PROP_MRJ_VERSION);
                }
            }
        }
        return _launcher;
    }

    /**
     * Open the given URL in the default web browser.
     *
     * @param url URL to open in web browser.
     */
    public static void openURL(String url) {
        final edu.stanford.ejalbert.BrowserLauncher launcher = getLauncher();
        if (launcher == null) {
            _logger.warn("Cannot open '{}' in web browser", url);
        } else {
            launcher.openURLinBrowser(url);
            _logger.debug("URL '{}' opened in web browser", url);
        }
    }

    /**
     * Private constructor
     */
    private BrowserLauncher() {
        super();
    }
}
