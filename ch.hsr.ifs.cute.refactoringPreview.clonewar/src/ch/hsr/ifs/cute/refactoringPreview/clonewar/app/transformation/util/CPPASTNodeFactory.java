package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

/**
 * Factory class providing functions to create different node structures used in
 * the clonewar plugin.
 * 
 * @author ythrier(at)hsr.ch
 */

public class CPPASTNodeFactory {
    private static int TYPENAME_SPECIFIER = ICPPASTSimpleTypeTemplateParameter.st_typename;
    private CPPNodeFactory cppNodes_ = new CPPNodeFactory();

    /**
     * Creates a new template parameter with a typename specifier and no default
     * type, e.g. <code>typename T</code>
     * 
     * @param paramName
     *            Name of the template parameter.
     * @return Simple type template parameter.
     */
    public ICPPASTSimpleTypeTemplateParameter createTemplateParameterDefinition(
            String paramName) {
        return new CPPASTSimpleTypeTemplateParameter(TYPENAME_SPECIFIER,
                cppNodes_.newName(paramName.toCharArray()), null);
    }

    /**
     * Returns a named type specifier.
     * 
     * @param name
     *            Name of the type.
     * @return Named type specifier.
     */
    public ICPPASTNamedTypeSpecifier createNamedTypeSpecifier(String name) {
        return new CPPASTNamedTypeSpecifier(createName(name));
    }

    /**
     * Returns an AST name.
     * 
     * @param name
     *            Name for the AST name.
     * @return AST name node.
     */
    private IASTName createName(String name) {
        return new CPPASTName(name.toCharArray());
    }

    /**
     * Creates a new template declaration with given template parameters and the
     * corresponding body of the template (the template implementation).
     * 
     * @param params
     *            Template parameters.
     * @param body
     *            Template body (implementation).
     * @return Template declaration.
     */
    public IASTNode createTemplateDeclaration(
            List<ICPPASTSimpleTypeTemplateParameter> params,
            IASTDeclaration body) {
        ICPPASTTemplateDeclaration templateDecl = new CPPASTTemplateDeclaration(
                body);
        for (ICPPASTSimpleTypeTemplateParameter param : params)
            templateDecl.addTemplateParameter(param);
        return templateDecl;
    }
}
