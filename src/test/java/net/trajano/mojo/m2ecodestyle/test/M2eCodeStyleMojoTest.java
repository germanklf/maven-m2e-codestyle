package net.trajano.mojo.m2ecodestyle.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

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
        final M2eCodeStyleMojo mojo = (M2eCodeStyleMojo) rule.lookupMojo("configure", testPom);
        assertNotNull(mojo);
        mojo.execute();
    }
}
