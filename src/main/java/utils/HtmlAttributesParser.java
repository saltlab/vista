package utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebElement;

public class HtmlAttributesParser {
	private Document document;

	public HtmlAttributesParser(String htmlFileFullPath) {
		try {
			this.document = Jsoup.parse(new File(htmlFileFullPath), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> getHTMLAttributesForElement(String xpath) throws IOException {

		Map<String, String> htmlAttributes = new HashMap<String, String>();
		Element e = UtilsXPath.getElementFromXPathJava(xpath, document);
		if (e != null) {

			for (Attribute attribute : e.attributes()) {
				htmlAttributes.put(attribute.getKey(), attribute.getValue());
			}
			if (e.hasText()) {
				htmlAttributes.put("text", e.ownText());
			}
		}
		return htmlAttributes;
	}

	public Map<String, String> getHTMLAttributesForSeleniumElement(WebElement rootElementFromSelenium) {

		Map<String, String> htmlAttributes = new HashMap<String, String>();

		htmlAttributes.put("id", rootElementFromSelenium.getAttribute("id"));
		htmlAttributes.put("class", rootElementFromSelenium.getAttribute("class"));
		htmlAttributes.put("name", rootElementFromSelenium.getAttribute("name"));
		// htmlAttributes.put("text", rootElementFromSelenium.getText());

		return htmlAttributes;
	}
}
