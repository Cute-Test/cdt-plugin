package ch.hsr.ifs.cute.templator.plugin.asttools.resolving;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import ch.hsr.ifs.cute.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.RelevantNameCache;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.UnresolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.asttools.type.finding.RelevantNameType;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

public final class NameDeduction {
	private NameDeduction() {
	}

	public static UnresolvedNameInfo deduceName(IASTName originalName, boolean acceptUnknownBindings,
			ASTAnalyzer analyzer, RelevantNameCache cache) throws TemplatorException {
		IASTName name = originalName;

		/*
		 * so we don't find a sub name inside a template-id again Foo<int> will be called twice with a visitor that
		 * visits IASTNodes Foo: IASTName; Foo<int>: ICPPASTTemplateId which is also a subtype of IASTNode
		 *
		 * We only want to process the latter.
		 */
		if (name.getParent() instanceof ICPPASTTemplateId) {
			return null;
		}

		// So we don't find the name twice
		if (name instanceof ICPPASTQualifiedName) {
			return null;
		}

		UnresolvedNameInfo result = new UnresolvedNameInfo(name);

		IASTName resolvingName = null;

		// first check if this exact binding has already been resolved and is cached
		RelevantNameType nameType = cache.getFor(originalName);
		if (nameType != null) {
			resolvingName = nameType.getTypeName();
		} else {
			nameType = analyzer.extractResolvingName(originalName, acceptUnknownBindings);

			if (nameType != null && nameType.getTypeName() != null) {
				resolvingName = nameType.getTypeName();

				RelevantNameType cachedDeclaration = cache.getFor(nameType.getTypeBinding());
				if (cachedDeclaration != null) {
					nameType = cachedDeclaration;
				}
			}
			cache.put(originalName.resolveBinding(), nameType);
		}
		result.setResolvingName(resolvingName);
		result.setNameType(nameType);

		if (resolvingName != null) {
			IBinding resolvedType = resolvingName.resolveBinding();

			if (resolvedType instanceof IVariable) {
				IType ultimateType = SemanticUtil.getUltimateType(((IVariable) resolvedType).getType(), false);
				if (ultimateType instanceof ICPPSpecialization) {
					resolvedType = (IBinding) ultimateType;
				}
			}

			// determine if the binding is relevant for us, sets the type if relevant for us. If not == null
			result.setBinding(resolvedType, true);
			if (result.isRelevant()) {
				return result;
			}
		}
		return null;
	}
}
