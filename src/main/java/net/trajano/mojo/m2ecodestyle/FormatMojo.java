package net.trajano.mojo.m2ecodestyle;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.model.FileSet;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Mojo(name = "format",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    threadSafe = true,
    requiresOnline = false)
public class FormatMojo extends AbstractMojo {

    /**
     * Build context.
     */
    @Component
    private BuildContext buildContext;

    /**
     * <p>
     * This is the URL that points to the base URL where the files are located.
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
     * <p>
     * If the value is not specified, then the default Java conventions would be
     * used.
     * </p>
     */
    @Parameter(required = false)
    private String codeStyleBaseUrl;

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
     * The Maven Project.
     */
    @Parameter(defaultValue = "${project}",
        readonly = true)
    private MavenProject project;

    /**
     * Injected property retrieval component.
     */
    @Component
    private Retrieval retrieval;

    @Parameter(property = "maven.compiler.source",
        defaultValue = "1.5")
    private String source;

    @Parameter(property = "maven.compiler.target",
        defaultValue = "1.5")
    private String target;

    @SuppressWarnings("unchecked")
    public void addJavaCoreProperties(@SuppressWarnings("rawtypes") final Map options) {

        final Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-compiler-plugin");
        if (plugin == null) {
            getLog().warn("Maven compiler plugin is not present, will use the default Java targets");
        } else {
            options.put(JavaCore.COMPILER_SOURCE, source);
            options.put(JavaCore.COMPILER_COMPLIANCE, source);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
                target);
        }

    }

    /**
     * Builds the code formatter.
     *
     * @return configured code formatter
     * @throws MojoExecutionException
     *             wraps any error that has occurred when building the
     *             formatter.
     */
    private CodeFormatter buildFormatter() throws MojoExecutionException {

        final Map<?, ?> options;
        try {

            if (isUseJavaConventions()) {
                options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
                addJavaCoreProperties(options);
            } else {

                options = buildOptionsFromConfiguration();
            }
            addJavaCoreProperties(options);
            return ToolFactory.createCodeFormatter(options);

        } catch (final MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (final XPathExpressionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Creates the formatter options from the configuration data provided.
     *
     * @return populated options
     * @throws URISyntaxException
     *             Problem with the URI syntax
     * @throws IOException
     *             I/O problem
     * @throws MojoExecutionException
     *             other wrapped Mojo issue
     * @throws XPathExpressionException
     *             XPath issue, should not happen.
     */
    private Properties buildOptionsFromConfiguration() throws URISyntaxException,
        IOException,
        MojoExecutionException,
        XPathExpressionException {

        final Properties props = new Properties();

        if (codeStyleBaseUrl != null) {
            final URI codeStyleBaseUri = new URI(codeStyleBaseUrl);
            final InputStream prefStream = retrieval.openPreferenceStream(codeStyleBaseUri, "org.eclipse.jdt.core.prefs");
            if (prefStream == null) {
                throw new MojoExecutionException("unable to retrieve org.eclipse.jdt.core.prefs from " + codeStyleBaseUri);
            }
            props.load(prefStream);
            prefStream.close();
        }

        if (javaFormatterProfileXmlUrl != null) {

            final XPathFactory xpf = XPathFactory.newInstance();
            final XPath xp = xpf.newXPath();
            final InputStream xmlStream = retrieval.openStream(javaFormatterProfileXmlUrl);
            if (xmlStream == null) {
                throw new MojoExecutionException("unable to load " + javaFormatterProfileXmlUrl);
            }
            final Element profileNode = (Element) xp.evaluate("/profiles/profile", new InputSource(xmlStream), XPathConstants.NODE);
            xmlStream.close();
            final NodeList settings = (NodeList) xp.evaluate("setting", profileNode, XPathConstants.NODESET);

            for (int i = 0; i < settings.getLength(); ++i) {
                final Element setting = (Element) settings.item(i);
                props.put(setting.getAttribute("id"), setting.getAttribute("value"));
            }

        }
        return props;
    }

    @Override
    public void execute() throws MojoExecutionException,
        MojoFailureException {

        final CodeFormatter codeFormatter = buildFormatter();
        final FileSet sourceSet = new FileSet();
        sourceSet.setDirectory(project.getBuild().getSourceDirectory());
        sourceSet.addInclude("**/*.java");

        final FileSet testSet = new FileSet();
        testSet.setDirectory(project.getBuild().getTestSourceDirectory());
        testSet.addInclude("**/*.java");

        for (final FileSet sources : new FileSet[] {
            sourceSet,
            testSet
        }) {
            final File dir = new File(sources.getDirectory());
            if (!dir.exists()) {
                continue;
            }
            final org.codehaus.plexus.util.Scanner scanner = buildContext.newScanner(dir, false);
            scanner.setIncludes(sources.getIncludes().toArray(new String[0]));
            scanner.scan();
            for (final String includedFile : scanner.getIncludedFiles()) {
                final File file = new File(scanner.getBasedir(), includedFile);
                formatFile(file, codeFormatter);
            }
        }

    }

    /**
     * Formats an individual file. Made public to allow testing.
     *
     * @param file
     *            file to process
     * @param codeFormatter
     *            configured code formatter
     * @throws MojoFailureException
     *             failed processing individual file
     */
    public void formatFile(final File file,
        final CodeFormatter codeFormatter) throws MojoFailureException {

        final IDocument doc = new Document();
        try {
            final Scanner scanner = new Scanner(file);
            final String content = scanner.useDelimiter("\\Z").next();

            doc.set(content);
            scanner.close();

            final TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, content, 0, content.length(), 0,
                null);

            if (edit != null) {
                edit.apply(doc);
            } else {
                throw new MojoFailureException("unable to format " + file);
            }

            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new EolNormalizingStream(buildContext.newFileOutputStream(file))));
            try {

                out.write(doc.get());
                out.flush();

            } finally {

                out.close();

            }

        } catch (final IOException e) {
            throw new MojoFailureException("IO Exception" + file, e);
        } catch (final BadLocationException e) {
            throw new MojoFailureException("Bad Location Exception " + file, e);
        }
    }

    /**
     * Checks if the standard java conventions formatter should be used. This is
     * determined when {@link #codeStyleBaseUrl} and
     * {@link #javaFormatterProfileXmlUrl} are both null.
     *
     * @return
     */
    private boolean isUseJavaConventions() {

        return codeStyleBaseUrl == null && javaFormatterProfileXmlUrl == null;
    }
}
