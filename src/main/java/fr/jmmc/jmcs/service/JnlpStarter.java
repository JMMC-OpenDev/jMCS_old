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

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.runner.EmptyJobListener;
import fr.jmmc.jmcs.util.runner.JobListener;
import fr.jmmc.jmcs.util.runner.LocalLauncher;
import fr.jmmc.jmcs.util.runner.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper on http://code.google.com/p/vo-urp/ task runner.
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class JnlpStarter {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(JnlpStarter.class.getName());
    /** application identifier for LocalLauncher */
    public final static String APP_NAME = "JnlpStarter";
    /** user for LocalLauncher */
    public final static String USER_NAME = "JMMC";
    /** task identifier for LocalLauncher */
    public final static String TASK_NAME = "JavaWebStart";
    /** javaws command */
    public final static String JAVAWS_CMD = "javaws";
    /** flag to execute javaws with -verbose option */
    private static boolean JNLP_VERBOSE = false;

    /** Forbidden constructor */
    private JnlpStarter() {
    }

    /**
     * Launch the given Java WebStart application in background.
     * 
     * @param jnlpUrl JNLP URL to launch the application
     * @return the job context identifier
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static Long launch(final String jnlpUrl) throws IllegalStateException {
        return launch(jnlpUrl, EmptyJobListener.INSTANCE);
    }

    /**
     * Launch the given Java WebStart application in background.
     * 
     * @param jnlpUrl JNLP URL to launch the application
     * @param jobListener job event listener (not null)
     * @return the job context identifier
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static Long launch(final String jnlpUrl, final JobListener jobListener) throws IllegalStateException {

        if (StringUtils.isEmpty(jnlpUrl)) {
            throw new IllegalArgumentException("empty JNLP url !");
        }
        if (jobListener == null) {
            throw new IllegalArgumentException("undefined job listener !");
        }

        _logger.info("launch: {}", jnlpUrl);

        // create the execution context without log file:
        final RootContext jobContext = LocalLauncher.prepareMainJob(APP_NAME, USER_NAME, FileUtils.getTempDirPath(), null);

        // command line: 'javaws <jnlpUrl>'
        final String[] cmd;
        if (JNLP_VERBOSE) {
            cmd = new String[]{JAVAWS_CMD, "-verbose", jnlpUrl};
        } else {
            cmd = new String[]{JAVAWS_CMD, jnlpUrl};
        }

        LocalLauncher.prepareChildJob(jobContext, TASK_NAME, cmd);

        // Puts the job in the job queue (can throw IllegalStateException if job not queued)
        LocalLauncher.startJob(jobContext, jobListener);

        return jobContext.getId();
    }

    /** Start Java WebStart viewer */
    public static void launchJavaWebStartViewer() {
        _logger.info("launch 'javaws -viewer'");

        // create the execution context without log file:
        final RootContext jobContext = LocalLauncher.prepareMainJob(APP_NAME, USER_NAME, FileUtils.getTempDirPath(), null);

        // command line: 'javaws -viewer'
        LocalLauncher.prepareChildJob(jobContext, TASK_NAME, new String[]{JAVAWS_CMD, "-viewer"});

        // puts the job in the job queue :
        // can throw IllegalStateException if job not queued :
        LocalLauncher.startJob(jobContext);
    }

    /**
     * Return the flag to execute javaws with -verbose option
     * @return flag to execute javaws with -verbose option
     */
    public static boolean isJavaWebStartVerbose() {
        return JNLP_VERBOSE;
    }

    /**
     * Define the flag to execute javaws with -verbose option
     * @param verbose flag to execute javaws with -verbose option
     */
    public static void setJavaWebStartVerbose(final boolean verbose) {
        JNLP_VERBOSE = verbose;
    }
}
