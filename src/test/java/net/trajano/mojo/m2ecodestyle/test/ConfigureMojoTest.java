package net.trajano.mojo.m2ecodestyle.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import net.trajano.mojo.m2ecodestyle.ConfigureMojo;

public class ConfigureMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testDefault() throws Exception {

        System.setProperty("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
        final File testPom = new File("src/test/resources/pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            assertNotNull(mojo);
            rule.setVariableValueToObject(mojo, "destDir", tmp);
            mojo.execute();
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs",
                new File(tmp, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    /**
     * This tests when the settings files already exists.
     *
     * @throws Exception
     */
    @Test
    public void testExecuteTwice() throws Exception {

        System.setProperty("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
        final File testPom = new File("src/test/resources/pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            assertNotNull(mojo);
            final File settingsFolder = new File(tmp, "settings");
            rule.setVariableValueToObject(mojo, "destDir", settingsFolder);
            mojo.execute();
            mojo.execute();
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs", new File(settingsFolder, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    public void testImplicitSlash() throws Exception {

        System.setProperty("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
        final File testPom = new File("src/test/resources/implicitslash-pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            final Log log = mock(Log.class);
            mojo.setLog(log);
            rule.setVariableValueToObject(mojo, "destDir", tmp);
            mojo.execute();
            verify(log).warn("the value of codeStyleBaseUrl does not end with '/' and will be implicitly appended");
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs", new File(tmp, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    /**
     * This tests when not running in an Eclipse environment.
     *
     * @throws Exception
     */
    @Test
    public void testNoEclipse() throws Exception {

        System.clearProperty("eclipse.startTime");
        final File testPom = new File("src/test/resources/pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            final Log log = mock(Log.class);
            mojo.setLog(log);
            assertNotNull(mojo);
            rule.setVariableValueToObject(mojo, "destDir", tmp);
            mojo.execute();
            verify(log).warn("'eclipse.startTime' was not defined, may not be running in Eclipse.");
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs", new File(tmp, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    /**
     * This tests when the settings folder does not exist and is not running in
     * Eclipse.
     *
     * @throws Exception
     */
    @Test
    public void testNoEclipseAndNoFolder() throws Exception {

        System.clearProperty("eclipse.startTime");
        final File testPom = new File("src/test/resources/pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            final Log log = mock(Log.class);
            mojo.setLog(log);
            assertNotNull(mojo);
            final File settingsFolder = new File(tmp, "settings");
            rule.setVariableValueToObject(mojo, "destDir", settingsFolder);
            mojo.execute();
            verify(log).warn("'eclipse.startTime' was not defined, may not be running in Eclipse.");

            assertFalse(settingsFolder.exists());
            assertTrue(tmp.exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    /**
     * This tests when the settings files already exists when not running in
     * Eclipse.
     *
     * @throws Exception
     */
    @Test
    public void testNoEclipseTwice() throws Exception {

        System.clearProperty("eclipse.startTime");
        final File testPom = new File("src/test/resources/pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            final Log log = mock(Log.class);
            mojo.setLog(log);
            assertNotNull(mojo);
            final File settingsFolder = new File(tmp, "settings");
            settingsFolder.mkdir();
            rule.setVariableValueToObject(mojo, "destDir", settingsFolder);
            mojo.execute();
            verify(log).warn("'eclipse.startTime' was not defined, may not be running in Eclipse.");

            Mockito.reset(log);
            mojo.execute();
            verify(log).warn("'eclipse.startTime' was not defined, may not be running in Eclipse.");
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs", new File(settingsFolder, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    /**
     * This tests when the settings files already exists.
     *
     * @throws Exception
     */
    @Test
    public void testWithXmlConfiguration1() throws Exception {

        System.setProperty("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
        final File testPom = new File("src/test/resources/pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final ConfigureMojo mojo = (ConfigureMojo) rule.lookupMojo("configure", testPom);
            assertNotNull(mojo);
            final File settingsFolder = new File(tmp, "settings");
            rule.setVariableValueToObject(mojo, "destDir", settingsFolder);
            rule.setVariableValueToObject(mojo, "javaFormatterProfileXmlUrl", new File("src/test/resources/codestyle/eclipse/java-code-formatter.xml").toURI().toASCIIString());
            mojo.execute();
            mojo.execute();
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs", new File(settingsFolder, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
