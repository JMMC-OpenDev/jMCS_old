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
package fr.jmmc.jmcs.network;

import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.util.StringUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gathers general network settings:
 * - socket and connect timeouts;
 * - proxy (host / port).
 *
 * It uses Java System properties and also jMCS Preferences to get the proxy settings.
 * 
 * @author Laurent BOURGES, Guillaume MELLA.
 */
public final class NetworkSettings {

    /** logger */
    private final static Logger _logger = LoggerFactory.getLogger(NetworkSettings.class.getName());
    /* system properties */
    /** Timeout to establish connection in milliseconds (sun classes) */
    public static final String PROPERTY_DEFAULT_CONNECT_TIMEOUT = "sun.net.client.defaultConnectTimeout";
    /** Timeout "waiting for data" (read timeout) in milliseconds (sun classes) */
    public static final String PROPERTY_DEFAULT_READ_TIMEOUT = "sun.net.client.defaultReadTimeout";
    /** Use System Proxies */
    public static final String PROPERTY_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";
    /** Java plug-in proxy list */
    public static final String PROPERTY_JAVA_PLUGIN_PROXY_LIST = "javaplugin.proxy.config.list";
    /** HTTP proxy host */
    public static final String PROPERTY_HTTP_PROXY_HOST = "http.proxyHost";
    /** HTTP proxy port */
    public static final String PROPERTY_HTTP_PROXY_PORT = "http.proxyPort";
    /** HTTP non proxy hosts */
    public static final String PROPERTY_HTTP_NO_PROXY_HOSTS = "http.nonProxyHosts";
    /** HTTPS proxy host */
    public static final String PROPERTY_HTTPS_PROXY_HOST = "https.proxyHost";
    /** HTTPS proxy port */
    public static final String PROPERTY_HTTPS_PROXY_PORT = "https.proxyPort";
    /* JMMC standard values */
    /** Use system proxies (false by default) */
    public static final String USE_SYSTEM_PROXIES = "true";
    /** default value for the connection timeout in milliseconds (3 s) */
    public static final int DEFAULT_CONNECT_TIMEOUT = 3 * 1000;
    /** default value for the read timeout in milliseconds (10 minutes) */
    public static final int DEFAULT_SOCKET_READ_TIMEOUT = 10 * 60 * 1000;
    /** The default maximum number of connections allowed per host */
    public static final int DEFAULT_MAX_HOST_CONNECTIONS = 5;
    /** The default maximum number of connections allowed overall */
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 10;
    /** JMMC web host */
    private final static String JMMC_WEB_HOST = "www.jmmc.fr";
    /** JMMC web to detect proxies */
    private final static String JMMC_WEB_URL = "http://" + JMMC_WEB_HOST;
    /** cached JMMC web URL */
    private static URI JMMC_WEB_URI = null;
    /** Prefix of the preference which stores optional IP addresses */
    public static final String PREFIX_PREFERENCE_IP = "ip.";

    /**
     * Forbidden constructor
     */
    private NetworkSettings() {
        super();
    }

    /**
     * Main entry point : calls defineDefaults()
     * @param args unused
     */
    public static void main(final String[] args) {
        defineDefaults();

        getHostIP(JMMC_WEB_HOST);
    }

    /**
     * Define default values (timeouts, proxy ...)
     */
    public static void defineDefaults() {
        defineTimeouts();

        defineProxy();
    }

    /**
     * Define timeouts (HTTP / socket)
     */
    public static void defineTimeouts() {
        _logger.info("define default Connect timeout to {} ms.", DEFAULT_CONNECT_TIMEOUT);
        System.setProperty(PROPERTY_DEFAULT_CONNECT_TIMEOUT, Integer.toString(DEFAULT_CONNECT_TIMEOUT));

        _logger.info("define default Read timeout to {} ms.", DEFAULT_SOCKET_READ_TIMEOUT);
        System.setProperty(PROPERTY_DEFAULT_READ_TIMEOUT, Integer.toString(DEFAULT_SOCKET_READ_TIMEOUT));
    }

    /**
     * Define the proxy settings for HTTP protocol
     */
    public static void defineProxy() {
        // FIRST STEP: force JVM to use System proxies if System properties are not defined (or given by JNLP RE):

        // NOTE: USE of SYSTEM_PROXIES can cause problems with SOCKS / HTTPS / Other protocols ?
        // unset env var all_proxy=socks://w and ALL_PROXY
        System.setProperty(PROPERTY_USE_SYSTEM_PROXIES, USE_SYSTEM_PROXIES);

        // Get Http Proxy settings from ProxySelector:
        final ProxyConfig config = getProxyConfiguration(getJmmcHttpURI());

        if (config.getHostname() != null) {
            _logger.info("Get proxy settings from Java ProxySelector.");

            defineProxy(config.getHostname(), config.getPort());
        } else {
            // Try environment variables:
            String envHttpProxy = System.getenv("http_proxy");
            if (envHttpProxy == null) {
                envHttpProxy = System.getenv("HTTP_PROXY");
            }
            URI uri = null;
            if (envHttpProxy != null) {
                try {
                    uri = new URI(envHttpProxy);
                } catch (URISyntaxException use) {
                    _logger.info("Invalid http proxy: {}", envHttpProxy, use);
                }
            }
            if (uri != null) {
                _logger.info("Get proxy settings from environment variables: " + uri);

                final String proxyHost = uri.getHost();
                final int port = uri.getPort();
                if ((proxyHost != null) && !proxyHost.isEmpty() && (port != 0)) {
                    defineProxy(proxyHost, port);
                    return;
                }
            }

            _logger.info("Get proxy settings from CommonPreferences.");

            final CommonPreferences prefs = CommonPreferences.getInstance();

            final String proxyHost = prefs.getPreference(CommonPreferences.HTTP_PROXY_HOST);
            final String proxyPort = prefs.getPreference(CommonPreferences.HTTP_PROXY_PORT);

            if (!StringUtils.isTrimmedEmpty(proxyHost) && !StringUtils.isTrimmedEmpty(proxyPort)) {
                try {
                    final int port = Integer.valueOf(proxyPort);
                    if (port != 0) {
                        defineProxy(proxyHost, port);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    // invalid number
                }
            }
            _logger.info("No http proxy defined.");
        }
    }

    /**
     * Define the proxy settings for HTTP protocol
     * @param proxyHost host name
     * @param proxyPort port
     */
    private static void defineProxy(final String proxyHost, final int proxyPort) {
        _logger.info("define http proxy to {}:{}", proxyHost, proxyPort);

        // HTTP protocol:
        // # http.proxyHost
        System.setProperty(PROPERTY_HTTP_PROXY_HOST, proxyHost);

        // # http.proxyPort
        System.setProperty(PROPERTY_HTTP_PROXY_PORT, Integer.toString(proxyPort));

        // # http.nonProxyHosts
        System.setProperty(PROPERTY_HTTP_NO_PROXY_HOSTS, "localhost|127.0.0.1");

        // TODO : support also advanced proxy settings (user, password ...)
        // # http.proxyUser
        // # http.proxyPassword
        // # http.nonProxyHosts
        // HTTPS protocol:
        // # https.proxyHost
        System.setProperty(PROPERTY_HTTPS_PROXY_HOST, proxyHost);

        // # https.proxyPort
        System.setProperty(PROPERTY_HTTPS_PROXY_PORT, Integer.toString(proxyPort));
    }

    /**
     * This class returns the proxy configuration for the associated URI.
     * @param uri reference URI used to get the proper proxy
     * @return ProxyConfig instance or ProxyConfig.NONE
     */
    public static ProxyConfig getProxyConfiguration(final URI uri) {
        if (uri != null) {
            final ProxySelector proxySelector = ProxySelector.getDefault();
            final List<Proxy> proxyList = proxySelector.select(uri);
            final Proxy proxy = proxyList.get(0);

            _logger.debug("using {}", proxy);

            if (proxy.type() != Proxy.Type.DIRECT) {
                final String hostname;
                final InetSocketAddress epoint = (InetSocketAddress) proxy.address();
                if (epoint.isUnresolved()) {
                    hostname = epoint.getHostName();
                } else {
                    hostname = epoint.getAddress().getHostName();
                }
                final int port = epoint.getPort();

                if ((hostname != null) && !hostname.isEmpty() && (port > 0)) {
                    return new ProxyConfig(hostname, port);
                }
            }
        }
        return ProxyConfig.NONE;
    }

    /**
     * Get JMMC HTTP URI
     * @return JMMC HTTP URI
     */
    public static URI getJmmcHttpURI() {
        if (JMMC_WEB_URI == null) {
            JMMC_WEB_URI = URI.create(JMMC_WEB_URL);
        }
        return JMMC_WEB_URI;
    }

    /**
     * Return the IP address of the given host name
     * @param hostname host name to lookup
     * @return IP address
     */
    public static String getHostIP(final String hostname) {
        return getHostIP(hostname, null);
    }

    /**
     * Return the IP address of the given host name
     * @param hostname host name to lookup
     * @param defaultIP (optional) default IP address (hard-coded)
     * @return IP address
     */
    public static String getHostIP(final String hostname, final String defaultIP) {
        String ipAddr = null;
        if (!StringUtils.isTrimmedEmpty(hostname)) {
            try {
                // DNS query (if the network is available):
                ipAddr = InetAddress.getByName(hostname).getHostAddress();

            } catch (UnknownHostException uhe) {
                _logger.error("Host resolution failed: {}", uhe.getMessage());
            }

            if (StringUtils.isEmpty(ipAddr)) {
                _logger.info("Get the IP address from CommonPreferences.");

                final CommonPreferences prefs = CommonPreferences.getInstance();

                final String prefKey = PREFIX_PREFERENCE_IP + hostname;

                // ignore if missing
                final String addrPref = prefs.getPreference(prefKey, true);

                if (!StringUtils.isTrimmedEmpty(addrPref)) {
                    ipAddr = addrPref;
                }
            }

            if (StringUtils.isEmpty(ipAddr) && !StringUtils.isEmpty(defaultIP)) {
                _logger.info("Use the hard-coded IP address.");
                ipAddr = defaultIP;
            }

            _logger.info("getHostIP[{}] = {}", hostname, ipAddr);
        }
        return ipAddr;
    }
}
