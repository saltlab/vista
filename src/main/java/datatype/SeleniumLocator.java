package datatype;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SeleniumLocator implements Serializable {

	String strategy, value;

	public SeleniumLocator(String strategy, String value) {
		this.strategy = strategy;
		this.value = value;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "By." + strategy + "(\"" + value + "\")";
	}

}