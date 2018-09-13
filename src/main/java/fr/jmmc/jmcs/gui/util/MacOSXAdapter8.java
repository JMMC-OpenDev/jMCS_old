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
package fr.jmmc.jmcs.gui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mac OS X adapter.
 * 
 * @author Brice COLUCCI, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class MacOSXAdapter8 {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(MacOSXAdapter8.class.getName());

    /**
     * Register the given adapter com.apple.eawt.Application (java &lt;= 8)
     * @param instance MacOSXInterface implementation
     * @param usePreferences true to register Preferences handler
     */
    public static void registerMacOSXApplication(final MacOSXInterface instance, final boolean usePreferences) {
        // Get Application instance:
        // May throw exceptions on non-mac or JVM >= 9
        final com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();

        if (application == null) {
            _logger.warn("com.apple.eawt.Application is null");
            return;
        }

        // Link 'About...' menu entry
        application.setAboutHandler(new com.apple.eawt.AboutHandler() {
            @Override
            public void handleAbout(final com.apple.eawt.AppEvent.AboutEvent ae) {
                instance.handleAbout();
            }
        });

        // Set up quitting behaviour
        application.setQuitHandler(new com.apple.eawt.QuitHandler() {
            @Override
            public void handleQuitRequestWith(final com.apple.eawt.AppEvent.QuitEvent qe,
                                              final com.apple.eawt.QuitResponse qr) {

                instance.handleQuitRequestWith(new MacOSXQuitCallback() {
                    @Override
                    public void performQuit() {
                        qr.performQuit();
                    }

                    @Override
                    public void cancelQuit() {
                        qr.cancelQuit();
                    }
                });
            }
        });
        application.disableSuddenTermination();
        application.setQuitStrategy(com.apple.eawt.QuitStrategy.SYSTEM_EXIT_0);

        // Set up double-clicked file opening handler
        application.setOpenFileHandler(new com.apple.eawt.OpenFilesHandler() {
            @Override
            public void openFiles(final com.apple.eawt.AppEvent.OpenFilesEvent ofe) {
                instance.openFiles(ofe.getFiles());
            }
        });

        // Link 'Preferences' menu entry (if any)
        com.apple.eawt.PreferencesHandler preferencesHandler = null;
        if (usePreferences) {
            preferencesHandler = new com.apple.eawt.PreferencesHandler() {
                @Override
                public void handlePreferences(final com.apple.eawt.AppEvent.PreferencesEvent pe) {
                    instance.handlePreferences();
                }
            };
        }
        application.setPreferencesHandler(preferencesHandler);
    }

    private MacOSXAdapter8() {
    }

}
