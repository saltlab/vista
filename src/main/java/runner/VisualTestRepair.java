package runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import config.Settings;
import config.Settings.RepairMode;
import crawler.CrawlPathExport;
import crawler.Crawler;
import crawler.EventableExport;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;
import parser.ParseTest;
import utils.UtilsCrawler;
import utils.UtilsFileGetters;
import utils.UtilsParser;
import utils.UtilsRepair;
import utils.UtilsRunner;
import utils.UtilsVisualRepair;

/**
 * The VisualTestRepair runs a JUnit Selenium test suites on a new version of
 * the application for which is was developed. It uses the visual execution
 * traces captured previously to verify the correctness of the statements prior
 * to their execution. In case of mismatches, automatic repair techniques are
 * triggered.
 * 
 * @author astocco
 * @author yrahulkr
 *
 */
public class VisualTestRepair {

	RepairMode repairStrategy;
	private static Scanner scanner = new Scanner(System.in);

	public VisualTestRepair(RepairMode rm) {
		/*
		 * aspectJ must be disable here. TODO: eventually enable it in the future to
		 * re-create the new visual execution trace
		 */
		Settings.aspectActive = false;
		repairStrategy = rm;
	}

	void runTestWithVisualValidation(String prefix, String className) throws IOException {

		/* get the path to the test that needs to be verified. */
		String testBroken = UtilsFileGetters.getTestFile(className, Settings.pathToTestSuiteUnderTest);

		System.out.println("[LOG]\tVerifying test " + prefix + className);
		System.out.println("[LOG]\tRepair strategy is " + repairStrategy);

		Class<?> clazz = null;
		Object inst = null;

		try {
			clazz = Class.forName(prefix + className);
			inst = clazz.newInstance();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		}

		/* run the setup method (typically opens the browser). */
		UtilsRunner.runMethod(clazz, inst, "setUp");

		/* retrieve the WebDriver instance. */
		WebDriver driver = (WebDriver) UtilsRunner.runMethod(clazz, inst, "getDriver");

		String url = "";
		try {
			url = driver.getCurrentUrl();
		} catch (NullPointerException e) {
			System.err.println("[ERROR]\tInsert getDriver() method in the test " + className);
			UtilsRunner.cleanup(clazz, inst);
			System.exit(1);
		}

		/* parse the tests and create the abstractions. */
		ParseTest pt = null;
		EnhancedTestCase etc = null;
		EnhancedTestCase testCorrect = null;

		try {
			pt = new ParseTest(Settings.referenceTestSuiteVisualTraceExecutionFolder);
			etc = pt.parseAndSerialize(testBroken);

			pt.setFolder(Settings.referenceTestSuiteVisualTraceExecutionFolder);
			testCorrect = pt.parseAndSerialize(UtilsFileGetters.getTestFile(className, Settings.pathToReferenceTestSuite));

			UtilsParser.sanityCheck(etc, testCorrect);

		} catch (NullPointerException e) {
			System.out.println("[ERROR]\tErrors occurred while initiliazing the tests. "
					+ "Verify that the settings are correct and that the test classes start with the same line number.");
			UtilsRunner.cleanup(clazz, inst);
			System.exit(1);
		}

		/* map of the original statements. */
		Map<Integer, Statement> statementMap = etc.getStatements();

		/* map of the repaired statements. */
		Map<Integer, Statement> repairedTest = new LinkedHashMap<Integer, Statement>();
		Map<Integer, Statement> addedSteps = new LinkedHashMap<Integer, Statement>();
		List<Integer> deletedSteps = new ArrayList<Integer>();

		int numStepsAdded = 0;
		int numStepsDeleted = 0;
		boolean noSuchElementException = false;

		/* for each statement. */
		for (Integer statementNumber : statementMap.keySet()) {

			Statement statement = statementMap.get(statementNumber);

			System.out.println("[LOG]\tStatement " + statementNumber + ": " + statement.toString());
			System.out.println("[LOG]\tAsserting visual correcteness");

			WebElement webElementFromDomLocator = null;

			Statement repairedStatement = (Statement) UtilsRepair.deepClone(statement);

			try {

				/* try to poll the DOM looking for the web element. */
				webElementFromDomLocator = UtilsVisualRepair.retrieveWebElementFromDomLocator(driver, statement.getDomLocator());
				noSuchElementException = false;

			} catch (NoSuchElementException Ex) {

				noSuchElementException = true;
				/*
				 * if NoSuchElementException is captured, it means that I'm incurred into a
				 * non-selection that would lead to a direct breakage in the test.
				 */

				System.out.println("[LOG]\tDirect breakage detected at line " + statement.getLine());
				System.out
						.println("[LOG]\tCause: Non-selection of elements by the locator " + statement.getDomLocator() + " in the current DOM state");
				System.out.println("[LOG]\tApplying visual detection of the element");

				/*
				 * if the element is not found it can either be:
				 * 
				 * 1. on the same state (current default strategy) 2. another state (requires
				 * local crawling) 3. absent (we delete the statement)
				 * 
				 */

				/* strategy 1. search web element visually on the same state. */
				webElementFromDomLocator = UtilsVisualRepair.visualAssertWebElement(driver, webElementFromDomLocator, testCorrect, statementNumber,
						repairStrategy);

				/*
				 * actually the local crawling step might also check whether the step is no
				 * longer possible.
				 */
				if (webElementFromDomLocator == null) {

					File resultFile = new File(System.getProperty("user.dir") + Settings.sep + "matchingStates.txt");
					try {
						resultFile.delete();
					} catch (Exception Ex2) {
						System.out.println("Couldn't delete matching states file");
					}

					new Crawler(url, etc, testCorrect, statementNumber, repairedTest, repairStrategy).runLocalCrawling();

					if (resultFile.exists()) {

						List<CrawlPathExport> matchingStates = UtilsCrawler.getCrawledStates();

						if (!matchingStates.isEmpty()) {
							// Add a test step here
							CrawlPathExport matchingState = matchingStates.get(0);
							List<EventableExport> eventList = matchingState.eventableExportList;

							/* For each event in crawl, add a statement to added steps */
							for (EventableExport event : eventList) {
								// How getHow = event.getHow;
								String xpath = event.getValue;
								WebElement elementFromCrawl = driver.findElement(By.xpath(xpath));

								/* Create a new statement and add it to added steps */
								Statement added = (Statement) UtilsRepair.deepClone(statement);

								String source = elementFromCrawl.getAttribute("outerHTML");
								if (source == null || source.length() == 0) {
									source = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].outerHTML;", elementFromCrawl);
								}

								if (source == null || source.length() == 0) {
									System.out.println("[ERROR]\tCannot retrieve outerHTML for webElement " + webElementFromDomLocator);

									/* repaired locator is an XPath. */
									added.setDomLocator(elementFromCrawl);
								} else {

									/* generate a smartest locator based on the attributes of the element. */

									SeleniumLocator fixedLocator = UtilsRepair.getLocatorsFromOuterHtml(source);

									added.setDomLocator(fixedLocator);

								}

								added.setAction("click");
								added.setValue("");
								System.out.println("Statement added : " + added.toString());
								addedSteps.put(new Integer(statementNumber + numStepsAdded), added);
								numStepsAdded += 1;

								/* Click on the element from the crawl to navigate to the page */
								elementFromCrawl.click();

							}

							/*
							 * After all steps are added, rerun the find element on the web page for the
							 * current statement
							 */
							webElementFromDomLocator = UtilsVisualRepair.visualAssertWebElement(driver, webElementFromDomLocator, testCorrect,
									statementNumber, repairStrategy);
						}
					} else {
						// Delete the step

						System.out.println("Statement " + statementNumber + " deleted : " + statement.toString());
						deletedSteps.add(statementNumber);
						numStepsDeleted += 1;
						continue;
					}
				}

				if (webElementFromDomLocator == null) {

					/* the visual check has failed. */
					System.err.println("[LOG]\tStatement " + statementNumber + " still broken.");

				} else {

					String source = webElementFromDomLocator.getAttribute("outerHTML");
					if (source == null || source.length() == 0) {
						source = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].outerHTML;", webElementFromDomLocator);
					}

					if (source == null || source.length() == 0) {
						System.out.println("[ERROR]\tCannot retrieve outerHTML for webElement " + webElementFromDomLocator);

						/* repaired locator is an XPath. */
						repairedStatement.setDomLocator(webElementFromDomLocator);
					} else {

						/* generate a smartest locator based on the attributes of the element. */

						SeleniumLocator fixedLocator = UtilsRepair.getLocatorsFromOuterHtml(source);

						if (fixedLocator == null) {
							repairedStatement.setDomLocator(webElementFromDomLocator);
						} else {
							repairedStatement.setDomLocator(fixedLocator);
						}

					}

					/* add the repaired statement to the test. */
					repairedTest.put(statementNumber, repairedStatement);

				}

			}

			if (webElementFromDomLocator != null) {

				if (!noSuchElementException) {

					WebElement webElementVisual = null;

					/* check the web element visually. */
					webElementVisual = UtilsVisualRepair.visualAssertWebElement(driver, webElementFromDomLocator, testCorrect, statementNumber,
							repairStrategy);

					if (webElementVisual != null) {

						// webElementFromDomLocator = webElementVisual;

						String source = webElementFromDomLocator.getAttribute("outerHTML");
						if (source == null || source.length() == 0) {
							source = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].outerHTML;", webElementFromDomLocator);
						}

						if (source == null || source.length() == 0) {
							System.out.println("[ERROR]\tCannot retrieve outerHTML for webElement " + webElementFromDomLocator);

							/* repaired locator is an XPath. */
							repairedStatement.setDomLocator(webElementFromDomLocator);
						} else {

							/* generate a smartest locator based on the attributes of the element. */

							SeleniumLocator fixedLocator = UtilsRepair.getLocatorsFromOuterHtml(source);

							if (fixedLocator == null) {
								repairedStatement.setDomLocator(webElementFromDomLocator);
							} else {
								repairedStatement.setDomLocator(fixedLocator);
							}

						}

					}

				}
			}

			try {
				/* after ascertaining the right element, perform the action. */
				if (statement.getAction().equalsIgnoreCase("click")) {

					webElementFromDomLocator.click();

				} else if (statement.getAction().equalsIgnoreCase("clear")) {

					webElementFromDomLocator.clear();

				} else if (statement.getAction().equalsIgnoreCase("sendkeys")) {

					webElementFromDomLocator.sendKeys(statement.getValue());

				} else if (statement.getAction().equalsIgnoreCase("selectByVisibleText")) {

					new Select(webElementFromDomLocator).selectByVisibleText(statement.getValue());

				} else if (statement.getAction().equalsIgnoreCase("getText")) {

					if (webElementFromDomLocator.getText().equals(statement.getValue())) {

						// System.out.println("[LOG]\tAssertion value correct");
						// System.out.println(statement.toString());

					} else {

						// System.out.println("[LOG]\tAssertion value incorrect: " + "\"" +
						// webElementFromDomLocator.getText() + "\"" + " <> " + "\"" +
						// statement.getValue() + "\"");
						// System.out.println("[LOG]\tSuggested new value for assertion: " + "\"" +
						// webElementFromDomLocator.getText() + "\"");

						/* repair assertion value. */
						repairedStatement.setValue(webElementFromDomLocator.getText());
					}

				}

				/* add the repaired statement to the test. */
				repairedTest.put(statementNumber, repairedStatement);

			} catch (Exception ex) {
				ex.printStackTrace();
				break;
			}

			if (Settings.debugMode) {
				System.out.println("Do you want to continue to the next statement? [type Y and Enter key to proceed]");
				while (!scanner.next().equals("Y")) {
				}
			}

			System.out.print("\n");

		}

		System.out.println("[LOG]\toriginal test case");
		UtilsRepair.printTestCaseWithLineNumbers(etc);

		EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(etc);
		temp.replaceStatements(repairedTest);

		/* For each added statement, add it to the test case */
		for (Integer addedStatement : addedSteps.keySet()) {
			temp.addStatementAtPosition(addedStatement, addedSteps.get(addedStatement));
		}

		/* For each deleted statement, delete it from the position in test case */
		for (Integer deletedStatement : deletedSteps) {
			temp.removeStatementAtPosition(deletedStatement);
		}

		System.out.println("[LOG]\trepaired test case");
		UtilsRepair.printTestCaseWithLineNumbers(temp);

		UtilsRunner.cleanup(clazz, inst);

		UtilsRepair.saveTest(prefix, className, repairStrategy, temp);

		UtilsRunner.cleanup(clazz, inst);

		/*
		 * Runtime rt = Runtime.getRuntime(); rt.exec("killall firefox-bin");
		 */
	}

}
