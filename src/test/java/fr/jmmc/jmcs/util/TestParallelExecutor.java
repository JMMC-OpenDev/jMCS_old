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

import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.util.concurrent.ParallelJobExecutor;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public class TestParallelExecutor {

    /** logger */
    private final static Logger logger = LoggerFactory.getLogger(TestFileUtils.class.getName());

    public static void main(String[] args) {

        // invoke App method to initialize logback now:
        Bootstrapper.getState();

        /** jMCS Parallel Job executor */
        final ParallelJobExecutor jobExecutor = ParallelJobExecutor.getInstance();

        for (int nIter = 1; nIter <= 100 * 1000; nIter *= 10) {

            System.gc();
            ThreadExecutors.sleep(100l);

            final int nJobs = jobExecutor.getMaxParallelJob() * nIter;

            logger.info("Test: {} jobs");

            final long start = System.nanoTime();

            // computation tasks:
            final Runnable[] jobs = new Runnable[nJobs];

            // create tasks:
            for (int i = 0; i < nJobs; i++) {

                jobs[i] = new Runnable() {
                    @Override
                    public void run() {
                        // nothing to do
                        Thread.currentThread().isInterrupted();
                    }
                };
            }

            // execute jobs in parallel or using current thread if only one job (throws InterruptedJobException if interrupted):
            jobExecutor.forkAndJoin("TestParallelExecutor.main", jobs);

            logger.info("TestParallelExecutor.main: {} iterations - duration = {} / {} ms.", nIter, 1e-6d * (System.nanoTime() - start));
        }

        logger.info("TestParallelExecutor.main: shutdown");

        jobExecutor.shutdown();
    }
}
