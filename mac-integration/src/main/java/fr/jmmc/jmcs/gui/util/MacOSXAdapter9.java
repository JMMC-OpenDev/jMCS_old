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

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.desktop.QuitStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mac OS X adapter.
 * 
 * @author Brice COLUCCI, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class MacOSXAdapter9 {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(MacOSXAdapter9.class.getName());

    /**
     * Register the given adapter using java.awt.Desktop (java 9+)
     * @param instance MacOSXInterface implementation
     * @param usePreferences true to register Preferences handler
     */
    public static void registerMacOSXApplication(final MacOSXInterface instance, final boolean usePreferences) {
        // Get Desktop instance:
        final Desktop desktop = Desktop.getDesktop();

        if (desktop == null) {
            _logger.warn("java.awt.Desktop is null");
            return;
        }

        // May throw exceptions on non-mac or JVM < 9
        // Link 'About...' menu entry
        desktop.setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(final AboutEvent ae) {
                instance.handleAbout();
            }
        });

        // Set up quitting behaviour
        desktop.setQuitHandler(new QuitHandler() {
            @Override
            public void handleQuitRequestWith(final QuitEvent qe,
                                              final QuitResponse qr) {

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
        desktop.disableSuddenTermination();
        desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);

        // Set up double-clicked file opening handler
        desktop.setOpenFileHandler(new OpenFilesHandler() {
            @Override
            public void openFiles(final OpenFilesEvent ofe) {
                instance.openFiles(ofe.getFiles());
            }
        });

        // Link 'Preferences' menu entry (if any)
        PreferencesHandler preferencesHandler = null;
        if (usePreferences) {
            preferencesHandler = new PreferencesHandler() {
                @Override
                public void handlePreferences(final PreferencesEvent pe) {
                    instance.handlePreferences();
                }
            };
        }
        desktop.setPreferencesHandler(preferencesHandler);
    }

    private MacOSXAdapter9() {
    }

}
