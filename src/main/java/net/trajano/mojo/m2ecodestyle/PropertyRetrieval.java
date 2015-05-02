package net.trajano.mojo.m2ecodestyle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
     * @throws IOException
     *             I/O error
     */
    void fetchAndMerge(final URI codeStyleBaseUri,
        final String prefsFile,
        final File destDir) throws IOException;

    /**
     * Create an input stream pointing to the prefs file inside the code style
     * base URI. The caller is responsible for closing the stream.
     *
     * @param codeStyleBaseUri
     *            code style base URI
     * @param prefsFile
     *            prefs file to look up
     * @return stream or <code>null</code> if the target is not available.
     * @throws IOException
     *             I/O error
     */
    InputStream openPreferenceStream(final URI codeStyleBaseUri,
        final String prefsFile) throws IOException;

    /**
     * Create an input stream pointing to the URL. The caller is responsible for
     * closing the stream.
     *
     * @param url
     *            url to fetch
     * @return stream or <code>null</code> if the target is not available.
     * @throws IOException
     *             I/O error
     */
    InputStream openStream(String url) throws IOException;
}
