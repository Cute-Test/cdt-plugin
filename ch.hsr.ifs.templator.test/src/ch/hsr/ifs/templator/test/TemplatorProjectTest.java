package ch.hsr.ifs.templator.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;
import ch.hsr.ifs.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.templator.plugin.asttools.data.RelevantNameCache;
import ch.hsr.ifs.templator.plugin.asttools.data.UnresolvedNameInfo;
import ch.hsr.ifs.templator.plugin.asttools.resolving.FindAllNamesVisitor;
import ch.hsr.ifs.templator.plugin.asttools.resolving.NameDeduction;
import ch.hsr.ifs.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.util.EclipseUtil;

public class TemplatorProjectTest extends CDTTestingTest {
	protected ASTAnalyzer analyzer;
	protected ICPPNodeFactory factory;
	protected List<IASTDeclaration> definitions;
	protected AbstractResolvedNameInfo firstStatementInMain;

	/**
	 * Using setUp() and not @Before because the existing CDTTestingTest uses this and the correct order would not be
	 * guaranteed.
	 */
	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("filesOutsideWorkspace");
		super.setUp();
		initAnalyzer();
		initTopLevelDefinitions();
		factory = CPPNodeFactory.getDefault();
		resolveFirstFoundCallInMain();
	}

	private void initAnalyzer() {
		IASTTranslationUnit ast = TestHelper.parse(getCurrentSource(), ParserLanguage.CPP);
		IIndex index = EclipseUtil.getIndexFromProject(cproject);
		analyzer = new ASTAnalyzer(index, ast);
	}

	protected void initTopLevelDefinitions() {
		IASTDeclaration[] allDeclarations = analyzer.getAst().getDeclarations();
		definitions = new ArrayList<>(allDeclarations.length);

		// just writing it out is easier than a visitor
		for (IASTDeclaration topLevelDeclaration : allDeclarations) {
			if (topLevelDeclaration instanceof IASTFunctionDefinition) {
				definitions.add(topLevelDeclaration);
			}
			if (topLevelDeclaration instanceof ICPPASTTemplateDeclaration) {
				ICPPASTTemplateDeclaration templateDeclaration = (ICPPASTTemplateDeclaration) topLevelDeclaration;
				definitions.add(templateDeclaration);
			}
		}
	}

	protected List<UnresolvedNameInfo> getSubStatements(IASTDeclaration definition) throws TemplatorException {
		List<UnresolvedNameInfo> preStatements = new ArrayList<>();

		IASTNode body = ASTTools.getBody(definition);
		if (body != null) {
			FindAllNamesVisitor visitor = new FindAllNamesVisitor();
			body.accept(visitor);

			for (IASTName name : visitor.getAllNames()) {
				UnresolvedNameInfo subStatement = NameDeduction.deduceName(name, false, analyzer,
						new RelevantNameCache());
				if (subStatement != null) {
					preStatements.add(subStatement);
				}
			}
		}

		return preStatements;
	}

	protected IASTFunctionDefinition getMain() throws TemplatorException {
		for (IASTDeclaration declaration : definitions) {
			if (declaration instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition definition = (IASTFunctionDefinition) declaration;
				if (getName(definition).toString().equals("main")
						&& definition.getDeclSpecifier().toString().equals("int")) {
					return definition;
				}
			}
		}
		throw new TemplatorException("no main function found in " + activeFileName);
	}

	protected IASTName getName(IASTFunctionDefinition definition) {
		return definition.getDeclarator().getName();
	}

	protected void assertEquals(TemplateArgumentMap expected, TemplateArgumentMap actual) {
		int expectedSize = expected.size();
		int actualSize = actual.size();
		if (expectedSize != actualSize) {
			fail("TemplateArgumentMap size was expected to be " + expectedSize + " but was " + actualSize);
		}

		for (int parameterId = 0; parameterId < expectedSize; parameterId++) {
			ICPPTemplateArgument expectedArgument = expected.getArgument(parameterId);
			ICPPTemplateArgument actualArgument = actual.getArgument(parameterId);

			String expectedString = expectedArgument.toString();
			String actualString = actualArgument.toString();

			IType expectedType = expectedArgument.getTypeValue();
			IType actualType = actualArgument.getTypeValue();
			if (expectedType instanceof IBinding) {
				expectedString = ((IBinding) expectedType).getName();
			}
			if (actualType instanceof IBinding) {
				actualString = ((IBinding) actualType).getName();
			}

			String errorMessage = "Argument #" + parameterId + " was expected to be " + expectedArgument + " but was "
					+ actualArgument + "\n";
			assertEquals(errorMessage, expectedString, actualString);
		}
	}

	protected <T> void assertEqualsToString(T[] expected, T[] actual) {
		if (expected.length != actual.length) {
			fail("Array length was expected to be " + expected.length + " but was " + actual.length);
		}
		for (int i = 0; i < actual.length; i++) {
			T actualElement = actual[i];
			T expectedElement = expected[i];
			assertEquals(expectedElement.toString(), actualElement.toString());
		}
	}

	protected void resolveFirstFoundCallInMain() throws TemplatorException {
		IASTFunctionDefinition main = getMain();
		List<UnresolvedNameInfo> subcalls = getSubStatements(main);

		firstStatementInMain = AbstractResolvedNameInfo.create(subcalls.get(0), null, analyzer);
	}

	protected void assertEquals(TemplateArgumentMap actual, ICPPTemplateArgument... expectedArguments) {
		TemplateArgumentMap expected = new TemplateArgumentMap();

		for (int i = 0; i < expectedArguments.length; i++) {
			ICPPTemplateArgument argument = expectedArguments[i];
			expected.put(i, argument);
		}

		assertEquals(expected, actual);
	}

	protected void testArgumentMap(TemplateArgumentMap actual, ICPPTemplateArgument... expectedArguments) {
		assertEquals(actual, expectedArguments);
	}

}