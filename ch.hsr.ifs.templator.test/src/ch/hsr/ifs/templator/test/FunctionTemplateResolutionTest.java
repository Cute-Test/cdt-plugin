package ch.hsr.ifs.templator.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;

import ch.hsr.ifs.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.templator.plugin.asttools.data.ResolvedName;
import ch.hsr.ifs.templator.plugin.asttools.data.NameTypeKind;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.util.ILoadingProgress;

public abstract class FunctionTemplateResolutionTest extends TemplatorProjectTest {
	protected static ILoadingProgress loadingProgress = new NullLoadingProgress();
	private static final IType INT_TYPE;

	private static final IType INT_POINTER_TYPE;
	public static final ICPPTemplateArgument INT;
	public static final ICPPTemplateArgument INT_POINTER;
	public static final ICPPTemplateArgument INT_POINTER_CONST;
	public static final ICPPTemplateArgument INT_REFERENCE;
	public static final ICPPTemplateArgument INT_CONST_REFERENCE;
	public static final ICPPTemplateArgument DOUBLE;
	public static final ICPPTemplateArgument CHAR;
	public static final ICPPTemplateArgument LONG;
	public static final ICPPTemplateArgument UNSIGNED_LONG;
	public static final ICPPTemplateArgument UNSIGNED_LONG_LONG;
	public static final ICPPTemplateArgument BOOL;

	static {
		INT_TYPE = new CPPBasicType(Kind.eInt, 0);
		INT_POINTER_TYPE = new CPPPointerType(INT_TYPE);
		INT = new CPPTemplateTypeArgument(INT_TYPE);
		INT_POINTER = new CPPTemplateTypeArgument(INT_POINTER_TYPE);
		INT_POINTER_CONST = new CPPTemplateTypeArgument(new CPPQualifierType(INT_POINTER_TYPE, true, false));
		INT_REFERENCE = new CPPTemplateTypeArgument(new CPPReferenceType(INT_TYPE, false));
		INT_CONST_REFERENCE = new CPPTemplateTypeArgument(
				new CPPReferenceType(new CPPQualifierType(INT_TYPE, true, false), false));
		DOUBLE = new CPPTemplateTypeArgument(new CPPBasicType(Kind.eDouble, 0));
		CHAR = new CPPTemplateTypeArgument(new CPPBasicType(Kind.eChar, 0));
		LONG = new CPPTemplateTypeArgument(new CPPBasicType(Kind.eInt, IBasicType.IS_LONG));
		UNSIGNED_LONG = new CPPTemplateTypeArgument(
				new CPPBasicType(Kind.eInt, IBasicType.IS_LONG | IBasicType.IS_UNSIGNED));
		UNSIGNED_LONG_LONG = new CPPTemplateTypeArgument(
				new CPPBasicType(Kind.eInt, IBasicType.IS_LONG_LONG | IBasicType.IS_UNSIGNED));
		BOOL = new CPPTemplateTypeArgument(new CPPBasicType(Kind.eBoolean, 0, null));
	}

	@Override
	protected void initTopLevelDefinitions() {
		super.initTopLevelDefinitions();

		for (int i = 0; i < definitions.size(); i++) {
			IASTDeclaration topLevelDeclaration = definitions.get(i);
			if (topLevelDeclaration instanceof ICPPASTTemplateDeclaration) {
				ICPPASTTemplateDeclaration templateDeclaration = (ICPPASTTemplateDeclaration) topLevelDeclaration;
				IASTDeclaration declaration = templateDeclaration.getDeclaration();
				if (declaration instanceof IASTFunctionDefinition) {
					definitions.set(i, declaration);
				}
			}
		}
	}

	protected List<ResolvedName> getOnlyFunctionCallSubstatements(AbstractResolvedNameInfo instance) {
		List<ResolvedName> functionCalls = new ArrayList<>();
		for (ResolvedName sub : instance.getSubNames()) {
			NameTypeKind type = sub.getInfo().getType();
			if (type == NameTypeKind.FUNCTION_TEMPLATE || type == NameTypeKind.FUNCTION) {
				functionCalls.add(sub);
			}
		}
		return functionCalls;
	}

	protected void testOuterArgumentMap(ICPPTemplateArgument... expectedArguments) {
		testArgumentMap(firstStatementInMain.getTemplateArgumentMap(), expectedArguments);
	}

	protected void testFirstInnerArgumentMap(ICPPTemplateArgument... expected) throws TemplatorException {
		firstStatementInMain.searchSubNames(loadingProgress);
		AbstractResolvedNameInfo innerCall = getOnlyFunctionCallSubstatements(firstStatementInMain).get(0).getInfo();
		testArgumentMap(innerCall.getTemplateArgumentMap(), expected);
	}

	protected void testFirstInnerCallResolvesToFirstDefinition() throws TemplatorException {
		testFirstInnerCallResolvesTo((IASTFunctionDefinition) definitions.get(0));
	}

	protected void testFirstInnerCallResolvesTo(IASTFunctionDefinition expected) throws TemplatorException {
		firstStatementInMain.searchSubNames(loadingProgress);
		ResolvedName actualCall = getOnlyFunctionCallSubstatements(firstStatementInMain).get(0);
		IASTDeclaration actualDefinition = actualCall.getInfo().getDefinition();
		if (actualDefinition instanceof IASTFunctionDefinition) {
			assertEquals(expected, actualDefinition);
		} else if (actualDefinition instanceof ICPPASTTemplateDeclaration) {
			actualDefinition = ((ICPPASTTemplateDeclaration) actualDefinition).getDeclaration();
			assertEquals(expected, actualDefinition);
		} else {
			fail("could not determine IASTFunctionDefinition for " + actualCall.getOriginalName());
		}
	}
}
