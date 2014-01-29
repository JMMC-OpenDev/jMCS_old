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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains URL related utility methods.
 * 
 * @author Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public class UrlUtils {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(UrlUtils.class.getName());
    /** URL encoding use UTF-8 */
    private static final String URL_ENCODING = "UTF-8";

    /**
     * Private constructor
     */
    private UrlUtils() {
        super();
    }

    /**
     * http://forums.sun.com/thread.jspa?messageID=10522645
     *
     * @param url the URL to fix
     *
     * @return the fixed URL
     */
    public static URL fixJarURL(final URL url) {
        if (url == null) {
            return null;
        }

        // final String method = _module + ".fixJarURL";
        final String originalURLProtocol = url.getProtocol();

        _logger.debug("examining '{}' protocol url: {}", originalURLProtocol, url);

        if (!"jar".equalsIgnoreCase(originalURLProtocol)) {
            _logger.debug("skipping fix: URL is not 'jar' protocol: {}", url);
            return url;
        }

        _logger.debug("URL is jar protocol, continuing");

        final String originalURLString = url.toString();

        _logger.debug("using originalURLString: {}", originalURLString);

        final int bangSlashIndex = originalURLString.indexOf("!/");

        if (bangSlashIndex > -1) {
            _logger.debug("skipping fix: originalURLString already has bang-slash: {}", originalURLString);
            return url;
        }

        _logger.debug("originalURLString needs fixing (it has no bang-slash)");

        final String originalURLPath = url.getPath();

        _logger.debug("using originalURLPath: {}", originalURLPath);

        URLConnection urlConnection;

        try {
            urlConnection = url.openConnection();

            if (urlConnection == null) {
                throw new Exception("urlConnection is null");
            }
        } catch (Exception e) {
            // skip complex case
            _logger.debug("skipping fix: openConnection() exception: ", e);
            return url;
        }

        _logger.debug("using urlConnection: {}", urlConnection);

        Permission urlConnectionPermission;

        try {
            urlConnectionPermission = urlConnection.getPermission();

            if (urlConnectionPermission == null) {
                throw new Exception("urlConnectionPermission is null");
            }
        } catch (Exception e) {
            // skip complex case
            _logger.debug("skipping fix: getPermission() exception:", e);
            return url;
        }

        _logger.debug("using urlConnectionPermission: {}", urlConnectionPermission);

        final String urlConnectionPermissionName = urlConnectionPermission.getName();

        if (urlConnectionPermissionName == null) {
            _logger.debug("skipping fix: urlConnectionPermissionName is null");
            return url;
        }

        _logger.debug("using urlConnectionPermissionName: {}", urlConnectionPermissionName);

        final File file = new File(urlConnectionPermissionName);

        if (!file.exists()) {
            _logger.debug("skipping fix: file does not exist: {}", file);
            return url;
        }

        _logger.debug("using file: {}", file);

        String newURLStr;

        try {
            newURLStr = "jar:" + file.toURL().toExternalForm() + "!/" + originalURLPath;

        } catch (MalformedURLException mue) {
            _logger.debug("skipping fix: exception creating newURLStr", mue);
            return url;
        }

        _logger.debug("using newURLStr: {}", newURLStr);
        try {
            return new URL(newURLStr);
        } catch (MalformedURLException mue) {
            _logger.debug("skipping fix: exception creating new URL", mue);
        }

        return url;
    }

    /**
     * Parse the given URL.
     * @param url URL as string.
     * @return URL object.
     * @throws IllegalStateException if the URL is malformed.
     */
    public static URL parseURL(final String url) throws IllegalStateException {
        try {
            return new URL(url);
        } catch (MalformedURLException mue) {
            throw new IllegalStateException("Cannot parse url " + url, mue);
        }
    }

    /**
     * Encode the given query string into <code>application/x-www-form-urlencoded</code>
     * @param queryString query string to encode
     * @return encoded query string
     * @throws IllegalStateException if the UTF-8 encoding is not supported
     */
    public static String encode(final String queryString) throws IllegalStateException {
        try {
            return URLEncoder.encode(queryString, URL_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Unsupported encoding : " + URL_ENCODING, uee);
        }
    }
}
