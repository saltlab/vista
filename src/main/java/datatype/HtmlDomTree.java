package datatype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import config.Settings;
import utils.HtmlAttributesParser;
import utils.UtilsParser;

public class HtmlDomTree {

	private Node<HtmlElement> root;
	private HtmlAttributesParser htmlAttributesParser;

	public HtmlDomTree(WebDriver driver, String htmlFileFullPath) throws SAXException, IOException {

		/* retrieve the root element. */
		List<WebElement> elements = driver.findElements(By.xpath("//*"));
		WebElement rootElementFromSelenium = elements.get(0);
		HtmlElement htmlRootElement = new HtmlElement();
		int x = rootElementFromSelenium.getLocation().x;
		int y = rootElementFromSelenium.getLocation().y;
		int w = rootElementFromSelenium.getSize().width;
		int h = rootElementFromSelenium.getSize().height;

		/* parse HTML attributes. */
		htmlAttributesParser = new HtmlAttributesParser(htmlFileFullPath);

		htmlRootElement.setSeleniumWebElement(rootElementFromSelenium);
		htmlRootElement.setTagName(rootElementFromSelenium.getTagName());
		htmlRootElement.setX(x);
		htmlRootElement.setY(y);
		htmlRootElement.setWidth(w);
		htmlRootElement.setHeight(h);
		this.root = new Node<HtmlElement>(null, htmlRootElement);
		htmlRootElement.setXPath(computeXPath(this.root));
		htmlRootElement.setHtmlAttributes(htmlAttributesParser.getHTMLAttributesForElement(htmlRootElement.getXPath()));
	}

	public void buildHtmlDomTree() {
		buildHtmlDomTreeFromNode(this.root);
	}

	private void buildHtmlDomTreeFromNode(Node<HtmlElement> node) {
		try {
			List<WebElement> children = node.getData().getSeleniumWebElement().findElements(By.xpath("*"));
			for (WebElement child : children) {
				int x = child.getLocation().x;
				int y = child.getLocation().y;
				int w = child.getSize().width;
				int h = child.getSize().height;

				/* adjust size of <option> to that of the parent (<select>). */
				if (child.getTagName().equals("option")) {
					if (node.getData().getTagName().equals("select")) {
						x = node.getData().getX();
						y = node.getData().getY();
					}
				}

				/* discard elements with no visual appearance. */
				// if(x >= 0 && y >= 0 && w > 0 && h > 0)
				if (!Arrays.asList(Settings.TAGS_BLACKLIST).contains(child.getTagName())) {
					
					HtmlElement newChild = new HtmlElement();
					newChild.setTagName(child.getTagName());
					newChild.setId(child.getAttribute("id"));
					newChild.setSeleniumWebElement(child);
					newChild.setX(x);
					newChild.setY(y);
					newChild.setWidth(w);
					newChild.setHeight(h);

					Node<HtmlElement> newNode = new Node<HtmlElement>(node, newChild);
					newChild.setXPath(computeXPath(newNode));
					newChild.setHtmlAttributes(htmlAttributesParser.getHTMLAttributesForElement(newChild.getXPath()));

					buildHtmlDomTreeFromNode(newNode);
				}
			}
		} catch (NoSuchElementException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void preOrderTraversalRTree() {
		preOrderTraversalRTree(this.root);
	}

	private void preOrderTraversalRTree(Node<HtmlElement> node) {
		if (node == null) {
			return;
		}

		if (node.getChildren() != null) {
			for (Node<HtmlElement> child : node.getChildren()) {
				preOrderTraversalRTree(child);
			}
		}
	}

	/**
	 * compute XPath of the invoking element from the root.
	 */
	private String computeXPath(Node<HtmlElement> node) {
		return getElementTreeXPath(node);
	}

	private static String getElementTreeXPath(Node<HtmlElement> node) {
		ArrayList<String> paths = new ArrayList<String>();
		for (; node != null; node = node.getParent()) {
			HtmlElement element = node.getData();
			int index = 0;

			int siblingIndex = node.getCurrentNodeSiblingIndex();
			for (Node<HtmlElement> sibling = node.getSiblingNodeAtIndex(--siblingIndex); sibling != null; sibling = node
					.getSiblingNodeAtIndex(--siblingIndex)) {
				if (sibling.getData().getTagName().equals(element.getTagName())) {
					++index;
				}
			}
			String tagName = element.getTagName().toLowerCase();
			String pathIndex = "[" + (index + 1) + "]";
			paths.add(tagName + pathIndex);
		}

		String result = null;
		if (paths.size() > 0) {
			result = "/";
			for (int i = paths.size() - 1; i > 0; i--) {
				result = result + paths.get(i) + "/";
			}
			result = result + paths.get(0);
		}

		return result;
	}

	public Node<HtmlElement> searchHtmlDomTreeByNode(Node<HtmlElement> searchNode) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (node.equals(searchNode)) {
				return node;
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	// REFINE THIS TO USE RELATIVE XPATHS
	public HtmlElement searchHtmlDomTreeByXPath(String xpath) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		// System.out.println("searching for " + xpath);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();

			//System.out.println(node.getData().getXPath());

			if (node.getData().getXPath().equalsIgnoreCase(xpath)) {
				return node.getData();
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public Node<HtmlElement> searchHtmlDomTreeByPoint(int x, int y) {
		return searchHtmlDomTreeByPoint(this.root, x, y);
	}

	public Node<HtmlElement> searchHtmlDomTreeByPoint(Node<HtmlElement> node, int x, int y) {
		// traverse in pre-order
		// for visit, check if the node contains this point
		// if yes, go to children
		// if node is leaf and contains the point return node
		// else return parent

		HtmlElement element = node.getData();
		if (node.getChildren() == null && UtilsParser.isPointInRectangle(x, y, element.getX(), element.getY(),
				element.getWidth(), element.getHeight(), true)) {
			return node;
		} else {
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					if (UtilsParser.isPointInRectangle(x, y, child.getData().getX(), child.getData().getY(),
							child.getData().getWidth(), child.getData().getHeight(), true)) {
						node = searchHtmlDomTreeByPoint(child, x, y);
						return node;
					}
				}
			}
			return node;
		}
	}

	public HtmlElement searchHtmlDomTreeByAttribute(String attribute, String value) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (containsAttributeValue(node.getData().getHtmlAttributes(), attribute, value)) {
				return node.getData();
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public HtmlElement searchHtmlDomTreeByText(String text) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();

			if (node.getData().getHtmlAttributes().get("text").equals(text)) {
				return node.getData();
			}

			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public HtmlElement searchHtmlDomTreeByTagName(String tagName) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (node.getData().getTagName().equalsIgnoreCase(tagName)) {
				return node.getData();
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public void getClickables(List<String> tags, List<Node<HtmlElement>> nodes) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (tags.contains(node.getData().getTagName())) {
				nodes.add(node);
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
	}

	private static boolean containsAttributeValue(Map<String, String> m, String a, String v) {
		if (m.containsKey(a))
			return m.get(a).equals(v);
		return false;
	}

	public HtmlAttributesParser getHtmlAttributesParser() {
		return htmlAttributesParser;
	}

	public void setHtmlAttributesParser(HtmlAttributesParser htmlAttributesParser) {
		this.htmlAttributesParser = htmlAttributesParser;
	}

	public Node<HtmlElement> getRoot() {
		return root;
	}

	public void setRoot(Node<HtmlElement> root) {
		this.root = root;
	}

}
