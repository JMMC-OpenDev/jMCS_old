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
package fr.jmmc.jmcs.util.runner.process;

import fr.jmmc.jmcs.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process Runner : manages Unix process (start and kill).
 * 
 * @author Laurent BOURGES (voparis).
 */
public final class ProcessRunner {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(ProcessRunner.class.getName());
    /** ERROR prefix */
    public final static String ERR_PREFIX = "ERROR";
    /** undefined process status */
    public final static int STATUS_UNDEFINED = -1;
    /** normal process status (OK) */
    public final static int STATUS_NORMAL = 0;
    /** interrupted process status */
    public final static int STATUS_INTERRUPTED = -100;
    /** flag to use timeout when waiting on process stream close */
    public final static boolean USE_TIMEOUT = true;
    /** wait timeout on standard streams (5 seconds)  */
    public final static long STREAM_TIMEOUT = 5l;

    /**
     * Forbidden constructor
     */
    private ProcessRunner() {
    }

    /**
     * Runs a job context (UNIX command) and redirects the STD OUT / ERR to the ring buffer associated to the given job context
     * @see StreamRedirector
     * @see RingBuffer
     * @param runCtx job context
     * @return process status (0 to 255) or -1 if undefined
     */
    public static int execute(final ProcessContext runCtx) {
        int status = STATUS_UNDEFINED;
        // params :

        final File workingDir = FileUtils.getDirectory(runCtx.getWorkingDir());
        if (workingDir == null) {
            _logger.error("ProcessRunner.execute : working directory does not exist: ", runCtx.getWorkingDir());
        } else {
            final String[] args = runCtx.getCommandArray();
            final RingBuffer ring = runCtx.getRing();

            if (_logger.isInfoEnabled()) {
                _logger.info("ProcessRunner.execute : starting process: {} in directory: {}", Arrays.toString(args), workingDir);
            }

            // initialization :
            ring.prepare();

            final StreamRedirector outputRedirect = new StreamRedirector(ring);
            final StreamRedirector errorRedirect = new StreamRedirector(ring, ERR_PREFIX);

            final long start = System.nanoTime();
            try {
                final Process process = exec(workingDir, args);
                // keep reference to allow killing process :
                runCtx.setProcess(process);

                // capture stdout :
                outputRedirect.setInputStream(process.getInputStream());
                // capture stderr :
                errorRedirect.setInputStream(process.getErrorStream());

                Future<?> outputFuture = null;
                Future<?> errorFuture = null;

                // start StreamRedirectors and place in runnable state :
                _logger.debug("ProcessRunner.execute : starting outputRedirect task ...");

                outputFuture = ThreadExecutors.getGenericExecutor().submit(outputRedirect);

                _logger.debug("ProcessRunner.execute : starting errorRedirect task ...");

                errorFuture = ThreadExecutors.getGenericExecutor().submit(errorRedirect);

                _logger.debug("ProcessRunner.execute : waitFor process to end ...");

                // todo use timeout to stop waiting (workaround: use cancel(true) to interrupt current thread)...
                status = process.waitFor();

                // calls thread.join to be sure that other threads finish before leaving from here :
                // note: this thread is waiting FOR EVER until stdout/stderr streams are closed 
                // by the child process itself

                _logger.debug("ProcessRunner.execute : join output Redirect ...");

                if (USE_TIMEOUT) {
                    outputFuture.get(STREAM_TIMEOUT, TimeUnit.SECONDS);
                } else {
                    outputFuture.get();
                }

                _logger.debug("ProcessRunner.execute : join error Redirect ...");

                if (USE_TIMEOUT) {
                    errorFuture.get(STREAM_TIMEOUT, TimeUnit.SECONDS);
                } else {
                    errorFuture.get();
                }

            } catch (CancellationException ce) {
                _logger.error("ProcessRunner.run : execution failure :", ce);
            } catch (ExecutionException ee) {
                _logger.error("ProcessRunner.run : execution failure :", ee);
            } catch (IllegalStateException ise) {
                _logger.error("ProcessRunner.execute : illegal state failure :", ise);
            } catch (TimeoutException te) {
                _logger.debug("ProcessRunner.execute : stream timeout failure :", te);
            } catch (InterruptedException ie) {
                // occurs when the threadpool shutdowns or interrupts the task (future.cancel) :
                _logger.debug("ProcessRunner.execute : interrupted failure :", ie);
                // Interrupted status :
                status = STATUS_INTERRUPTED;
            } catch (IOException ioe) {
                _logger.error("ProcessRunner.execute : unable to start process: {}", Arrays.toString(args), ioe);
                ring.add(ERR_PREFIX, ioe.getMessage());
            } finally {
                // in all cases : 
                final double duration = 1e-6d * (System.nanoTime() - start);

                runCtx.setDuration((long) duration);
                runCtx.setExitCode(status);

                // cleanup : free process in whatever state and close streams:
                stop(runCtx, false);

                _logger.info("ProcessRunner.execute : process status: {}", runCtx.getExitCode());
            }
        }

        return status;
    }

    /**
     * Kill a running UNIX Process from the given job context
     * @param runCtx job context
     */
    public static void kill(final ProcessContext runCtx) {
        stop(runCtx, true);
    }

    /**
     * Kill a running UNIX Process from the given job context
     * @param runCtx job context
     * @param kill true to indicate a kill operation
     */
    private static void stop(final ProcessContext runCtx, final boolean kill) {
        final Process process = runCtx.getProcess();
        if (process != null) {
            if (kill) {
                _logger.info("ProcessRunner.stop : killing process: {}", process);
            } else {
                _logger.debug("ProcessRunner.stop : stopping process: {}", process);
            }

            // kills unix process & close all streams (stdin, stdout, stderr) :
            process.destroy();

            // workaround to closing bugs:
            FileUtils.closeStream(process.getOutputStream());
            FileUtils.closeStream(process.getErrorStream());
            FileUtils.closeStream(process.getInputStream());

            if (kill) {
                _logger.info("ProcessRunner.stop : process killed.");
            } else {
                _logger.debug("ProcessRunner.stop : process stopped.");
            }
            // free killed process :
            runCtx.setProcess(null);
        }
    }

    /**
     * Launches a UNIX command with the given args (command is included in that array) and working directory
     * @see ProcessBuilder
     * @param workingDir process working directory
     * @param args UNIX command array (command + arguments)
     * @return UNIX Process
     * @throws java.io.IOException if the process can not be created
     */
    private static Process exec(final File workingDir, final String[] args) throws IOException {
        return new ProcessBuilder(args).directory(workingDir).start();
    }
}
