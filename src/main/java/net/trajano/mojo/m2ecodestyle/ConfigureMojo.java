package net.trajano.mojo.m2ecodestyle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

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
    private static final String[] DEFAULT_PREFS = { "org.eclipse.core.resources.prefs",
        "org.eclipse.jdt.core.prefs",
        "org.eclipse.jdt.ui.prefs",
        "org.eclipse.wst.jsdt.core.prefs",
        "org.eclipse.wst.jsdt.ui.prefs", };

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
     */
    @Parameter(required = true)
    private String codeStyleBaseUrl;

    /**
     * The directory that contains the Eclipse settings.
     */
    @Parameter(defaultValue = "${basedir}/.settings",
        required = true)
    private File destDir;

    /**
     * A list of <em>prefs</em> files to load from the source. The contents of
     * the <em>prefs</em> files will be merged with the existing <em>prefs</em>
     * files if they already exist. This defaults to {@value #DEFAULT_PREFS}
     * which are known <em>prefs</em> relating to code styles.
     */
    @Parameter(required = false)
    private List<String> prefsFiles;

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
    private PropertyRetrieval retrieval;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

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
        if (prefsFiles == null) {
            prefsFiles = Arrays.asList(DEFAULT_PREFS);
        }
        try {
            final URI codeStyleBaseUri;
            if (codeStyleBaseUrl.endsWith("/")) {
                codeStyleBaseUri = new URI(codeStyleBaseUrl);
            } else {
                codeStyleBaseUri = new URI(codeStyleBaseUrl + "/");
                getLog().warn("the value of codeStyleBaseUrl does not end with '/' and will be implicitly appended");
            }
            for (final String prefsFile : prefsFiles) {
                fetchAndMerge(codeStyleBaseUri, prefsFile);
            }
        } catch (final URISyntaxException e) {
            throw new MojoExecutionException(e.getMessage(), e);
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
        final String prefsFile) throws URISyntaxException {

        try {
            retrieval.fetchAndMerge(codeStyleBaseUri, prefsFile, destDir);
        } catch (final FileNotFoundException e) {
            getLog().debug("Ignoring file not found for " + prefsFile, e);
        } catch (final IOException e) {
            buildContext.addMessage(new File(destDir, prefsFile), 0, 0, "I/O failure, will not process the file", BuildContext.SEVERITY_ERROR, e);
        }
    }

}
