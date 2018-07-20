package runner;

import java.io.IOException;

import config.Settings;
import utils.UtilsRunner;

/**
 * The RepairedTestRunner class runs the repaired JUnit Selenium test suites
 * 
 * @author astocco
 * @author yrahulkr
 *
 */
public class RepairedTestRunner {

	public static void main(String[] args) throws IOException {

		/* disable the AspectJ module. */
		Settings.aspectActive = false;

		/* Claroline example. */
		UtilsRunner.runTest(Settings.testSuiteRepaired, "TestLoginAdmin");

	}

}
