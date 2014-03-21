package ch.hsr.ifs.cute.macronator.common;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.macronator.MacronatorPlugin;

/**
 * Implements the macro classification process.
 * 
 * @author silvano brugnoni
 * 
 */
public class MacroClassifier {

    private final IASTPreprocessorMacroDefinition macroDefinition;
    private final MacroProperties macroProperties;

    public MacroClassifier(IASTPreprocessorMacroDefinition macroDefinition) {
        this.macroDefinition = macroDefinition;
        this.macroProperties = new MacroProperties(macroDefinition);
    }

    public boolean isObjectLike() {
        return (macroProperties.isObjectStyle() && !isConfigurational());
    }

    public boolean isFunctionLike() {
        return (!macroProperties.isObjectStyle() && !isConfigurational());
    }

    public boolean areDependenciesValid() {
        List<String> freeVariables = macroProperties.getFreeVariables();
        for (String variableName : freeVariables) {
            if (!inScope(variableName) || isPointer(variableName))
                return false;
        }
        return true;
    }

    private boolean inScope(String variableName) {
        IIndexMacro[] macros = getMacroDefinitions(macroDefinition.getTranslationUnit(), variableName);
        boolean isMacro = (macros.length == 1);
        boolean inScope = (macroDefinition.getTranslationUnit().getScope().find(variableName).length > 0);
        return (isMacro || inScope);
    }

    private boolean isPointer(String variableName) {
        IBinding[] find = macroDefinition.getTranslationUnit().getScope().find(variableName);
        for (IBinding binding : find) {
            if (binding instanceof IVariable) {
                IVariable var = (IVariable) binding;
                IType type = var.getType();
                if (type instanceof IPointerType) {
                    return true;
                }
            }
        }
        return false;
    }

    private IIndexMacro[] getMacroDefinitions(IASTTranslationUnit translationUnit, String variableName) {
        try {
            return translationUnit.getIndex().findMacros(variableName.toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
        } catch (CoreException e) {
            MacronatorPlugin.log(e);
        }
        return new IIndexMacro[0];
    }

    public boolean isConfigurational() {
        for (IIndexName macroReference : macroProperties.getReferences()) {
            if (isInsideConditionalPreprocessorStatement(macroReference)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideConditionalPreprocessorStatement(IIndexName macroReference) {
        IASTTranslationUnit referenceAst = getTranslationUnit(macroReference);
        List<IASTPreprocessorStatement> statements = Arrays.<IASTPreprocessorStatement> asList(referenceAst.getAllPreprocessorStatements());
        for (IASTPreprocessorStatement statement : statements) {
            if (!isMacroDefinition(statement) && isReferenceContainedInStatement(macroReference, statement)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMacroDefinition(IASTPreprocessorStatement statement) {
        return statement instanceof IASTPreprocessorMacroDefinition;
    }

    private IASTTranslationUnit getTranslationUnit(IIndexName indexName) {
        try {
            ICProject proj = CoreModel.getDefault().create(new Path(indexName.getFileLocation().getFileName())).getCProject();
            ITranslationUnit referenceTU = CoreModel.getDefault().createTranslationUnitFrom(proj, (new Path(indexName.getFileLocation().getFileName())));
            return referenceTU.getAST(macroDefinition.getTranslationUnit().getIndex(), ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isReferenceContainedInStatement(IIndexName macroReference, IASTPreprocessorStatement statement) {
        try {
            IToken token = statement.getSyntax();
            while (token != null) {
                if (token.getType() == IToken.tIDENTIFIER) {
                    if (token.getImage().equals(new String(macroReference.getSimpleID()))) {
                        return true;
                    }
                }
                token = token.getNext();
            }
        } catch (ExpansionOverlapsBoundaryException e) {
            MacronatorPlugin.log(e);
        }
        return false;
    }
}
