package datatype;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import utils.UtilsXPath;

public class DOMInformation {

	private String xpath;
	private String tagName;
	private String id;
	private String classAttribute;
	private String nameAttribute;
	private String textualcontent;

	/* location of top left hand corner. */
	private int x;
	private int y;

	/* rectangular dimensions. */
	private int width;
	private int height;

	public DOMInformation(JavascriptExecutor js, WebElement e) {
		this.xpath = UtilsXPath.getElementXPath(js, e).substring(1);
		this.tagName = e.getTagName();
		this.id = e.getAttribute("id");
		this.x = e.getLocation().x;
		this.y = e.getLocation().y;
		this.width = e.getSize().width;
		this.height = e.getSize().height;
		this.classAttribute = e.getAttribute("class");
		this.nameAttribute = e.getAttribute("name");
		this.textualcontent = e.getAttribute("textContent").trim();
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

	public String getTextualContent() {
		return textualcontent;
	}

	public void setTextualContent(String text) {
		this.textualcontent = text;
	}

	@Override
	public String toString() {
		return "DOMInformation [xpath=" + xpath + ", tagName=" + tagName + ", id=" + id + ", classAttribute=" + classAttribute + ", nameAttr="
				+ nameAttribute + ", textualcontent=" + textualcontent + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
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
		DOMInformation other = (DOMInformation) obj;
		if (xpath == null) {
			if (other.xpath != null)
				return false;
		} else if (!xpath.equals(other.xpath))
			return false;
		return true;
	}

}
