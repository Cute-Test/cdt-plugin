package ch.hsr.ifs.cute.constificator.quickfixes;

import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPLambdaExpressionParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.constificator.core.util.ast.Relation;

@SuppressWarnings("restriction")
public class FunctionParametersQuickFix extends QuickFix {

	private boolean isTesting = false;

	public FunctionParametersQuickFix() {
		super();
	}

	@Override
	public String getLabel() {
		return "Add const qualification";
	}

	public FunctionParametersQuickFix setTesting(boolean isTesting) {
		this.isTesting = isTesting;
		return this;
	}

	@Override
	protected void handleNode(IASTNode node, IIndex index, ASTRewriteCache cache) {
		ICPPASTFunctionDeclarator declarator;
		if ((declarator = Relation.getAncestorOf(ICPPASTFunctionDeclarator.class, node)) == null) {
			return;
		}

		ICPPASTParameterDeclaration parameterDeclaration;
		if ((parameterDeclaration = getAncestorOf(ICPPASTParameterDeclaration.class, node)) == null) {
			return;
		}

		IBinding parameterBinding = parameterDeclaration.getDeclarator().getName().resolveBinding();

		if (parameterBinding instanceof CPPLambdaExpressionParameter) {
			if (node instanceof ICPPASTDeclSpecifier) {
				ICPPASTDeclSpecifier original = (ICPPASTDeclSpecifier) node;
				ICPPASTDeclSpecifier replacement = original.copy();

				if(!original.isConst()) {
					replacement.setConst(true);
				}

				ASTRewrite rewrite = ASTRewrite.create(original.getTranslationUnit());
				rewrite.replace(original, replacement, null);
			}
		} else {

			CPPParameter parameter;
			if ((parameter = as(CPPParameter.class, parameterBinding)) == null) {
				return;
			}

			int parameterIndex = parameter.getParameterPosition();
			IIndexBinding adaptBinding = index.adaptBinding(declarator.getName().resolveBinding());

			ICProject project = declarator.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
			
			Set<ICPPASTFunctionDeclarator> nodes = new HashSet<>();
			
			try {
				IIndexName[] declarations = index.findNames(adaptBinding, IIndex.FIND_DECLARATIONS_DEFINITIONS);

				for (IIndexName declaration : declarations) {
					IIndexFileLocation file = declaration.getFile().getLocation();
					ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(file, project);
					IASTTranslationUnit ast = cache.getASTTranslationUnit(tu);
					IASTName currentName = ast.getNodeSelector(null).findName(declaration.getNodeOffset(),
							declaration.getNodeLength());
					ICPPASTFunctionDeclarator currentNode;
					if ((currentNode = Relation.getAncestorOf(ICPPASTFunctionDeclarator.class, currentName)) != null) {
						nodes.add(currentNode);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
			Set<ICPPASTFunctionDeclarator> decls = nodes;

			int rewriteCount = 0;

			for (ICPPASTFunctionDeclarator decl : decls) {
				ASTRewrite rewrite = cache.getASTRewrite(decl.getTranslationUnit().getOriginatingTranslationUnit());
				ICPPASTParameterDeclaration currentParameter = decl.getParameters()[parameterIndex];

				IASTNode original = null;
				IASTNode replacement = null;

				if (node instanceof ICPPASTName && isDescendendOf(ICPPASTDeclSpecifier.class, node)) {
					node = getAncestorOf(ICPPASTDeclSpecifier.class, node);
				}

				if (node instanceof ICPPASTDeclSpecifier) {
					original = currentParameter.getDeclSpecifier();

					if (((IASTDeclSpecifier) original).isConst()) {
						continue;
					}

					replacement = original.copy();
					((IASTDeclSpecifier) replacement).setConst(true);
				} else if (node instanceof IASTPointer) {
					IASTDeclarator markedDeclarator = parameterDeclaration.getDeclarator();
					List<IASTPointerOperator> pointerOps = Arrays.asList(markedDeclarator.getPointerOperators());
					int pointerIndex = pointerOps.indexOf(node);
					original = currentParameter.getDeclarator().getPointerOperators()[pointerIndex];

					if (((IASTPointer) original).isConst()) {
						continue;
					}

					replacement = original.copy();
					((IASTPointer) replacement).setConst(true);
				}

				rewrite.replace(original, replacement, null);
				rewriteCount += 1;
				
			}
			
			hasMultipleChanges = rewriteCount > 1 && !isTesting;
			
		}
	}

}