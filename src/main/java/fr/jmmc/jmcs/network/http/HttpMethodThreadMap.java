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
package fr.jmmc.jmcs.network.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.httpclient.HttpMethodBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the mapping thread (name) <=> HTTPMethod
 *
 * @author Laurent BOURGES.
 */
public final class HttpMethodThreadMap {

    /** logger */
    private final static Logger _logger = LoggerFactory.getLogger(HttpMethodThreadMap.class.getName());
    /** singleton instance */
    private static final HttpMethodThreadMap _methodThreadMap = new HttpMethodThreadMap();

    /**
     * Return the singleton instance
     * @return singleton instance
     */
    public static HttpMethodThreadMap get() {
        return _methodThreadMap;
    }

    /**
     * Remove the HttpMethodBase instance associated to the given thread name
     * @param threadName thread name
     */
    public static void release(final String threadName) {
        get().remove(threadName);
    }

    /**
     * memorize HTTPMethodBase associated to the current thread:
     * @param method method to assicate
     */
    public static void setCurrentThread(final HttpMethodBase method) {
        // memorize HTTPMethodBase associated to the current thread:
        get().set(Thread.currentThread().getName(), method);
    }

    /* members */
    /** mapping thread <name => HTTPMethod (thread-safe) */
    private final Map<String, HttpMethodBase> methodMap = new ConcurrentHashMap<String, HttpMethodBase>(32);

    /**
     * Protected constructor
     */
    protected HttpMethodThreadMap() {
        super();
    }

    /**
     * Return the HttpMethodBase instance associated to the given thread name
     * @param threadName thread name
     * @return HttpMethodBase instance or null if undefined
     */
    public HttpMethodBase get(final String threadName) {
        final HttpMethodBase method = methodMap.get(threadName);
        _logger.debug("HttpMethodThreadMap.get: {} = {}", threadName, method);
        return method;
    }

    /**
     * Define the HttpMethodBase instance associated to the given thread name
     * @param threadName thread name
     * @param method HttpMethodBase instance
     */
    private void set(final String threadName, final HttpMethodBase method) {
        _logger.debug("HttpMethodThreadMap.set: {} = {}", threadName, method);
        methodMap.put(threadName, method);
    }

    /**
     * Remove the HttpMethodBase instance associated to the given thread name
     * @param threadName thread name
     * @return HttpMethodBase removed HttpMethodBase instance or null if not found
     */
    private HttpMethodBase remove(final String threadName) {
        _logger.debug("HttpMethodThreadMap.remove: {}", threadName);
        return methodMap.remove(threadName);
    }
}
