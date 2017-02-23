package net.trajano.mojo.m2ecodestyle.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.junit.Rule;
import org.junit.Test;

import net.trajano.mojo.m2ecodestyle.FormatMojo;

public class FormatMojoTest {

	@Rule
	public MojoRule rule = new MojoRule();

	@SuppressWarnings("unchecked")
	@Test
	public void testFormatSingleFile() throws Exception {

		@SuppressWarnings("rawtypes")
		final Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		options.put(JavaCore.COMPILER_SOURCE, "1.7");
		options.put(JavaCore.COMPILER_COMPLIANCE, "1.7");
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.7");

		final CodeFormatter codeFormatter = new DefaultCodeFormatter(options);

		final File testPom = new File("src/test/resources/formatter/pom.xml");
		final FormatMojo mojo = (FormatMojo) rule.lookupConfiguredMojo(testPom.getParentFile(), "format");
		assertNotNull(mojo);

		final File temp = File.createTempFile("Temp", ".java");
		FileUtils.copyFile(new File("src/test/resources/BadlyFormatted.java"), temp);
		mojo.formatFile(temp, codeFormatter);
		temp.delete();
	}

	/**
	 * Even with bad code, the Eclipse code formatter does not return
	 * <code>null</code> anymore. So the exception expected no longer applies.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFormatSingleFileWithBadCode() throws Exception {

		final File temp = File.createTempFile("tmp", "");
		temp.delete();
		temp.mkdir();
		FileUtils.copyDirectoryStructure(new File("src/it/javaconvention"), temp);

		final File tempPom = new File(temp, "pom.xml");
		FileUtils.copyFile(new File("src/test/resources/formatter/xmlonly-pom.xml"), tempPom);
		FileUtils.copyFile(new File("src/test/resources/formatter/xmlonly-pom.xml"),
				new File(temp, "src/main/java/Foo.java"));
		FileUtils.copyFile(new File("README.md"), new File(temp, "src/main/java/Bar.java"));
		final FormatMojo mojo = (FormatMojo) rule.lookupConfiguredMojo(temp, "format");
		rule.setVariableValueToObject(mojo, "javaFormatterProfileXmlUrl",
				new File("src/test/resources/formatter/java-code-formatter.xml").toURI().toURL().toString());

		try {
			mojo.execute();
		} finally {
			FileUtils.deleteDirectory(temp);
		}

	}

	@Test
	public void testFormatSingleFileWithJavaConfiguration() throws Exception {

		final File temp = File.createTempFile("tmp", "");
		temp.delete();
		temp.mkdir();
		FileUtils.copyDirectoryStructure(new File("src/it/javaconvention"), temp);

		final File tempPom = new File(temp, "pom.xml");
		FileUtils.copyFile(new File("src/test/resources/formatter/pom.xml"), tempPom);
		final FormatMojo mojo = (FormatMojo) rule.lookupConfiguredMojo(temp, "format");

		try {
			mojo.execute();
		} finally {
			FileUtils.deleteDirectory(temp);
		}

	}

	@Test
	public void testFormatSingleFileWithXmlConfiguration() throws Exception {

		final File temp = File.createTempFile("tmp", "");
		temp.delete();
		temp.mkdir();
		FileUtils.copyDirectoryStructure(new File("src/it/javaconvention"), temp);

		final File tempPom = new File(temp, "pom.xml");
		FileUtils.copyFile(new File("src/test/resources/formatter/pom.xml"), tempPom);
		final FormatMojo mojo = (FormatMojo) rule.lookupConfiguredMojo(temp, "format");
		rule.setVariableValueToObject(mojo, "javaFormatterProfileXmlUrl",
				new File("src/test/resources/formatter/java-code-formatter.xml").toURI().toURL().toString());

		try {
			mojo.execute();
		} finally {
			FileUtils.deleteDirectory(temp);
		}

	}

	@Test
	public void testFormatSingleFileWithXmlConfigurationFromPom() throws Exception {

		final File temp = File.createTempFile("tmp", "");
		temp.delete();
		temp.mkdir();
		FileUtils.copyDirectoryStructure(new File("src/it/javaconvention"), temp);

		final File tempPom = new File(temp, "pom.xml");
		FileUtils.copyFile(new File("src/test/resources/formatter/xmlonly-pom.xml"), tempPom);
		final FormatMojo mojo = (FormatMojo) rule.lookupConfiguredMojo(temp, "format");
		rule.setVariableValueToObject(mojo, "javaFormatterProfileXmlUrl",
				new File("src/test/resources/formatter/java-code-formatter.xml").toURI().toURL().toString());

		try {
			mojo.execute();
		} finally {
			FileUtils.deleteDirectory(temp);
		}

	}

	@Test
	public void testFormatString() throws Exception {

		@SuppressWarnings("unchecked")
		final CodeFormatter codeFormatter = new DefaultCodeFormatter(
				DefaultCodeFormatterConstants.getJavaConventionsSettings());
		final String content = "package x;import java.util.Date;class F { public int  a( Long x) { return Date.get();}}";
		final TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
				content, 0, content.length(), 0, null);

		final IDocument document = new Document();
		document.set(content);
		edit.apply(document);

	}
}
