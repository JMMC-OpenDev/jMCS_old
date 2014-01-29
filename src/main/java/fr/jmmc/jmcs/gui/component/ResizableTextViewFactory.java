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
package fr.jmmc.jmcs.gui.component;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.service.BrowserLauncher;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create resize-able, best-sized window able to display either plain text or HTML.
 *
 * @author Sylvain LAFRASE
 */
public class ResizableTextViewFactory {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(ResizableTextViewFactory.class.getName());
    // Constants
    private static final int MARGIN = 35;
    private static final int MINIMUM_WIDTH = 400;
    private static final int MAXIMUM_WIDTH = 700;
    private static final int MINIMUM_HEIGHT = 300;
    private static final int MAXIMUM_HEIGHT = 550;
    private static final int BUTTON_HEIGHT = 20;

    /**
     * Create a window containing the given plain text with the given title.
     * @param text plain text to show.
     * @param title window title
     * @param modal true to make the window modal, false otherwise.
     */
    public static void createTextWindow(final String text, final String title, final boolean modal) {
        createTextWindow(text, title, modal, 0);
    }

    /**
     * Create a window containing the given plain text with the given title.
     * @param text plain text to show.
     * @param title window title
     * @param modal true to make the window modal, false otherwise.
     * @param timeoutMillis timeout in milliseconds to wait before the window is hidden (auto-hide)
     */
    public static void createTextWindow(final String text, final String title, final boolean modal, final int timeoutMillis) {
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            @Override
            public void run() {
                final JDialog dialog = new JDialog(App.getFrame(), title, modal);
                final JEditorPane editorPane = startLayout(dialog);

                // if modal, blocks until the dialog is closed:
                finishLayout(editorPane, dialog, text, modal, timeoutMillis);
            }
        });
    }

    /**
     * Create a window containing the given HTML text with the given title.
     * @param html HTML text to show.
     * @param title window title
     * @param modal true to make the window modal, false otherwise.
     */
    public static void createHtmlWindow(final String html, final String title, final boolean modal) {
        createHtmlWindow(html, title, modal, 0);
    }

    /**
     * Create a window containing the given HTML text with the given title.
     * @param html HTML text to show.
     * @param title window title
     * @param modal true to make the window modal, false otherwise.
     * @param timeoutMillis timeout in milliseconds to wait before the window is hidden (auto-hide)
     */
    public static void createHtmlWindow(final String html, final String title, final boolean modal, final int timeoutMillis) {
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            @Override
            public void run() {
                final JDialog dialog = new JDialog(App.getFrame(), title, modal);
                final JEditorPane editorPane = startLayout(dialog);

                editorPane.setContentType("text/html");
                editorPane.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent event) {
                        // When a link is clicked
                        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

                            // Get the clicked URL
                            final URL url = event.getURL();

                            // If it is valid
                            if (url != null) {
                                // Get it in the good format
                                final String clickedURL = url.toExternalForm();
                                // Open the url in web browser
                                BrowserLauncher.openURL(clickedURL);
                            } else { // Assume it was an anchor
                                String anchor = event.getDescription();
                                editorPane.scrollToReference(anchor);
                            }
                        }
                    }
                });

                // if modal, blocks until the dialog is closed:
                finishLayout(editorPane, dialog, html, modal, timeoutMillis);
            }
        });
    }

    /**
     * Initialize the frame layout and return the editor pane
     * @param dialog frame to layout
     * @return editor pane
     */
    private static JEditorPane startLayout(final JDialog dialog) {
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setAlwaysOnTop(true);

        final JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setMargin(new Insets(5, 5, 5, 5));
        return editorPane;
    }

    /**
     * Finish the frame layout (editor pane) using the given text to display
     * @param editorPane editor pane to use
     * @param dialog frame to layout
     * @param text text to display
     * @param modal true to make the window modal, false otherwise.
     * @param timeoutMillis timeout in milliseconds to wait before the window is hidden (auto-hide)
     */
    private static void finishLayout(final JEditorPane editorPane, final JDialog dialog, final String text,
            final boolean modal, final int timeoutMillis) {

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(editorPane);
        scrollPane.setBorder(null);

        // Window layout
        final Container contentPane = dialog.getContentPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);
        editorPane.setText(text);
        editorPane.setCaretPosition(0); // Move back focus at the top of the content
        editorPane.setSize(MINIMUM_WIDTH, Integer.MAX_VALUE);

        if (modal) {
            final JButton button = new JButton("OK");
            button.addActionListener(new CloseWindowAction(dialog));
            contentPane.add(button, BorderLayout.SOUTH);
            // Set as default button with focus activated
            dialog.getRootPane().setDefaultButton(button);

            SwingUtils.invokeLaterEDT(new Runnable() {
                @Override
                public void run() {
                    button.requestFocusInWindow();
                }
            });
        }

        // Sizing
        dialog.pack();
        final int minimumEditorPaneWidth = editorPane.getWidth() + MARGIN;
        final int minimumEditorPaneHeight = editorPane.getMinimumSize().height + MARGIN;
        final int finalWidth = Math.max(Math.min(minimumEditorPaneWidth, MAXIMUM_WIDTH), MINIMUM_WIDTH);
        int finalHeight = Math.max(Math.min(minimumEditorPaneHeight, MAXIMUM_HEIGHT), MINIMUM_HEIGHT);
        if (modal) {
            finalHeight += BUTTON_HEIGHT; // For button height
        }
        dialog.setPreferredSize(new Dimension(finalWidth, finalHeight));

        WindowUtils.setClosingKeyboardShortcuts(dialog);
        dialog.pack();
        WindowUtils.centerOnMainScreen(dialog);

        if (timeoutMillis > 0) {
            // Use Timer to wait before closing this dialog :
            final Timer timer = new Timer(timeoutMillis, new CloseWindowAction(dialog));

            // timer runs only once :
            timer.setRepeats(false);
            timer.start();
        }

        // Show it and if modal, waits until dialog is not visible or disposed:
        dialog.setVisible(true);
    }

    /**
     * Check the JVM version and show a warning message if it is unsupported
     */
    public static void showUnsupportedJdkWarning() {

        final float requiredRuntime = 1.6f;
        final float javaRuntime = SystemUtils.JAVA_VERSION_FLOAT;

        final String javaVersion = System.getProperty("java.version");
        final String jvmVendor = System.getProperty("java.vm.vendor");
        final String jvmName = System.getProperty("java.vm.name");
        final String jvmVersion = System.getProperty("java.vm.version");

        int timeoutMillis = 0; // disabled by default
        boolean shouldWarn = false;
        String message = "<HTML><BODY>";

        if (jvmName != null && jvmName.toLowerCase().contains("openjdk")) {
            shouldWarn = true;

            final boolean isOpenJDK7 = (javaRuntime >= 1.7);

            _logger.warn("Detected OpenJDK runtime environment: {} {} {} - {}", jvmVendor, jvmName, javaVersion, jvmVersion);

            message += "<FONT COLOR='" + ((isOpenJDK7) ? "ORANGE" : "RED") + "'>WARNING</FONT> : ";
            message += "Your Java Virtual Machine is an OpenJDK JVM, which may have known bugs (SWING look and feel,"
                    + " fonts, PDF issues...) on several Linux distributions." + "<BR><BR>";

            if (isOpenJDK7) {
                // If OpenJDK 1.7+, set auto-hide delay to 5s:
                timeoutMillis = 5000;
            }
        }

        if (javaRuntime < requiredRuntime) {
            shouldWarn = true;
            _logger.warn("Detected JDK {} runtime environment: {} {} {} - {}", javaRuntime, jvmVendor, jvmName, javaVersion, jvmVersion);

            message += "<FONT COLOR='RED'>WARNING</FONT> : ";
            message += "Your Java Virtual Machine is too old and not supported anymore.<BR><BR>";
        }
        if (shouldWarn) {
            final String jvmHome = SystemUtils.getJavaHome().getAbsolutePath();

            message += "<BR>" + "<B>JMMC strongly recommends</B> Sun Java Runtime Environments version '" + requiredRuntime
                    + "' or newer, available at:" + "<BR><CENTER><A HREF='http://java.sun.com/javase/downloads/'>"
                    + "http://java.sun.com/javase/downloads/</A></CENTER>" + "<BR><BR>"
                    + "<I>Your current JVM Information :</I><BR><TT>"
                    + "java.vm.name    = '" + jvmName + "'<BR>"
                    + "java.vm.vendor  = '" + jvmVendor + "'<BR>"
                    + "java.version    = '" + javaVersion + "'<BR>"
                    + "java.vm.version = '" + jvmVersion + "'<BR>"
                    + "Java Home:<BR>'" + jvmHome + "'" + "</TT>";
            message += "</BODY></HTML>";

            ResizableTextViewFactory.createHtmlWindow(message, "Deprecated Java environment detected !", true, timeoutMillis);
        }
    }

    /** Action to close the given window by sending a window closing event */
    private final static class CloseWindowAction implements ActionListener {

        /** window to close */
        private final Window _window;

        CloseWindowAction(final Window window) {
            _window = window;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            _logger.info("CloseWindowAction called.");

            if (_window.isVisible()) {
                // trigger standard closing action (@see JFrame.setDefaultCloseOperation)
                // i.e. hide or dispose the window:
                _window.dispatchEvent(new WindowEvent(_window, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    /**
     * Test code
     * @param args unused arguments
     */
    public static void main(String[] args) {
        final int autoHideDelay = 2000; // 2s

        // TEXT Windows:
        final String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum congue tincidunt justo. Etiam massa arcu, vestibulum pulvinar accumsan ut, ullamcorper sed sapien. Quisque ullamcorper felis eget turpis mattis vestibulum. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras et turpis justo, sed lacinia libero. Sed in tellus eget libero posuere euismod. In nulla mi, semper a condimentum quis, tincidunt eget magna. Etiam tristique venenatis ante eu interdum. Phasellus ultrices rhoncus urna, ac pretium ante ultricies condimentum. Vestibulum et turpis ac felis pulvinar rhoncus nec a nulla. Proin eu ante eu leo fringilla ornare in a massa. Morbi varius porttitor nibh ac elementum. Cras sed neque massa, sed vulputate magna. Ut viverra velit magna, sagittis tempor nibh.";
        ResizableTextViewFactory.createTextWindow(text, "Text", true);
        System.out.println("modal dialog passed.");

        ResizableTextViewFactory.createHtmlWindow("Test Modal Text Window with timeout: wait the dialog to auto-hide", "Text", true, autoHideDelay);
        System.out.println("modal dialog passed.");

        // HTML Windows:
        final String html = "<html><head><title>Déclaration universelle des droits de l'homme</title></head><body><h1>Déclaration universelle des droits de l'homme</h1><h2>Préambule</h2><p><em>Considérant</em> que la reconnaissance de la dignité inhérente à tous les membres de la famille humaine et de leurs droits égaux et inaliénables constitue le fondement de la liberté, de la justice et de la paix dans le monde.</p><p><em>Considérant</em> que la méconnaissance et le mépris des droits de l'homme ont conduit à des actes de barbarie qui révoltent la conscience de l'humanité et que l'avènement d'un monde où les êtres humains seront libres de parler et de croire, libérés de la terreur et de la misère, a été proclamé comme la plus haute aspiration de l'homme.</p><p><em>Considérant</em> qu'il est essentiel que les droits de l'homme soient protégés par un régime de droit pour que l'homme ne soit pas contraint, en suprême recours, à la révolte contre la tyrannie et l'oppression.</p><p><em>Considérant</em> qu'il est essentiel d'encourager le développement de relations amicales entre nations.</p><p><em>Considérant</em> que dans la Charte les peuples des Nations Unies ont proclamé à nouveau leur foi dans les droits fondamentaux de l'homme, dans la dignité et la valeur de la personne humaine, dans l'égalité des droits des hommes et des femmes, et qu'ils se sont déclarés résolus à favoriser le progrès social et à instaurer de meilleures conditions de vie dans une liberté plus grande.</p><p><em>Considérant</em> que les Etats Membres se sont engagés à assurer, en coopération avec l'Organisation des Nations Unies, le respect universel et effectif des droits de l'homme et des libertés fondamentales.</p><p><em>Considérant</em> qu'une conception commune de ces droits et libertés est de la plus haute importance pour remplir pleinement cet engagement.</p><p><strong>L'Assemblée Générale proclame la présente Déclaration universelle des droits de l'homme</strong> comme l'idéal commun à atteindre par tous les peuples et toutes les nations afin que tous les individus et tous les organes de la société, ayant cette Déclaration constamment à l'esprit, s'efforcent, par l'enseignement et l'éducation, de développer le respect de ces droits et libertés et d'en assurer, par des mesures progressives d'ordre national et international, la reconnaissance et l'application universelles et effectives, tant parmi les populations des Etats Membres eux-mêmes que parmi celles des territoires placés sous leur juridiction.</p><h2>Article premier</h2><p>Tous les êtres humains naissent libres et égaux en dignité et en droits. Ils sont doués de raison et de conscience et doivent agir les uns envers les autres dans un esprit de fraternité.<p/><h2>Article 2</h2><p>1.Chacun peut se prévaloir de tous les droits et de toutes les libertés proclamés dans la présente Déclaration, sans distinction aucune, notamment de race, de couleur, de sexe, de langue, de religion, d'opinion politique ou de toute autre opinion, d'origine nationale ou sociale, de fortune, de naissance ou de toute autre situation.<br>2.De plus, il ne sera fait aucune distinction fondée sur le statut politique, juridique ou international du pays ou du territoire dont une personne est ressortissante, que ce pays ou territoire soit indépendant, sous tutelle, non autonome ou soumis à une limitation quelconque de souveraineté.</p><h2>Article 3</h2><p>Tout individu a droit à la vie, à la liberté et à la sûreté de sa personne.<p/><h2>Article 4</h2><p>Nul ne sera tenu en esclavage ni en servitude; l'esclavage et la traite des esclaves sont interdits sous toutes leurs formes.</p><h2>Article 5</h2><p>Nul ne sera soumis à la torture, ni à des peines ou traitements cruels, inhumains ou dégradants.<p/><h2>Article 6</h2><p>Chacun a le droit à la reconnaissance en tous lieux de sa personnalité juridique.<p/><h2>Article 7</h2><p>Tous sont égaux devant la loi et ont droit sans distinction à une égale protection de la loi. Tous ont droit à une protection égale contre toute discrimination qui violerait la présente Déclaration et contre toute provocation à une telle discrimination.</p><h2>Article 8</h2><p>Toute personne a droit à un recours effectif devant les juridictions nationales compétentes contre les actes violant les droits fondamentaux qui lui sont reconnus par la constitution ou par la loi.<p/><h2>Article 9</h2><p>Nul ne peut être arbitrairement arrêté, détenu ou exilé.<p/><h2>Article 10</h2><p>Toute personne a droit, en pleine égalité, à ce que sa cause soit entendue équitablement et publiquement par un tribunal indépendant et impartial, qui décidera, soit de ses droits et obligations, soit du bien-fondé de toute accusation en matière pénale dirigée contre elle.<p/><h2>Article 11</h2><p>1. Toute personne accusée d'un acte délictueux est présumée innocente jusqu'à ce que sa culpabilité ait été légalement établie au cours d'un procès public où toutes les garanties nécessaires à sa défense lui auront été assurées.<br>2. Nul ne sera condamné pour des actions ou omissions qui, au moment où elles ont été commises, ne constituaient pas un acte délictueux d'après le droit national ou international. De même, il ne sera infligé aucune peine plus forte que celle qui était applicable au moment où l'acte délictueux a été commis.<p/><h2>Article 12</h2><p>Nul ne sera l'objet d'immixtions arbitraires dans sa vie privée, sa famille, son domicile ou sa correspondance, ni d'atteintes à son honneur et à sa réputation. Toute personne a droit à la protection de la loi contre de telles immixtions ou de telles atteintes.<p/><h2>Article 13</h2><p>1. Toute personne a le droit de circuler librement et de choisir sa résidence à l'intérieur d'un Etat.<br>2. Toute personne a le droit de quitter tout pays, y compris le sien, et de revenir dans son pays.<p/><h2>Article 14</h2><p>1. Devant la persécution, toute personne a le droit de chercher asile et de bénéficier de l'asile en d'autres pays.<br>2. Ce droit ne peut être invoqué dans le cas de poursuites réellement fondées sur un crime de droit commun ou sur des agissements contraires aux buts et aux principes des Nations Unies.<p/><h2>Article 15</h2><p>1. Tout individu a droit à une nationalité.<br>2. Nul ne peut être arbitrairement privé de sa nationalité, ni du droit de changer de nationalité.<p/><h2>Article 16</h2><p>1. A partir de l'âge nubile, l'homme et la femme, sans aucune restriction quant à la race, la nationalité ou la religion, ont le droit de se marier et de fonder une famille. Ils ont des droits égaux au regard du mariage, durant le mariage et lors de sa dissolution.<br> 2. Le mariage ne peut être conclu qu'avec le libre et plein consentement des futurs époux.<br>3. La famille est l'élément naturel et fondamental de la société et a droit à la protection de la société et de l'Etat.<p/><h2>Article 17</h2><p>1. Toute personne, aussi bien seule qu'en collectivité, a droit à la propriété.<br /> 2. Nul ne peut être arbitrairement privé de sa propriété.<p/><h2>Article 18</h2><p>Toute personne a droit à la liberté de pensée, de conscience et de religion ; ce droit implique la liberté de changer de religion ou de conviction ainsi que la liberté de manifester sa religion ou sa conviction seule ou en commun, tant en public qu'en privé, par l'enseignement, les pratiques, le culte et l'accomplissement des rites.</p><h2>Article 19</h2><p>Tout individu a droit à la liberté d'opinion et d'expression, ce qui implique le droit de ne pas être inquiété pour ses opinions et celui de chercher, de recevoir et de répandre, sans considérations de frontières, les informations et les idées par quelque moyen d'expression que ce soit.</p><h2>Article 20</h2><p>1. Toute personne a droit à la liberté de réunion et d'association pacifiques.<br>2. Nul ne peut être obligé de faire partie d'une association.<p/><h2>Article 21</h2><p>1. Toute personne a le droit de prendre part à la direction des affaires publiques de son pays, soit directement, soit par l'intermédiaire de représentants librement choisis.<br> 2. Toute personne a droit à accéder, dans des conditions d'égalité, aux fonctions publiques de son pays.<br /> 3. La volonté du peuple est le fondement de l'autorité des pouvoirs publics ; cette volonté doit s'exprimer par des élections honnêtes qui doivent avoir lieu périodiquement, au suffrage universel égal et au vote secret ou suivant une procédure équivalente assurant la liberté du vote.<p/><h2>Article 22</h2><p>Toute personne, en tant que membre de la société, a droit à la sécurité sociale ; elle est fondée à obtenir la satisfaction des droits économiques, sociaux et culturels indispensables à sa dignité et au libre développement de sa personnalité, grâce à l'effort national et à la coopération internationale, compte tenu de l'organisation et des ressources de chaque pays.</p><h2>Article 23</h2><p>1. Toute personne a droit au travail, au libre choix de son travail, à des conditions équitables et satisfaisantes de travail et à la protection contre le chômage.<br> 2. Tous ont droit, sans aucune discrimination, à un salaire égal pour un travail égal.<br> 3. Quiconque travaille a droit à une rémunération équitable et satisfaisante lui assurant ainsi qu'à sa famille une existence conforme à la dignité humaine et complétée, s'il y a lieu, par tous autres moyens de protection sociale.<br>4. Toute personne a le droit de fonder avec d'autres des syndicats et de s'affilier à des syndicats pour la défense de ses intérêts.<p/><h2>Article 24</h2><p>Toute personne a droit au repos et aux loisirs et notamment à une limitation raisonnable de la durée du travail et à des congés payés périodiques.<p/><h2>Article 25</h2><p>1. Toute personne a droit à un niveau de vie suffisant pour assurer sa santé, son bien-être et ceux de sa famille, notamment pour l'alimentation, l'habillement, le logement, les soins médicaux ainsi que pour les services sociaux nécessaires ; elle a droit à la sécurité en cas de chômage, de maladie, d'invalidité, de veuvage, de vieillesse ou dans les autres cas de perte de ses moyens de subsistance par suite de circonstances indépendantes de sa volonté.<br>2. La maternité et l'enfance ont droit à une aide et à une assistance spéciales. Tous les enfants, qu'ils soient nés dans le mariage ou hors mariage, jouissent de la même protection sociale.<p/><h2>Article 26</h2><p>1. Toute personne a droit à l'éducation. L'éducation doit être gratuite, au moins en ce qui concerne l'enseignement élémentaire et fondamental. L'enseignement élémentaire est obligatoire. L'enseignement technique et professionnel doit être généralisé ; l'accès aux études supérieures doit être ouvert en pleine égalité à tous en fonction de leur mérite.<br> 2. L'éducation doit viser au plein épanouissement de la personnalité humaine et au renforcement du respect des droits de l'homme et des libertés fondamentales. Elle doit favoriser la compréhension, la tolérance et l'amitié entre toutes les nations et tous les groupes raciaux ou religieux, ainsi que le développement des activités des Nations Unies pour le maintien de la paix.<br>3. Les parents ont, par priorité, le droit de choisir le genre d'éducation à donner à leurs enfants.<p/><h2>Article 27</h2><p>1. Toute personne a le droit de prendre part librement à la vie culturelle de la communauté, de jouir des arts et de participer au progrès scientifique et aux bienfaits qui en résultent.<br>2. Chacun a droit à la protection des intérêts moraux et matériels découlant de toute production scientifique, littéraire ou artistique dont il est l'auteur.<p/><h2>Article 28</h2><p>Toute personne a droit à ce que règne, sur le plan social et sur le plan international, un ordre tel que les droits et libertés énoncés dans la présente Déclaration puissent y trouver plein effet.<p/><h2>Article 29</h2><p>1. L'individu a des devoirs envers la communauté dans laquelle seule le libre et plein développement de sa personnalité est possible.<br> 2. Dans l'exercice de ses droits et dans la jouissance de ses libertés, chacun n'est soumis qu'aux limitations établies par la loi exclusivement en vue d'assurer la reconnaissance et le respect des droits et libertés d'autrui et afin de satisfaire aux justes exigences de la morale, de l'ordre public et du bien-être général dans une société démocratique.<br>3. Ces droits et libertés ne pourront, en aucun cas, s'exercer contrairement aux buts et aux principes des Nations Unies.<p/><h2>Article 30</h2><p>Aucune disposition de la présente Déclaration ne peut être interprétée comme impliquant pour un Etat, un groupement ou un individu un droit quelconque de se livrer à une activité ou d'accomplir un acte visant à la destruction des droits et libertés qui y sont énoncés.<p/></body></html>";
        ResizableTextViewFactory.createHtmlWindow(html, "HTML", false);

        ResizableTextViewFactory.createHtmlWindow("<html><head><title>Test Modal HTML Window</title></head><body><h1>Déclaration universelle des droits de l'homme</h1>"
                + "<p>Test Modal HTML Window: click on button to close this window</p></body></html>", "HTML", true);
        System.out.println("modal dialog passed.");

        ResizableTextViewFactory.createHtmlWindow("<html><head><title>Test Modal HTML Window with timeout</title></head><body><h1>Déclaration universelle des droits de l'homme</h1>"
                + "<p>Test Modal HTML Window with timeout: wait the dialog to auto-hide</p></body></html>", "HTML", true, autoHideDelay);
        System.out.println("modal dialog passed.");

        ResizableTextViewFactory.showUnsupportedJdkWarning();
        System.out.println("modal dialog passed.");

        System.out.println("That's all Folks !");

        try {
            Thread.sleep(30 * 1000L);
        } catch (InterruptedException ex) {
            // nop
        }

        System.exit(0);
    }
}
