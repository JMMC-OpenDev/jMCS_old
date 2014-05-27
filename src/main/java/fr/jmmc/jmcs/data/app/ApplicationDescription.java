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
import fr.jmmc.jmcs.data.app.model.Menubar;
import fr.jmmc.jmcs.data.app.model.Package;
import fr.jmmc.jmcs.data.app.model.Program;
import fr.jmmc.jmcs.data.app.model.Release;
import fr.jmmc.jmcs.util.jaxb.JAXBFactory;
import fr.jmmc.jmcs.util.jaxb.XmlBindException;
import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
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
    /** Package name for JAXB generated code */
    private static final String APP_DATA_MODEL_JAXB_PATH = "fr.jmmc.jmcs.data.app.model";
    /** Application data file i.e. "ApplicationData.xml" */
    private static final String APPLICATION_DATA_FILE = "ApplicationData.xml";
    /** Shared application data model */
    private static ApplicationDescription _appDataModel = null;
    /** jMCS application data model */
    private static ApplicationDescription _defaultDataModel = null;

    /**
     * ApplicationDescription instance initialization.
     */
    public static void init() {
        getJmcsInstance();
        getInstance();
    }

    /**
     * @return jMCS ApplicationDescription instance.
     */
    public static ApplicationDescription getJmcsInstance() {
        if (_defaultDataModel == null) {
            loadJMcsData();
        }
        return _defaultDataModel;
    }

    /**
     * @return ApplicationDescription instance.
     */
    public static ApplicationDescription getInstance() {
        if (_appDataModel == null) {
            loadApplicationData();
            // if application is undefined: _appDataModel is still null and uses the default ApplicationData.xml.
            if (_appDataModel == null) {
                return getJmcsInstance();
            }
        }
        return _appDataModel;
    }

    /**
     * Custom loader to load an ApplicationDescription from any URL (module for example)
     * @param filePath path to any file included in the application class loader like 
     * @return new loaded and parsed ApplicationDescription instance
     * @throws IllegalStateException if the given URL can not be loaded
     */
    public static ApplicationDescription loadDescription(final String filePath) throws IllegalStateException {
        // TODO: fix that code : To be discussed
        final URL fileURL = ResourceUtils.getResource(filePath);
        return new ApplicationDescription(fileURL);
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
        if (isBetaVersion()) {
            return false;
        }
        return getInstance().getProgramVersion().contains("a");
    }

    /**
     * Tell if the application is a beta version or not.
     * This flag is given searching one 'b' in the program version number.
     *
     * @return true if it is a beta, false otherwise.
     */
    public static boolean isBetaVersion() {
        return getInstance().getProgramVersion().contains("b");
    }
    // Members
    /** internal JAXB Factory */
    private final JAXBFactory _jf;
    /** The JAVA class which JAXB has generated with the XSD file */
    private ApplicationData _applicationData = null;
    /** The JAVA class which JAXB has generated with the XSD file */
    private Company _company = null;
    /** Logo file name */
    private String _companyLogoFileName = null;
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

    /**
     * Public constructor
     * @param dataModelURL location of the file to load
     * @throws IllegalStateException if the given URL can not be loaded
     */
    private ApplicationDescription(final URL dataModelURL) throws IllegalStateException {
        _logger.debug("Loading Application data model from {}", dataModelURL);

        // Start JAXB
        _jf = JAXBFactory.getInstance(APP_DATA_MODEL_JAXB_PATH);

        _logger.debug("JAXBFactory: {}", _jf);

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
        _companyLogoFileName = _company.getLogoResource();
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

        _logger.debug("Application data model loaded.");
    }

    /** Invoke JAXB to load ApplicationData.xml file */
    private ApplicationData loadData(final URL dataModelURL) throws XmlBindException, IllegalArgumentException, IllegalStateException {

        // Note : use input stream to avoid JNLP offline bug with URL (Unknown host exception)
        try {
            final Unmarshaller u = _jf.createUnMarshaller();
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
        _logger.debug("logoUrl: {}", _companyLogoFileName);

        return _companyLogoFileName;
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
        Program program = null;
        String programName = "Unknown";

        // Get program
        program = _applicationData.getProgram();

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
        Program program = null;
        String programVersion = "?.?";

        // Get program
        program = _applicationData.getProgram();

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
        _logger.debug("HotNewsRSSFeedLink: {}", _hotNewsRSSFeedLink);

        return _hotNewsRSSFeedLink;
    }

    /**
     * @return the value of the element compilation date from the XML file
     */
    public String getCompilationDate() {
        Compilation compilation = null;
        String compilationDate = "Unknown";

        // Get compilation
        compilation = _applicationData.getCompilation();

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
        Compilation compilation = null;
        String compilationCompilator = "Unknown";

        // Get compilation
        compilation = _applicationData.getCompilation();

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
        int year = 0;
        String compilationDate = getCompilationDate();

        try {
            // Try to get the year from the compilation date
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(compilationDate);
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

    /**
     * @return Application documentation URL if any, null otherwise.
     */
    public String getDocumetationUrl() {
        return _applicationData.getDocumentationlink();
    }

    public String getJnlpUrl() {
        return _applicationData.getJnlp();
    }

    /**
     * Return the release notes
     * @return release list.
     */
    public List<Release> getReleases() {
        return _applicationData.getReleasenotes().getReleases();
    }
}
/*___oOo___*/
