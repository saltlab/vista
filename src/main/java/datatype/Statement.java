package datatype;

import java.io.File;
import java.io.Serializable;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import utils.UtilsParser;

@SuppressWarnings("serial")
public abstract class Statement implements Serializable {

	/* kind of statement. */
	WebDriver driverGet;
	WebElement webElement;
	Select select;

	/* statement's information. */
	private String seleniumAction;
	private String value;
	private String name;
	private int line;

	/* DOM-based information. */
	private File htmlPage;
	private Point coordinates;
	private Dimension dimension;
	private File domBefore;
	private File domAfter;
	private SeleniumLocator domLocator; // on domBefore
	private String xpath;
	private String tagName;
	private String id;
	private String classAttribute;
	private String nameAttribute;
	private String text;

	/* visual-based information. */
	private File screenshotBefore;
	private File screenshotAfter;
	private File annotatedScreenshot;
	private File visualLocator; // extracted on screenshotBefore image

	public WebDriver getDriverGet() {
		return driverGet;
	}

	public void setDriverGet(WebDriver driverGet) {
		this.driverGet = driverGet;
	}

	public WebElement getWebElement() {
		return webElement;
	}

	public void setWebElement(WebElement webElement) {
		this.webElement = webElement;
	}

	public Select getSelect() {
		return select;
	}

	public void setSelect(Select select) {
		this.select = select;
	}

	public File getDomBefore() {
		return domBefore;
	}

	public void setDomBefore(File dom) {
		this.domBefore = dom;
	}

	public File getDomAfter() {
		return domAfter;
	}

	public void setDomAfter(File dom) {
		this.domAfter = dom;
	}

	public SeleniumLocator getDomLocator() {
		return domLocator;
	}

	public void setDomLocator(SeleniumLocator domLocator) {
		this.domLocator = domLocator;
	}

	public void setDomLocator(WebElement domLocator) {
		this.domLocator = UtilsParser.extractSeleniumLocatorFromWebElement(domLocator);
	}

	public String getAction() {
		return seleniumAction;
	}

	public void setAction(String action) {
		this.seleniumAction = action;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public File getScreenshotBefore() {
		return screenshotBefore;
	}

	public void setScreenshotBefore(File screenshot) {
		this.screenshotBefore = screenshot;
	}

	public File getScreenshotAfter() {
		return screenshotAfter;
	}

	public void setScreenshotAfter(File screenshot) {
		this.screenshotAfter = screenshot;
	}

	public File getAnnotatedScreenshot() {
		return annotatedScreenshot;
	}

	public void setAnnotatedScreenshot(File annotatedScreenshot) {
		this.annotatedScreenshot = annotatedScreenshot;
	}

	public Point getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}

	public Dimension getDimension() {
		return dimension;
	}

	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getName() {
		return name;
	}

	public void setName(String statementName) {
		this.name = statementName;
	}

	public File getHtmlPage() {
		return htmlPage;
	}

	public void setHtmlPage(File htmlPage) {
		this.htmlPage = htmlPage;
	}

	public String getSeleniumAction() {
		return seleniumAction;
	}

	public void setSeleniumAction(String seleniumAction) {
		this.seleniumAction = seleniumAction;
	}

	public File getVisualLocator() {
		return visualLocator;
	}

	public void setVisualLocator(File vl) {
		this.visualLocator = vl;
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassAttribute() {
		return classAttribute;
	}

	public void setClassAttribute(String classAttribute) {
		this.classAttribute = classAttribute;
	}

	public String getNameAttribute() {
		return nameAttribute;
	}

	public void setNameAttribute(String nameAttribute) {
		this.nameAttribute = nameAttribute;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
