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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements Runnable to redirect an input stream to a ring buffer
 * 
 * @see ProcessRunner
 * @see RingBuffer
 * 
 * @author Laurent BOURGES (voparis).
 */
public final class StreamRedirector implements Runnable {

    /** Logger */
    private final static Logger _logger = LoggerFactory.getLogger(StreamRedirector.class.getName());
    /** debug flag : dump every read line in logs */
    private final static boolean DEBUG = false;
    /** pause flag : waits 10 milliseconds after each line read */
    private final static boolean PAUSE = false;
    // Members
    /** prefix for example : 'ERROR' */
    private final String _prefix;
    /** input Stream to redirect to buffer */
    private InputStream _is;
    /** ring buffer */
    private final RingBuffer _ring;

    /**
     * Constructor with the given ring buffer
     * @param ring buffer to use
     */
    public StreamRedirector(final RingBuffer ring) {
        this(ring, null);
    }

    /**
     * Constructor with the given ring buffer and prefix
     * @param ring buffer to use
     * @param prefix line prefix to use
     */
    public StreamRedirector(final RingBuffer ring, final String prefix) {
        _ring = ring;
        _prefix = prefix;
    }

    /**
     * Defines the inputStream to read
     * @param in stream to read
     */
    public void setInputStream(final InputStream in) {
        _is = in;
    }

    /**
     * The method reads lines from a buffered reader for the inputStream and adds them to the ring buffer as long as the inputStream is ready.
     * The input stream is not closed by this method. 
     */
    @Override
    public void run() {
        _logger.debug("StreamRedirector - thread.run : enter");

        if (_is == null) {
            _logger.error("StreamRedirector.run : undefined input stream !");
        } else {
            try {
                // 8K buffer :
                final BufferedReader br = new BufferedReader(new InputStreamReader(_is));
                for (String line = null; (line = br.readLine()) != null;) {

                    if (DEBUG) {
                        _logger.error(line);
                        if (PAUSE) {
                            // pause thread to slow down the job :
                            ThreadExecutors.sleep(10l);
                        }
                    }

                    if (_prefix != null) {
                        _ring.add(_prefix, line);
                    } else {
                        _ring.add(line);
                    }
                }
            } catch (IOException ioe) {
                // occurs when process is killed (buffer.readLine() says 'Stream closed') :
                _logger.debug("StreamRedirector.run : io failure : ", ioe);
            }
        }
        _logger.debug("StreamRedirector - thread.run : exit");
    }
}
