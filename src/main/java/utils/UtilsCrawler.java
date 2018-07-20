package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import crawler.CrawlPathExport;

public class UtilsCrawler {

	public static List<CrawlPathExport> getCrawledStates() {
		// TODO Auto-generated method stub
		List<CrawlPathExport> crawlPaths = new ArrayList<CrawlPathExport>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("MatchingStates.txt"));
			String line = reader.readLine();

			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			Type type = new TypeToken<CrawlPathExport>() {
			}.getType();

			while (line != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					CrawlPathExport crawlPathExport = gson.fromJson(line, type);
					crawlPaths.add(crawlPathExport);
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return crawlPaths;
	}
}
