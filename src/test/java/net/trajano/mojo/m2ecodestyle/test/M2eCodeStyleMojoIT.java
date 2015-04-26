package net.trajano.mojo.m2ecodestyle.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import net.trajano.mojo.m2ecodestyle.M2eCodeStyleMojo;

/**
 * Tests using the remote retrieval method.
 *
 * @author Archimedes Trajano
 */
public class M2eCodeStyleMojoIT {

    @BeforeClass
    public static void setProperties() {

        System.setProperty("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
    }

    @AfterClass
    public static void unsetProperties() {

        System.setProperty("eclipse.startTime", "");

    }

    @Rule
    public MojoRule rule = new MojoRule();

    @Test(expected = MojoExecutionException.class)
    public void testBadUrl() throws Exception {

        final File testPom = new File("src/test/resources/it-pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final M2eCodeStyleMojo mojo = (M2eCodeStyleMojo) rule.lookupMojo("configure", testPom);
            assertNotNull(mojo);
            rule.setVariableValueToObject(mojo, "destDir", tmp);
            rule.setVariableValueToObject(mojo, "codeStyleBaseUrl", ":");
            mojo.execute();
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    public void testDefault() throws Exception {

        final File testPom = new File("src/test/resources/it-pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final M2eCodeStyleMojo mojo = (M2eCodeStyleMojo) rule.lookupMojo("configure", testPom);
            assertNotNull(mojo);
            rule.setVariableValueToObject(mojo, "destDir", tmp);
            mojo.execute();
            assertTrue(tmp.exists());
            assertTrue("Missing org.eclipse.jdt.core.prefs", new File(tmp, "org.eclipse.jdt.core.prefs").exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    public void testInvalidUrl() throws Exception {

        final File testPom = new File("src/test/resources/it-pom.xml");
        assertTrue(testPom.exists());

        final File tmp = File.createTempFile("test", "test");
        tmp.delete();
        try {
            tmp.mkdir();
            final M2eCodeStyleMojo mojo = (M2eCodeStyleMojo) rule.lookupMojo("configure", testPom);
            assertNotNull(mojo);
            rule.setVariableValueToObject(mojo, "destDir", tmp);
            rule.setVariableValueToObject(mojo, "codeStyleBaseUrl", "http://foo.bar");
            mojo.execute();
            assertTrue(tmp.exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
