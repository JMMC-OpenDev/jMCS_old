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

import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.network.NetworkSettings;
import static fr.jmmc.jmcs.network.NetworkSettings.getJmmcHttpURI;
import fr.jmmc.jmcs.network.ProxyConfig;
import fr.jmmc.jmcs.util.FileUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This utility class is dedicated to gather code associated to HTTP domain.
 *
 *  It actually :
 * - returns a well configured apache commons HttpClient (legacy project)
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public final class Http {

    /** logger */
    private final static Logger _logger = LoggerFactory.getLogger(Http.class.getName());
    /** HTTP GET value for the read timeout in milliseconds (30 seconds) */
    public static final int GET_SOCKET_READ_TIMEOUT = 30 * 1000;

    /** shared HTTP Client (thread safe) */
    private static volatile HttpClient _sharedHttpClient = null;
    /** shared connection manager (thread safe) */
    private static volatile MultiThreadedHttpConnectionManager _sharedConnectionManager = null;
    /** shared Http retry handler that disables http retries */
    private static final HttpMethodRetryHandler _httpNoRetryHandler = new DefaultHttpMethodRetryHandler(0, false);
    /** shared Http retry handler that uses 3 http retries */
    private static final HttpMethodRetryHandler _httpRetryHandler = new DefaultHttpMethodRetryHandler(3, false);

    /**
     * Forbidden constructor
     */
    private Http() {
        super();
    }

    /**
     * This class returns a multi threaded HTTP client.
     * This client:
     *  * uses the default proxy configuration (based on http://www.jmmc.fr).
     *  * is thread safe.
     *
     * @return httpClient instance
     */
    public static HttpClient getHttpClient() {
        return getHttpClient(getJmmcHttpURI(), false);
    }

    /**
     * This class returns a single-threaded HTTP client for the associated URI.
     * This client:
     *  * uses the default proxy configuration (based on the given uri).
     * @param uri reference URI used to get the proper proxy
     *
     * @return httpClient instance
     */
    private static HttpClient createNewHttpClient(final URI uri) {
        return getHttpClient(uri, true);
    }

    /**
     * This class returns a multi-threaded HTTP client for the associated URI.
     * This client:
     *  * uses the default proxy configuration (based on http://www.jmmc.fr).
     *  * is thread safe.
     * @param uri reference URI used to get the proper proxy
     * @param useDedicatedClient use one dedicated HttpClient if true (proxy resolver) or the shared multi-threaded one else
     *
     * @todo remove the limit for support of the first proxy.
     *
     * @return httpClient instance
     */
    private static synchronized HttpClient getHttpClient(final URI uri, final boolean useDedicatedClient) {
        // Create an HttpClient with the MultiThreadedHttpConnectionManager.
        if (_sharedConnectionManager == null) {
            // This connection manager must be used if more than one thread will
            // be using the HttpClient.
            _sharedConnectionManager = new MultiThreadedHttpConnectionManager();
        }
        final HttpClient httpClient;
        if (useDedicatedClient) {
            // create a new client to use custom proxy settings:
            httpClient = new HttpClient(_sharedConnectionManager);
        } else {
            if (_sharedHttpClient != null) {
                // reuse shared http client:
                return _sharedHttpClient;
            } else {
                _sharedHttpClient = httpClient = new HttpClient(_sharedConnectionManager);
            }
        }

        setConfiguration(httpClient);

        // Get Proxy settings for the given URI:
        final ProxyConfig config = NetworkSettings.getProxyConfiguration(uri);
        if (config.getHostname() != null) {
            final HostConfiguration hostConfig = new HostConfiguration();
            hostConfig.setProxy(config.getHostname(), config.getPort());
            httpClient.setHostConfiguration(hostConfig);
        }

        return httpClient;
    }

    /**
     * Define client configuration
     * @param httpClient instance to configure
     */
    private static void setConfiguration(final HttpClient httpClient) {
        final HttpConnectionManagerParams httpParams = httpClient.getHttpConnectionManager().getParams();
        // define connect timeout:
        httpParams.setConnectionTimeout(NetworkSettings.DEFAULT_CONNECT_TIMEOUT);
        // define read timeout:
        httpParams.setSoTimeout(NetworkSettings.DEFAULT_SOCKET_READ_TIMEOUT);

        // define connection parameters:
        httpParams.setMaxTotalConnections(NetworkSettings.DEFAULT_MAX_TOTAL_CONNECTIONS);
        httpParams.setDefaultMaxConnectionsPerHost(NetworkSettings.DEFAULT_MAX_HOST_CONNECTIONS);

        // set content-encoding to UTF-8 instead of default ISO-8859
        final HttpClientParams httpClientParams = httpClient.getParams();
        // define timeout value for allocation of connections from the pool
        httpClientParams.setConnectionManagerTimeout(NetworkSettings.DEFAULT_CONNECT_TIMEOUT);
        // encoding to UTF-8
        httpClientParams.setParameter(HttpClientParams.HTTP_CONTENT_CHARSET, "UTF-8");
        // avoid any http retries (POST):
        httpClientParams.setParameter(HttpMethodParams.RETRY_HANDLER, _httpNoRetryHandler);

        // Customize the user agent:
        httpClientParams.setParameter(HttpMethodParams.USER_AGENT, System.getProperty(NetworkSettings.PROPERTY_USER_AGENT));
    }

    /**
     * Transform the given URL in URI if valid.
     * @param url URL as string
     * @return URI instance
     * @throws IllegalArgumentException if the URI is malformed
     */
    public static URI validateURL(final String url) throws IllegalArgumentException {
        try {
            return new URI(url);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("Invalid URL:" + url, use);
        }
    }

    /**
     * Save the document located at the given URI in the given file. 
     * Requests with dedicatedClient will instance one new client with proxies compatible with given URI. 
     * Other requests will use the common multi-threaded HTTP client .
     * 
     * @param uri URI to download
     * @param outputFile file to save into
     * @param useDedicatedClient use one dedicated HttpClient if true (proxy resolver) or the shared multi-threaded one else
     * @return true if successful
     * @throws IOException if any I/O operation fails (HTTP or file) 
     */
    public static boolean download(final URI uri, final File outputFile, final boolean useDedicatedClient) throws IOException {
        // Create an HTTP client for the given URI to detect proxies for this host or use common one depending of given flag
        final HttpClient client = (useDedicatedClient) ? Http.createNewHttpClient(uri) : Http.getHttpClient();

        return download(uri, client, new StreamProcessor() {
            /**
             * Process the given input stream and CLOSE it anyway (try/finally)
             * @param in input stream to process
             * @throws IOException if any IO error occurs
             */
            @Override
            public void process(final InputStream in) throws IOException {
                try {
                    FileUtils.saveStream(in, outputFile);
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("File '{}' saved ({} bytes).", outputFile, outputFile.length());
                    }
                } catch (IOException ioe) {
                    if (outputFile.exists()) {
                        _logger.debug("File '{}' deleted (partial download).", outputFile);
                        outputFile.delete();
                    }
                    throw ioe;
                }
            }
        });
    }

    /**
     * Read a text file from the given URI into a string
     *
     * @param uri URI to load
     * @param useDedicatedClient use one dedicated HttpClient if true (proxy resolver) or the shared multi-threaded one else
     * @return text file content or null if no result
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String download(final URI uri, final boolean useDedicatedClient) throws IOException {
        // Create an HTTP client for the given URI to detect proxies for this host or use common one depending of given flag
        final HttpClient client = (useDedicatedClient) ? Http.createNewHttpClient(uri) : Http.getHttpClient();

        return download(uri, client);
    }

    /**
     * Read a text file from the given URI into a string
     *
     * @param uri URI to load
     * @param client http client to use
     * @return text file content or null if no result
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String download(final URI uri, final HttpClient client) throws IOException {

        final StringStreamProcessor stringProcessor = new StringStreamProcessor();

        if (download(uri, client, stringProcessor)) {
            return stringProcessor.getResult();
        }

        return null;
    }

    /**
     * Post a request to the given URI and get a string as result.
     *
     * @param uri URI to load
     * @param useDedicatedClient use one dedicated HttpClient if true (proxy resolver) or the shared multi-threaded one else
     * @param queryProcessor post query processor to define query parameters
     * @return result as string or null if no result
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String post(final URI uri, final boolean useDedicatedClient,
                              final PostQueryProcessor queryProcessor) throws IOException {

        // Create an HTTP client for the given URI to detect proxies for this host or use common one depending of given flag
        final HttpClient client = (useDedicatedClient) ? Http.createNewHttpClient(uri) : Http.getHttpClient();

        return post(uri, client, queryProcessor);
    }

    /**
     * Post a request to the given URI and get a string as result.
     *
     * @param uri URI to load
     * @param client http client to use
     * @param queryProcessor post query processor to define query parameters
     * @return result as string or null if no result
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String post(final URI uri, final HttpClient client,
                              final PostQueryProcessor queryProcessor) throws IOException {

        final StringStreamProcessor stringProcessor = new StringStreamProcessor();

        if (post(uri, client, queryProcessor, stringProcessor)) {
            return stringProcessor.getResult();
        }

        return null;
    }

    /**
     * Execute a request to the given URI and get a string as result.
     *
     * @param client HttpClient to use
     * @param method http method to execute
     * @return result as string or null if no result
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String execute(final HttpClient client, final HttpMethodBase method) throws IOException {

        final StringStreamProcessor stringProcessor = new StringStreamProcessor();

        if (execute(client, method, stringProcessor)) {
            return stringProcessor.getResult();
        }

        return null;
    }

    /**
     * Abort the execution of the Http method associated to the given thread name
     *
     * @param threadName thread name
     */
    public static void abort(final String threadName) {
        if (threadName != null) {
            final HttpMethodBase method = HttpMethodThreadMap.get().get(threadName);

            _logger.debug("abort: {} = {}", threadName, method);

            if (method != null) {
                // abort method:
                try {
                    /* This closes the socket handling our blocking I/O, which will
                     * interrupt the request immediately. */
                    method.abort();

                } finally {
                    // To be sure to call relaseConnection altought the thread should do it (normally):
                    releaseConnection(method, threadName);
                }
            }
        }
    }

    /**
     * Release both connection and the HttpMethodBase reference to the current thread name
     *
     * @param method HttpMethodBase to release
     */
    public static void releaseConnection(final HttpMethodBase method) {
        releaseConnection(method, Thread.currentThread().getName());
    }

    /**
     * Release both connection and the HttpMethodBase reference to the given thread name
     *
     * @param method HttpMethodBase to release
     * @param threadName thread name to use
     */
    public static void releaseConnection(final HttpMethodBase method, final String threadName) {
        _logger.debug("releaseConnection: {} = {}", threadName, method);

        // clear HttpMethodBase and release connection once:
        HttpMethodThreadMap.release(threadName);

        // release connection back to pool:
        if (method != null) {
            method.releaseConnection();
        }
    }

    /**
     * Save the document located at the given URI and use the given processor to get the result.
     * Requests with dedicatedClient will instance one new client with proxies compatible with given URI.
     * Other requests will use the common multi-threaded HTTP client.
     * 
     * @param uri URI to download
     * @param resultProcessor stream processor to use to consume HTTP response
     * @param client http client to use
     * @return true if successful
     * @throws IOException if any I/O operation fails (HTTP or file) 
     */
    private static boolean download(final URI uri, final HttpClient client,
                                    final StreamProcessor resultProcessor) throws IOException {
        return download(uri, client, resultProcessor, 0);
    }

    /**
     * Save the document located at the given URI and use the given processor to get the result.
     * Requests with dedicatedClient will instance one new client with proxies compatible with given URI.
     * Other requests will use the common multi-threaded HTTP client.
     * 
     * @param uri URI to download
     * @param resultProcessor stream processor to use to consume HTTP response
     * @param client http client to use
     * @param level recursion level (authentication attempt)
     * @return true if successful
     * @throws IOException if any I/O operation fails (HTTP or file) 
     * @throws AuthenticationException if authentication failed
     */
    private static boolean download(final URI uri, final HttpClient client,
                                    final StreamProcessor resultProcessor,
                                    final int level) throws IOException {

        final String url = uri.toString();
        final GetMethod method = new GetMethod(url);

        final HttpMethodParams httpMethodParams = method.getParams();
        // customize timeouts:
        httpMethodParams.setSoTimeout(GET_SOCKET_READ_TIMEOUT);
        // allow http retries (GET):
        httpMethodParams.setParameter(HttpMethodParams.RETRY_HANDLER, _httpRetryHandler);

        if (_logger.isDebugEnabled()) {
            _logger.debug("HTTP client and GET method have been created. doAuthentication = {}", method.getDoAuthentication());
        }

        int resultCode = -1;
        try {
            // memorize HTTPMethodBase associated to the current thread:
            HttpMethodThreadMap.setCurrentThread(method);

            // Send HTTP GET query:
            resultCode = client.executeMethod(method);
            if (_logger.isDebugEnabled()) {
                _logger.debug("The query has been sent. Status code: {}", resultCode);
            }

            // If everything went fine
            if (resultCode == HttpStatus.SC_OK) {

                // Get response
                final InputStream in = new BufferedInputStream(method.getResponseBodyAsStream());
                resultProcessor.process(in);

                return true;
            }

        } finally {
            // Release the connection.
            releaseConnection(method);
        }

        if (resultCode == 401) {
            // Memorize the credentials into a session ... reuse login per host name ? or query part ?
            String host;
            URI nextURI;
            try {
                // try getting current host (redirection compatible):
                org.apache.commons.httpclient.URI currentURI = method.getURI();

                _logger.debug("method.uri : {}", currentURI);

                nextURI = validateURL(currentURI.toString());
                host = currentURI.getHost();

            } catch (URIException ue) {
                _logger.debug("uri failure:", ue);
                nextURI = uri;
                host = uri.getHost();
            }

            final String realm = method.getHostAuthState().getRealm();

            final AuthScope authScope = new AuthScope(host, AuthScope.ANY_PORT, realm, AuthScope.ANY_SCHEME);

            // check if already credentials ?
            Credentials credentials = client.getState().getCredentials(authScope);

            // check if 'skip' credentials ?
            if (!shouldSkip(credentials)) {
                final List<Credentials> holder = new ArrayList<Credentials>(1);

                // Show Swing FORM using EDT:
                SwingUtils.invokeAndWaitEDT(new Runnable() {
                    @Override
                    public void run() {
                        // Request user/login password and try again with given credential
                        final HttpCredentialForm credentialForm = new HttpCredentialForm(method);
                        // show modal dialog:
                        credentialForm.setVisible(true);

                        final Credentials credentials = credentialForm.getCredentials();
                        if (credentials != null) {
                            holder.add(credentials);
                        }
                    }
                });

                if (holder.isEmpty()) {
                    // Cancel button => interrupt thread to cancel any background task:
                    Thread.currentThread().interrupt();
                } else {
                    // if user gives one login/password, try again with the new credential
                    credentials = holder.get(0);

                    // when present, add the credential to the client  for the correct scope (host + realm):
                    final HttpState state = client.getState();
                    state.setCredentials(authScope, credentials);

                    if (!shouldSkip(credentials)) {
                        return download(nextURI, client, resultProcessor, level + 1);
                    }
                }
            }
            throw new AuthenticationException("Authentication failed for url:" + uri);
        }

        _logger.info("download failed [{}]: result code: {}, status: {}", uri, resultCode, method.getStatusText());

        return false;
    }

    private static boolean shouldSkip(final Credentials credentials) {
        if (credentials instanceof UsernamePasswordCredentials) {
            final UsernamePasswordCredentials userPass = (UsernamePasswordCredentials) credentials;
            return (HttpCredentialForm.SKIP.equals(userPass.getUserName()));
        }
        return false;
    }

    /**
     * Push the post form to the given URI and use the given processor to get the result.
     * Requests with dedicatedClient will instance one new client (with automatic proxies compatible with given URI). 
     * Other requests will use the common multi-threaded HttpClient.
     * 
     * @param uri URI to download
     * @param queryProcessor post query processor to define query parameters
     * @param resultProcessor stream processor to use to consume HTTP response
     * @param client HttpClient to use
     * @return true if successful
     * @throws IOException if any I/O operation fails (HTTP or file) 
     */
    private static boolean post(final URI uri, final HttpClient client,
                                final PostQueryProcessor queryProcessor, final StreamProcessor resultProcessor) throws IOException {

        final PostMethod method = new PostMethod(uri.toString());
        _logger.debug("HTTP client and POST method have been created");

        try {
            // Define HTTP POST parameters
            queryProcessor.process(method);

            // memorize HTTPMethodBase associated to the current thread:
            HttpMethodThreadMap.setCurrentThread(method);

            // Send HTTP query
            final int resultCode = client.executeMethod(method);
            if (_logger.isDebugEnabled()) {
                _logger.debug("The query has been sent. Status code: {}", resultCode);
            }

            // If everything went fine
            if (resultCode == HttpStatus.SC_OK) {
                // Get response
                final InputStream in = new BufferedInputStream(method.getResponseBodyAsStream());
                resultProcessor.process(in);

                return true;
            }
        } finally {
            // Release the connection.
            releaseConnection(method);
        }

        return false;
    }

    /**
     * Execute the given Http method (GET, POST...) to the given URI and use the given processor to get the result.
     * 
     * @param client HttpClient to use
     * @param method http method to execute
     * @param resultProcessor stream processor to use to consume HTTP response
     * @return true if successful
     * @throws IOException if any I/O operation fails (HTTP or file) 
     */
    private static boolean execute(final HttpClient client,
                                   final HttpMethodBase method, final StreamProcessor resultProcessor) throws IOException {
        try {
            // memorize HTTPMethodBase associated to the current thread:
            HttpMethodThreadMap.setCurrentThread(method);

            // Send HTTP query
            final int resultCode = client.executeMethod(method);
            if (_logger.isDebugEnabled()) {
                _logger.debug("The query has been sent. Status code: {}", resultCode);
            }

            // If everything went fine
            if (resultCode == HttpStatus.SC_OK) {
                // Get response
                final InputStream in = new BufferedInputStream(method.getResponseBodyAsStream());
                resultProcessor.process(in);

                return true;
            }
        } finally {
            // Release the connection.
            releaseConnection(method);
        }

        return false;
    }

    /**
     * Custom StreamProcessor that copy the input stream to one String
     */
    private static final class StringStreamProcessor implements StreamProcessor {

        /** result as String */
        private String result = null;

        /**
         * Process the given input stream and CLOSE it anyway (try/finally)
         * @param in input stream to process
         * @throws IOException if any IO error occurs
         */
        @Override
        public void process(final InputStream in) throws IOException {
            // TODO check if we can get response size from HTTP headers
            result = FileUtils.readStream(in);
            if (_logger.isDebugEnabled()) {
                _logger.debug("String stored in memory ({} chars).", result.length());
            }
        }

        /**
         * Return the result as String
         * @return result as String or null
         */
        String getResult() {
            return result;
        }
    }
}
