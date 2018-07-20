package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlTaskConsumer;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import config.Settings;
import config.Settings.RepairMode;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;
import utils.UtilsComputerVision;
import utils.UtilsTemplateMatching;
import utils.UtilsVisualRepair;
import utils.UtilsXPath;

/*
 *  In order to run test steps to get to initial state. 
 *  		https://github.com/aminmf/crawljax/blob/master/core/src/main/java/com/crawljax/core/plugin/ExecuteInitialPathsPlugin.java
		extends plugin with ExecuteInitialPathsPlugin which can CrawljaxConfiguration config, CrawlTaskConsumer firstConsumer
 */

public class Plugin implements OnNewStatePlugin, OnUrlLoadPlugin, PostCrawlingPlugin, PreCrawlingPlugin {

	CrawlerContext arg = null;

	private static final Logger LOG = LoggerFactory.getLogger(Plugin.class);

	private HostInterface hostInterface;

	private String testCaseFile;

	private int brokenStep;

	private String templateToMatch = null;

	private HashMap<Integer, Statement> repairedTest;

	private EnhancedTestCase testBroken;

	private EnhancedTestCase testCorrect;

	private RepairMode repairStrategy;

	private boolean onlyFirstMatch;

	public Plugin(HostInterfaceImpl hostInterfaceImpl, EnhancedTestCase testBroken, EnhancedTestCase testCorrect,
			int brokenStep, HashMap<Integer, Statement> repairedTest, RepairMode repairStrategy) {

		Settings.aspectActive = false;
		this.hostInterface = hostInterfaceImpl;
		this.testBroken = testBroken;
		this.testCorrect = testCorrect;
		this.brokenStep = brokenStep;
		this.repairedTest = repairedTest;
		this.repairStrategy = repairStrategy;
		this.onlyFirstMatch = true;
	}

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {

		CrawlPath path = context.getCrawlPath();
		CrawlPathExport crawlPathExport = new CrawlPathExport(path);

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.disableHtmlEscaping();
		Gson gson = gsonBuilder.create();
		Type type = new TypeToken<CrawlPathExport>() {
		}.getType();
		String jsonToWrite = gson.toJson(crawlPathExport, type);

		System.out.println(jsonToWrite);
		System.out.println(path.size());
		FileWriter outputFile = null;

		try {
			outputFile = new FileWriter(System.getProperty("user.dir") + Settings.sep + "crawloutput.txt", true);
			outputFile.write(String.valueOf(path.size()) + " : ");
			outputFile.write(path.toString() + '\n');
			outputFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				outputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* get old statement info. */
		Statement oldst = testCorrect.getStatements().get(brokenStep);

		/* get current WebDriver instance. */
		WebDriver driver = context.getBrowser().getWebDriver();

		/* try to get a match (repair) on the new found state. */
		WebElement fromVisual = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver, oldst, repairStrategy);

		/* no element found or it is not a leaf. */
		if (fromVisual == null || !UtilsXPath.isLeaf(fromVisual))

			return;

		else {

			System.out.println("[LOG]\tFound a match for old template in the state: " + newState.getName());

			FileWriter resultFile = null;

			try {
				resultFile = new FileWriter(System.getProperty("user.dir") + Settings.sep + "matchingStates.txt", true);
				resultFile.write(jsonToWrite + '\n');
				resultFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				try {
					resultFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (onlyFirstMatch)
					context.stop();
			}

		}

	}

	@Override
	public void postCrawling(CrawlSession arg0, ExitStatus arg1) {

		// System.out.println("The number of states is: " +
		// arg0.getStateFlowGraph().getNumberOfStates());
		//
		// StateFlowGraph sfg = arg0.getStateFlowGraph();
		// ImmutableSet<StateVertex> sfg_states = sfg.getAllStates();
		// for (StateVertex sv : sfg_states) {
		// ImmutableList<CandidateElement> candidateElements =
		// sv.getCandidateElements();
		// if (candidateElements != null) {
		// for (CandidateElement ce : candidateElements)
		// System.out.println(ce.getGeneralString());
		// }
		// }
		//
		// System.out.println("The number of edges is: " +
		// arg0.getStateFlowGraph().getAllEdges().size());

	}

	@Override
	public void onUrlLoad(CrawlerContext arg0) {

		System.out.println("[LOG]\tLoading test case into Crawljax");
		// System.out.println("Initial URL: " + arg0.getBrowser().getCurrentUrl());

		WebDriver driver = arg0.getBrowser().getWebDriver();

		if (testBroken == null || brokenStep == -1)
			return;

		Map<Integer, Statement> statementMap = testBroken.getStatements();

		for (Integer testStep : statementMap.keySet()) {

			if (testStep == this.brokenStep) {
				break;
			}

			WebElement element = null;

			Statement statement = statementMap.get(testStep);
			SeleniumLocator domSelector = statement.getDomLocator();

			if (this.repairedTest != null && this.repairedTest.containsKey(testStep)) {

				Statement repairedStatement = this.repairedTest.get(testStep);
				domSelector = repairedStatement.getDomLocator();

			}

			element = UtilsVisualRepair.retrieveWebElementFromDomLocator(driver, domSelector);

			if (element != null) {

				try {
					/* after ascertaining the right element, perform the action. */
					if (statement.getAction().equalsIgnoreCase("click")) {

						element.click();

					} else if (statement.getAction().equalsIgnoreCase("clear")) {

						element.clear();

					} else if (statement.getAction().equalsIgnoreCase("sendkeys")) {

						element.sendKeys(statement.getValue());

					} else if (statement.getAction().equalsIgnoreCase("selectByVisibleText")) {

						new Select(element).selectByVisibleText(statement.getValue());

					} else if (statement.getAction().equalsIgnoreCase("getText")) {

						if (element.getText() == statement.getValue()) {

							System.out.println("[LOG]\tAssertion value correct");
							System.out.println(statement.toString());

						}

					}

				} catch (Exception ex) {
					ex.printStackTrace();
					break;
				}
			}

		}

		// System.out.println("Changed initial path URL : " +
		// arg0.getBrowser().getCurrentUrl());
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {

		// System.out.println("PreCrawl");

	}

	private Eventable getCorrespondingEventable(WebElement webElement, Identification identification,
			EventType eventType, EmbeddedBrowser browser) {

		CandidateElement candidateElement = getCorrespondingCandidateElement(webElement, identification, browser);
		Eventable event = new Eventable(candidateElement, eventType);
		System.out.println(event);
		return event;
	}

	public org.w3c.dom.Element getElementFromXpath(String xpathToRetrieve, EmbeddedBrowser browser)
			throws XPathExpressionException {

		Document dom;
		org.w3c.dom.Element element = null;
		try {
			xpathToRetrieve = xpathToRetrieve.toUpperCase();
			String source = browser.getStrippedDomWithoutIframeContent();
			dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			// System.out.println(source);
			XPath xPath = XPathFactory.newInstance().newXPath();
			// System.out.println("value is " + xPath.evaluate(xpathToRetrieve, dom));
			// NodeList nodes = (NodeList)xPath.evaluate(xpathToRetrieve,
			// dom.getDocumentElement(), XPathConstants.NODESET);
			// System.out.println(nodes.getLength());
			// element = (Element) nodes.item(0);
			element = (org.w3c.dom.Element) xPath.evaluate(xpathToRetrieve, dom.getDocumentElement(),
					XPathConstants.NODE);
			// System.out.println("element.getNodeName(): " + element.getNodeName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return element;
	}

	private CandidateElement getCorrespondingCandidateElement(WebElement webElement, Identification identification,
			EmbeddedBrowser browser) {

		Document dom;
		try {
			dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());

			// Get the corresponding org.w3c.dom.Element of a WebElement
			// String xpath = getXPath(webElement);
			String xpath = identification.getValue();
			org.w3c.dom.Element sourceElement = getElementFromXpath(xpath, browser);
			// CandidateElement candidateElement = new CandidateElement(sourceElement, new
			// Identification(Identification.How.xpath, xpath), "");
			CandidateElement candidateElement = new CandidateElement(sourceElement, identification, "");
			LOG.debug("Found new candidate element: {} with eventableCondition {}", candidateElement.getUniqueString(),
					null);
			candidateElement.setEventableCondition(null);
			return candidateElement;

			/*
			 * Previous inefficient way for (CrawlElement crawlTag :
			 * config.getCrawlRules().getAllCrawlElements()) { // checking all tags defined
			 * in the crawlRules NodeList nodeList =
			 * dom.getElementsByTagName(crawlTag.getTagName());
			 * 
			 * //String xpath1 = getXPath(webElement); String xpath2 = null;
			 * org.w3c.dom.Element sourceElement = null;
			 * 
			 * for (int k = 0; k < nodeList.getLength(); k++){ sourceElement =
			 * (org.w3c.dom.Element) nodeList.item(k); // check if sourceElement is
			 * webElement if (checkEqulity(webElement, sourceElement)){ xpath2 =
			 * XPathHelper.getXPathExpression(sourceElement); //
			 * System.out.println("xpath : " + xpath2); CandidateElement candidateElement =
			 * new CandidateElement(sourceElement, new
			 * Identification(Identification.How.xpath, xpath2), "");
			 * LOG.debug("Found new candidate element: {} with eventableCondition {}",
			 * candidateElement.getUniqueString(), null);
			 * candidateElement.setEventableCondition(null); return candidateElement; } } }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("could not find the corresponding CandidateElement");
		return null;
	}

}
