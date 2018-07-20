package utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.aspectj.lang.JoinPoint;
import org.openqa.selenium.WebDriver;

import config.Settings;

public class UtilsAspect {

	/**
	 * return an identifier for the statement in the form <testname>-<line> from a
	 * joinPoint of type WebElement
	 * 
	 * @param joinPoint
	 * @return String
	 */
	public static String getStatementNameFromJoinPoint(JoinPoint joinPoint) {

		String name = "";

		name = joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
		name = name.concat("-");
		name = name.concat(Integer.toString(joinPoint.getStaticPart().getSourceLocation().getLine()));

		return name;
	}

	/**
	 * return the statement line from a joinPoint of type WebElement
	 * 
	 * @param joinPoint
	 * @return int
	 */
	public static int getStatementLineFromJoinPoint(JoinPoint joinPoint) {
		return joinPoint.getStaticPart().getSourceLocation().getLine();
	}

	/**
	 * creates a directory in the project workspace
	 * 
	 * @param joinPoint
	 * @return int
	 */
	public static void createTestFolder(String path) {

		File theDir = new File(path);
		if (!theDir.exists()) {

			if (Settings.VERBOSE)
				System.out.print("[LOG]\tcreating directory " + path + "...");

			boolean result = theDir.mkdir();
			if (result) {
				if (Settings.VERBOSE)
					System.out.println("done");
			} else {
				if (Settings.VERBOSE)
					System.out.print("failed!");
				System.exit(1);
			}
		}

	}

	/**
	 * save an HTML file of the a WebDriver instance
	 * 
	 * @param d
	 * @param filePath
	 */
	public static void saveDOM(WebDriver d, String filePath) {

		try {
			FileUtils.writeStringToFile(new File(filePath), d.getPageSource());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save rendered webpage path = where to save the html file
	 */
	public static File saveHTMLPage(String urlString, String path) throws IOException {

		File savedHTML = new File(path);

		/* necessary to avoid garbage. */
		if (savedHTML.exists()) {
			FileUtils.deleteDirectory(savedHTML);
		}

		/* wget to save html page. */
		Runtime runtime = Runtime.getRuntime();
		Process p = runtime.exec("/usr/local/bin/wget -p -k -E -nd -P " + path + " " + urlString);

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return savedHTML;
	}

}
