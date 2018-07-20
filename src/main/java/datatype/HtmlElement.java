package datatype;

import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import utils.UtilsXPath;

/**
 * Adapted from WebSee.
 *
 */
public class HtmlElement {

	private String xpath;
	private String tagName;
	private String id;
	private WebElement seleniumWebElement;

	/* location of top left hand corner. */
	private int x;
	private int y;

	/* rectangular dimensions. */
	private int width;
	private int height;

	/* to be used later to find root cause. */
	private Map<String, String> htmlAttributes;

	/* R-tree rectangle id. */
	private int rectId;

	public HtmlElement() {
	}

	public HtmlElement(JavascriptExecutor js, WebElement e) {
		this.xpath = UtilsXPath.getElementXPath(js, e);
		this.tagName = e.getTagName();
		this.id = e.getAttribute("id");
		this.seleniumWebElement = e;
		this.x = e.getLocation().x;
		this.y = e.getLocation().y;
		this.width = e.getSize().width;
		this.height = e.getSize().height;
	}

	public String getXPath() {
		return xpath;
	}

	public void setXPath(String xpath) {
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

	public WebElement getSeleniumWebElement() {
		return seleniumWebElement;
	}

	public void setSeleniumWebElement(WebElement seleniumWebElement) {
		this.seleniumWebElement = seleniumWebElement;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Map<String, String> getHtmlAttributes() {
		return htmlAttributes;
	}

	public void setHtmlAttributes(Map<String, String> htmlAttributes) {
		this.htmlAttributes = htmlAttributes;
	}

	public int getRectId() {
		return rectId;
	}

	public void setRectId(int rectId) {
		this.rectId = rectId;
	}

	@Override
	public String toString() {
		return "HtmlElement [xpath=" + xpath + ", tagName=" + tagName + ", id=" + id + ", seleniumWebElement=" + seleniumWebElement + ", x=" + x
				+ ", y=" + y + ", width=" + width + ", height=" + height + ", htmlAttributes=" + htmlAttributes + ", rectId=" + rectId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HtmlElement other = (HtmlElement) obj;
		if (xpath == null) {
			if (other.xpath != null)
				return false;
		} else if (!xpath.equals(other.xpath))
			return false;
		return true;
	}

}
