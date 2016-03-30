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

import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides Java 5 Thread uncaught exception handlers
 *
 * see http://stuffthathappens.com/blog/2007/10/07/programmers-notebook-uncaught-exception-handlers/
 *
 * see http://stuffthathappens.com/blog/2007/10/15/one-more-note-on-uncaught-exception-handlers/
 *
 * JNLP issues :
 * - Thread.defaultUncaughtExceptionHandler never used
 * => do not use this default UncaughtExceptionHandler to have the same behavior using standard java runtime
 *
 * - main thread (starting the application) use a general try/catch (throwable) and opens a JNLP error dialog
 * => do not set the UncaughtExceptionHandler to this thread
 * => Be sure to catch all exceptions in main() and use the feedback report manually
 * 
 * @author Laurent BOURGES.
 */
public final class MCSExceptionHandler {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(MCSExceptionHandler.class.getName());
    /** flag indicating to use the default UncaughtExceptionHandler (true for JDK 1.7.0) */
    private static final boolean USE_DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = true;
    /** flag indicating to set the UncaughtExceptionHandler to the current thread (main) (false because of JNLP) */
    private static final boolean SET_HANDLER_TO_CURRENT_THREAD = false;
    /** counter for OutOfMemoryError to avoid reporting too many */
    private static final AtomicInteger COUNT_OOME = new AtomicInteger();
    /** flag indicating that Swing is enabled (StatusBar can be used) */
    private static boolean SWING_ENABLED = false;
    /** uncaughtException handler singleton */
    private static volatile Thread.UncaughtExceptionHandler EXCEPTION_HANDLER = null;

    /**
     * Public method to initialize the exception handler singleton with the LoggingExceptionHandler
     */
    public static void installLoggingHandler() {
        setExceptionHandler(new LoggingExceptionHandler());
    }
    
    /**
     * Public method to initialize the exception handler singleton with the ExitExceptionHandler
     */
    public static void installExitExceptionHandler() {
        setExceptionHandler(new ExitExceptionHandler());
    }

    /**
     * Public method to initialize the exception handler singleton with the SwingExceptionHandler
     */
    public static void installSwingHandler() {
        // Requires SecurityManager disabled: @see Bootstrapper.disableSecurityManager()

        // AWT exception handler for modal dialogs :
        System.setProperty("sun.awt.exception.handler", MCSExceptionHandler.class.getName());

        setExceptionHandler(new SwingExceptionHandler());
    }

    /**
     * Public method to apply the exception handler singleton to the given thread
     * @param thread thread to use
     */
    public static void installThreadHandler(final Thread thread) {
        final Thread.UncaughtExceptionHandler handler = getExceptionHandler();
        if (handler != null) {
            applyUncaughtExceptionHandler(thread, handler);
        } else {
            _logger.debug("No UncaughtExceptionHandler defined !");
        }
    }

    /**
     * Execute the exception handler with the given throwable
     * @param th throwable (exception, error)
     */
    public static void runExceptionHandler(final Throwable th) {
        final Thread.UncaughtExceptionHandler handler = getExceptionHandler();
        if (handler != null) {
            handler.uncaughtException(Thread.currentThread(), th);
        } else {
            _logger.debug("No UncaughtExceptionHandler defined !");
        }
    }

    /**
     * Return the exception handler singleton
     * @return exception handler singleton or null if undefined
     */
    private static Thread.UncaughtExceptionHandler getExceptionHandler() {
        return EXCEPTION_HANDLER;
    }

    /**
     * Define the exception handler singleton and apply it to the JVM.
     * If the singleton is already defined, this method has no effect.
     *
     * @see #applyUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
     *
     * @param handler handler to set
     */
    private static synchronized void setExceptionHandler(final Thread.UncaughtExceptionHandler handler) {
        if (handler != null) {
            EXCEPTION_HANDLER = handler;
            SWING_ENABLED = (handler instanceof SwingExceptionHandler);

            applyUncaughtExceptionHandler(handler);
        }
    }

    /**
     * Apply the given UncaughtExceptionHandler to the JVM :
     * - define as default if USE_DEFAULT_UNCAUGHT_EXCEPTION_HANDLER is enabled
     * - define it to the current thread if SET_HANDLER_TO_CURRENT_THREAD is enabled
     * - define it to EDT if the given handler is a SwingExceptionHandler
     *
     * @see #applyUncaughtExceptionHandler(java.lang.Thread, java.lang.Thread.UncaughtExceptionHandler)
     *
     * @param handler handler to set
     */
    private static void applyUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler handler) {
        _logger.debug("New UncaughtExceptionHandler: {}", handler);

        if (USE_DEFAULT_UNCAUGHT_EXCEPTION_HANDLER) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Current Default UncaughtExceptionHandler: {}", Thread.getDefaultUncaughtExceptionHandler());
            }

            Thread.setDefaultUncaughtExceptionHandler(handler);

            if (_logger.isDebugEnabled()) {
                _logger.debug("Updated Default UncaughtExceptionHandler: {}", Thread.getDefaultUncaughtExceptionHandler());
            }
        }

        if (SET_HANDLER_TO_CURRENT_THREAD) {
            applyUncaughtExceptionHandler(Thread.currentThread(), handler);
        }

        // Set or reset the UncaughtExceptionHandler for EDT:
        try {
            // Using invokeAndWait to be in sync with this thread :
            // note: invokeAndWaitEDT throws an IllegalStateException if any exception occurs
            SwingUtils.invokeAndWaitEDT(new Runnable() {
                /**
                 * Add my handler to the Event-Driven Thread.
                 */
                @Override
                public void run() {
                    applyUncaughtExceptionHandler(Thread.currentThread(), handler);
                }
            });
        } catch (IllegalStateException ise) {
            _logger.error("exception occured: ", ise);
        }
    }

    /**
     * Define the UncaughtExceptionHandler to the given thread
     * @param thread thread to use
     * @param handler handler to set
     */
    private static void applyUncaughtExceptionHandler(final Thread thread, final Thread.UncaughtExceptionHandler handler) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Current Thread = {} in group = {}", thread, thread.getThreadGroup());
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Current Thread UncaughtExceptionHandler: {}", thread.getUncaughtExceptionHandler());
        }

        // Adding my handler to this thread (may be unnecessary) :
        thread.setUncaughtExceptionHandler(handler);

        if (_logger.isDebugEnabled()) {
            _logger.debug("Updated Thread UncaughtExceptionHandler: {}", thread.getUncaughtExceptionHandler());
        }
    }

    /**
     * Return true if the given exception must be ignored (filtered).
     * For example : ThreadDeath are ignored.
     * @param e
     * @return true if the given exception must be ignored
     */
    private static boolean isFilteredException(final Throwable e) {
        if (e instanceof ThreadDeath) {
            return true;
        }
        if (e instanceof OutOfMemoryError) {
            if (SWING_ENABLED) {
                StatusBar.show("OutOfMemoryError detected: the application may behave hazardously ...");
            }

            // count them:
            final int countOOME = COUNT_OOME.incrementAndGet();
            if (countOOME > 3) {
                // log it anyway:
                _logger.info("Ignored repeated OutOfMemoryError ({}): ", countOOME, e);
                return true;
            }
        }
        // ignore java.lang.ArrayIndexOutOfBoundsException: 1
        // from apple.awt.CWindow.displayChanged(CWindow.java:924):
        if (e instanceof ArrayIndexOutOfBoundsException) {
            final StackTraceElement lastStack = getLastStackElement(e);
            if (lastStack != null) {
                if ("apple.awt.CWindow".equalsIgnoreCase(lastStack.getClassName())
                        && "displayChanged".equalsIgnoreCase(lastStack.getMethodName())) {
                    // log it anyway:
                    _logger.info("Ignored apple exception: ", e);
                    return true;
                }
            }
        }
        // ignore XRender issue (linux) on JDK8 with multiple displays:
        // java.lang.ClassCastException: sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData
        // at sun.java2d.xr.XRPMBlitLoops.cacheToTmpSurface(XRPMBlitLoops.java:145)
        // sun.java2d.xr.XrSwToPMBlit.Blit(XRPMBlitLoops.java:353)
        // sun.java2d.pipe.DrawImage.blitSurfaceData(DrawImage.java:959)
        // sun.java2d.pipe.DrawImage.renderImageCopy(DrawImage.java:577)
        // sun.java2d.pipe.DrawImage.copyImage(DrawImage.java:67)
        // sun.java2d.pipe.DrawImage.copyImage(DrawImage.java:1014)
        // sun.java2d.pipe.ValidatePipe.copyImage(ValidatePipe.java:186)
        // sun.java2d.SunGraphics2D.drawImage(SunGraphics2D.java:3318)
        if (e instanceof ClassCastException) {
            final String msg = e.getMessage();
            if (!StringUtils.isEmpty(msg) && msg.contains("sun.java2d.xr.XRSurfaceData")) {
                 // log it anyway:
                _logger.info("Ignored xrender exception: ", e);
                return true;
            }
        }
        
        // Avoid reentrance:
        if (checkReentrance(e)) {
            // log it anyway:
            _logger.info("Ignored cycling exception: ", e);
            return true;
        }

        return false;
    }

    /**
     * Return the last stack element or null if undefined
     * @param e exception to get its stack traces
     * @return last stack element or null if undefined
     */
    private static StackTraceElement getLastStackElement(final Throwable e) {
        final StackTraceElement[] stackElements = e.getStackTrace();
        if (stackElements.length > 0) {
            return stackElements[0];
        }
        return null;
    }

    /**
     * Return true if this class is already present in the exception's stack traces
     * @param e exception to get its stack traces
     * @return true if this class is already present in the exception's stack traces
     */
    private static boolean checkReentrance(final Throwable e) {
        final String className = MCSExceptionHandler.class.getName();

        final StackTraceElement[] stackElements = e.getStackTrace();

        for (int i = 0, len = stackElements.length; i < len; i++) {
            if (stackElements[i].getClassName().startsWith(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Log the exception to both System.err and logback streams
     * @param t the thread
     * @param e the exception
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static void logException(final Thread t, final Throwable e) {
        System.err.println("An unexpected exception occured in thread " + t.getName());
        e.printStackTrace(System.err);
        _logger.error("An unexpected exception occured in thread {}", t.getName(), e);
    }

    /**
     * Report the exception to the user via Swing using EDT :
     * - display an error message with the exception message
     * - open a modal feedback report
     * @param t the thread
     * @param e the exception
     */
    private static void showException(final Thread t, final Throwable e) {
        MessagePane.showErrorMessage("An unexpected exception occured", e);

        // Show the feedback report (modal) :
        FeedbackReport.openDialog(true, e);
    }

    /**
     * Public constructor used by reflection (AWT exception handler)
     */
    public MCSExceptionHandler() {
        super();
    }

    /**
     * AWT exception handler useful for exceptions occurring in modal dialogs
     * 
     * WARNING: Don't change the signature of this method!
     *
     * @param throwable the exception
     */
    public void handle(final Throwable throwable) {
        runExceptionHandler(throwable);
    }

    /**
     * Logging exception handler that delegates exception handling to logException(thread, throwable)
     */
    private static class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {

        /**
         * Method invoked when the given thread terminates due to the
         * given uncaught exception.
         * <p>Any exception thrown by this method will be ignored by the
         * Java Virtual Machine.
         * @param thread the thread
         * @param e the exception
         */
        @Override
        public void uncaughtException(final Thread thread, final Throwable e) {
            if (!isFilteredException(e)) {
                logException(thread, e);
            }
        }
    }

    /**
     * Exist exception handler that delegates exception handling to logException(thread, throwable)
     * then exit() anyway
     */
    private final static class ExitExceptionHandler extends LoggingExceptionHandler {

        /**
         * Method invoked when the given thread terminates due to the
         * given uncaught exception.
         * <p>Any exception thrown by this method will be ignored by the
         * Java Virtual Machine.
         * @param thread the thread
         * @param e the exception
         */
        @Override
        public void uncaughtException(final Thread thread, final Throwable e) {
            try {
                super.uncaughtException(thread, e);
            } finally {
                System.exit(1);
            }
        }
    }
    /**
     * Swing exception handler that delegates exception handling to showException(thread, throwable)
     * using EDT
     */
    private final static class SwingExceptionHandler implements Thread.UncaughtExceptionHandler {

        /**
         * Method invoked when the given thread terminates due to the
         * given uncaught exception.
         * <p>Any exception thrown by this method will be ignored by the
         * Java Virtual Machine.
         * @param thread the thread
         * @param e the exception
         */
        @Override
        public void uncaughtException(final Thread thread, final Throwable e) {
            if (!isFilteredException(e)) {
                SwingUtils.invokeEDT(new Runnable() {
                    @Override
                    public void run() {
                        showException(thread, e);
                    }
                });
            }
        }
    }
}
