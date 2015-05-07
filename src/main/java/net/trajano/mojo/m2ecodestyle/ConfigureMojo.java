package net.trajano.mojo.m2ecodestyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.trajano.mojo.m2ecodestyle.internal.PreferenceFileName;

/**
 * Configures Eclipse before any builds.
 */
@Mojo(name = "configure",
    defaultPhase = LifecyclePhase.INITIALIZE,
    threadSafe = true,
    requiresOnline = false)
public class ConfigureMojo extends AbstractMojo {

    /**
     * Default list of <em>prefs</em> files that are related to code styles.
     */
    private static final String[] DEFAULT_PREFS = {
        "org.eclipse.core.resources.prefs",
        "org.eclipse.jdt.launching.prefs",
        "org.eclipse.wst.xml.core.prefs",
        "org.eclipse.wst.validation.prefs",
        PreferenceFileName.JDT_CORE,
        PreferenceFileName.JDT_UI,
        PreferenceFileName.JSDT_CORE,
        PreferenceFileName.JSDT_UI, };

    /**
     * Build context.
     */
    @Component
    private BuildContext buildContext;

    /**
     * <p>
     * This is the URL that points to the base URL where the mergable prefs
     * files are located. If this is not provided, then merging of property
     * files is not performed.
     * </p>
     * <p>
     * The URL <b>must</b> end with a trailing slash as the names referenced by
     * {@link #prefsFiles} are resolved against it. If the trailing slash is
     * missing, it will append it automatically and log a warning.
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false)
    private String codeStyleBaseUrl;

    /**
     * The directory that contains the Eclipse settings.
     */
    @Parameter(defaultValue = "${basedir}/.settings",
        required = true)
    private File destDir;

    /**
     * <p>
     * This is the URL that points to the Java cleanup profile XML. The contents
     * of this will be merged into "org.eclipse.jdt.ui.prefs"
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false)
    private String javaCleanupProfileXmlUrl;

    /**
     * <p>
     * This is the URL that points to the Java formatter profile XML. The
     * contents of this will be merged into {@value PreferenceFileName#JDT_CORE}
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false,
        property = "codestyle.java.formatter.xml")
    private String javaFormatterProfileXmlUrl;

    /**
     * <p>
     * This is the URL that points to the JavaScript cleanup profile XML. The
     * contents of this will be merged into {@value PreferenceFileName#JSDT_UI}.
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false)
    private String javaScriptCleanupProfileXmlUrl;

    /**
     * <p>
     * This is the URL that points to the JavaScript formatter profile XML. The
     * contents of this will be merged into
     * {@value PreferenceFileName#JSDT_CORE}.
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false)
    private String javaScriptFormatterProfileXmlUrl;

    /**
     * <p>
     * This is the URL that points to the JavaScript formatter profile XML. The
     * contents of this will be merged into "org.eclipse.wst.jsdt.core.prefs"
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false)
    private String javaScriptTemplatesXmlUrl;

    /**
     * <p>
     * This is the URL that points to the Java formatter profile XML. The
     * contents of this will be merged into "org.eclipse.jdt.core.prefs"
     * </p>
     * <p>
     * If this is not an absolute URL, it assumes that the value passed in is
     * referring to something in the classpath.
     * </p>
     */
    @Parameter(required = false)
    private String javaTemplatesXmlUrl;

    /**
     * A list of <em>prefs</em> files to load from the source. The contents of
     * the <em>prefs</em> files will be merged with the existing <em>prefs</em>
     * files if they already exist. This defaults to {@value #DEFAULT_PREFS}
     * which are known <em>prefs</em> relating to code styles.
     */
    @Parameter(required = false)
    private List<String> prefsFiles;

    /**
     * Injected property retrieval component.
     */
    @Component
    private Retrieval retrieval;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException,
        MojoFailureException {

        if (System.getProperty("eclipse.startTime") == null) {
            getLog().warn("'eclipse.startTime' was not defined, may not be running in Eclipse.");
            if (!destDir.exists()) {
                getLog().warn(String.format("%s is missing it will not be created as plugin is not running in Eclipse", destDir));
                return;
            }
        }

        if (!destDir.exists()) {
            destDir.mkdirs();

        }

        if (codeStyleBaseUrl != null) {
            handlePreferenceMerge();
        }

        if (javaFormatterProfileXmlUrl != null) {
            handleXmlPreferenceMerge(javaFormatterProfileXmlUrl, PreferenceFileName.JDT_CORE);
        }

        if (javaCleanupProfileXmlUrl != null) {
            handleXmlPreferenceMerge(javaCleanupProfileXmlUrl, PreferenceFileName.JDT_UI);
        }

        if (javaTemplatesXmlUrl != null) {
            setPreferenceValue(javaTemplatesXmlUrl, PreferenceFileName.JDT_UI, "org.eclipse.jdt.ui.text.custom_code_templates");
        }
        if (javaScriptFormatterProfileXmlUrl != null) {
            handleXmlPreferenceMerge(javaScriptFormatterProfileXmlUrl, PreferenceFileName.JSDT_CORE);
        }

        if (javaScriptCleanupProfileXmlUrl != null) {
            handleXmlPreferenceMerge(javaScriptCleanupProfileXmlUrl, PreferenceFileName.JSDT_UI);
        }

        if (javaScriptTemplatesXmlUrl != null) {
            setPreferenceValue(javaScriptTemplatesXmlUrl, PreferenceFileName.JSDT_UI, "org.eclipse.wst.jsdt.ui.text.custom_code_templates");
        }
    }

    /**
     * Fetch and merge the preferences file.
     *
     * @param codeStyleBaseUri
     *            base URI
     * @param prefsFile
     *            prefs file being processed
     * @throws URISyntaxException
     */
    public void fetchAndMerge(final URI codeStyleBaseUri,
        final String prefsFile) {

        try {
            retrieval.fetchAndMerge(codeStyleBaseUri, prefsFile, destDir);
        } catch (final FileNotFoundException e) {
            getLog().debug("Ignoring file not found for " + prefsFile, e);
        } catch (final IOException e) {
            buildContext.addMessage(new File(destDir, prefsFile), 0, 0, "I/O failure, will not process the file", BuildContext.SEVERITY_ERROR, e);
        }
    }

    /**
     * Does the preference merge operations.
     *
     * @throws MojoExecutionException
     */
    private void handlePreferenceMerge() throws MojoExecutionException {

        if (prefsFiles == null) {
            prefsFiles = Arrays.asList(DEFAULT_PREFS);
        }

        final URI codeStyleBaseUri;
        if (codeStyleBaseUrl.endsWith("/")) {
            codeStyleBaseUri = URI.create(codeStyleBaseUrl);
        } else {
            codeStyleBaseUri = URI.create(codeStyleBaseUrl + "/");
            getLog().warn("the value of codeStyleBaseUrl does not end with '/' and will be implicitly appended");
        }
        for (final String prefsFile : prefsFiles) {
            fetchAndMerge(codeStyleBaseUri, prefsFile);
        }

    }

    /**
     * @param url
     *            URL to the XML file
     * @param prefsFile
     *            prefs file to update
     * @return profile element
     */
    private Element handleXmlPreferenceMerge(final String url,
        final String prefsFile) throws MojoExecutionException {

        final XPathFactory xpf = XPathFactory.newInstance();
        try {
            final XPath xp = xpf.newXPath();
            final InputStream xmlStream = retrieval.openStream(url);
            if (xmlStream == null) {
                throw new MojoExecutionException("unable to open url: " + url);
            }
            final Element profileNode = (Element) xp.evaluate("/profiles/profile", new InputSource(xmlStream), XPathConstants.NODE);
            xmlStream.close();
            final NodeList settings = (NodeList) xp.evaluate("setting", profileNode, XPathConstants.NODESET);
            final Properties prop = new Properties();
            final File settingsFile = new File(destDir, prefsFile);
            final FileInputStream prefsInputStream = new FileInputStream(settingsFile);
            prop.load(prefsInputStream);
            prefsInputStream.close();
            for (int i = 0; i < settings.getLength(); ++i) {
                final Element setting = (Element) settings.item(i);
                prop.put(setting.getAttribute("id"), setting.getAttribute("value"));
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(settingsFile);
            prop.store(fileOutputStream, "Generated by m2e-codestyle-plugin");
            fileOutputStream.close();

            return profileNode;
        } catch (final XPathExpressionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * @param url
     *            URL to the file
     * @param prefsFile
     *            prefs file to update
     * @param key
     *            preference key
     * @throws MojoExecutionException
     */
    private void setPreferenceValue(final String url,
        final String prefsFile,
        final String key) throws MojoExecutionException {

        try {
            final Properties prop = new Properties();
            final File settingsFile = new File(destDir, prefsFile);
            final FileInputStream prefsInputStream = new FileInputStream(settingsFile);
            prop.load(prefsInputStream);
            prefsInputStream.close();

            final InputStream dataStream = retrieval.openStream(url);
            final Scanner scanner = new Scanner(dataStream);
            final String contents = scanner.useDelimiter("\\A").next();
            scanner.close();
            dataStream.close();

            final FileOutputStream fileOutputStream = new FileOutputStream(settingsFile);
            prop.setProperty(key, contents);
            prop.store(fileOutputStream, "Generated by m2e-codestyle-plugin");
            fileOutputStream.close();
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

}
