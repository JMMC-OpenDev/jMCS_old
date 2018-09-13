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
import fr.jmmc.jmcs.util.StringUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import fr.jmmc.jmcs.util.concurrent.FastSemaphore;
import java.util.ArrayDeque;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ring buffer : maintain a limited list of string. 
 * Thread safe on add / getContent methods.
 *
 * @author Laurent BOURGES (voparis)?
 */
public final class RingBuffer {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(RingBuffer.class.getName());
    /** default line size */
    public static final int DEFAULT_LINE_SIZE = 100;
    // Members
    /** maximum of lines */
    private final int _maxCount;
    /** write logs file name */
    private final String _writeLogFile;
    /** line counter */
    private int _count;
    /** file writer */
    private Writer _fw = null;
    /** list of lines */
    private final Deque<String> _anchor;
    /** anchor semaphore */
    private final FastSemaphore _semAnchor = new FastSemaphore(1, true); // LBO : test fairness
    /** buffer semaphore */
    private final FastSemaphore _semBuffer = new FastSemaphore(1);
    /** line semaphore */
    private final FastSemaphore _semLine = new FastSemaphore(1);
    /** internal buffer to get content */
    private StringBuilder _buffer = null;
    /** internal buffer to concat prefix & line */
    private StringBuilder _lineBuffer = null;

    /**
     * Constructor
     *
     * @param max number of lines
     * @param writeLogFile file name for standard out / err traces (null implies no file written)
     */
    public RingBuffer(final int max, final String writeLogFile) {
        _maxCount = max;
        _count = 0;
        _anchor = new ArrayDeque<String>(_maxCount);
        _writeLogFile = writeLogFile;
    }

    /**
     * Allocates temporary line &amp; output buffers &amp; file writer
     */
    public void prepare() {
        _buffer = new StringBuilder(_maxCount * DEFAULT_LINE_SIZE / 2);
        _lineBuffer = new StringBuilder(DEFAULT_LINE_SIZE);

        if (!StringUtils.isEmpty(_writeLogFile)) {
            _fw = FileUtils.openFile(_writeLogFile);
        }
    }

    /**
     * Clear temporary line &amp; output buffers &amp; file writer
     */
    public void close() {
        _buffer = null;
        _lineBuffer = null;
        _fw = FileUtils.closeFile(_fw);
    }

    /**
     * add a line in the buffer like tail. Concatenates prefix and line before adding the new line
     *
     * @param prefix starting line prefix
     * @param line content to add in buffer
     */
    public final void add(final String prefix, final String line) {
        String res = null;

        final StringBuilder sb = _lineBuffer;

        if (sb != null) {
            // thread safe protection for line buffer :
            // maybe multiple calls to this method :
            try {
                _semLine.acquire();
                // work on lineBuffer :
                sb.append(prefix).append(" : ").append(line);

                res = sb.toString();
                sb.setLength(0);

                // finished with lineBuffer
            } catch (final InterruptedException ie) {
                _logger.error("RingBuffer : line semaphore interrupted : ", ie);
                res = line;
            } finally {
                _semLine.release();
            }
        } else {
            // uses a new buffer :
            res = prefix + " : " + line;
        }

        add(res);
    }

    /**
     * add a line in the buffer like tail
     *
     * @param line content to add in buffer
     *
     * @return this ring buffer
     */
    public final RingBuffer add(final String line) {
        // thread safe protection for list & count :
        // maybe multiple calls to this method :
        try {
            _semAnchor.acquire();

            // first, write the line into file writer :
            writeLine(line);

            // work on anchor & count :
            while (_count >= _maxCount) {
                _anchor.pollFirst();
                _count--;
            }

            _anchor.offerLast(line);
            _count++;
            // finished with anchor & count
        } catch (final InterruptedException ie) {
            _logger.error("RingBuffer : anchor semaphore interrupted : ", ie);
        } finally {
            _semAnchor.release();
        }

        return this;
    }

    /**
     * Returns buffer content like tail with CR separator
     *
     * @return buffer content
     */
    public final String getContent() {
        return getContent(null);
    }

    /**
     * Returns buffer content like tail with CR separator
     *
     * @param startLine optional begin of content string
     *
     * @return buffer content
     */
    public final String getContent(final String startLine) {
        return getContent(startLine, "\n");
    }

    /**
     * Returns buffer content like tail
     *
     * @param startLine optional begin of content string
     * @param lineSep line separator
     *
     * @return buffer content
     */
    public final String getContent(final String startLine, final String lineSep) {
        String res = null;

        final StringBuilder sb = _buffer;

        if (sb != null) {
            // thread safe protection for buffer :
            // maybe multiple calls to this method :
            try {
                _semBuffer.acquire();

                // work on buffer :
                if (startLine != null) {
                    sb.append(startLine).append(lineSep);
                }

                // thread safe protection for list :
                try {
                    _semAnchor.acquire();

                    // work on anchor :
                    for (final Iterator<String> it = _anchor.iterator(); it.hasNext();) {
                        sb.append(it.next()).append(lineSep);
                    }

                    // finished with anchor
                } catch (final InterruptedException ie) {
                    _logger.error("RingBuffer : anchor semaphore interrupted : ", ie);
                } finally {
                    _semAnchor.release();
                }

                res = sb.toString();
            } catch (final InterruptedException ie) {
                _logger.error("RingBuffer : buffer semaphore interrupted : ", ie);
            } finally {
                _buffer.setLength(0);
                // finished with buffer
                _semBuffer.release();
            }
        } else {
            // Job was not started so Ring Buffer is undefined ...
            res = "";
        }

        return res;
    }

    /**
     * Adds line into logger file
     *
     * @param line content to add
     */
    private void writeLine(final String line) {
        if (_fw != null) {
            try {
                _fw.write(line);
                _fw.write("\n");
            } catch (final IOException ioe) {
                _logger.error("RingBuffer : write line failure : ", ioe);
            }
        }
    }
}
