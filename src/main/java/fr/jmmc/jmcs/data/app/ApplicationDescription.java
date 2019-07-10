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
package fr.jmmc.jmcs.data.app;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.data.app.model.ApplicationData;
import fr.jmmc.jmcs.data.app.model.Company;
import fr.jmmc.jmcs.data.app.model.Compilation;
import fr.jmmc.jmcs.data.app.model.Distribution;
import fr.jmmc.jmcs.data.app.model.Menubar;
import fr.jmmc.jmcs.data.app.model.Package;
import fr.jmmc.jmcs.data.app.model.Prerelease;
import fr.jmmc.jmcs.data.app.model.Program;
import fr.jmmc.jmcs.data.app.model.Release;
import fr.jmmc.jmcs.gui.action.ShowReleaseNotesAction;
import fr.jmmc.jmcs.gui.component.ResizableTextViewFactory;
import fr.jmmc.jmcs.network.http.Http;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import fr.jmmc.jmcs.util.jaxb.JAXBFactory;
import fr.jmmc.jmcs.util.jaxb.XmlBindException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the link between the application
 * XML file which stocked the application informations like
 * it's name, version, compiler etc... called <b>ApplicationData.xml</b>,
 * which is saved into the application module, and the others classes
 * which use it to access to the informations like <b>AboutBox</b>,
 * <b>SplashScreen</b> etc...
 *
 * This class uses <b>JAXB</b> classes to access to these informations
 * and provides the good getters for each field of the XML file.
 *
 * @author Guillaume MELLA, Brice COLUCCI, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class ApplicationDescription {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(ApplicationDescription.class.getName());
    /** flag to dump release versions and dates */
    private static boolean DUMP_RELEASES = false;
    /** Package name for JAXB generated code */
    private static final String APP_DATA_MODEL_JAXB_PATH = "fr.jmmc.jmcs.data.app.model";
    /** Application data file i.e. "ApplicationData.xml" */
    private static final String APPLICATION_DATA_FILE = "ApplicationData.xml";
    /** Shared application data model */
    private static ApplicationDescription _appDataModel = null;
    /** jMCS application data model */
    private static ApplicationDescription _defaultDataModel = null;
    /** Flag indicating that loading the application data model happened */
    private static boolean _loadAppDataModel = false;

    /**
     * ApplicationDescription instance initialization.
     */
    public static void init() {
        getJmcsInstance();
        resetInstance();
        getInstance();
    }

    /**
     * @return jMCS ApplicationDescription instance.
     */
    public static synchronized ApplicationDescription getJmcsInstance() {
        if (_defaultDataModel == null) {
            loadJMcsData();
        }
        return _defaultDataModel;
    }

    /**
     * Reset the application ApplicationDescription instance.
     */
    private static synchronized void resetInstance() {
        _appDataModel = null;
        _loadAppDataModel = false;
    }

    /**
     * @return ApplicationDescription instance.
     */
    public static synchronized ApplicationDescription getInstance() {
        if (_appDataModel == null) {
            if (!_loadAppDataModel) {
                // only try once:
                _loadAppDataModel = true;
                loadApplicationData();
            }
            // if application is undefined: _appDataModel is still null and uses the default ApplicationData.xml.
            if (_appDataModel == null) {
                return getJmcsInstance();
            }
        }
        return _appDataModel;
    }

    /**
     * Check application updates (program version) using the optional Distribution information
     */
    public static synchronized void checkUpdates() {
        if (_appDataModel == null) {
            return;
        }
        final Distribution dist = _appDataModel.getDistribution();

        if (dist != null && dist.isSetApplicationDataFile()) {
            // Note: check updates at every application launch to gather better usage statistics (jnlp & java launchers)

            String url = isAlphaVersion() ? dist.getAlphaUrl() : null;

            if (url == null) {
                url = isBetaVersion() ? dist.getBetaUrl() : null;
            }
            if (url == null) {
                url = dist.getPublicUrl();
            }

            final String distURL = url;

            if (distURL != null) {

                // Make all the network stuff run in the background
                ThreadExecutors.getGenericExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String currentVersion = _appDataModel.getProgramVersion();
                            final Date currentPubDate = _appDataModel.parsePubDate(currentVersion);

                            _logger.info("Current application version: {} @ {}",
                                    _appDataModel.getProgramNameWithVersion(), currentPubDate);

                            dumpVersions(_appDataModel);

                            final String appName = _appDataModel.getProgramName();
                            final float currentVersionNum = parseVersion(currentVersion, true);

                            _logger.debug("currentName: {}", appName);
                            _logger.debug("currentVersion: {} = {}", currentVersion, currentVersionNum);

                            final File tmpFile = FileUtils.getTempFile(appName + '-', ".xml");

                            final URI uri = new URI(distURL + dist.getApplicationDataFile());

                            _logger.info("downloading {} to {}", uri, tmpFile);

                            if (Http.download(uri, tmpFile, false)) {
                                // check version and release notes ...
                                _logger.info("{} downloaded: {} bytes", tmpFile, tmpFile.length());

                                final ApplicationDescription remoteAppDataModel = loadDescription(tmpFile);

                                final String remoteVersion = remoteAppDataModel.getProgramVersion();
                                final Date remotePubDate = remoteAppDataModel.parsePubDate(remoteVersion);

                                _logger.info("Remote application version: {} @ {}",
                                        remoteAppDataModel.getProgramNameWithVersion(), remotePubDate);

                                // Check program name and version increment
                                final String remoteName = remoteAppDataModel.getProgramName();
                                _logger.debug("remoteName: {}", remoteName);

                                if (appName.equalsIgnoreCase(remoteName)) {

                                    final float remoteVersionNum = parseVersion(remoteVersion, true);

                                    _logger.debug("remoteVersion: {} = {}", remoteVersion, remoteVersionNum);

                                    dumpVersions(remoteAppDataModel);

                                    if (remoteVersionNum > currentVersionNum
                                            || (remotePubDate != null && currentPubDate != null && remotePubDate.after(currentPubDate))) {

                                        _logger.info("Application update available: {} < {}", currentVersion, remoteVersion);

                                        final StringBuilder html = new StringBuilder(4 * 1024);
                                        html.append("<html><body><h1>New <b>").append(appName).append("</b> release available:</h1><br>")
                                                .append(remoteVersion).append(" > ").append(currentVersion)
                                                .append("<br><br>Release date: ").append(remotePubDate)
                                                .append(".<br><br>Please try restarting the application (Java Web Start)")
                                                .append("<br><br>or use the following link:<br><a href=\"").append(distURL)
                                                .append("\">download</a><br><br><h2>Changes:</h2>");

                                        // Show all changes since currentVersion:
                                        ShowReleaseNotesAction.generateReleaseNotesHtml(_appDataModel, currentVersion, html);

                                        html.append("</body></html>");

                                        ResizableTextViewFactory.createHtmlWindow(html.toString(), appName + " update available", true);

                                    } else {
                                        _logger.info("Application is up-to-date: {} >= {}", currentVersion, remoteVersion);
                                    }
                                }
                            }
                        } catch (URISyntaxException use) {
                            _logger.warn("Bad URI:", use);
                        } catch (IOException ioe) {
                            _logger.info("IO failure (no network / internet access ?):", ioe);
                        } catch (RuntimeException re) {
                            _logger.info("Runtime exception occured:", re);
                        }
                    }
                });
            }
        }
    }

    private static void dumpVersions(ApplicationDescription appData) {
        if (DUMP_RELEASES) {
            for (Release r : appData.getReleases()) {
                _logger.info("Release {}: {} @ {}", r.getVersion(),
                        parseVersion(r.getVersion(), true),
                        appData.parsePubDate(r.getVersion()));

                for (Prerelease p : r.getPrereleases()) {
                    _logger.info("Prerelease {}: {} @ {}", p.getVersion(),
                            parseVersion(p.getVersion(), true),
                            appData.parsePubDate(p.getVersion()));
                }
            }
        }
    }

    /**
     * Custom loader to load an ApplicationDescription from any classpath URL (module for example)
     * @param classLoaderPath path to any file included in the application class loader 
     * @return new loaded and parsed ApplicationDescription instance
     * @throws IllegalStateException if the given URL can not be loaded
     */
    public static ApplicationDescription loadDescription(final String classLoaderPath) throws IllegalStateException {
        // TODO: fix that code : To be discussed
        final URL fileURL = ResourceUtils.getResource(classLoaderPath);
        return new ApplicationDescription(fileURL);
    }

    /**
     * Custom loader to load an ApplicationDescription from any File
     * @param filePath file path
     * @return new loaded and parsed ApplicationDescription instance
     * @throws IllegalStateException if the given file can not be loaded
     * @throws MalformedURLException if the given file path can not be converted to an URL
     */
    public static ApplicationDescription loadDescription(final File filePath) throws IllegalStateException, MalformedURLException {
        return new ApplicationDescription(filePath.toURI().toURL());
    }

    /**
     * Load the default ApplicationData.xml
     * @throws IllegalStateException if the default ApplicationData.xml can not be loaded
     */
    private static void loadJMcsData() throws IllegalStateException {
        final URL defaultXmlURL = ResourceUtils.getUrlFromResourceFilename(App.class, APPLICATION_DATA_FILE);
        if (defaultXmlURL == null) {
            throw new IllegalStateException("Cannot load default application data.");
        }
        _logger.info("Loading default application data from '{}' file.", defaultXmlURL);
        _defaultDataModel = new ApplicationDescription(defaultXmlURL);
    }

    /**
     * Load application data if ApplicationData.xml exists into the module.
     */
    private static void loadApplicationData() {
        final URL fileURL = ResourceUtils.getUrlFromResourceFilename(APPLICATION_DATA_FILE);
        if (fileURL != null) {
            try {
                _logger.info("Loading application data from '{}' file.", fileURL);
                _appDataModel = new ApplicationDescription(fileURL);
            } catch (IllegalStateException iae) {
                _logger.error("Could not load application data from '{}' file.", fileURL, iae);
            }
        }
    }

    /**
     * Tell if the application is an alpha version or not.
     * This flag is given searching one 'a' in the program version number.
     * If one b is present the version is considered beta.
     *
     * @return true if it is a alpha, false otherwise.
     */
    public static boolean isAlphaVersion() {
        return isAlphaVersion(getInstance().getProgramVersion());
    }

    /**
     * Tell if the application is an alpha version or not.
     * This flag is given searching one 'a' in the program version number.
     * If one b is present the version is considered beta.
     *
     * @param version value of the "program version" element
     * @return true if it is a alpha, false otherwise.
     */
    public static boolean isAlphaVersion(final String version) {
        if (isBetaVersion(version)) {
            return false;
        }
        return (version != null) && version.contains("a");
    }

    /**
     * Tell if the application is a beta version or not.
     * This flag is given searching one 'b' in the program version number.
     *
     * @return true if it is a beta, false otherwise.
     */
    public static boolean isBetaVersion() {
        return isBetaVersion(getInstance().getProgramVersion());
    }

    /**
     * Tell if the application is a beta version or not.
     * This flag is given searching one 'b' in the program version number.
     *
     * @param version value of the "program version" element
     * @return true if it is a beta, false otherwise.
     */
    public static boolean isBetaVersion(final String version) {
        return (version != null) && version.contains("b");
    }

    /**
     * Parse the application's version string (0.9.4 beta 11 for example) as a float number to be comparable
     * @param version version as string
     * @return version number as float
     */
    public static float parseVersion(final String version) {
        return parseVersion(version, false);
    }

    /**
     * Parse the application's version string (0.9.4 beta 11 for example) as a float number to be comparable
     * @param version version as string
     * @param roundUp true to round up for major release (not beta nor alpha)
     * @return version number as float
     */
    public static float parseVersion(final String version, final boolean roundUp) {
        float res = 0f;

        // Extract first numeric part '0.'
        String tmp = version;
        final String first;

        int pos = tmp.indexOf('.');
        if (pos != -1) {
            pos++;
            first = tmp.substring(0, pos);
            tmp = tmp.substring(pos);
        } else {
            first = "0.";
        }

        // Remove whitespace and '.' in "9.4 beta 11" => "94beta11":
        tmp = StringUtils.removeNonAlphaNumericChars(tmp);

        String level = null;
        if (roundUp) {
            if (isBetaVersion(tmp)) {
                level = "009";
            } else if (isAlphaVersion(tmp)) {
                level = "001";
            } else {
                level = "099";
            }
        }

        final String[] parts = StringUtils.splitNonNumericChars(tmp);

        // (left) pad parts with 0:
        // "9.4 beta 1" => "9401"
        for (int i = 1; i < parts.length; i++) {
            final String part = parts[i];
            switch (part.length()) {
                case 0:
                    parts[i] = "00";
                    break;
                case 1:
                    parts[i] = "0" + part;
                    break;
                default:
            }
        }

        final StringBuilder sb = new StringBuilder(16);
        sb.append(first);
        if (parts.length != 0) {
            sb.append(parts[0]);
        }
        if (level != null) {
            sb.append(level);
        }
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i]);
        }
        tmp = sb.toString();

        _logger.debug("parse: {}", tmp);

        try {
            // parse tmp => 0.9411:
            res = Float.parseFloat(tmp);
        } catch (NumberFormatException nfe) {
            _logger.info("Unable to parse version: {}", version);
        }
        return res;
    }

    // Members
    /** The JAVA class which JAXB has generated with the XSD file */
    private ApplicationData _applicationData = null;
    /** The JAVA class which JAXB has generated with the XSD file */
    private Company _company = null;
    /** Company logo resource path */
    private String _companyLogoResourcePath = null;
    /** Application logo resource path */
    private String _applicationLogoResourcePath = null;
    /** Main web page URL */
    private String _mainWebPageURL = null;
    /** URL of the PHP script that handles Feedback reports */
    private String _phpScriptURL = null;
    /** Feedback report window header message in HTML format */
    private String _feedbackReportHeaderMessage = null;
    /** Authors list */
    private String _authors = null;
    /** Used throughout all jMCS GUI */
    private String _shortCompanyName = null;
    /** Used by SAMP */
    private String _legalCompanyName = null;
    /** User Support URL */
    private String _userSupportUrl = null;
    /** RSS URL */
    private String _hotNewsRSSFeedLink = null;
    /** FAQ URL */
    private String _faqLink = null;
    /** Documentation URL */
    private String _documentationLink = null;

    /**
     * Public constructor
     * @param dataModelURL location of the file to load
     * @throws IllegalStateException if the given URL can not be loaded
     */
    private ApplicationDescription(final URL dataModelURL) throws IllegalStateException {
        _logger.debug("Loading Application data model from {}", dataModelURL);

        // Load application data
        _applicationData = loadData(dataModelURL);

        final String programName = getProgramName();

        _feedbackReportHeaderMessage = "<html><body>"
                + "<center>"
                + "<big>Welcome to '" + programName + "' Feedback Report</big><br>"
                + "We are eager to get your feedback, questions or comments !<br>"
                + "So please do not hesitate to use this form.<br>"
                + "</center>"
                + "<br><br>"
                + "Moreover, we encourage you to provide us with your e-mail address, so we can :"
                + "<ul>"
                + "<li>keep you up to date on the status of your request;</li>"
                + "<li>ask you more information if needed.</li>"
                + "</ul>"
                + "<em>(*) Summary and description must be filled to enable the 'Submit' button.</em>"
                + "</body></html>";

        // Load company meta data
        _company = _applicationData.getCompany();

        // Mandatory data
        _shortCompanyName = _company.getShortName();
        _legalCompanyName = _shortCompanyName;
        _companyLogoResourcePath = _company.getLogoResource();
        _mainWebPageURL = _company.getHomepageUrl();

        // Optionnal data
        if (_company.isSetLegalName()) {
            _legalCompanyName = _company.getLegalName();
        }
        if (_applicationData.isSetAuthors()) {
            _authors = _applicationData.getAuthors();
        }
        if (_company.isSetFeedbackFormUrl()) {
            _phpScriptURL = _company.getFeedbackFormUrl();
        }
        if (_company.isSetUserSupportUrl()) {
            _userSupportUrl = _company.getUserSupportUrl();
        }
        if (_applicationData.isSetFaqlink()) {
            _faqLink = _applicationData.getFaqlink();
        }
        if (_applicationData.isSetRsslink()) {
            _hotNewsRSSFeedLink = _applicationData.getRsslink();
        }
        if (_applicationData.isSetDocumentationlink()) {
            _documentationLink = _applicationData.getDocumentationlink();
        }
        if (_applicationData.isSetLogoResource()) {
            _applicationLogoResourcePath = _applicationData.getLogoResource();
        }

        _logger.debug("Application data model loaded.");
    }

    /** 
     * Invoke JAXB to load ApplicationData.xml file
     * @param dataModelURL url pointing to the ApplicationData.xml file
     * @return ApplicationData instance
     */
    private static ApplicationData loadData(final URL dataModelURL) throws XmlBindException, IllegalArgumentException, IllegalStateException {

        final JAXBFactory jf = JAXBFactory.getInstance(APP_DATA_MODEL_JAXB_PATH);
        _logger.debug("JAXBFactory: {}", jf);

        // Note : use input stream to avoid JNLP offline bug with URL (Unknown host exception)
        try {
            final Unmarshaller u = jf.createUnMarshaller();
            return (ApplicationData) u.unmarshal(new BufferedInputStream(dataModelURL.openStream()));
        } catch (IOException ioe) {
            throw new IllegalStateException("Load failure on " + dataModelURL, ioe);
        } catch (JAXBException je) {
            throw new IllegalArgumentException("Load failure on " + dataModelURL, je);
        }
    }

    /**
     * @return the value of the "Acknowledgment" field from the XML file  if any, null otherwise.
     */
    public String getAcknowledgment() {
        if (_applicationData.getAcknowledgment() == null) {
            _logger.debug("_applicationDataCastorModel.getAcknowledgment() is null");

            return null;
        }

        return _applicationData.getAcknowledgment();
    }

    /**
     * @return the company logo resource path
     */
    public String getCompanyLogoResourcePath() {
        _logger.debug("companyLogoResourcePath: {}", _companyLogoResourcePath);

        return _companyLogoResourcePath;
    }

    /**
     * @return the application logo resource path
     */
    public String getApplicationLogoResourcePath() {
        _logger.debug("applicationLogoResourcePath: {}", _applicationLogoResourcePath);

        return _applicationLogoResourcePath;
    }

    /**
     * @return the application main web page URL
     */
    public String getMainWebPageURL() {
        return _mainWebPageURL;
    }

    /**
     * @return the feedback report form URL if any, null otherwise.
     */
    public String getFeedbackReportFormURL() {
        return _phpScriptURL;
    }

    /**
     * @return the feedback report window header message
     */
    public String getFeedbackReportHeaderMessage() {
        return _feedbackReportHeaderMessage;
    }

    /**
     * @return the value of the "program" element name from the XML file
     */
    public String getProgramName() {
        String programName = "Unknown";

        // Get program
        final Program program = _applicationData.getProgram();

        if (program != null) {
            programName = program.getName();
        }

        _logger.debug("Program name: {}", programName);

        return programName;
    }

    /**
     * @return the value of the "program version" element from the XML file
     */
    public String getProgramVersion() {
        String programVersion = "?.?";

        // Get program
        final Program program = _applicationData.getProgram();

        if (program != null) {
            programVersion = program.getVersion();
        }

        _logger.debug("Program version: {}", programVersion);
        return programVersion;
    }

    /**
     * @return the value of the "program" element name from the XML file
     */
    public String getProgramNameWithVersion() {
        return getProgramName() + " v" + getProgramVersion();
    }

    /**
     * @return the application main web page URL from the "link" field in the XML file
     */
    public String getLinkValue() {
        String linkValue = _applicationData.getLink();
        _logger.debug("Link value is: {}", linkValue);

        return linkValue;
    }

    /**
     * @return the application FAQ URL if any, null otherwise.
     */
    public String getFaqLinkValue() {
        _logger.debug("FaqLink value is: {}", _faqLink);

        return _faqLink;
    }

    /**
     * @return the application Hot News RSS feed URL if any, null otherwise.
     */
    public String getHotNewsRSSFeedLinkValue() {
        _logger.debug("HotNewsRSSFeedLink value is: {}", _hotNewsRSSFeedLink);

        return _hotNewsRSSFeedLink;
    }

    /**
     * @return the application Documentation URL if any, null otherwise.
     */
    public String getDocumentationLinkValue() {
        _logger.debug("DocumentationLink value is: {}", _documentationLink);

        return _documentationLink;
    }

    /**
     * @return the value of the element compilation date from the XML file
     */
    public String getCompilationDate() {
        String compilationDate = "Unknown";

        // Get compilation
        final Compilation compilation = _applicationData.getCompilation();

        if (compilation != null) {
            compilationDate = compilation.getDate();
        }

        _logger.debug("Compilation date: {}", compilationDate);
        return compilationDate;
    }

    /**
     * @return the value of the element compiler version from the XML file
     */
    public String getCompilatorVersion() {
        String compilationCompilator = "Unknown";

        // Get compilation
        final Compilation compilation = _applicationData.getCompilation();

        if (compilation != null) {
            compilationCompilator = compilation.getCompiler();
        }

        _logger.debug("Compilation compilator: {}", compilationCompilator);

        return compilationCompilator;
    }

    /**
     * @return the application description used in the AboutBox
     */
    public String getTextValue() {
        String text = _applicationData.getText();
        _logger.debug("Text value: {}", text);

        return text;
    }

    /**
     * @return the value of the "authors" field from the XML file if any, null otherwise.
     */
    public String getAuthors() {
        return _authors;
    }

    /**
     * @return list of dependency packages.
     */
    public List<Package> getPackages() {
        return _applicationData.getDependences().getPackages();
    }

    /**
     * @return Forge the "copyright" text used in the AboutBox
     */
    public String getCopyrightValue() {
        final String compilationDate = getCompilationDate();
        int year;
        try {
            // Try to get the year from the compilation date
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date date = df.parse(compilationDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
        } catch (ParseException pe) {
            _logger.warn("Cannot parse date '{}' will use current year instead.", compilationDate, pe);

            // Otherwise use the current year
            Calendar cal = new GregorianCalendar();
            year = cal.get(Calendar.YEAR);
        }

        return "Copyright " + SpecialChars.SYMBOL_COPYRIGHT + " " + year + ", " + _shortCompanyName + ".";
    }

    /**
     * @return menu bar from XML description
     */
    public Menubar getMenubar() {
        return _applicationData.getMenubar();
    }

    /**
     * @return company short name
     */
    public String getShortCompanyName() {
        return _shortCompanyName;
    }

    /**
     * @return company legal name if any, short name otherwise.
     */
    public String getLegalCompanyName() {
        return _legalCompanyName;
    }

    /**
     * @return User Support URL if any, null otherwise.
     */
    public String getUserSupportURL() {
        return _userSupportUrl;
    }

    /**
     * @return SAMP description if any, null otherwise.
     */
    public String getSampDescription() {
        return _applicationData.getSampdescription();
    }

    public String getJnlpUrl() {
        return _applicationData.getJnlp();
    }

    public Distribution getDistribution() {
        return _applicationData.getDistribution();
    }

    /**
     * Return the release notes
     * @return release list.
     */
    public List<Release> getReleases() {
        return _applicationData.getReleasenotes().getReleases();
    }

    public Date parsePubDate() {
        return parsePubDate(getProgramVersion());
    }

    public Date parsePubDate(final String version) {
        final String pubDate = getPubDate(version);

        if (pubDate != null) {
            final DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
            try {
                return df.parse(pubDate);
            } catch (ParseException pe) {
                _logger.warn("Cannot parse publication date '{}'.", pubDate, pe);
            }
        }
        return null;
    }

    public String getPubDate() {
        return getPubDate(getProgramVersion());
    }

    public String getPubDate(final String version) {
        for (Release r : getReleases()) {
            final String releasePubDate = r.getPubDate();

            if (version.equalsIgnoreCase(r.getVersion())) {
                return releasePubDate;
            }

            for (Prerelease p : r.getPrereleases()) {
                final String preReleasePubDate = r.getPubDate();

// TODO TRY            DateFormat.getDateTimeInstance().parse(version)
                if (version.equalsIgnoreCase(p.getVersion())) {
                    return (preReleasePubDate != null) ? preReleasePubDate : releasePubDate;
                }
            }
        }
        return null;
    }
}
/*___oOo___*/
