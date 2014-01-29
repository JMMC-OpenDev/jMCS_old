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

import fr.jmmc.jmcs.network.http.Http;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.util.IntrospectionUtils;
import java.lang.reflect.Method;
import java.util.Properties;
import org.apache.commons.httpclient.HostConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gathers general network settings:
 * - socket and connect timeouts
 * - proxy (host / port)
 *
 * It uses Java System properties and also JMCS Preferences to get the proxy settings
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
    /** SOCKS proxy host */
    public static final String PROPERTY_SOCKS_PROXY_HOST = "socksProxyHost";
    /** SOCKS proxy port */
    public static final String PROPERTY_SOCKS_PROXY_PORT = "socksProxyPort";
    /* JMMC standard values */
    /** Use system proxies (false by default) */
    public static final String USE_SYSTEM_PROXIES = "false";
    /** default value for the connection timeout in milliseconds (15 s) */
    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000;
    /** default value for the read timeout in milliseconds (10 minutes) */
    public static final int DEFAULT_SOCKET_READ_TIMEOUT = 10 * 60 * 1000;
    /** The default maximum number of connections allowed per host */
    public static final int DEFAULT_MAX_HOST_CONNECTIONS = 5;
    /** The default maximum number of connections allowed overall */
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 10;

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

        // first, dump all known network properties:
        final Method netPropertiesGetMethod = getNetPropertiesGetMethod();
        if (netPropertiesGetMethod != null) {
            final Properties netProperties = new Properties();
            String value;

            // HTTP Proxy:
            value = getNetProperty(netPropertiesGetMethod, PROPERTY_HTTP_PROXY_HOST);
            if (value != null) {
                netProperties.put(PROPERTY_HTTP_PROXY_HOST, value);
            }
            value = getNetProperty(netPropertiesGetMethod, PROPERTY_HTTP_PROXY_PORT);
            if (value != null) {
                netProperties.put(PROPERTY_HTTP_PROXY_PORT, value);
            }

            // SOCKS Proxy:
            value = getNetProperty(netPropertiesGetMethod, PROPERTY_SOCKS_PROXY_HOST);
            if (value != null) {
                netProperties.put(PROPERTY_SOCKS_PROXY_HOST, value);
            }
            value = getNetProperty(netPropertiesGetMethod, PROPERTY_SOCKS_PROXY_PORT);
            if (value != null) {
                netProperties.put(PROPERTY_SOCKS_PROXY_PORT, value);
            }

            if (!netProperties.isEmpty() && _logger.isInfoEnabled()) {
                _logger.info("Java net properties:\n{}", Preferences.dumpProperties(netProperties));
            }
        }

        final String proxyList = System.getProperty(PROPERTY_JAVA_PLUGIN_PROXY_LIST);
        if (proxyList != null) {
            _logger.info("Java plugin proxy list: {}", proxyList);
        }

        // Dump Http Proxy settings from ProxySelector:
        HostConfiguration hostConfiguration = Http.getHttpProxyConfiguration();

        if (hostConfiguration.getProxyHost() != null) {
            _logger.info("Found http proxy: {}:{}", hostConfiguration.getProxyHost(), hostConfiguration.getProxyPort());
        }

        // Dump Socks Proxy settings from ProxySelector:
        hostConfiguration = Http.getSocksProxyConfiguration();

        if (hostConfiguration.getProxyHost() != null) {
            _logger.info("Found socks proxy: {}:{}", hostConfiguration.getProxyHost(), hostConfiguration.getProxyPort());
        }

        // Get Proxy settings (available at least in JNLP runtime environement):
        hostConfiguration = Http.getHttpProxyConfiguration();

        if (hostConfiguration.getProxyHost() != null) {
            _logger.info("Get proxy settings from Java ProxySelector.");

            defineProxy(hostConfiguration.getProxyHost(), hostConfiguration.getProxyPort());
        } else {
            _logger.info("Get proxy settings from CommonPreferences.");

            final CommonPreferences prefs = CommonPreferences.getInstance();

            final String proxyHost = prefs.getPreference(CommonPreferences.HTTP_PROXY_HOST);
            final String proxyPort = prefs.getPreference(CommonPreferences.HTTP_PROXY_PORT);

            if (proxyHost != null && proxyHost.length() > 0) {
                if (proxyPort != null && proxyPort.length() > 0) {
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

        // # http.proxyHost
        System.setProperty(PROPERTY_HTTP_PROXY_HOST, proxyHost);

        // # http.proxyPort
        System.setProperty(PROPERTY_HTTP_PROXY_PORT, Integer.toString(proxyPort));

        // # http.nonProxyHosts
//        System.setProperty(PROPERTY_HTTP_NO_PROXY_HOSTS, "localhost|127.0.0.1");

        // TODO : support also advanced proxy settings (user, password ...)
        // # http.proxyUser
        // # http.proxyPassword
        // # http.nonProxyHosts
    }

    /**
     * Returns the sun.net.NetProperties specific property
     *
     * @param netPropertiesGetMethod sun.net.NetProperties.get(String)
     * @param key the property key
     * @return a networking system property. If no system property was defined
     * returns the default value, if it exists, otherwise returns <code>null</code>.
     */
    private static String getNetProperty(final Method netPropertiesGetMethod, final String key) {
        return (String) IntrospectionUtils.getMethodValue(netPropertiesGetMethod, new Object[]{key});
    }

    /**
     * Return the sun.net.NetProperties.get(String) method
     * @return NetProperties.get(String) method or null if unavailable
     */
    private static Method getNetPropertiesGetMethod() {
        return IntrospectionUtils.getMethod("sun.net.NetProperties", "get", new Class<?>[]{String.class});
    }
}
