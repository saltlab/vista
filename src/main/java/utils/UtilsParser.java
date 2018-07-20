package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.InvalidFileFormatException;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import config.Settings;
import datatype.DOMInformation;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.SeleniumLocator;
import japa.parser.ast.stmt.Statement;

public class UtilsParser {

	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Auxiliary method to get the value for the get statement (i.e., the URL)
	 * 
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static String getUrlFromDriverGet(String s) throws Exception {

		if (s.length() == 0 || s.isEmpty() || !s.contains("\"")) {
			throw new Exception("[ERR]\tdriver get statement malformed");
		}

		String[] valuesInQuotes = StringUtils.substringsBetween(s, "\"", "\"");

		if (valuesInQuotes.length != 1) {
			throw new Exception("[ERR]\tdriver get statement malformed");
		}

		return valuesInQuotes[0];
	}

	/**
	 * Auxiliary method to get the value for the sendKeys statement
	 * 
	 * @param st
	 * @return
	 */
	public static String getValueFromSendKeys(String s) throws Exception {

		if (s.length() == 0 || s.isEmpty() || !s.contains("sendKeys")) {
			throw new Exception("[ERR]\tsendKeys statement malformed");
		}

		String[] valuesInQuotes = StringUtils.substringsBetween(s, "sendKeys(\"", "\"");

		if (valuesInQuotes.length != 1) {
			throw new Exception("[ERR]\\tsendKeys statement malformed");
		}

		return valuesInQuotes[0];
	}

	/**
	 * get class name from path e.g. src/clarolineDirectBreakage/DirectBreakage.java
	 * => DirectBreakage
	 * 
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	public static String getClassNameFromPath(String arg) throws Exception {

		if (arg.length() == 0 || arg.isEmpty() || !arg.contains(Settings.JAVA_EXT)) {
			throw new Exception("[ERR]\tmalformed classname path");
		}

		return arg.substring(arg.lastIndexOf("/") + 1).replace(Settings.JAVA_EXT, "");
	}

	/**
	 * auxiliary method to extract the DOM locator used by the web element
	 * 
	 * @param webElement
	 * @return
	 * @throws Exception
	 */
	public static SeleniumLocator getDomLocator(String s) throws Exception {

		if (s.length() == 0 || s.isEmpty() || !s.contains("driver.findElement")) {
			throw new Exception("[ERR]\tdriver findElement statement malformed");
		}

		String[] valuesInQuotes = StringUtils.substringsBetween(s, "By.", ")");

		if (valuesInQuotes.length != 1) {
			throw new Exception("[ERR]\tdriver findElement statement malformed");
		}

		String[] splitted = StringUtils.split(valuesInQuotes[0], "(");

		String strategy = splitted[0].trim();
		String value = splitted[1].replaceAll("\"", "").trim();

		return new SeleniumLocator(strategy, value);
	}

	/**
	 * auxiliary method to extract the DOM locator used by a web element
	 * 
	 * @param webElement
	 * @return
	 * @throws Exception
	 */
	public static SeleniumLocator getDomLocator(WebElement st) throws Exception {

		String domLocator = st.toString();

		return getDomLocator(domLocator);

		// domLocator = domLocator.substring(domLocator.indexOf("By"),
		// domLocator.length()); // By.id("login")).sendKeys("admin");
		// domLocator = domLocator.substring(domLocator.indexOf("By"),
		// domLocator.indexOf(")") + 1); // By.id("login")
		// domLocator = domLocator.replace("By.", ""); // id("login")
		// String strategy = domLocator.split("\\(")[0].trim();
		// String value = domLocator.split("\\(")[1];
		// value = value.substring(0, value.length() - 1).replaceAll("\"", "").trim();
		//
		// return new SeleniumLocator(strategy, value);
	}

	public static SeleniumLocator extractSeleniumLocatorFromWebElement(WebElement webElement) {
		String res = webElement.toString();
		res = res.substring(res.indexOf("-> ") + 3, res.length());
		res = res.substring(0, res.length() - 1);
		String strategy = res.split(":")[0].trim();
		String value = res.split(":")[1].trim();
		value = value.replaceAll("\"", "").trim();
		return new SeleniumLocator(strategy, value);
	}

	public static String getValueFromSelect(Statement st) {

		String value = st.toString(); // new
										// Select(driver.findElement(By.id("course_category"))).selectByVisibleText("(SC)
										// Sciences");
		value = value.substring(value.indexOf("selectBy"), value.length()); // selectByVisibleText("(SC)
																			// Sciences");
		value = value.substring(value.indexOf("(") + 1, value.indexOf("\");") + 1); // (SC)
																					// Sciences
		return value;
	}

	public static String getAssertion(Statement st) {

		// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
		// Doe"));
		String a = st.toString();
		int begin = a.indexOf("assert");
		int end = a.indexOf("(", begin);
		a = a.substring(begin, end);
		return a;
	}

	/**
	 * return the predicate used in the assertion
	 * 
	 * @param st
	 * @return
	 */
	public static String getPredicate(Statement st) {

		// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
		// Doe"));
		if (st.toString().contains("assert") && st.toString().contains("getText()")) {

			String a = st.toString();
			int begin = a.indexOf("getText().");
			a = a.substring(begin + "getText().".length(), a.length() - 2);
			a = a.substring(0, a.indexOf("("));
			return a;
		} else {
			System.err.println("[WARNING]\tUnable to extract predicate from assertion");
			return "";
		}

	}

	/**
	 * return the value used in the assertion
	 * 
	 * @param st
	 * @return
	 */
	public static String getValueFromAssertion(Statement st) {

		// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
		// Doe"));
		if (st.toString().contains("assert") && st.toString().contains("getText()")) {

			String a = st.toString();
			int begin = a.indexOf("getText().");
			a = a.substring(begin + "getText().".length(), a.length() - 2);
			a = a.substring(a.indexOf("("), a.length());
			a = a.replaceAll("\"", "");
			a = a.replaceAll("\\(", "");
			a = a.replaceAll("\\)", "");
			a = a.trim();
			return a;
		} else {
			System.err.println("[WARNING]\tUnable to extract predicate from assertion");
			return "";
		}

	}

	/**
	 * Save the test case in JSON format
	 * 
	 * @param tc
	 * @param path
	 */
	public static void serializeTestCase(EnhancedTestCase tc, String path, String folder) {

		// src/clarolineDirectBreakage/DirectBreakage.java
		// testSuite/DirectBreakage/DirectBreakage.json

		int lastSlash = path.lastIndexOf("/");
		int end = path.indexOf(".java");
		String testName = path.substring(lastSlash + 1, end);
		String newPath = folder + testName + Settings.sep + testName + Settings.JSON_EXT;

		try {
			FileUtils.write(new File(newPath), gson.toJson(tc));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save the exception in JSON format
	 * 
	 * @param tc
	 * @param path
	 */
	public static void serializeException(EnhancedException ex, String path) {

		try {
			FileUtils.write(new File(path), gson.toJson(ex, EnhancedException.class));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Settings.VERBOSE)
			System.out.println("[LOG]\tException saved: " + path);
	}

	public static String toJsonPath(String path) {
		// src/clarolineDirectBreakage/DirectBreakage.java
		// testSuite/DirectBreakage/exception.json

		int lastSlash = path.lastIndexOf("/");
		int end = path.indexOf(".java");
		String testName = path.substring(lastSlash + 1, end);
		String newPath = Settings.testingTestSuiteVisualTraceExecutionFolder + testName + Settings.sep + "exception" + Settings.JSON_EXT;
		return newPath;
	}

	/**
	 * Save the exception in JSON format
	 * 
	 * @param tc
	 * @param path
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static EnhancedException readException(String path) throws JsonSyntaxException, IOException {

		if (!path.endsWith(Settings.JSON_EXT)) {
			throw new InvalidFileFormatException("[ERROR]\tInvalid file extension");
		} else if (!path.contains("exception")) {
			throw new InvalidFileFormatException("[ERROR]\tInvalid exception file");
		}

		File f = new File(path);
		EnhancedException ea = gson.fromJson(FileUtils.readFileToString(f), EnhancedException.class);
		return ea;
	}

	static int getSiblingIndex(String xPathElement) {
		String value = getValueFromRegex(Settings.REGEX_FOR_GETTING_INDEX, xPathElement);
		if (value == null)
			return -1;
		return Integer.parseInt(value);
	}

	static String getElementId(String xPathElement) {
		return getValueFromRegex(Settings.REGEX_FOR_GETTING_ID, xPathElement);
	}

	public static String getValueFromRegex(String regex, String str) {
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public static boolean isPointInRectangle(int x, int y, int left, int top, int width, int height, boolean isBorderIncluded) {

		if (isBorderIncluded) {
			if (x >= left && y >= top && x <= (left + width) && y <= (top + height))
				return true;
		} else {
			if (x > left && y > top && x < (left + width) && y < (top + height))
				return true;
		}
		return false;
	}

	// OK
	public static String getFailedTestFromFailure(Failure f) {
		String s = f.getTestHeader().substring(0, f.getTestHeader().indexOf("("));
		return s;
	}

	// OK
	public static String getExceptionFromFailure(Failure f) {
		String s = null;

		try {
			s = f.getException().toString().substring(0, f.getException().toString().indexOf("For documentation", 0));
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("[ERROR]\tException not supported by the current implementation");
			System.out.println(f.getMessage());
			System.exit(1);
		}

		return s;
	}

	// OK
	public static String getMessageFromFailure(Failure f) {

		String s;

		if (f.getMessage().contains("Cannot locate element with text:")) {
			s = f.getMessage().toString().substring(0, f.getException().toString().indexOf("For documentation", 0));
			s = s.substring(0, s.indexOf("For documentation"));
		} else {
			s = f.getMessage().toString().substring(0, f.getMessage().toString().indexOf("Command"));
		}

		return s;
	}

	// OK
	public static String getLineFromFailure(Failure f) {
		String s = f.getTrace();
		int begin = s.indexOf(getFailedTestFromFailure(f), 0);
		s = s.substring(begin, s.indexOf(System.getProperty("line.separator"), begin));
		return s.replaceAll("\\D+", "");
	}

	public static Map<String, File> convertToHashMap(File[] tests) {

		Map<String, File> m = new HashMap<String, File>();
		for (File test : tests)
			m.put(test.getName(), test);
		return m;
	}

	/**
	 * retrieve the HTML elements
	 * 
	 * @param dt
	 * @param list
	 */
	public static void extractClickablesFromHtmlPage(HtmlDomTree dt, List<Node<HtmlElement>> list) {

		List<String> tags = new LinkedList<String>();
		tags.add("a");
		tags.add("input");
		dt.getClickables(tags, list);

	}

	public static String getTestSuiteNameFromWithinType(String withinType) {
		// class clarolineDirectBreakage.TestLoginAdmin -> clarolineDirectBreakage

		if (withinType.contains("main.java")) {
			withinType = withinType.replaceAll("class ", "");
		} else {
			withinType = withinType.replaceAll("class ", "");
		}

		withinType = withinType.substring(0, withinType.indexOf("."));
		return withinType;
	}

	public static String getPackageName(String newclazz) {
		// src/main/resources/clarolineDirectBreakageRepaired/TestLoginAdminRepaired.java
		newclazz = newclazz.replace("src/main/resources/", "");
		newclazz = newclazz.substring(0, newclazz.indexOf("/"));
		return newclazz;
	}

	/**
	 * Save pertinent DOM information for the given web element and stores them in
	 * JSON file
	 * 
	 * @param d
	 * 
	 * @param we
	 * @param domInfoJsonFile
	 */
	public static void saveDOMInformation(WebDriver d, WebElement we, String domInfoJsonFile) {

		JavascriptExecutor js = (JavascriptExecutor) d;
		DOMInformation webElementWithDomInfo = new DOMInformation(js, we);

		try {
			FileUtils.writeStringToFile(new File(domInfoJsonFile), gson.toJson(webElementWithDomInfo, DOMInformation.class));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void sanityCheck(EnhancedTestCase etc, EnhancedTestCase testCorrect) {

		Map<Integer, datatype.Statement> a = etc.getStatements();
		Map<Integer, datatype.Statement> b = testCorrect.getStatements();
		
		ArrayList<Integer> list1 = new ArrayList<Integer>(a.keySet());
		ArrayList<Integer> list2 = new ArrayList<Integer>(b.keySet());
		
		if (list1.get(0) != list2.get(0)) {
			System.out.println("[ERROR]\tTests numbering is not aligned: " + list1.get(0) + "!=" + list2.get(0));
			throw new NullPointerException();
		}

	}

}
