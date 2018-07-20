package parser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Result;

import datatype.DOMInformation;
import datatype.DriverGet;
import datatype.EnhancedAssertion;
import datatype.EnhancedSelect;
import datatype.EnhancedTestCase;
import datatype.EnhancedWebElement;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import utils.UtilsFileGetters;
import utils.UtilsParser;
import utils.UtilsRunner;

public class ParseTest {

	static EnhancedTestCase tc;
	static String folder;

	public ParseTest(String f) {
		super();
		folder = f;
	}

	/**
	 * parse a test, get its static information and serialize a JSON file
	 * 
	 * @param clazz
	 * @return EnhancedTestCase
	 */
	public EnhancedTestCase parseAndSerialize(String clazz) {

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(clazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		new MethodVisitor().visit(cu, clazz);

		UtilsParser.serializeTestCase(tc, clazz, folder);

		return tc;
	}

	public void setFolder(String f) {
		folder = f;
	}

	private static String getFolder() {
		return folder;
	}

	/**
	 * parse a test, get its static information and serialize a Java file
	 * 
	 * @param clazz
	 * @return EnhancedTestCase
	 * @throws IOException
	 */
	public static EnhancedTestCase parseAndSaveToJava(EnhancedTestCase newTest, String oldclazz, String newclazz) throws IOException {

		tc = newTest;
		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(oldclazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		/* generate and change package name. */
		String packageName = UtilsParser.getPackageName(newclazz);
		changePackage(cu, packageName);

		/* replace test method with that present in the repaired test. */
		new ChangeMethodMethodVisitor().visit(cu, oldclazz);

		/* save back to java. */
		String source = cu.toString();
		File fileMod = new File(newclazz);
		FileUtils.touch(fileMod);
		FileUtils.writeStringToFile(fileMod, source);

		return tc;
	}

	/**
	 * run a test
	 * 
	 * @param clazz
	 * @return EnhancedTestCase
	 * @throws IOException
	 */
	public static Result runTest(EnhancedTestCase newTest, String clazz) throws IOException {

		tc = newTest;

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(clazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		// replace body method with that present in tc
		new ChangeMethodMethodVisitor().visit(cu, clazz);

		// save back to java
		// String source = cu.toString();
		File fileMod = new File(clazz);
		// FileUtils.writeStringToFile(fileMod, source);

		Result r = UtilsRunner.runTestSuite(fileMod.getClass());

		return r;
	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class MethodVisitor extends VoidVisitorAdapter<Object> {
		@Override
		public void visit(MethodDeclaration m, Object arg) {

			if (m.getAnnotations() != null && m.getAnnotations().get(0).getName().getName().equals("Test")) {

				String className = "";

				try {
					className = UtilsParser.getClassNameFromPath((String) arg);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				String fullPath = arg.toString();

				tc = new EnhancedTestCase(m.getName(), fullPath);

				for (Statement st : m.getBody().getStmts()) {

					/*
					 * driver get is managed separately, but current implementation does not support
					 * it fully. The driver.get commands are expected to be moved into the setUp
					 * method of the test class.
					 */
					if (st.toString().contains("driver.get(")) {

						DriverGet dg = new DriverGet();
						dg.setAction("get");
						dg.setLine(st.getBeginLine());

						try {
							dg.setValue(UtilsParser.getUrlFromDriverGet(st.toString()));
						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(dg.getLine(), dg);

						// web element not assertion not select
					} else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")
							&& !st.toString().contains("new Select")) {

						EnhancedWebElement ewe = new EnhancedWebElement();
						int line = st.getBeginLine();
						ewe.setLine(line);
						try {
							ewe.setDomLocator(UtilsParser.getDomLocator(st.toString()));
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						if (st.toString().contains("click()")) {
							ewe.setAction("click");
							ewe.setValue("");
						} else if (st.toString().contains("sendKeys")) {
							ewe.setAction("sendKeys");
							try {
								ewe.setValue(UtilsParser.getValueFromSendKeys(st.toString()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (st.toString().contains("getText")) {
							ewe.setAction("getText");
							ewe.setValue("");
						} else if (st.toString().contains("clear")) {
							ewe.setAction("clear");
							ewe.setValue("");
						}

						try {
							/* get the screenshots. */
							ewe.setScreenshotBefore(UtilsFileGetters.getScreenshot(className, line, "1before", getFolder()));
							ewe.setScreenshotAfter(UtilsFileGetters.getScreenshot(className, line, "2after", getFolder()));
							ewe.setVisualLocator(UtilsFileGetters.getScreenshot(className, line, "visualLocator", getFolder()));

							ewe.setAnnotatedScreenshot(UtilsFileGetters.getScreenshot(className, line, "Annotated", getFolder()));

							/* get the DOMs. */
							ewe.setDomBefore(UtilsFileGetters.getHTMLDOMfile(className, line, "1before", "", getFolder()));
							ewe.setDomAfter(UtilsFileGetters.getHTMLDOMfile(className, line, "2after", "", getFolder()));

							/* get the other DOM information. */
							DOMInformation info = UtilsFileGetters.getDOMInformationFromJsonFile(className, line, "domInfo", getFolder());
							ewe.setTagName(info.getTagName());
							ewe.setXpath(info.getXPath());
							ewe.setId(info.getId());
							ewe.setClassAttribute(info.getClassAttribute());
							ewe.setName(info.getNameAttribute());
							ewe.setText(info.getTextualContent());

						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(line, ewe);

					}
					// select
					else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")
							&& st.toString().contains("new Select")) {

						EnhancedSelect esl = new EnhancedSelect();
						int line = st.getBeginLine();
						esl.setLine(line);
						try {
							esl.setDomLocator(UtilsParser.getDomLocator(st.toString()));
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						if (st.toString().contains("selectByVisibleText")) {
							esl.setAction("selectByVisibleText");
							esl.setValue(UtilsParser.getValueFromSelect(st));
						} else if (st.toString().contains("selectByIndex")) {
							esl.setAction("selectByIndex");
							esl.setValue(UtilsParser.getValueFromSelect(st));
						} else if (st.toString().contains("selectByValue")) {
							esl.setAction("selectByValue");
							esl.setValue(UtilsParser.getValueFromSelect(st));
						}

						try {
							// get the screenshots
							esl.setScreenshotBefore(UtilsFileGetters.getScreenshot(className, line, "1before", getFolder()));
							esl.setScreenshotAfter(UtilsFileGetters.getScreenshot(className, line, "2after", getFolder()));
							esl.setVisualLocator(UtilsFileGetters.getScreenshot(className, line, "visualLocator", getFolder()));

							esl.setAnnotatedScreenshot(UtilsFileGetters.getScreenshot(className, line, "Annotated", getFolder()));

							// get the DOMs
							esl.setDomBefore(UtilsFileGetters.getHTMLDOMfile(className, line, "1before", "", getFolder()));
							esl.setDomAfter(UtilsFileGetters.getHTMLDOMfile(className, line, "2after", "", getFolder()));

							/* get the other DOM information. */
							DOMInformation info = UtilsFileGetters.getDOMInformationFromJsonFile(className, line, "domInfo", getFolder());
							esl.setTagName(info.getTagName());
							esl.setXpath(info.getXPath());
							esl.setId(info.getId());
							esl.setClassAttribute(info.getClassAttribute());
							esl.setName(info.getNameAttribute());
							esl.setText(info.getTextualContent());

						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(line, esl);
					}
					// assertion
					else if (st.toString().contains("driver.findElement(") && st.toString().contains("assert")) {

						EnhancedAssertion ea = new EnhancedAssertion();
						int line = st.getBeginLine();
						ea.setAssertion(UtilsParser.getAssertion(st));
						ea.setPredicate(UtilsParser.getPredicate(st));

						if (st.toString().contains("getText")) {
							ea.setAction("getText");
							ea.setValue(UtilsParser.getValueFromAssertion(st));
						} else {
							System.err.println("[LOG]\tAnalysing an assertion with no getText()");
						}

						ea.setLine(line);
						try {
							ea.setDomLocator(UtilsParser.getDomLocator(st.toString()));
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						try {
							// get the screenshots
							ea.setScreenshotBefore(UtilsFileGetters.getScreenshot(className, line, "1before", getFolder()));
							ea.setScreenshotAfter(UtilsFileGetters.getScreenshot(className, line, "2after", getFolder()));
							ea.setVisualLocator(UtilsFileGetters.getScreenshot(className, line, "visualLocator", getFolder()));

							ea.setAnnotatedScreenshot(UtilsFileGetters.getScreenshot(className, line, "Annotated", getFolder()));

							// get the DOMs
							ea.setDomBefore(UtilsFileGetters.getHTMLDOMfile(className, line, "1before", "", getFolder()));
							ea.setDomAfter(UtilsFileGetters.getHTMLDOMfile(className, line, "2after", "", getFolder()));

							/* get the other DOM information. */
							DOMInformation info = UtilsFileGetters.getDOMInformationFromJsonFile(className, line, "domInfo", getFolder());
							ea.setTagName(info.getTagName());
							ea.setXpath(info.getXPath());
							ea.setId(info.getId());
							ea.setClassAttribute(info.getClassAttribute());
							ea.setName(info.getNameAttribute());
							ea.setText(info.getTextualContent());

						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(line, ea);

					}
				}
			}
		}
	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class ChangeMethodMethodVisitor extends VoidVisitorAdapter<Object> {
		@Override
		public void visit(MethodDeclaration m, Object arg) {

			if (m.getAnnotations() != null && m.getAnnotations().get(0).getName().getName().equals("Test") && m.getName().equals(tc.getName())) {

				BlockStmt newBlockStmt = new BlockStmt();

				for (Integer i : tc.getStatements().keySet()) {
					ASTHelper.addStmt(newBlockStmt, new NameExpr(tc.getStatements().get(i).toString()));
				}

				m.setBody(newBlockStmt);

			}
		}
	}

	/**
	 * Modifies the Java package declaration of the repaired tests
	 * 
	 * @param cu
	 * @param packageName
	 */
	public static void changePackage(CompilationUnit cu, String packageName) {
		new PackageVisitor().visit(cu, packageName);
	}

	/**
	 * Simple visitor implementation for visiting package nodes.
	 */
	private static class PackageVisitor extends VoidVisitorAdapter<Object> {

		public void visit(PackageDeclaration p, Object arg) {
			p.setName(new NameExpr(arg.toString()));
		}
	}

}
