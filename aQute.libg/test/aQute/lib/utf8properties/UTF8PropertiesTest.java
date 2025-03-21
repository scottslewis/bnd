package aQute.lib.utf8properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import aQute.bnd.test.jupiter.InjectTemporaryDirectory;
import aQute.lib.io.IO;
import aQute.libg.reporter.ReporterAdapter;
import aQute.service.reporter.Report.Location;

/**
 * Test if we properly can read
 *
 * @author aqute
 */
public class UTF8PropertiesTest {

	@Test
	public void testEscapedQuotesInQuotedStrings() throws IOException {
		testProperty("Provide-Capability: \\\n" + " test; effective:=\"resolve\"; \\\n" + "  test =\"aName\"; \\\n"
			+ "  version : Version=\"1.0\"; \\\n" + "  long :Long=\"100\"; \\\n" + "  double: Double=\"1.001\"; \\\n"
			+ "  string:String =\"aString\"; \\\n" + "  version.list:List < Version > = \"1.0, 1.1, 1.2\"; \\\n"
			+ "  long.list : List  <Long  >=\"1, 2, 3, 4\"; \\\n"
			+ "  double.list: List<  Double>= \"1.001, 1.002, 1.003\"; \\\n"
			+ "  string.list :List<String  >= \"aString,bString,cString\"; \\\n"
			+ "  string.list2:List=\"a\\\\\"quote,a\\\\,comma, aSpace ,\\\\\"start,\\\\,start,end\\\\\",end\\\\,\"; \\\n"
			+ "  string.list3 :List<String>= \" aString , bString , cString \",", "Provide-Capability",
			"test; effective:=\"resolve\"; test =\"aName\"; version : Version=\"1.0\"; long :Long=\"100\"; double: Double=\"1.001\"; string:String =\"aString\"; version.list:List < Version > = \"1.0, 1.1, 1.2\"; long.list : List  <Long  >=\"1, 2, 3, 4\"; double.list: List<  Double>= \"1.001, 1.002, 1.003\"; string.list :List<String  >= \"aString,bString,cString\"; string.list2:List=\"a\\\"quote,a\\,comma, aSpace ,\\\"start,\\,start,end\\\",end\\,\"; string.list3 :List<String>= \" aString , bString , cString \",");

		testProperty("Foo: a=\"\\\\\"\";b= \"x\",", "Foo", "a=\"\\\"\";b= \"x\",");

		testProperty("Foo=foo;foo=\"a\\'quote\"", "Foo", "foo;foo=\"a\'quote\"",
			"Found backslash escaped quote character"); // foo;foo="a\'quote"
		testProperty("Foo=foo;foo=\"a\\\\\"quote\"", "Foo", "foo;foo=\"a\\\"quote\""); // foo;foo="a\"quote"
		testProperty("Foo=foo;foo=\"a\\\\\\\\\\\"quote\"", "Foo", "foo;foo=\"a\\\\\"quote\""); // foo;foo="a\\\"quote"
		testProperty("Foo=foo;foo=\"a\\\\\\\\\"", "Foo", "foo;foo=\"a\\\\\""); // foo;foo="a\\\""
		testProperty("Foo=foo;foo='a\\\\\"quote'", "Foo", "foo;foo='a\\\"quote'"); // foo;foo="a\"quote"
		testProperty("Foo=foo;foo='a\"quote'", "Foo", "foo;foo='a\"quote'"); // foo;foo="a\"quote"

	}

	@Test
	public void testNBSP() throws IOException {
		testProperty("-instr: 'foo,\u202Fbar'", "-instr", "'foo,\u202Fbar'");
		testProperty("-instr: \"foo,\u202Fbar\"", "-instr", "\"foo,\u202Fbar\"");

		testProperty("Bundle-Description: foo,\u202Fbar", "Bundle-Description", "foo,\u202Fbar");

		testProperty("macro: foo,\u202Fbar", "macro", "foo,\u202Fbar");

		testProperty("-exportcontents: foo,\u202Fbar", "-exportcontents", "foo,\u202Fbar",
			"Non breaking space found \\[NARROW NO-BREAK SPACE");

		testProperty("Export-Package: foo,\u2007bar", "Export-Package", "foo,\u2007bar",
			"Non breaking space found \\[FIGURE SPACE");

		testProperty("Private-Package: foo,\u00A0bar", "Private-Package", "foo,\u00A0bar",
			"Non breaking space found \\[NON BREAKING SPACE");
	}

	@Test
	public void testQuotedStringInMacro() throws IOException {
		assertError("-runproperties: ${def;foo;bar='a b c'}", "-runproperties", 0);
		assertError("-runproperties: $(def;foo;bar='a b c')", "-runproperties", 0);
		assertError("-runproperties: $[def;foo;bar=\"a b c\"]", "-runproperties", 0);
		assertError("-runproperties: $<def;foo;bar='a b c'>", "-runproperties", 0);
		// Guillemet double << >>
		assertError("-runproperties: $«def;foo;bar='a b c'»", "-runproperties", 0);
		// Guillemet single < >
		assertError("-runproperties: $‹def;foo;bar='a b c'›", "-runproperties", 0);
	}

	@Test
	public void testMissingDelimeterAfterQuotedString() throws IOException {
		assertError("-foo: bar='abc' ' '    ;", "-foo", 0,
			"Found a quote ''' while expecting a delimeter. You should quote the whole values, you can use both single and double quotes:");
		assertError("-foo: bar='abc', baz='def' goo='hji'", "-foo", 0,
			"Expected a delimeter, like comma or semicolon, after a quoted string but found 'g':");
		assertError("-foo: bar='abc'                ,   baz='def'", "-foo", 0);
		assertError("-foo: bar='abc'     ;", "-foo", 0);
		assertError("-foo: bar='\\'abc\\' foo'     ;", "-foo", 0);
		assertError("-foo: bar=\"\\\"abc\\\" foo\"     ;", "-foo", 0);
		assertError("foo: bar='abc' ' '    ;", "foo", 0);
		assertError("foo: abc \"  \" '  '", "foo", 0);
	}

	String trickypart = "\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF"
		+ "\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF"
		+ "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF"
		+ "\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF"
		+ "\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF"
		+ "\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";

	/*
	 * Entries are generally expected to be a single line of the form, one of
	 * the following: propertyName=propertyValue propertyName:propertyValue}
	 */
	@Test
	public void testSpecificationAssignment() throws IOException {
		testProperty("propertyName=propertyValue\n", "propertyName", "propertyValue");
		testProperty("propertyName:propertyValue\n", "propertyName", "propertyValue");
		testProperty("propertyName propertyValue\n", "propertyName", "propertyValue");
	}

	/*
	 * White space that appears between the property name and property value is
	 * ignored, so the following are equivalent.
	 */
	@Test
	public void testSpecificationValueSpaces() throws IOException {
		testProperty("name=Stephen\n", "name", "Stephen");
		testProperty("name = Stephen\n", "name", "Stephen");
		testProperty("name=Stephen", "name", "Stephen");
		testProperty("name = Stephen", "name", "Stephen");
	}

	/*
	 * White space at the beginning of the line is also ignored.
	 */
	@Test
	public void testSpecificationKeySpaces() throws IOException {
		testProperty("    name = Stephen\n", "name", "Stephen");
		testProperty("    name = Stephen", "name", "Stephen");
	}

	/*
	 * Lines that start with the comment characters ! or # are ignored. Blank
	 * lines are also ignored.
	 */
	@Test
	public void testSpecificationComments() throws IOException {
		testProperty("\n\n# comment\n!comment\n\nfoo=bar", "foo", "bar");
		testProperty("foo=bar\n# comment", "foo", "bar");
		testProperty("foo=bar\n! comment", "foo", "bar");
		testProperty("# comment\nfoo=bar\n# comment", "foo", "bar");
		testProperty("! comment\nfoo=bar\n! comment", "foo", "bar");
		testProperty("# comment\nfoo=bar\n# comment\n", "foo", "bar");
		testProperty("! comment\nfoo=bar\n! comment\n", "foo", "bar");
	}

	/*
	 * Lines that start with the comment characters ! or # are ignored. Blank
	 * lines are also ignored. Comments which end with \ must not continue to
	 * next line.
	 */
	@Test
	public void testSpecificationCommentContinuation() throws IOException {
		testProperty("\n\n# comment\\\n!comment\\\n\nfoo=bar", "foo", "bar");
		testProperty("foo=bar\n# comment\\", "foo", "bar");
		testProperty("foo=bar\n! comment\\", "foo", "bar");
		testProperty("# comment\\\nfoo=bar\n# comment\\", "foo", "bar");
		testProperty("! comment\\\nfoo=bar\n! comment\\", "foo", "bar");
		testProperty("# comment\\\nfoo=bar\n# comment\\\n", "foo", "bar");
		testProperty("! comment\\\nfoo=bar\n! comment\\\n", "foo", "bar");
	}

	/*
	 * The property value is generally terminated by the end of the line. White
	 * space following the property value is not ignored, and is treated as part
	 * of the property value.
	 */
	@Test
	public void testSpecificationWhitespaceValue() throws IOException {
		testProperty("foo=bar ", "foo", "bar ");
		testProperty("foo=bar \n", "foo", "bar ");
	}

	/*
	 * A property value can span several lines if each line is terminated by a
	 * backslash (‘\’) character. For example:
	 */
	@Test
	public void testSpecificationContinutation() throws IOException {
		testProperty("targetCities=\\\n" //
			+ "        Detroit,\\\n" //
			+ "        Chicago,\\\n" //
			+ "        Los Angeles\n", "targetCities", "Detroit,Chicago,Los Angeles");

	}

	/*
	 * The characters newline, carriage return, and tab can be inserted with
	 * characters \n, \r, and \t, respectively.
	 */
	@Test
	public void testSpecificationControlCharacters() throws IOException {
		testProperty("control= \\t\\n\\r\n", "control", "\t\n\r");
	}

	/*
	 * The backslash character must be escaped as a double backslash. For
	 * example:
	 */
	@Test
	public void testSpecificationBackslashes() throws IOException {
		testProperty("path=c:\\\\docs\\\\doc1", "path", "c:\\docs\\doc1");
	}

	/*
	 * UNICODE characters can be entered as they are in a Java program, using
	 * the \\u prefix. For example, \u002c.
	 */
	@Test
	public void testSpecificationUnicode() throws IOException {
		testProperty("unicode=\\u002c\n", "unicode", ",");
		testProperty("unicode=\\uFEF0\n", "unicode", "\uFEF0");
	}

	/*
	 * You can have control characters in keys
	 */
	@Test
	public void testControlCharactersInKeys() throws IOException {
		testProperty("key\\ key = value\n", "key key", "value", "Found |Invalid");
		testProperty("key\\u002Ckey = value\n", "key,key", "value", "Found |Invalid");
		testProperty("key\\:key = value\n", "key:key", "value", "Found |Invalid");
	}

	@Test
	public void testEmptyContinuations() throws IOException {
		testProperty("-plugin: \\\n\\\n\\\nabc", "-plugin", "abc");
		testProperty("-plugin: \\\n\\\n\\\nabc\n", "-plugin", "abc");
		testProperty("-plugin: \\\n\\\n\\\n    abc\n", "-plugin", "abc");
		testProperty("-plugin: \\\n  \\\n  \\\n    abc\n", "-plugin", "abc");
	}

	@Test
	public void testEmptyKey() throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load("	-runvm: \n" //
			+ "#\"-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044\"", null, ra);
		assertThat(p).containsEntry("-runvm", "")
			.hasSize(1);
	}

	@Test
	public void testEmptySpace() throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load("version 1.1\n ", null, ra);
		assertThat(p).containsEntry("version", "1.1")
			.hasSize(1);
	}

	@Test
	public void testNewlineCr() throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load("a=2\n\rb=3\n\r", null, ra);
		assertThat(p).containsEntry("a", "2")
			.containsEntry("b", "3")
			.hasSize(2);
	}

	@Test
	public void testEscapedNewlinEmpty() throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load("a=x\\\n\n", null, ra);
		assertThat(p).containsEntry("a", "x")
			.hasSize(1);
	}

	@Test
	public void testPackageinfo() throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load("version 1.0.1", null, ra);
		assertThat(p).containsEntry("version", "1.0.1")
			.hasSize(1);
	}

	@Test
	public void testPackageinfoWithCrLf() throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load("version 1.0.1\r\n", null, ra);
		assertThat(p).containsEntry("version", "1.0.1")
			.hasSize(1);
	}

	@Test
	public void testErrorsInParsing() throws IOException {
		assertError("\n\n\n\n\n\n\n" //
			+ "a;b=9", "a;b", 7, "Invalid property key: `a;b`:");
		assertError("\n" //
			+ "a=\\ \n     a;v=4", "a", 1, "Found \\\\<whitespace>", "Invalid property key: `a;v`");
		assertError("\n\n\n\n\n\n\n" //
			+ "a", "a", 7, "No value specified for key");
		assertError("\npropertyName=property\0Value\n", "propertyName", 1,
			"Invalid character in properties: 0 at pos 21:");
		assertError("\nproperty\0Name=propertyValue\n", "property?Name", 1,
			"Invalid character in properties: 0 at pos 8:");
	}

	private void assertError(String string, String key, int line, String... check) throws IOException {
		ReporterAdapter ra = new ReporterAdapter();
		UTF8Properties up = new UTF8Properties();
		up.load(string, IO.getFile("foo"), ra);
		assertThat(up).as("No '%s' property found", key)
			.containsKey(key);
		if (check.length == 0) {
			assertThat(ra.getWarnings()).isEmpty();
		} else {
			assertThat(ra.getWarnings()).isNotEmpty();
			Location location = ra.getLocation(ra.getWarnings()
				.get(0));
			assertThat(location.line).as("Faulty line number")
				.isEqualTo(line);
		}
		assertThat(ra.check(check)).isTrue();
	}

	@Test
	public void testBackslashEncodingWithReader() throws IOException {
		Properties p = new UTF8Properties();
		p.load(new StringReader("x=abc \\\\ def\n"));
		assertThat(p).containsEntry("x", "abc \\ def");
	}

	@Test
	public void testISO8859Encoding() throws IOException {
		Properties p = new UTF8Properties();
		p.load(new ByteArrayInputStream(("x=" + trickypart + "\n").getBytes("ISO-8859-1")));
		assertThat(p).containsEntry("x", trickypart);
	}

	@Test
	public void testUTF8Encoding() throws IOException {
		Properties p = new UTF8Properties();
		p.load(new ByteArrayInputStream(("x=" + trickypart + "\n").getBytes("UTF-8")));
		assertThat(p).containsEntry("x", trickypart);
	}

	@Test
	public void testShowUTF8PropertiesDoNotSkipBackslash() throws IOException {
		Properties p = new UTF8Properties();
		p.load(new ByteArrayInputStream("x=abc \\ def\n".getBytes("UTF-8")));
		assertThat(p).containsEntry("x", "abc  def");
	}

	@Test
	public void testShowPropertiesSkipBackslash() throws IOException {
		Properties p = new Properties();
		p.load(new StringReader("x=abc \\ def\n"));
		assertThat(p).containsEntry("x", "abc  def");
	}

	@Test
	public void testContinuation() throws IOException {
		Properties p = new Properties();
		p.load(new StringReader("x=abc \\\n        def\n"));
		assertThat(p).containsEntry("x", "abc def");
	}

	@Test
	public void testWriteWithoutComment() throws IOException {
		UTF8Properties p = new UTF8Properties();
		p.put("Foo", "Foo");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		p.store(bout);
		String s = new String(bout.toByteArray(), StandardCharsets.UTF_8);
		assertThat(s).doesNotStartWith("#")
			.contains("Foo");
	}

	@Test
	public void testWrite() throws IOException {
		UTF8Properties p = new UTF8Properties();
		p.put("Foo", "Foo");
		p.put("Bar", "Bar\n#comment");
		StringWriter sw = new StringWriter();
		p.store(sw, null);
		String s = sw.toString();
		assertThat(s).doesNotStartWith("#")
			.contains("#comment");
		UTF8Properties p1 = new UTF8Properties();
		p1.load(new StringReader(s));
		assertThat(p1).containsExactlyInAnyOrderEntriesOf(p);
	}

	@Test
	public void testWriteFile(@InjectTemporaryDirectory
	File tmp) throws Exception {
		UTF8Properties p = new UTF8Properties();
		p.put("Foo", "Foo");
		p.put("Bar", "Bar");
		File props = new File(tmp, "props.properties");
		p.store(props);
		assertThat(props).isFile();
		assertThat(IO.collect(props)).doesNotStartWith("#");
		UTF8Properties p1 = new UTF8Properties();
		p1.load(props, null);
		assertThat(p1).containsExactlyInAnyOrderEntriesOf(p);
	}

	@Test
	public void testProvenance() throws IOException {
		UTF8Properties a = new UTF8Properties();
		a.load("""
			a.a = 1
			a.b = 2
			x = 0
			""", null, null, null, "from_a");
		UTF8Properties b = new UTF8Properties();
		b.load("""
			b.a = 1
			b.c = 3
			x = 0
			""", null, null, null, "from_b");

		assertThat(a.getProvenance("x")).isPresent()
			.get()
			.isEqualTo("from_a");
		assertThat(b.getProvenance("x")).isPresent()
			.get()
			.isEqualTo("from_b");
		assertThat(a.getProvenance("y")).isNotPresent();

		a.load(b, true);
		assertThat(a.getProvenance("x")).isPresent()
			.get()
			.isEqualTo("from_b");
		assertThat(a.getProvenance("a.a")).isPresent()
			.get()
			.isEqualTo("from_a");
		assertThat(a.getProvenance("b.a")).isPresent()
			.get()
			.isEqualTo("from_b");
	}

	private void testProperty(String content, String key, String value) throws IOException {
		testProperty(content, key, value, null);
	}

	private void testProperty(String content, String key, String value, String check) throws IOException {
		testProperty0(content, key, value, check);
		testProperty0(content.replaceAll("\n", "\r\n"), key, value, check);
		testProperty0(content.replaceAll("\n", "\r"), key, value, check);
	}

	private void testProperty0(String content, String key, String value, String check) throws IOException {
		UTF8Properties p = new UTF8Properties();
		ReporterAdapter ra = new ReporterAdapter();
		p.load(content, null, ra, new String[] {
			"Export-Package", "Private-Package", "Import-Package", "Provide-Capability", "Foo"
		});

		assertThat((check == null) ? ra.check() : ra.check(check)).isTrue();

		assertThat(p).containsEntry(key, value)
			.hasSize(1);

		Properties pp = new Properties();
		pp.load(new StringReader(content));
		assertThat(pp).containsExactlyInAnyOrderEntriesOf(p);
	}
}
