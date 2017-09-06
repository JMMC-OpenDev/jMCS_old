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
package fr.jmmc.jmcs.gui.action;

import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.data.app.model.Change;
import fr.jmmc.jmcs.data.app.model.Prerelease;
import fr.jmmc.jmcs.data.app.model.Release;
import fr.jmmc.jmcs.gui.component.ResizableTextViewFactory;
import fr.jmmc.jmcs.util.StringUtils;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action generates release notes for the given ApplicationDescription.
 * @author Laurent BOURGES, Sylvain LAFRASSE.
 */
public final class ShowReleaseNotesAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String _className = ShowReleaseNotesAction.class.getName();
    /** Class logger */
    private final static Logger _logger = LoggerFactory.getLogger(_className);
    // Members
    /** Description to extract release notes from */
    private final ApplicationDescription _applicationDescription;
    /** Title */
    private String _windowTitle = null;
    /** HTML content (cached) */
    private String _windowContent = null;

    /**
     * Constructor that use the default ApplicationDescription instance and generate title automatically.
     * @param actionName the name of the action.
     */
    public ShowReleaseNotesAction(final String actionName) {
        super(_className, actionName, "Release Notes");
        _applicationDescription = ApplicationDescription.getInstance();
    }

    /**
     * Constructor that automatically register the action in RegisteredAction.
     * 
     * @param actionName the name of the action.
     * @param titlePrefix title prefix to use in window title and HTML content
     * @param applicationDescription application description to use
     */
    public ShowReleaseNotesAction(final String actionName, final String titlePrefix, final ApplicationDescription applicationDescription) {
        super(_className, actionName);
        _windowTitle = titlePrefix;
        _applicationDescription = applicationDescription;
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        _logger.debug("actionPerformed");

        // Lazily compute content only once
        if (_windowContent == null) {
            _windowContent = generateHtml();
        }

        ResizableTextViewFactory.createHtmlWindow(_windowContent, _windowTitle, false);
    }

    /** 
     * Generate HTML content
     * @return HTML content
     */
    private String generateHtml() {

        // Compute title (if none)
        if (_windowTitle == null) {
            _windowTitle = _applicationDescription.getProgramNameWithVersion();
        }
        _windowTitle += " Release Notes";

        // Compose standard header
        final StringBuilder html = new StringBuilder(8 * 1024);
        html.append("<html><body>");
        html.append("<h1><center><b>").append(_windowTitle).append("</b></center></h1>\n");

        generateReleaseNotesHtml(_applicationDescription, null, html);

        html.append("</body></html>");

        return html.toString();
    }

    public static void generateReleaseNotesHtml(final ApplicationDescription appData,
                                                final String lastVersion,
                                                final StringBuilder html) {

        // Extracted changes per type:
        final List<Change> changeList = new ArrayList<Change>(20);

        boolean match = false;

        for (Release r : appData.getReleases()) {

            match |= (lastVersion != null && lastVersion.equalsIgnoreCase(r.getVersion()));

            html.append("<hr>").append("<h3>").append("Version ").append(r.getVersion());
            String pubDate = r.getPubDate();
            if (pubDate == null) {
                pubDate = "no publication date yet";
            }
            html.append(" (<i>").append(pubDate).append("</i>)</h3>\n");

            match |= processChangeType("FEATURE", "Features", r.getPrereleases(), lastVersion, html, changeList);
            match |= processChangeType("CHANGE", "Changes", r.getPrereleases(), lastVersion, html, changeList);
            match |= processChangeType(null, null, r.getPrereleases(), lastVersion, html, changeList); // empty type considered as 'Change'
            match |= processChangeType("BUGFIX", "Bug Fixes", r.getPrereleases(), lastVersion, html, changeList);

            if (match) {
                break;
            }
        }
    }

    /**
     * Generate HTML for the given change type.
     * @see #findChangeByType(java.lang.String, java.util.List, java.util.List) 
     * @param type type to match or null (matches empty type)
     * @param label label to display for the given type
     * @param prereleaseList list of prerelease 
     * @param generatedHtml HTML buffer to fill
     * @param changeList temporary list of Change to fill
     * @return true if the lastVersion was found
     */
    private static boolean processChangeType(final String type, final String label, final List<Prerelease> prereleaseList,
                                             final String lastVersion,
                                             final StringBuilder generatedHtml, final List<Change> changeList) {

        final boolean match = findChangeByType(type, prereleaseList, lastVersion, changeList);

        if (!changeList.isEmpty()) {
            if (label != null) {
                generatedHtml.append(label).append(":\n");
            }
            generatedHtml.append("<ul>\n");

            for (Change c : changeList) {
                generatedHtml.append("<li>").append(c.getValue()).append("</li>\n");
            }
            generatedHtml.append("</ul>\n");
        }
        return match;
    }

    /**
     * Extract Change instances according to their type.
     * @param type type to match or null (matches empty type)
     * @param prereleaseList list of prerelease 
     * @param changeList list of Change to fill
     * @return true if Change instances found for the given type, false otherwise.
     */
    private static boolean findChangeByType(final String type, final List<Prerelease> prereleaseList,
                                            final String lastVersion, final List<Change> changeList) {
        changeList.clear();

        final boolean noType = StringUtils.isEmpty(type);

        for (Prerelease p : prereleaseList) {

            final boolean match = (lastVersion != null && lastVersion.equalsIgnoreCase(p.getVersion()));

            for (Change c : p.getChanges()) {
                if (noType) {
                    if (StringUtils.isEmpty(c.getType())) {
                        changeList.add(c);
                    }
                } else if (type.equalsIgnoreCase(c.getType())) {
                    changeList.add(c);
                }
            }

            if (match) {
                return true;
            }
        }
        return false;
    }
}
