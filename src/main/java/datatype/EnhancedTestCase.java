package datatype;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class EnhancedTestCase implements Serializable {

	String name;
	Map<Integer, Statement> statements;
	String path;

	public EnhancedTestCase(String testName, Map<Integer, Statement> statements, String path) {
		setName(testName);
		this.statements = statements;
		setPath(path);
	}

	public EnhancedTestCase(String testcasename, String path) {
		setName(testcasename);
		this.statements = new LinkedHashMap<Integer, Statement>();
		setPath(path);
	}

	public EnhancedTestCase(String testcasename) {
		setName(testcasename);
		this.statements = new LinkedHashMap<Integer, Statement>();
		setPath(null);
	}

	public EnhancedTestCase() {
	}

	public String getName() {
		return name;
	}

	public Map<Integer, Statement> getStatements() {
		return statements;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStatements(Map<Integer, Statement> map) {
		this.statements = map;
	}

	public void addStatementAtPosition(Integer i, Statement st) {
		if (!statements.containsKey(i)) {
			statements.put(i, st);
		} else {

			Map<Integer, Statement> newMap = new LinkedHashMap<Integer, Statement>();

			for (Statement statement : statements.values()) {
				if (statement.getLine() < i) {
					newMap.put(statement.getLine(), statement);
				} else if (statement.getLine() == i) {
					newMap.put(i, st);
					statement.setLine(1 + statement.getLine());
					newMap.put(statement.getLine(), statement);
				} else if (statement.getLine() > i) {
					statement.setLine(1 + statement.getLine());
					newMap.put(statement.getLine(), statement);
				}
			}

			statements = newMap;

		}

	}

	public void removeStatementAtPosition(Integer i) {

		if (!statements.containsKey(i)) {
			return;
		} else {

			Map<Integer, Statement> newMap = new LinkedHashMap<Integer, Statement>();

			for (Statement statement : statements.values()) {
				if (statement.getLine() < i) {
					newMap.put(statement.getLine(), statement);
				} else if (statement.getLine() == i) {
					continue;
				} else if (statement.getLine() > i) {
					statement.setLine(statement.getLine() - 1);
					newMap.put(statement.getLine(), statement);
				}
			}

			statements = newMap;

		}

	}

	public void replaceStatements(Map<Integer, Statement> map) {
		this.statements = null;
		this.statements = map;
	}

	public void addAndReplaceStatement(Integer i, Statement st) {
		statements.put(i, st);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "TestCase [name=" + name + ", statements=" + statements + "]";
	}

}
