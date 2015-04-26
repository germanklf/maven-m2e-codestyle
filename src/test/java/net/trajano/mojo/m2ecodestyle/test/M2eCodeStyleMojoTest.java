package net.trajano.mojo.m2ecodestyle.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import net.trajano.mojo.m2ecodestyle.M2eCodeStyleMojo;

public class M2eCodeStyleMojoTest {

    @BeforeClass
    public static void setProperties() {

        System.setProperty("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
    }

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testDefault() throws Exception {

        final File testPom = new File("src/test/resources/pom.xml");
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
}
