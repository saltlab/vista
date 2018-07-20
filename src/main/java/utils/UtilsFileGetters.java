package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

import config.Settings;
import datatype.DOMInformation;

public class UtilsFileGetters {

	public static File getExceptionFile(File file) {
		File[] exception = file.listFiles(FileFilters.exceptionFilter);
		if (exception.length == 1 && exception[0].getName().equals("exception.json"))
			return exception[0];
		return null;
	}

	public static boolean isTestBroken(File file) {
		File[] exception = file.listFiles(FileFilters.exceptionFilter);
		if (exception.length == 1 && exception[0].getName().equals("exception.json"))
			return true;
		return false;
	}

	public static File[] getBeforeScreenshots(String directory) {

		File[] files = new File(directory).listFiles(FileFilters.beforeScreenshotsFilter);
		return files;

	}

	public static File[] getAfterScreenshots(String directory) {

		File[] files = new File(directory).listFiles(FileFilters.afterScreenshotsFilter);
		return files;

	}

	public static File[] getVisualLocators(String directory) {

		File[] files = new File(directory).listFiles(FileFilters.visualLocatorFilter);
		return files;

	}

	public static File[] getAnnotatedScreenshots(String directory) {

		File[] files = new File(directory).listFiles(FileFilters.annotatedScreenshotsFilter);
		return files;

	}

	public static String getTestFile(String name, String pathToTestSuiteUnderTest) {

		File[] files = new File(pathToTestSuiteUnderTest).listFiles(FileFilters.javaFilesFilter);
		for (File file : files) {
			if (file.getName().contains(name)) {
				return file.getAbsolutePath();
			}
		}

		return null;
	}

	/**
	 * Auxiliary method to get the screenshot file
	 * 
	 * @param st
	 * @return
	 * @throws Exception
	 */
	public static File getScreenshot(String name, int beginLine, String type, String folder) throws Exception {
	
		String p = folder + name + Settings.sep;
	
		File dir = new File(p);
		File[] listOfFiles = dir.listFiles(new FilenameFilter() {
	
			@Override
			public boolean accept(File dir, String n) {
				return (n.startsWith(Integer.toString(beginLine)) && n.endsWith(Settings.PNG_EXT) && n.contains(name)
						&& n.contains(type));
			}
		});
	
		if (listOfFiles.length == 0) {
			return null;
		} else if (listOfFiles.length == 1) {
			return listOfFiles[0];
		} else {
			throw new Exception("[LOG]\tToo many files retrieved");
		}
	
	}

	/**
	 * Auxiliary method to get the JSON file with the DOM information
	 * 
	 * @param st
	 * @return
	 * @throws Exception
	 */
	public static DOMInformation getDOMInformationFromJsonFile(String name, int beginLine, String type, String folder)
			throws Exception {
	
		String p = folder + name + Settings.sep;
	
		File dir = new File(p);
		File[] listOfFiles = dir.listFiles(new FilenameFilter() {
	
			@Override
			public boolean accept(File dir, String n) {
				return (n.startsWith(Integer.toString(beginLine)) && n.endsWith(Settings.JSON_EXT) && n.contains(name)
						&& n.contains(type));
			}
	
		});
	
		if (listOfFiles.length == 0) {
	
			throw new Exception("[LOG]\tNo JSON file retrieved");
	
		} else if (listOfFiles.length == 1) {
	
			DOMInformation obj = UtilsParser.gson.fromJson(new BufferedReader(new FileReader(listOfFiles[0])),
					DOMInformation.class);
	
			return obj;
	
		} else {
			throw new Exception("[LOG]\tToo many files retrieved");
		}
	
	}

	/**
	 * Auxiliary method to get the HTML file
	 * 
	 * @param st
	 * @return
	 * @throws Exception
	 */
	public static File getHTMLDOMfile(String name, int beginLine, String type, String useExtension, String folder)
			throws Exception {
	
		String p = folder + name + Settings.sep + beginLine + "-" + type + "-" + name + "-" + beginLine;
	
		File dir = new File(p);
		File[] listOfFiles = dir.listFiles(new FilenameFilter() {
	
			@Override
			public boolean accept(File dir, String n) {
				return (n.endsWith(Settings.HTML_EXT));
			}
		});
	
		if (listOfFiles == null || listOfFiles.length == 0) {
			return null;
		} else {
			return listOfFiles[0];
		}
	
	}

}
