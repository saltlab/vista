package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public class FileFilters {

	public static FileFilter directoryFilter = new FileFilter() {
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};

	public static FilenameFilter exceptionFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.equals("exception.json");
		}
	};

	public static FilenameFilter javaFilesFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".java");
		}
	};

	public static FilenameFilter annotatedScreenshotsFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return (name.contains("Annotated") && name.endsWith("png"));
		}
	};

	public static FilenameFilter visualLocatorFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return (name.contains("visualLocator") && name.endsWith("png"));
		}
	};

	public static FilenameFilter afterScreenshotsFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return (name.contains("2after") && name.endsWith("png"));
		}
	};

	public static FilenameFilter beforeScreenshotsFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return (name.contains("1before") && name.endsWith("png"));
		}
	};

}
