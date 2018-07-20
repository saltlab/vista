package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.Settings;
import config.Settings.RepairMode;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;

public class UtilsVisualRepair {

	private static Scanner scanner = new Scanner(System.in);

	/**
	 * Procedure to verify a DOM element locator by means of a visual locator.
	 * 
	 * @param repairStrategy
	 */
	public static WebElement visualAssertWebElement(WebDriver driver, WebElement webElementFromDomLocator, EnhancedTestCase testCorrect, Integer i,
			RepairMode repairStrategy) {

		Statement statement = null;
		WebElement webElementFromVisualLocator = null;

		/*
		 * try to retrieve the visual locator and raises an exception if it does not
		 * exist on the filesystem.
		 */
		try {

			testCorrect.getStatements().get(i).getVisualLocator().toString();
			statement = testCorrect.getStatements().get(i);

		} catch (NullPointerException e) {

			System.out.println("[ERROR]\tVisual locator not found in " + testCorrect.getStatements().get(i));
			System.out.println("[ERROR]\tRe-run the TestSuiteRunner on " + testCorrect.getPath());
			System.exit(1);

		}

		/* retrieve the web element visually. */
		webElementFromVisualLocator = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver, statement, repairStrategy);

		if (webElementFromVisualLocator == null && webElementFromDomLocator == null) {

			System.err.println("[LOG]\tElement not found by either DOM or visual locators. Visual assertion failed. Element might be deleted?");
			// System.exit(1);

		} else if (webElementFromVisualLocator == null) {

			System.err.println("[LOG]\tElement not found (visually) in the state. Visual assertion failed.");
			return null;

		} else if (webElementFromDomLocator == null) {

			System.out.println("[LOG]\tCandidate repair element " + webElementFromVisualLocator);
			webElementFromDomLocator = webElementFromVisualLocator;

		} else if (!UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocator)) {

			System.out.println("[LOG]\tChance of propagated breakage at line " + i);
			System.out.println("[LOG]\tDOM locator and visual locator target two different elements");

			/* I trust the element found by the visual locator. */
			System.out.println("[LOG]\tCandidate repair element " + webElementFromVisualLocator);
			webElementFromDomLocator = webElementFromVisualLocator;

		} else {

			System.out.println("[LOG]\tDOM locator and visual locator target the same element");
			System.out.println("[LOG]\tVisual verification succeeded");

		}

		return webElementFromDomLocator;
	}

	public static WebElement retrieveWebElementFromDomLocator(WebDriver driver, SeleniumLocator domSelector) {

		String strategy = domSelector.getStrategy();
		String locator = domSelector.getValue();
		WebElement element = null;

		if (strategy.equalsIgnoreCase("xpath")) {
			element = driver.findElement(By.xpath(locator));
		} else if (strategy.equalsIgnoreCase("name")) {
			element = driver.findElement(By.name(locator));
		} else if (strategy.equalsIgnoreCase("id")) {
			element = driver.findElement(By.id(locator));
		} else if (strategy.equalsIgnoreCase("linkText")) {
			element = driver.findElement(By.linkText(locator));
		} else if (strategy.equalsIgnoreCase("cssSelector")) {
			element = driver.findElement(By.cssSelector(locator));
		}

		return element;
	}

	public static WebElement retrieveWebElementFromVisualLocator(WebDriver driver, Statement statement, RepairMode repairStrategy) {

		String visualLocator = statement.getVisualLocator().toString();
		String currentScreenshot = System.getProperty("user.dir") + Settings.sep + "currentScreenshot.png";
		UtilsComputerVision.saveScreenshot(driver, currentScreenshot);

//		 if (Settings.debugMode) {
//		 System.out.println("Do you want to continue to the next statement? [type Y and Enter key to proceed]");
//		 while (!scanner.next().equals("Y")) {
//		 }
//		 }

		Point bestMatch = null;

		if (repairStrategy == RepairMode.HYBRID) {

			WebElement fromDom = null;
			fromDom = retrieveWebElementFromDOMInfo(driver, statement);

			if (fromDom == null) {

				Set<Point> allMatches = UtilsTemplateMatching.featureDetectorAndTemplateMatching_dom(currentScreenshot, visualLocator, statement);

				WebElement res = getBestMatch(allMatches, driver, statement);
				return res;

			} else {

				return fromDom;

			}

		} else if (repairStrategy == RepairMode.VISUAL) {

			bestMatch = UtilsTemplateMatching.featureDetectorAndTemplateMatching(currentScreenshot, visualLocator, statement);

			if (bestMatch == null) {

				FileUtils.deleteQuietly(new File(currentScreenshot));
				return null;

			} else {

				String xpathForMatches = UtilsXPath.getXPathFromLocation(bestMatch, driver);
				System.out.println("[LOG]\tElement found by visual analysis: " + xpathForMatches);
				WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));

				FileUtils.deleteQuietly(new File(currentScreenshot));

				return fromVisual;
			}

		} else if (repairStrategy == RepairMode.DOM) {
			return retrieveWebElementFromDOMInfo(driver, statement);
		}

		return null;

	}

	public static List<WebElement> waterRepairLocators(WebDriver driver, Statement statement) throws IOException {

		String id = statement.getId();
		String classAttr = statement.getClassAttribute();
		String nameAttr = statement.getNameAttribute();
		String tagName = statement.getTagName();
		String text = statement.getText();
		String xpath = statement.getXpath();

		List<WebElement> matches = new LinkedList<WebElement>();

		if (id != null && !id.isEmpty()) {
			try {
				matches.addAll(driver.findElements(By.id(id)));
			} catch (Exception Ex) {
			}
		}

		if (xpath != null && !xpath.isEmpty()) {
			try {
				matches.add(driver.findElement(By.xpath(xpath)));
			} catch (Exception Ex) {
			}
		}

		if (classAttr != null && !classAttr.isEmpty()) {
			try {
				matches.addAll(driver.findElements(By.className(classAttr)));
			} catch (Exception Ex) {
			}
		}

		if (text != null && !text.isEmpty()) {
			try {
				matches.addAll(driver.findElements(By.xpath("//*[text()='" + text + "']")));
			} catch (Exception Ex) {
			}

		}

		if (nameAttr != null && !nameAttr.isEmpty()) {
			try {
				matches.addAll(driver.findElements(By.name(nameAttr)));
			} catch (Exception Ex) {
			}
		}

		if (!matches.isEmpty())
			return matches;
		else {
			matches.addAll(UtilsWater.getSimilarNodesBySimilarityScore(driver, statement));
		}

		return matches;

	}

	public static WebElement retrieveWebElementFromDOMInfo(WebDriver driver, Statement statement) {

		String id = statement.getId();
		String classAttr = statement.getClassAttribute();
		String nameAttr = statement.getNameAttribute();
		String tagName = statement.getTagName();
		String text = statement.getText();
		String xpath = statement.getXpath();

		List<WebElement> domElements = new LinkedList<WebElement>();

		if (id != null && !id.isEmpty()) {
			WebElement elem_id = null;
			try {
				elem_id = driver.findElement(By.id(id));
			} catch (Exception Ex) {

			}
			if (elem_id != null)
				return elem_id;
		}

		if (text != null && !text.isEmpty()) {
			WebElement elem_text = null;
			try {
				elem_text = driver.findElement(By.xpath("//*[text()='" + text + "']"));
			} catch (Exception Ex) {

			}
			if (elem_text != null)
				return elem_text;
		}

		if (nameAttr != null && !nameAttr.isEmpty()) {
			WebElement elem_name = null;
			try {
				elem_name = driver.findElement(By.name(nameAttr));
			} catch (Exception Ex) {

			}
			if (elem_name != null)
				return elem_name;
		}

		if (classAttr != null && !classAttr.isEmpty()) {
			WebElement elem_class = null;
			try {
				elem_class = driver.findElement(By.className(classAttr));
			} catch (Exception Ex) {

			}
			if (elem_class != null) {
				return elem_class;
			}
		}

		if (xpath != null && !xpath.isEmpty()) {
			WebElement elem_xpath = null;
			try {
				elem_xpath = driver.findElement(By.xpath(xpath));
			} catch (Exception Ex) {

			}
			if (elem_xpath != null)
				return elem_xpath;
		}

		return null;
	}

	private static boolean insideSeenRectangles(Point p, List<Rect> rects) {
		for (Rect seenRect : rects) {
			if (p.inside(seenRect))
				return true;
		}
		return false;
	}

	/**
	 * Filter the results obtained by the visual locators.
	 * 
	 * @param allMatches
	 * @param driver
	 * @param statement
	 * @return
	 */
	private static WebElement getBestMatch(Set<Point> allMatches, WebDriver driver, Statement statement) {

		if (allMatches == null)
			return null;

		List<Rect> seenRectangles = new ArrayList<Rect>();
		List<WebElement> distinctWebElements = new ArrayList<WebElement>();

		for (Point match : allMatches) {

			if (insideSeenRectangles(match, seenRectangles))
				continue;

			String xpathForMatch = UtilsXPath.getXPathFromLocation(match, driver);
			WebElement webElementForMatch = driver.findElement(By.xpath(xpathForMatch));

			// Consider only the leaf elements
			if (UtilsXPath.isLeaf(webElementForMatch)) {
				// check if other points belong to this rectangle
				Rectangle rect = webElementForMatch.getRect();

				Rect r = new Rect(rect.x, rect.y, rect.width, rect.height);

				seenRectangles.add(r);
				distinctWebElements.add(webElementForMatch);
			}
		}

		/*
		 * Filter results obtained by the visual locators with DOM information. An
		 * alternative might be calculate a similarity score.
		 */
		List<WebElement> filtered_tagName = new ArrayList<WebElement>();
		String tagName = statement.getTagName();
		for (WebElement distinct : distinctWebElements) {
			if (distinct.getTagName().equalsIgnoreCase(tagName))
				filtered_tagName.add(distinct);
		}

		distinctWebElements = filtered_tagName;

		/* filter by id. */
		List<WebElement> filtered_id = new ArrayList<WebElement>();
		String idattr = statement.getId();
		if (!idattr.isEmpty()) {
			for (WebElement distinct : distinctWebElements) {
				String id = distinct.getAttribute("id");
				if (id != null) {
					if (id.equalsIgnoreCase(idattr))
						filtered_id.add(distinct);
				}
			}
			if (filtered_id.size() == 1)
				return filtered_id.get(0);
		}

		/* filter by textual content. */
		String textContent = statement.getText();
		List<WebElement> filtered_text = new ArrayList<WebElement>();
		if (!textContent.trim().isEmpty()) {
			for (WebElement elem : distinctWebElements) {
				if (elem.getAttribute("textContent").trim().equalsIgnoreCase(textContent))
					filtered_text.add(elem);
			}
			if (filtered_text.size() == 1)
				return filtered_text.get(0);
		}

		/* filter by name. */
		List<WebElement> filtered_name = new ArrayList<WebElement>();
		String nameattr = statement.getName();
		if (nameattr != null && !nameattr.isEmpty()) {
			for (WebElement distinct : distinctWebElements) {
				String name = distinct.getAttribute("name");
				if (name != null) {
					if (name.equalsIgnoreCase(nameattr))
						filtered_name.add(distinct);
				}
			}
			if (filtered_name.size() == 1)
				return filtered_name.get(0);
		}

		/* filter by class. */
		List<WebElement> filtered_class = new ArrayList<WebElement>();
		String classattr = statement.getClassAttribute();
		if (!classattr.isEmpty()) {
			for (WebElement distinct : distinctWebElements) {
				String clazz = distinct.getAttribute("class");
				if (clazz != null) {
					if (clazz.equalsIgnoreCase(classattr))
						filtered_class.add(distinct);
				}
			}
			if (filtered_class.size() == 1)
				return filtered_class.get(0);
		}

		/* filter by XPath. */
		List<WebElement> filtered_xpath = new ArrayList<WebElement>();
		String xpath = statement.getXpath();
		for (WebElement distinct : distinctWebElements) {
			String xp = UtilsXPath.generateXPathForWebElement(distinct, "");
			if (xp.equalsIgnoreCase(xpath))
				filtered_xpath.add(distinct);
		}
		if (filtered_xpath.size() == 1)
			return filtered_xpath.get(0);

		/* if none of the filters has been applied, null is returned. */
		return null;
	}

	public static boolean areWebElementsEquals(WebElement webElementFromDomLocator, WebElement webElementFromVisualLocator) {

		return webElementFromDomLocator.equals(webElementFromVisualLocator);

	}

}
