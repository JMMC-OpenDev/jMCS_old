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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to get informations from one central property file.
 * Applications must start to set the resource file name before any GUI construction.
 * 
 * @author Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public abstract class PropertyUtils {

    /** the logger facility */
    private static final Logger _logger = LoggerFactory.getLogger(PropertyUtils.class.getName());
    /** Resource filename, that must be overloaded by subclasses */
    private static String _resourceName = "fr/jmmc/jmcs/resource/Resources";
    /** Cached resource bundle */
    private static ResourceBundle _resources = null;
    /** Flag to indicate whether the resource bundle is resolved or not */
    private static boolean _resolved = false;
    /** Store whether the execution platform is a Mac or not */
    private static final boolean MAC_OS_X = SystemUtils.IS_OS_MAC_OSX;

    /**
     * Indicates the property file where informations will be extracted.
     * The property file must end with .properties filename extension. But the
     * given name should omit the extension.
     *
     * @param name Indicates property file to use.
     */
    public static void setResourceName(final String name) {
        _logger.debug("Application will grab resources from '{}'", name);
        _resourceName = name;
        _resolved = false;
    }

    /**
     * Get content from resource file.
     *
     * @param resourceName name of resource
     *
     * @return the content of the resource or null indicating error
     */
    public static String getResourceProperty(final String resourceName) {
        return getResourceProperty(resourceName, false);
    }

    /**
     * Get content from resource file.
     *
     * @param resourceKey name of resource
     * @param quietIfNotFound true to not log at warning level i.e. debug level
     *
     * @return the content of the resource or null indicating error
     */
    public static String getResourceProperty(final String resourceKey, final boolean quietIfNotFound) {
        if (_resources == null) {

            if (!_resolved) {
                _logger.debug("getResource for '{}'", _resourceName);
                try {
                    // update the resolve flag to avoid redundant calls to getBundle when no bundle is available:
                    _resolved = true;
                    _resources = ResourceBundle.getBundle(_resourceName);
                } catch (MissingResourceException mre) {
                    if (quietIfNotFound) {
                        _logger.debug("Resource bundle can't be found : {}", mre.getMessage());
                    } else {
                        _logger.warn("Resource bundle can't be found : {}", mre.getMessage());
                    }
                }
            }

            if (_resources == null) {
                return null;
            }
        }

        _logger.debug("getResource for '{}'", resourceKey);
        try {
            return _resources.getString(resourceKey);
        } catch (MissingResourceException mre) {
            if (quietIfNotFound) {
                _logger.debug("Entry can't be found : {}", mre.getMessage());
            } else {
                _logger.warn("Entry can't be found : {}", mre.getMessage());
            }
        }

        return null;
    }

    /**
     * Get the text of an action.
     *
     * @param actionName the actionInstanceName
     *
     * @return the associated text
     */
    public static String getActionText(final String actionName) {
        return getResourceProperty("actions.action." + actionName + ".text", true);
    }

    /**
     * Get the description of an action.
     *
     * @param actionName the actionInstanceName
     *
     * @return the associated description
     */
    public static String getActionDescription(final String actionName) {
        return getResourceProperty("actions.action." + actionName + ".description", true);
    }

    /**
     * Get the tool-tip text of widget related to the common widget group.
     *
     * @param widgetName the widgetInstanceName
     *
     * @return the tool-tip text
     */
    public static String getToolTipText(final String widgetName) {
        return getResourceProperty("widgets.widget." + widgetName + ".tooltip", true);
    }

    /**
     * Get the accelerator (aka. keyboard short cut) of an action .
     *
     * @param actionName the actionInstanceName
     *
     * @return the associated accelerator
     */
    public static KeyStroke getActionAccelerator(final String actionName) {
        // Get the accelerator string description from the Resource.properties file
        String keyString = getResourceProperty("actions.action." + actionName + ".accelerator", true);

        if (keyString == null) {
            return null;
        }

        // If the execution is on Mac OS X
        if (MAC_OS_X) {
            // The 'command' key (aka Apple key) is used
            keyString = "meta " + keyString;
        } else {
            // The 'control' key ise used elsewhere
            keyString = "ctrl " + keyString;
        }

        // Get and return the KeyStroke from the accelerator string description
        KeyStroke accelerator = KeyStroke.getKeyStroke(keyString);

        if (_logger.isDebugEnabled()) {
            _logger.debug("keyString['{}'] = '{}' -> accelerator = '{}'.",
                    actionName, keyString, accelerator);
        }

        return accelerator;
    }

    /**
     * Get the icon of an action.
     *
     * @param actionName the actionInstanceName
     *
     * @return the associated icon
     */
    public static ImageIcon getActionIcon(final String actionName) {
        // Get back the icon image path
        String iconPath = getResourceProperty("actions.action." + actionName + ".icon", true);
        return ImageUtils.loadResourceIcon(iconPath);
    }

    /**
     * Private constructor
     */
    private PropertyUtils() {
        super();
    }
}
/*___oOo___*/
