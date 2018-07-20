package datatype;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EnhancedTestSuite {

	List<EnhancedTestCase> testcases;

	public EnhancedTestSuite(List<EnhancedTestCase> tc) {
		this.testcases = tc;
	}

	public EnhancedTestSuite() {
		this.testcases = new LinkedList<EnhancedTestCase>();
	}

	public List<EnhancedTestCase> getTestCases() {
		return testcases;
	}

	public void setTestCases(List<EnhancedTestCase> tc) {
		this.testcases = tc;
	}

	public void addTestCase(EnhancedTestCase tc) {
		testcases.add(tc);
	}

	public void addAllStatements(EnhancedTestCase... tc) {
		testcases.addAll(Arrays.asList(tc));
	}

	@Override
	public String toString() {
		return "EnhancedTestSuite [testcases=" + testcases + "]";
	}

	public void print() {
		System.out.println("\n\n******** PRINTING TEST SUITE ********");
		System.out.println("Number of test cases: " + testcases.size());
		System.out.println(this);
		System.out.println("*****************************************");
	}

}
