package net.trajano.mojo.m2ecodestyle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public interface PropertyRetrieval {

    /**
     * Fetch and merge the preferences file.
     *
     * @param codeStyleBaseUri
     *            base URI
     * @param prefsFile
     *            prefs file being processed
     * @param destDir
     *            destination directory containing the prefs file.
     * @throws URISyntaxException
     */
        void fetchAndMerge(final URI codeStyleBaseUri,
            final String prefsFile,
            final File destDir) throws URISyntaxException, IOException;

    /**
     * Create an input stream pointing to the prefs file inside the code style
     * base URI.
     *
     * @param codeStyleBaseUri
     * @param prefsFile
     * @return stream or <code>null</code> if the target is not available.
     * @throws MalformedURLException
     * @throws IOException
     */
        InputStream openPreferenceStream(final URI codeStyleBaseUri,
            final String prefsFile) throws MalformedURLException, IOException;
}
