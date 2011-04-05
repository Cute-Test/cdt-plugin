package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.RefactoringTester;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.condition.ConditionCheckStrategy;

/**
 * Reader for the test xml configuration creating test suites
 * needed for the refactoring tests include setting the appropriate
 * strategies.
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("nls")
public class TestConfiguration {
	
	/**
	 * Open bracket to add the config file to the refactoring description.
	 */
	private static final String CLOSE_BRACKET = ")";
	
	/**
	 * Close bracket to add the config file to the refactoring description.
	 */
	private static final String OPEN_BRACKET = "(";
	
	/**
	 * Test xml tag.
	 */
	private static final String TEST_TAG = "Test";
	
	/**
	 * Test configuration xml tag.
	 */
	private static final String TESTCONFIG_TAG = "TestConfig";
	
	/**
	 * Description xml tag.
	 */
	private static final String DESCRIPTION_TAG = "Description";
	
	/**
	 * Initial assert strategy xml tag.
	 */
	private static final String INITIALASSERT_TAG = "InitialAssert";
	
	/**
	 * Final assert strategy xml tag.
	 */
	private static final String FINALASSERT_TAG = "FinalAssert";
	
	/**
	 * Test strategy xml tag.
	 */
	private static final String TESTSTRATEGY_TAG = "TestStrategy";

	/**
	 * Exception xml tag.
	 */
	private static final String EXCEPTION_TAG = "Exception";
	
	private File file_;
	
	/**
	 * Create the test configuration.
	 * @param configuration Path to the configuration xml file.
	 * @throws FileNotFoundException If the configuration was not found.
	 */
	public TestConfiguration(String configuration) throws FileNotFoundException {
		file_ = new File(configuration);
		if(!file_.exists())
			throw new FileNotFoundException(configuration+" not found");
	}
	
	/**
	 * Return a list of all refactoring tests.
	 * @return List of tests.
	 * @throws Exception Error reading the tests.
	 */
	public List<Test> getAllTests() throws Exception {
		Document document = readDocument();
		NodeList testNodes = document.getElementsByTagName(TEST_TAG);
		List<Test> tests = new ArrayList<Test>();
		for(int i=0; i<testNodes.getLength(); ++i) {
			processTestNode(testNodes.item(i), tests);
		}
		return tests;
	}

	/**
	 * Process the test nodes.
	 * @param node Node.
	 * @param tests List to add the tests to.
	 * @throws Exception Error creating the test class.
	 */
	private void processTestNode(Node node, List<Test> tests) throws Exception {
		String testConfig = readTestConfigPath(node);
		String testDescription = readTestDescription(node)+OPEN_BRACKET+testConfig+CLOSE_BRACKET;
		ConditionCheckStrategy initCheck = readInitCheckStrategy(node);
		ConditionCheckStrategy finalCheck = readFinalCheckStrategy(node);
		TestConfigurationStrategy testStrategy = readTestStrategy(node);
		Class<?> expectedException = readExpectedException(node);
		tests.add(createTest(testConfig, testDescription, initCheck, finalCheck, testStrategy, expectedException));
	}
	
	/**
	 * Return the expected exception class of the test or null if there is no expected exception.
	 * @param node Node.
	 * @return Class of the expected exception or null.
	 * @throws ClassNotFoundException Reflection.
	 */
	private Class<?> readExpectedException(Node node) throws ClassNotFoundException {
		Element element = (Element)node;
		Node expectedException = element.getElementsByTagName(EXCEPTION_TAG).item(0);
		if(expectedException==null)
			return null;
		String className = expectedException.getFirstChild().getNodeValue();
		return Class.forName(className);
	}

	/**
	 * Create the test.
	 * @param testConfig Test config path.
	 * @param testDescription Test description.
	 * @param initCheck Initial check strategy.
	 * @param finalCheck Final check strategy.
	 * @param testStrategy Test configuration strategy.
	 * @param expectedException Excpected exception class.
	 * @return Test.
	 * @throws Exception
	 */
	private Test createTest(String testConfig, String testDescription, ConditionCheckStrategy initCheck, ConditionCheckStrategy finalCheck, TestConfigurationStrategy testStrategy, Class<?> expectedException) throws Exception {
		return RefactoringTester.suite(testDescription, testConfig, initCheck, finalCheck, testStrategy, expectedException);
	}

	/**
	 * Create the test configuration strategy.
	 * @param node Test node.
	 * @return Test strategy instance.
	 * @throws ClassNotFoundException Reflection.
	 * @throws InstantiationException Reflection.
	 * @throws IllegalAccessException Reflection.
	 */
	private TestConfigurationStrategy readTestStrategy(Node node) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Element element = (Element)node;
		Node testStrategyNode = getFirstElementsFirstChild(element, TESTSTRATEGY_TAG);
		String className = testStrategyNode.getNodeValue();
		Class<?> cls = Class.forName(className);
		return (TestConfigurationStrategy) cls.newInstance();
	}

	/**
	 * Create the final condition check strategy.
	 * @param node Test node.
	 * @return Condition check strategy.
	 * @throws ClassNotFoundException Reflection.
	 * @throws InstantiationException Reflection.
	 * @throws IllegalAccessException Reflection.
	 */
	private ConditionCheckStrategy readFinalCheckStrategy(Node node) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Element element = (Element)node;
		String className = getFirstElementsFirstChild(element, FINALASSERT_TAG).getNodeValue();
		Class<?> cls = Class.forName(className);
		return (ConditionCheckStrategy) cls.newInstance();
	}
	
	/**
	 * Create the initial condition check strategy.
	 * @param node Test node.
	 * @return Condition check strategy.
	 * @throws ClassNotFoundException Reflection.
	 * @throws InstantiationException Reflection.
	 * @throws IllegalAccessException Reflection.
	 */
	private ConditionCheckStrategy readInitCheckStrategy(Node node) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Element element = (Element)node;
		String className = getFirstElementsFirstChild(element, INITIALASSERT_TAG).getNodeValue();
		Class<?> cls = Class.forName(className);
		return (ConditionCheckStrategy) cls.newInstance();
	}

	/**
	 * Read the description of the test.
	 * @param node Test node.
	 * @return Description of the test.
	 */
	private String readTestDescription(Node node) {
		Element element = (Element)node;
		Node descriptionNode = getFirstElementsFirstChild(element, DESCRIPTION_TAG);
		return descriptionNode.getNodeValue();
	}

	/**
	 * Read the path to the testconfig.
	 * @param node Test node.
	 * @return Path of the testconfig.
	 */
	private String readTestConfigPath(Node node) {
		Element element = (Element)node;
		Node testConfigNode = getFirstElementsFirstChild(element, TESTCONFIG_TAG);
		return testConfigNode.getNodeValue();
	}

	/**
	 * Get the first child of the first item of the element.
	 * @param element Element.
	 * @param tag Tag.
	 * @return Node.
	 */
	private Node getFirstElementsFirstChild(Element element, String tag) {
		return element.getElementsByTagName(tag).item(0).getFirstChild();
	}

	/**
	 * Read the xml document.
	 * @return Document.
	 * @throws IOException Exception reading file.
	 * @throws SAXException Exception reading xml.
	 * @throws ParserConfigurationException Invalid parser configuration.
	 */
	private Document readDocument() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(file_);
		document.getDocumentElement().normalize();
		return document;
	}
}
