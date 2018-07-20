package datatype;

public class EnhancedException {

	String exception;
	String message;
	String failedTest;
	String involvedLine;
	String actualValue;
	
	public String getException() { return exception; }
	public void setException(String exception) { this.exception = exception; }
	
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	
	public String getFailedTest() { return failedTest; }
	public void setFailedTest(String failedTest) { this.failedTest = failedTest; }
	
	public String getInvolvedLine() { return involvedLine; }
	public void setInvolvedLine(String involvedLine) { this.involvedLine = involvedLine; }
	
	public String getActualValue() { return actualValue; }
	public void setActualValue(String actualValue) { this.actualValue = actualValue; }
	
}
