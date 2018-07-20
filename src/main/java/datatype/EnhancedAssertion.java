package datatype;

public class EnhancedAssertion extends EnhancedWebElement {

	private static final long serialVersionUID = 1L;

	// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
	// Doe"));
	String assertion; // assertTrue
	String predicate; // contains

	public String getAssertion() {
		return assertion;
	}

	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getInputDataFromPredicate() {
		// contains("John Doe")
		String input = predicate.substring(0, predicate.indexOf("\""));
		input = input.substring(input.indexOf("\""), input.length());
		return input;
	}

	@Override
	public String toString() {
		return assertion + "(driver.findElement(" + getDomLocator() + ")" + "." + getAction() + "()." + predicate + "(\"" + getValue() + "\"))";
	}

}
