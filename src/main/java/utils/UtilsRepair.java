package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.WebElement;

import config.Settings;
import config.Settings.RepairMode;
import datatype.AttributesComparator;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.SeleniumLocator;
import parser.ParseTest;

public class UtilsRepair {

	public static EnhancedException saveExceptionFromFailure(Failure f) {

		EnhancedException ea = new EnhancedException();
		ea.setException(UtilsParser.getExceptionFromFailure(f));
		ea.setFailedTest(UtilsParser.getFailedTestFromFailure(f));
		ea.setInvolvedLine(UtilsParser.getLineFromFailure(f));
		ea.setMessage(UtilsParser.getMessageFromFailure(f));
		return ea;

	}

	public static void printFailure(Failure failure) {

		System.out.println("MESSAGE");
		System.out.println("--------------------");
		System.out.println(failure.getMessage());
		System.out.println("--------------------");

		System.out.println("TEST HEADER");
		System.out.println("--------------------");
		System.out.println(failure.getTestHeader());
		System.out.println("--------------------");

		System.out.println("TRACE");
		System.out.println("--------------------");
		System.out.println(failure.getTrace());
		System.out.println("--------------------");

		System.out.println("DESCRIPTION");
		System.out.println("--------------------");
		System.out.println(failure.getDescription());
		System.out.println("--------------------");

		System.out.println("EXCEPTION");
		System.out.println("--------------------");
		System.out.println(failure.getException());
		System.out.println("--------------------");

	}

	public static void saveFailures(Failure fail) throws IOException {

		FileWriter bw = new FileWriter(Settings.referenceTestSuiteVisualTraceExecutionFolder + "exception.txt");

		bw.write("MESSAGE" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getMessage() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("TEST HEADER" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getTestHeader() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("TRACE" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getTrace() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("DESCRIPTION" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getDescription().toString() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("EXCEPTION" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getException().toString() + "\n");
		bw.write("--------------------" + "\n");

		bw.close();

	}

	public static String capitalizeFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	public static void printTestCase(EnhancedTestCase tc) {
		for (Integer i : tc.getStatements().keySet()) {
			System.out.println(tc.getStatements().get(i) + ";");
		}
	}

	public static void printTestCaseWithLineNumbers(EnhancedTestCase tc) {
		for (Integer i : tc.getStatements().keySet()) {
			System.out.println(tc.getStatements().get(i).getLine() + ":\t" + tc.getStatements().get(i) + ";");
		}
	}

	/*
	 * Get the minimum of three arrays of integers
	 * 
	 */
	public static int getMinimumValue(File[] afterCorrectTrace, File[] beforeCorrectTrace, File[] afterBrokenTrace, File[] beforeBrokenTrace) {

		int min = Math.min(afterCorrectTrace.length, beforeCorrectTrace.length);
		min = Math.min(min, afterBrokenTrace.length);
		min = Math.min(min, beforeBrokenTrace.length);

		return min;
	}

	/**
	 * This method makes a "deep clone" of any object it is given.
	 */
	public static Object deepClone(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * Old stuff (probably will be used by WATER).
	 * 
	 */
	public static SeleniumLocator generateLocator(HtmlElement htmlElement) {

		SeleniumLocator loc = null;

		// text (<a>), id, name, xpath
		if (htmlElement.getTagName().equalsIgnoreCase("a") && htmlElement.getHtmlAttributes().get("text") != null)
			loc = new SeleniumLocator("linkText", htmlElement.getHtmlAttributes().get("text"));
		else if (htmlElement.getHtmlAttributes().get("id") != null)
			loc = new SeleniumLocator("id", htmlElement.getId());
		else if (htmlElement.getHtmlAttributes().get("name") != null)
			loc = new SeleniumLocator("name", htmlElement.getHtmlAttributes().get("name"));
		else
			loc = new SeleniumLocator("xpath", htmlElement.getXPath());

		return loc;
	}

	/*
	 * Old stuff (probably will be used by WATER).
	 * 
	 */
	public static List<SeleniumLocator> generateAllLocators(HtmlElement htmlElement) {

		List<SeleniumLocator> locs = new LinkedList<SeleniumLocator>();

		// text (<a>), id, name, xpath
		if (htmlElement.getTagName().equalsIgnoreCase("a") && htmlElement.getHtmlAttributes().get("text") != null)
			locs.add(new SeleniumLocator("linkText", htmlElement.getHtmlAttributes().get("text")));

		if (htmlElement.getHtmlAttributes().get("id") != null)
			locs.add(new SeleniumLocator("id", htmlElement.getId()));

		if (htmlElement.getHtmlAttributes().get("name") != null)
			locs.add(new SeleniumLocator("name", htmlElement.getHtmlAttributes().get("name")));

		locs.add(new SeleniumLocator("xpath", htmlElement.getXPath()));

		return locs;
	}

	/*
	 * Old stuff (probably will be used by WATER).
	 * 
	 */
	public static SeleniumLocator getLocators(HtmlDomTree page, WebElement webElementFromDomLocator) {

		String xpath = "/" + UtilsXPath.generateXPathForWebElement(webElementFromDomLocator, "");
		HtmlElement htmlElement = page.searchHtmlDomTreeByXPath(xpath);
		return generateLocator(htmlElement);

	}

	/**
	 * Save the abstract test @temp to file, where @className identifies the name of
	 * the Java class and @prefix the package name
	 * 
	 * @param repairStrategy
	 */
	public static void saveTest(String prefix, String className, RepairMode repairStrategy, EnhancedTestCase temp) {

		String oldPath = Settings.resourcesFolder + prefix.replace(".", "/") + className + Settings.JAVA_EXT;
		String newPath = "";

		if (repairStrategy == RepairMode.DOM) {
			newPath = Settings.resourcesFolder + prefix.replace(".", "RepairedDOM/") + className + Settings.JAVA_EXT;
		} else if (repairStrategy == RepairMode.VISUAL) {
			newPath = Settings.resourcesFolder + prefix.replace(".", "RepairedVisual/") + className + Settings.JAVA_EXT;
		} else if (repairStrategy == RepairMode.HYBRID) {
			newPath = Settings.resourcesFolder + prefix.replace(".", "RepairedHybrid/") + className + Settings.JAVA_EXT;
		}

		try {
			temp = ParseTest.parseAndSaveToJava(temp, oldPath, newPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Method to parse the attributes of an outerHTML of a web element, and retrieve
	 * the best SeleniumLocator according to the heustic used in Leotta et al.
	 * (http://dx.doi.org/10.1002/smr.1771)
	 * 
	 */
	public static SeleniumLocator getLocatorsFromOuterHtml(String source) {

		int tags = StringUtils.countMatches(source, "<") / 2;

		if (tags > 1) {

			String tag;

			if (StringUtils.countMatches(source, " ") > 0) {
				// <input value="Enter" name="submitAuth" tabindex="3" type="submit">
				tag = source.substring(source.indexOf("<") + 1, source.indexOf(">"));
				if (StringUtils.countMatches(tag, " ") > 0) {
					tag = source.substring(0, source.indexOf(" "));
				}
			} else {
				tag = source.substring(source.indexOf("<") + 1, source.indexOf(">"));
			}

			if (tag.equals("a")) {

				/* retrieve the text of the link. */
				String text = source.substring(source.indexOf(">") + 1, source.length());
				text = text.substring(0, text.indexOf("<")).trim();

				/* build a text-based locator. */
				return new SeleniumLocator("linkText", text);

			} else {
				return null;
			}

		} else {

			String tag;

			if (StringUtils.countMatches(source, " ") > 0) {
				// <input value="Enter" name="submitAuth" tabindex="3" type="submit">
				tag = source.substring(1, source.indexOf(" "));
			} else {
				tag = source.substring(1, source.indexOf(">"));
			}

			if (tag.equals("a")) {

				/* retrieve the text of the link. */
				String text = source.substring(source.indexOf(">") + 1, source.length());
				text = text.substring(0, text.indexOf("<")).trim();

				/* build a text-based locator. */
				return new SeleniumLocator("linkText", text);

			} else {

				if (StringUtils.countMatches(source, " ") == 0) {
					String text = source.substring(source.indexOf(">") + 1, source.lastIndexOf("<"));
					return new SeleniumLocator("xpath", "//*[text()='" + text + "']");
				} else {

					try {

						String attributes = source.substring(source.indexOf(" "));
						attributes = attributes.replace(">", "").trim();

						/* manage the case in which the tag is still open. */
						if (attributes.contains("<")) {
							/* retain the attributes and discard the rest. */
							attributes = attributes.substring(0, attributes.lastIndexOf("\"") + 1);
						}

						/* separate each key=value pair. */
						String[] splitted = attributes.split(" ");

						/* build the map of attributes. */
						Map<String, String> attributesMap = new HashMap<>();
						for (String string : splitted) {

							String[] keyvalue = string.split("=");

							/* retain only the attributes that are white-listed. */
							if (Arrays.asList(Settings.ATTRIBUTES_WHITELIST).contains(keyvalue[0]))
								attributesMap.put(keyvalue[0], keyvalue[1]);

						}

						/*
						 * sort the map according to the heuristic used in Leotta et al.
						 * (http://dx.doi.org/10.1002/smr.1771).
						 */
						AttributesComparator comparator = new AttributesComparator();
						SortedMap<String, String> sorted = new TreeMap<String, String>(comparator);
						sorted.putAll(attributesMap);

						/* create SeleniumLocator object. */
						return getLocatorFromTreeMap(sorted);

					} catch (Exception e) {
						return null;
					}
				}
			}
		}

	}

	/**
	 * create SeleniumLocator object in case of attribute-based locators
	 **/
	private static SeleniumLocator getLocatorFromTreeMap(SortedMap<String, String> sorted) {

		/* retrieve the first key (likely to be the best locator). */
		Object o = sorted.keySet().iterator().next();

		if (o.equals("id")) {
			return new SeleniumLocator("id", sorted.get(o).replaceAll("\"", ""));
		} else if (o.equals("name")) {
			return new SeleniumLocator("name", sorted.get(o).replaceAll("\"", ""));
		} else {
			return new SeleniumLocator("xpath", "//*[@" + o + "=" + sorted.get(o).replaceAll("\"", "'") + "]");
		}

	}

}
