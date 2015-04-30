package net.trajano.mojo.m2ecodestyle;

import java.io.FileNotFoundException;import java.io.IOException;import java.net.URI;import java.net.URISyntaxException;import java.util.Arrays;import java.util.List;

/**
 * Configures Eclipse before any builds.
 */
public class ConfigureMojo {

    /**
     * Default list of <em>prefs</em> files that are related to code styles.
     */
    private static final String[] DEFAULT_PREFS = { "org.eclipse.core.resources.prefs",
        "org.eclipse.jdt.core.prefs",        "org.eclipse.jdt.ui.prefs",
        "org.eclipse.wst.jsdt.core.prefs",        "org.eclipse.wst.jsdt.ui.prefs", };

}
