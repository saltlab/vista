package datatype;

import org.openqa.selenium.support.ui.Select;

public class EnhancedWebElement extends Statement {

	private static final long serialVersionUID = 1L;

	// web element != select
	public Select getSelect() {
		return null;
	}

	public void setSelect(Select we) {
		select = null;
	}

	public EnhancedWebElement() {
		super();
	}

	@Override
	public String toString() {

		if (getAction().equals("sendKeys")) {
			return "driver.findElement(" + getDomLocator() + ")." + getAction() + "(\"" + getValue() + "\")";
		} else if (getAction().equals("click")) {
			return "driver.findElement(" + getDomLocator() + ")." + getAction() + "(" + getValue() + ")";
		} else if (getAction().equals("clear")) {
			return "driver.findElement(" + getDomLocator() + ")." + getAction() + "(" + getValue() + ")";
		}

		return null;
	}

}
