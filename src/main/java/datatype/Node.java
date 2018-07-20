package datatype;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
	private Node<T> parent;
	private T data;
	private List<Node<T>> children;

	public Node(Node<T> parent, T data) {
		this.parent = parent;
		if (parent != null) {
			if (this.parent.getChildren() == null) {
				this.parent.setChildren(new ArrayList<Node<T>>());
			}
			this.parent.getChildren().add(this);
			this.setChildren(null);
		}
		this.setData(data);
	}

	public Node<T> getParent() {
		return parent;
	}

	public void setParent(Node<T> parent) {
		this.parent = parent;
	}

	public void setData(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setChildren(List<Node<T>> children) {
		this.children = children;
	}

	public List<Node<T>> getChildren() {
		return children;
	}

	public List<Node<T>> getNodeSiblings() {
		if (this.getParent() == null)
			return null;

		List<Node<T>> siblings = this.getParent().getChildren();
		List<Node<T>> temp = new ArrayList<Node<T>>();

		for (Node<T> node : siblings) {
			if (!node.equals(this)) {
				temp.add(node);
			}
		}
		return temp;
	}

	public int getCurrentNodeSiblingIndex() {
		if (this.getParent() == null)
			return -1;

		List<Node<T>> allSiblings = this.getParent().getChildren();
		int index = -1;
		if (allSiblings != null && allSiblings.size() > 1) {
			for (Node<T> node : allSiblings) {
				++index;
				if (node.getData().equals(this.getData())) {
					return index;
				}
			}
		}
		return index;
	}

	public Node<T> getSiblingNodeAtIndex(int index) {
		if (this.getParent() == null)
			return null;

		List<Node<T>> allSiblings = this.getParent().getChildren();
		if (allSiblings != null) {
			try {
				Node<T> node = allSiblings.get(index);
				return node;
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		return null;
	}
}