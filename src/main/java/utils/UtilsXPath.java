package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.Settings;

public class UtilsXPath {

	public static String generateXPathForWebElement(WebElement childElement, String current) {
		String childTag = childElement.getTagName();
		if (childTag.equals("html")) {
			return "html[1]" + current;
		}
		WebElement parentElement = childElement.findElement(By.xpath(".."));
		List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
		int count = 0;
		for (int i = 0; i < childrenElements.size(); i++) {
			WebElement childrenElement = childrenElements.get(i);
			String childrenElementTag = childrenElement.getTagName();
			if (childTag.equals(childrenElementTag)) {
				count++;
			}
			if (childElement.equals(childrenElement)) {
				return generateXPathForWebElement(parentElement, "/" + childTag + "[" + count + "]" + current);
			}
		}
		return null;
	}

	public static String getXPathFromLocation(Point matches, WebDriver driver) {
		String getXpathScript = "window.getPathTo = function(element) {" + "	if (element===document.body) "
				+ "		return element.tagName; " + "	var ix= 0; "
				+ "	var siblings= element.parentNode.childNodes; " + "	for (var i= 0; i<siblings.length; i++) {"
				+ "		var sibling= siblings[i];" + "		if (sibling===element)"
				+ "			return getPathTo(element.parentNode)+'/'+element.tagName+'['+(ix+1)+']';"
				+ "		if (sibling.nodeType===1 && sibling.tagName===element.tagName)" + " 			ix++;" + "	}"
				+ "};";
		String elemFromPoint = "elemForPoint = document.elementFromPoint(" + matches.x + "," + matches.y + ");";
		String getXpathFromPoint = "window.elemFromPoint = window.getPathTo(elemForPoint);";
		((JavascriptExecutor) driver).executeScript(getXpathScript + elemFromPoint + getXpathFromPoint);
		String xpath = (String) ((JavascriptExecutor) driver).executeScript("return window.elemFromPoint");
		String result = "HTML/" + xpath;
		return result.toLowerCase();
	}

	public static String getXPathFromVisualLocatorWebElement(WebElement webElementFromVisualLocatorPerfect) {
		String res = webElementFromVisualLocatorPerfect.toString();
		res = res.substring(res.indexOf("html"), res.length());
		res = res.substring(0, res.length() - 1);
		return res;
	}

	public static boolean isLeaf(WebElement fromVisual) {
		// TODO Auto-generated method stub
		List<WebElement> children = fromVisual.findElements(By.xpath(".//*"));
		if (children.size() == 1 && (children.get(0).getTagName().equalsIgnoreCase("text")
				|| children.get(0).getTagName().equalsIgnoreCase("#text")))
			return true;
		if (children.size() == 0)
			return true;
		return false;
	}

	/**
	 * Given an HTML element, retrieve its XPath
	 * 
	 * @param js
	 *            Selenium JavascriptExecutor object to execute javascript
	 * @param element
	 *            Selenium WebElement corresponding to the HTML element
	 * @return XPath of the given element
	 */
	public static String getElementXPath(JavascriptExecutor js, WebElement element) {
		return (String) js
				.executeScript("var getElementXPath = function(element) {" + "return getElementTreeXPath(element);"
						+ "};" + "var getElementTreeXPath = function(element) {" + "var paths = [];"
						+ "for (; element && element.nodeType == 1; element = element.parentNode)  {" + "var index = 0;"
						+ "for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {"
						+ "if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE) {" + "continue;" + "}"
						+ "if (sibling.nodeName == element.nodeName) {" + "++index;" + "}" + "}"
						+ "var tagName = element.nodeName.toLowerCase();"
						+ "var pathIndex = (\"[\" + (index+1) + \"]\");" + "paths.splice(0, 0, tagName + pathIndex);"
						+ "}" + "return paths.length ? \"/\" + paths.join(\"/\") : null;" + "};"
						+ "return getElementXPath(arguments[0]);", element);
	}

	public static Element getElementFromXPathJava(String xPath, Document doc) throws IOException {
	
		String xPathArray[] = xPath.split("/");
		ArrayList<String> xPathList = new ArrayList<String>();
	
		for (int i = 0; i < xPathArray.length; i++) {
			if (!xPathArray[i].isEmpty()) {
				xPathList.add(xPathArray[i]);
			}
		}
	
		Element foundElement = null;
		Elements elements;
		int startIndex = 0;
	
		String id = UtilsParser.getElementId(xPathList.get(0));
		if (id != null && !id.isEmpty()) {
			foundElement = doc.getElementById(id);
			if (foundElement == null)
				return null;
			elements = foundElement.children();
			startIndex = 1;
		} else {
			elements = doc.select(xPathList.get(0).replaceFirst(Settings.REGEX_FOR_GETTING_INDEX, ""));
		}
		for (int i = startIndex; i < xPathList.size(); i++) {
			String xPathFragment = xPathList.get(i);
			int index = UtilsParser.getSiblingIndex(xPathFragment);
			boolean found = false;
	
			// strip off sibling index in square brackets
			xPathFragment = xPathFragment.replaceFirst(Settings.REGEX_FOR_GETTING_INDEX, "");
	
			for (Element element : elements) {
				if (found == false && xPathFragment.equalsIgnoreCase(element.tagName())) {
					// check if sibling index present
					if (index > 1) {
						int siblingCount = 0;
						for (Element siblingElement = element
								.firstElementSibling(); siblingElement != null; siblingElement = siblingElement
										.nextElementSibling()) {
							if ((siblingElement.tagName().equalsIgnoreCase(xPathFragment))) {
								siblingCount++;
								if (index == siblingCount) {
									foundElement = siblingElement;
									found = true;
									break;
								}
							}
						}
						// invalid element (sibling index does not exist)
						if (found == false)
							return null;
					} else {
						foundElement = element;
						found = true;
					}
					break;
				}
			}
	
			// element not found
			if (found == false) {
				return null;
			}
	
			elements = foundElement.children();
		}
		return foundElement;
	}
}
