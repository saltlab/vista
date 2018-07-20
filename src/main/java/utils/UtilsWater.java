package utils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.runner.Result;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.SeleniumLocator;
import datatype.Statement;
import datatype.WebDriverSingleton;
import parser.ParseTest;

public class UtilsWater {

	private static Scanner scanner;
	static WebDriverSingleton instance;

	/**
	 * Return the list of nodes in @param newTree that are found similar to @param
	 * oldNode according to the @param similarityThreshold
	 * 
	 * @param oldNode
	 * @param newTree
	 * @param similarityThreshold
	 * @return
	 */
	public static List<HtmlElement> getSimilarNodes(HtmlElement oldNode, HtmlDomTree newTree, double similarityThreshold) {

		List<HtmlElement> results = new LinkedList<HtmlElement>();
		return searchHtmlDomTreeByNode(oldNode, newTree.getRoot(), similarityThreshold, results);

	}

	public static HtmlElement getNodeByLocator(HtmlDomTree tree, String xpath) {
		return tree.searchHtmlDomTreeByXPath(xpath);
	}

	public static HtmlElement getNodeByLocator(HtmlDomTree tree, SeleniumLocator l) {

		if (l.getStrategy().equals("id")) {
			return tree.searchHtmlDomTreeByAttribute("id", l.getValue());

		} else if (l.getStrategy().equals("className")) {
			return tree.searchHtmlDomTreeByAttribute("class", l.getValue());

		} else if (l.getStrategy().equals("linkText")) {
			return tree.searchHtmlDomTreeByAttribute("text", l.getValue());

		} else if (l.getStrategy().equals("name")) {
			return tree.searchHtmlDomTreeByAttribute("name", l.getValue());

		} else if (l.getStrategy().equals("tagName")) {
			return tree.searchHtmlDomTreeByTagName(l.getValue());

		} else if (l.getStrategy().equals("xpath")) {

			/* workaround for XPath locators that need to be in a specific format. */
			String loc = UtilsWater.formatXPath(l.getValue());

			return tree.searchHtmlDomTreeByXPath(loc);
		}

		return null;
	}

	public static String formatXPath(String value) {

		String[] slices = value.split("/");
		String res = "/";

		for (String s : slices) {
			if (!s.endsWith("]")) {
				res = res.concat(s).concat("[1]");
			} else {
				res = res.concat(s);
			}
			res = res.concat("/");
		}

		res = res.substring(0, res.length() - 1);

		return res;
	}

	public static HtmlElement getNodesByProperty(HtmlDomTree tree, String attribute, String value) {
		return tree.searchHtmlDomTreeByAttribute(attribute, value);
	}

	public static boolean checkRepair(EnhancedTestCase t) throws IOException {
		Result r = ParseTest.runTest(t, t.getPath());
		return r.wasSuccessful();
	}

	public static List<HtmlElement> searchHtmlDomTreeByNode(HtmlElement searchNode, Node<HtmlElement> newTree, double similarityThreshold,
			List<HtmlElement> similarNodes) {

		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(newTree);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (getSimilarityScore(node.getData(), searchNode) > similarityThreshold) {
				if (!similarNodes.contains(node.getData())) {
					similarNodes.add(node.getData());
				}
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return similarNodes;
	}

	public static HtmlDomTree getDom(EnhancedTestCase tc, int line, boolean check) throws SAXException, IOException {

		scanner = new Scanner(System.in);

		String domPath;
		if (tc.getStatements().get(line).getDomBefore() == null)
			domPath = tc.getStatements().get(line).getDomAfter().getAbsolutePath();
		else
			domPath = tc.getStatements().get(line).getDomBefore().getAbsolutePath();

		/* html page to be cleaned. */
		String theHtmlPage = tc.getStatements().get(line).getDomAfter().getName();

		String htmlFileCleaned = domPath.toString();
		/* encode URL. */
		theHtmlPage = java.net.URLEncoder.encode(theHtmlPage, "UTF-8");

		htmlFileCleaned = htmlFileCleaned.substring(0, domPath.lastIndexOf("/") + 1);
		htmlFileCleaned = htmlFileCleaned.concat(theHtmlPage);

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFileCleaned);
		WebDriver driver = instance.getDriver();

		HtmlDomTree domTree;

		if (check) {
			/* extra check for the cases when the authentication is needed. */
			System.out.println("Is the web page correctly displayed? [type Y and Enter key to proceed]");
			while (!scanner.next().equals("Y")) {
			}

			String newFileName = domPath.replace(".html", "-temp.html");

			File newPageSource = new File(newFileName);
			FileUtils.write(newPageSource, driver.getPageSource());

			domTree = new HtmlDomTree(driver, newFileName);
			domTree.buildHtmlDomTree();

			FileUtils.deleteQuietly(newPageSource);
		} else {
			domTree = new HtmlDomTree(driver, domPath);
			domTree.buildHtmlDomTree();
		}

		return domTree;
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1,
						distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

		return distance[str1.length()][str2.length()];
	}

	private static double getSimilarityScore(HtmlElement a, HtmlElement b) {
		double alpha = 0.9;
		double rho, rho1, rho2 = 0;

		if (a.getTagName().equals(b.getTagName())) {
			double levDist = computeLevenshteinDistance(a.getXPath(), b.getXPath());
			rho1 = 1 - levDist / Math.max(a.getXPath().length(), b.getXPath().length());

			if (Math.abs(a.getX() - b.getX()) <= 5 && Math.abs((a.getX() + a.getWidth()) - (b.getY() - b.getHeight())) <= 5
					&& Math.abs(a.getY() - b.getY()) <= 5 && Math.abs((a.getY() + a.getWidth()) - (b.getY() - b.getHeight())) <= 5) {
				rho2 = rho2 + 1;
			}
			rho2 = rho2 / 2;
			rho = (rho1 * alpha + rho2 * (1 - alpha));

			return rho;
		}
		return 0;
	}

	public static List<WebElement> getSimilarNodesBySimilarityScore(WebDriver driver, Statement statement) throws IOException {

		List<WebElement> similar = new LinkedList<WebElement>();
		UtilsAspect.saveDOM(driver, "tempPage" + Settings.HTML_EXT);

		File input = new File("tempPage" + Settings.HTML_EXT);
		Document doc = Jsoup.parse(input, "UTF-8");

		Elements content = doc.getElementsByTag(statement.getTagName());

		for (Element e : content) {

			if (e.tagName().equals(statement.getTagName())) {
				String xp1 = e.xpathSelector(e, "");
				String xp2 = statement.getXpath();

				if (getSimilarityScore(xp1, xp2) > Settings.SIMILARITY_THRESHOLD) {
					WebElement webelem = driver.findElement(By.xpath(xp1));
					similar.add(webelem);
				}
			}

		}

		return similar;
	}

	private static double getSimilarityScore(String xpath1, String xpath2) {
		double alpha = 0.9;
		double rho, rho1, rho2 = 0;

		double levDist = computeLevenshteinDistance(xpath1, xpath2);
		rho1 = 1 - levDist / Math.max(xpath1.length(), xpath2.length());

		rho = (rho1 * alpha + rho2 * (1 - alpha));

		return rho;

	}

}
