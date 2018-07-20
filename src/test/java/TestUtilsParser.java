import static org.junit.Assert.assertTrue;

import org.junit.Test;

import datatype.SeleniumLocator;
import utils.UtilsParser;

public class TestUtilsParser {

	@Test
	public void testGetUrlFromDriverGet() throws Exception {

		String st = "driver.get(\"http://localhost:8888/claroline/issta2018/claroline-1.11.0/index.php\");";

		assertTrue(UtilsParser.getUrlFromDriverGet(st)
				.equals("http://localhost:8888/claroline/issta2018/claroline-1.11.0/index.php"));

		st = "stringwithoutquotes";

		try {
			UtilsParser.getUrlFromDriverGet(st);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetValueFromSendKeys() throws Exception {

		String st = "driver.findElement(By.id(\"login\")).sendKeys(\"admin\");";

		assertTrue(UtilsParser.getValueFromSendKeys(st).equals("admin"));

		st = "stringwithoutquotes";

		try {
			UtilsParser.getValueFromSendKeys(st);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetDomLocatorStatement() throws Exception {

		String st = "driver.findElement(By.id(\"login\")).sendKeys(\"admin\");";

		SeleniumLocator loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("id"));
		assertTrue(loc.getValue().equals("login"));

		st = "driver.findElement(By.xpath(\"//*[@id='loginBox']/form/fieldset/button\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("xpath"));
		assertTrue(loc.getValue().equals("//*[@id='loginBox']/form/fieldset/button"));

		st = "driver.findElement(By.linkText(\"001 - Course001\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("linkText"));
		assertTrue(loc.getValue().equals("001 - Course001"));

		st = "driver.findElement(By.name(\"submitEvent\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("name"));
		assertTrue(loc.getValue().equals("submitEvent"));

		st = "assertTrue(driver.findElement(By.xpath(\"//*[@id='claroBody']/div[2]/div\")).getText().contains(\"You have just created the course website : 003\"));";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("xpath"));
		assertTrue(loc.getValue().equals("//*[@id='claroBody']/div[2]/div"));

		st = "driver.findElement(By.cssSelector(\"img[alt='Edit']\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("cssSelector"));
		assertTrue(loc.getValue().equals("img[alt='Edit']"));

		st = "driver.findElement(By.cssSelector(\"a.msremove > img\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("cssSelector"));
		assertTrue(loc.getValue().equals("a.msremove > img"));

		st = "";

		try {
			UtilsParser.getDomLocator(st);
		} catch (Exception e) {
			assertTrue(true);
		}

		st = "notalocator";

		try {
			UtilsParser.getDomLocator(st);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetClassNameFromPath() throws Exception {

		String st = "src/clarolineDirectBreakage/DirectBreakage.java";

		assertTrue(UtilsParser.getClassNameFromPath(st).equals("DirectBreakage"));

		st = "";

		try {
			UtilsParser.getClassNameFromPath(st);
		} catch (Exception e) {
			assertTrue(true);
		}

		st = "afhjhasjd";

		try {
			UtilsParser.getClassNameFromPath(st);
		} catch (Exception e) {
			assertTrue(true);
		}

		st = "clarolineDirectBreakage/DirectBreakage.java";

		try {
			UtilsParser.getClassNameFromPath(st);
		} catch (Exception e) {
			assertTrue(true);
		}

		st = "DirectBreakage.java";

		try {
			UtilsParser.getClassNameFromPath(st);
		} catch (Exception e) {
			assertTrue(true);
		}

		st = "src/clarolineDirectBreakage/DirectBreakage.json";

		try {
			UtilsParser.getClassNameFromPath(st);
		} catch (Exception e) {
			assertTrue(true);
		}

	}

	@Test
	public void testGetSeleniumLocatorFromWebElement() {

	}

	@Test
	public void testGetValueFromSelect() {

	}

	@Test
	public void testGetAssertion() {

	}

	@Test
	public void testGetPredicate() {

	}

	@Test
	public void testGetValueFromAssertion() {

	}

	@Test
	public void testSerializeTestCase() {

	}

	@Test
	public void testSerializeException() {

	}

	@Test
	public void testSerializeHtmlDomTree() {

	}

	@Test
	public void testToJsonPath() {

	}

	@Test
	public void testReadException() {

	}

	@Test
	public void testGetElementXPath() {

	}

	@Test
	public void testGetElementFromXPathJava() {

	}

	@Test
	public void testGetValueFromRegex() {

	}

	@Test
	public void testIsPointInRectangle() {

	}

	@Test
	public void testGetFailedTestFromFailure() {

	}

	@Test
	public void testGetExceptionFromFailure() {

	}

	@Test
	public void testGetMessageFromFailure() {

	}

	@Test
	public void testGetLineFromFailure() {

	}

	@Test
	public void testConvertToHashMap() {

	}

	@Test
	public void testPrintResults() {

	}

	@Test
	public void testExtractClickablesFromHtmlPage() {

	}

	@Test
	public void testGetTestSuiteNameFromWithinType() {

	}

	@Test
	public void testGetPackageName() {

	}

	@Test
	public void testSaveDOMInformation() {

	}

}
