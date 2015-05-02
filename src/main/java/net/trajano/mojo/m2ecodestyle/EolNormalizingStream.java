package net.trajano.mojo.m2ecodestyle;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>
 * This ensures that the EOL characters are consistent when writing out the
 * stream. It will use the platform's EOL by default or a specified EOL
 * sequence. It simply strips out CR characters. This won't work correctly with
 * CR line endings, only CRLF or LF line endings will work correctly.
 * </p>
 * <p>
 * It also ensures that the files end with a new line
 * </p>
 *
 * @author Archimedes Trajano
 */
public final class EolNormalizingStream extends FilterOutputStream {

    /**
     * Carriage return.
     */
    private static final int CR = 13;

    /**
     * Line feed.
     */
    private static final int LF = 10;

    private boolean lastCharacterWrittenLF = true;

    /**
     * Line separator byte sequence.
     */
    private final byte[] lineSeparatorBytes;

    /**
     * Creates the normalizing stream with the system default line separator.
     *
     * @param os
     *            output stream to filter
     */
    public EolNormalizingStream(final OutputStream os) {

        this(os, System.getProperty("line.separator"));
    }

    /**
     * Creates the normalizing stream with the specified line separator
     * sequence.
     *
     * @param os
     *            output stream to filter
     * @param lineSeparator
     *            line separator string
     */
    public EolNormalizingStream(final OutputStream os,
        final String lineSeparator) {

        super(os);
        lineSeparatorBytes = lineSeparator.getBytes();
    }

    /**
     * If the last character written is not {@link #LF} then {@inheritDoc}
     */
    @Override
    public void close() throws IOException {

        if (!lastCharacterWrittenLF) {
            writeLineSeparator();
        }
        super.close();
    }

    /**
     * Checks if the "LF" character is received if so it will write the line
     * separator bytes, if it is CR it will ignore and other cases it will write
     * the character as-is.
     *
     * @param b
     *            character to write
     * @throws IOException
     *             I/O error
     */
    @Override
    public void write(final int b)
        throws IOException {

        if (b == LF) {
            writeLineSeparator();
            lastCharacterWrittenLF = true;
        } else if (b != CR) {
            super.write(b);
            lastCharacterWrittenLF = false;
        } else {
            lastCharacterWrittenLF = false;
        }
    }

    /**
     * Writes out the line separator bytes to the stream.
     *
     * @throws IOException
     */
    private void writeLineSeparator() throws IOException {

        for (final byte lb : lineSeparatorBytes) {
            super.write(lb);
        }
    }
}
