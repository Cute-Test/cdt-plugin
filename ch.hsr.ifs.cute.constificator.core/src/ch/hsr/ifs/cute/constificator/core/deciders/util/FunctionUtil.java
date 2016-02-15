package ch.hsr.ifs.cute.constificator.core.deciders.util;

import static ch.hsr.ifs.cute.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.constificator.core.util.ast.Relation;

@SuppressWarnings("restriction")
public class FunctionUtil {

	public static boolean hasConstOverload(ICPPASTName name, int parameterIndex, int pointerLevel, ASTRewriteCache cache) {
		Set<ICPPASTFunctionDeclarator> declarations = declarationsFor(name, true, cache);

		if (name == null) {
			return false;
		}

		ICPPFunction called;
		if ((called = as(ICPPFunction.class, name.resolveBinding())) == null) {
			return false;
		}

		if (called.getParameters().length <= parameterIndex) {
			return false;
		}

		ICPPParameter calledParameter = called.getParameters()[parameterIndex];
		for (ICPPASTFunctionDeclarator decl : declarations) {
			ICPPParameter currentParameter = as(ICPPParameter.class,
					decl.getParameters()[parameterIndex].getDeclarator().getName().resolveBinding());

			if (isMoreConst(currentParameter.getType(), calledParameter.getType())) {
				return true;
			}
		}

		return false;
	}

	public static Set<ICPPASTFunctionDeclarator> declarationsFor(ICPPASTName name, boolean includeOverloads, ASTRewriteCache cache) {
		Set<ICPPASTFunctionDeclarator> decls = new HashSet<>();

		if (name == null) {
			return decls;
		}

		ICPPFunction called;
		if ((called = as(ICPPFunction.class, name.resolveBinding())) == null) {
			return decls;
		}

		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(name, false, null);

		for (IBinding current : bindings) {
			if(current instanceof ICPPFunction) {
				if (matchTypes(called, (ICPPFunction) current, includeOverloads)) {
					IIndex index = name.getTranslationUnit().getIndex();
					ICProject project = name.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
					
					Set<ICPPASTFunctionDeclarator> nodes = new HashSet<>();
					
					try {
						IIndexName[] declarations = index.findNames(current, IIndex.FIND_DECLARATIONS_DEFINITIONS);

						for (IIndexName declaration : declarations) {
							IIndexFileLocation file = declaration.getFile().getLocation();
							ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(file, project);
							IASTTranslationUnit ast = cache.getASTTranslationUnit(tu); // get(file, index, project);
							IASTName currentName = ast.getNodeSelector(null).findName(declaration.getNodeOffset(),
									declaration.getNodeLength());
							ICPPASTFunctionDeclarator node;
							if ((node = Relation.getAncestorOf(ICPPASTFunctionDeclarator.class, currentName)) != null) {
								nodes.add(node);
							}
						}
					} catch (CoreException e) {

					}
					
				}
			}
		}

		return decls;
	}

	private static boolean matchTypes(ICPPFunction original, ICPPFunction suspect, boolean ignoreConst) {
		ICPPFunctionType originalType = original.getType();
		ICPPFunctionType suspectType = suspect.getType();

		if (!originalType.getReturnType().isSameType(suspectType.getReturnType())) {
			return false;
		}

		IType[] originalParameterTypes = originalType.getParameterTypes();
		IType[] suspectParameterTypes = suspectType.getParameterTypes();

		if (originalParameterTypes.length != suspectParameterTypes.length) {
			return false;
		}

		for (int index = 0; index < originalParameterTypes.length; ++index) {
			if (ignoreConst) {
				if (!areSameTypeIgnoringConst(suspectParameterTypes[index], originalParameterTypes[index])) {
					return false;
				}
			} else {
				if (!suspectParameterTypes[index].isSameType(originalParameterTypes[index])) {
					return false;
				}
			}
		}

		return true;
	}

}