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

import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.internal.InternalActionFactory;
import fr.jmmc.jmcs.util.IntrospectionUtils;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mac OS X adapter.
 * 
 * @author Brice COLUCCI, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class MacOSXAdapter implements MacOSXInterface {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(MacOSXAdapter.class.getName());
    /** enable / disable EDT fix (JDK 1.6.0_51 and 1.7.0_25) */
    private static final boolean _useEdtFix = true;
    /** enable / disable EDT fix debugging statements */
    private static final boolean _debugEdtFix = false;
    /** pseudo-singleton model; no point in making multiple instances */
    private static MacOSXAdapter _instance = null;
    /** Application's Event Dispatcher Thread (EDT) name */
    private static String _appEDTName = null;
    /* members */
    /** Store a proxy to the shared ActionRegistrar facility */
    private final ActionRegistrar _registrar;

    /**
     * Creates a new OSXAdapter object.
     */
    private MacOSXAdapter() {
        _registrar = ActionRegistrar.getInstance();
    }

    ActionRegistrar getRegistrar() {
        return _registrar;
    }

    /**
     * Register this adapter (should be performed by EDT)
     */
    public static void registerMacOSXApplication() {
        logEDT("registerMacOSXApplication");

        // Avoid reentrance:
        if (_instance != null) {
            return;
        }

        // ensure events are fired by Swing EDT:
        if (!SwingUtils.isEDT()) {
            throw new IllegalStateException("invalid thread : use EDT", new Throwable());
        }

        // MacOSX EDT Fix (JDK <= 7):
        if (_useEdtFix) {
            // Create EDT submitter from EDT within AppClassLoader (not JNLPClassLoader):
            invokeLaterUsingApplicationEDT(new Runnable() {
                @Override
                public void run() {
                    if (SwingUtils.isEDT()) {
                        setApplicationEDT(Thread.currentThread().getName());
                    }
                }
            });
        }

        // Create singleton once:
        _instance = new MacOSXAdapter();

        try {
            // Java 7/8 handlers:
            registerMacOSXAdapter(_instance, "MacOSXAdapter8");
        } catch (Throwable th) {
            _logger.info("registerMacOSXAdapter(8): failure", th);
            // Java 9 handlers
            registerMacOSXAdapter(_instance, "MacOSXAdapter9");
        }
    }

    /**
     * Register this adapter using java.awt.Desktop (java 9+)
     * @param instance MacOSXInterface implementation
     * @param MacOSXAdapterName MacOSXAdapter class name (8 or 9)
     */
    private static void registerMacOSXAdapter(final MacOSXAdapter instance, final String MacOSXAdapterName) {
        boolean done = false;

        final Class<?> osxAdapter = IntrospectionUtils.getClass("fr.jmmc.jmcs.gui.util." + MacOSXAdapterName);
        if (osxAdapter == null) {
            // This will be thrown first if the MacOSXAdapter is loaded on a system without required (apple or java9 classes)
            _logger.info("This version of Mac OS X does not support the jmcs [" + MacOSXAdapterName + "] integration.");
        } else {
            final Method registerMethod = IntrospectionUtils.getMethod(osxAdapter, "registerMacOSXApplication",
                    new Class<?>[]{MacOSXInterface.class, boolean.class});

            if (registerMethod != null) {
                if (IntrospectionUtils.executeMethod(registerMethod,
                        new Object[]{instance, (instance.getRegistrar().getPreferenceAction() != null)})) {
                    done = true;
                }
            }
        }

        if (!done) {
            throw new IllegalStateException("Unable to register " + MacOSXAdapterName + " integration !");
        }
    }

    /**
     * Handle about action 
     */
    @Override
    public void handleAbout() {
        invokeLaterUsingApplicationEDT(new Runnable() {
            @Override
            public void run() {
                logEDT("handleAbout");
                InternalActionFactory.showAboutBoxAction().actionPerformed(null);
            }
        });
    }

    /** 
     * Show the user preferences
     */
    @Override
    public void handlePreferences() {
        final AbstractAction preferenceAction = _registrar.getPreferenceAction();
        if (preferenceAction != null) {
            invokeLaterUsingApplicationEDT(new Runnable() {
                @Override
                public void run() {
                    logEDT("handlePreferences");
                    preferenceAction.actionPerformed(null);
                }
            });
        }
    }

    /** 
     * Handle quit action 
     * @param callback quit callback
     */
    @Override
    public void handleQuitRequestWith(final MacOSXQuitCallback callback) {
        /* This is important for cross-platform development -- have a universal quit
         * routine that chooses whether or not to quit, so the functionality is identical
         * on all platforms.  This example simply cancels the AppleEvent-based quit and
         * defers to that universal method. */
        invokeLaterUsingApplicationEDT(new Runnable() {
            @Override
            public void run() {
                logEDT("handleQuitRequestWith");

                /*
                 * the Quit action must call response.cancelQuit() or response.performQuit()
                 * Note: QuitResponse is thread safe and methods can be called after handleQuitRequestWith() returns.
                 */
                _registrar.getQuitAction().actionPerformed(new ActionEvent(callback, 0, null));
            }
        });
    }

    /** 
     * Handle the open action 
     * @param files files to open
     */
    @Override
    public void openFiles(final List<File> files) {
        final int FIRST_FILE_INDEX = 0;

        final String firstFilePath = files.get(FIRST_FILE_INDEX).getAbsolutePath();
        if (_logger.isInfoEnabled()) {
            _logger.info("Should open '{}' file.", firstFilePath);
        }

        invokeLaterUsingApplicationEDT(new Runnable() {
            @Override
            public void run() {
                logEDT("openFiles");
                _registrar.getOpenAction().actionPerformed(new ActionEvent(_registrar, 0, firstFilePath));
            }
        });
    }

    /* --- EDT Fix ---------------------------------------------------------- */
    /**
     * Get a single thread executor dedicated to EDT runnable submission
     * @return single thread executor
     */
    private static ThreadExecutors getEDTSubmitter() {
        /*
         * Note: This pool is not stopped during shutdown: avoid 10s delay before quit:
         * stopping thread calling Bootstrapper.quitApp() > Bootstrapper.stopApp() 
         * > Bootstrapper.___internalStop() > LocalLauncher.shutdown() > ThreadExecutors.stopExecutors()
         */
        return ThreadExecutors.getSingleExecutor("JmcsEDTSubmitter", false);
    }

    private static void invokeLaterUsingApplicationEDT(final Runnable runnable) {
        // If Current thread is not application EDT 
        // => use the EDT submitter to call invokeLater() ie transfer to the real application EDT
        if (_useEdtFix && !isApplicationEDT()) {
            getEDTSubmitter().submit(new Runnable() {
                @Override
                public void run() {
                    SwingUtils.invokeAndWaitEDT(runnable);
                }
            });
        } else {
            // current Thread is EDT, simply execute runnable:
            runnable.run();
        }
    }

    private static boolean isApplicationEDT() {
        final boolean isMainEdt = Thread.currentThread().getName().equals(_appEDTName);
        if (_debugEdtFix) {
            _logger.info("isApplicationEDT: thread {} : {}", Thread.currentThread(), isMainEdt);
        }
        return isMainEdt;
    }

    private static void setApplicationEDT(final String name) {
        _appEDTName = name;
        if (_debugEdtFix) {
            _logger.info("Application EDT: {}", _appEDTName);
        }
    }

    private static void logEDT(final String caller) {
        if (_debugEdtFix) {
            _logger.info("{}() invoked from thread {} classLoader {}", caller, Thread.currentThread(), Thread.currentThread().getContextClassLoader());
        }
    }
}
