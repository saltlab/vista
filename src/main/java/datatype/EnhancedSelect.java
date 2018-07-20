package datatype;

public class EnhancedSelect extends Statement {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "new Select(driver.findElement(" + getDomLocator() + "))." + getAction() + "(" + getValue() + ")";
	}

}
