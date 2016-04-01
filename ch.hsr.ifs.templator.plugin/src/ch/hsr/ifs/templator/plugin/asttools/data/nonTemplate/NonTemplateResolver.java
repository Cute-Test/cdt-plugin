package ch.hsr.ifs.templator.plugin.asttools.data.nonTemplate;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;

import ch.hsr.ifs.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;

public final class NonTemplateResolver {
	private NonTemplateResolver() {

	}

	public static AbstractResolvedNameInfo resolveName(IASTName name) throws TemplatorException {

		IBinding resolveBinding = name.resolveBinding();

		IASTSimpleDeclaration declaration = ASTTools.findFirstAncestorByType(name, IASTSimpleDeclaration.class, 2);

		IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();

		System.out.println("Decl: " + declaration);

		return null;
	}
}
