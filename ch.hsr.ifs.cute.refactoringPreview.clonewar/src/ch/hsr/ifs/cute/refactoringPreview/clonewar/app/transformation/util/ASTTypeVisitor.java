package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.BodyTransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.NestedTransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.ParamTransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.ReturnTransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.TransformAction;

/**
 * A visitor to find all types in an AST. The same node structure has to be
 * traversed twice:
 * 
 * <h1>First run:</h1> <li>Find types</li>
 * 
 * <h1>Second run:</h1> <li>Create actions for the types</li> <li>Set the copy
 * of the node (no modification of frozen AST)</li>
 * 
 * Therefore this visitor is only available for two runs, otherwise an
 * {@link IllegalStateException} is thrown.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class ASTTypeVisitor extends ASTVisitor {
    private final Map<ASTKeyPair, Class<? extends TransformAction>> registry_ = new HashMap<ASTKeyPair, Class<? extends TransformAction>>();
    private final Map<TypeInformation, List<TransformAction>> actions_ = new TreeMap<TypeInformation, List<TransformAction>>(
            new Comparator<TypeInformation>() {

                @Override
                public int compare(TypeInformation o1, TypeInformation o2) {
                    return o1.toString().compareTo(o2.toString());
                }});
    private final List<TypeInformation> typeInformations_ = new ArrayList<TypeInformation>();
    private Iterator<TypeInformation> typeInfoIterator_;
    private boolean secondRun_ = false;
    private Exception exception_;

    /**
     * Create the type visitor and load lookup registry for the actions.
     */
    public ASTTypeVisitor() {
        this.shouldVisitDeclSpecifiers = true;
        registry_.put(new ASTKeyPair(CPPASTParameterDeclaration.class,
                CPPASTSimpleDeclSpecifier.class), ParamTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTFunctionDefinition.class,
                CPPASTSimpleDeclSpecifier.class), ReturnTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTSimpleDeclaration.class,
                CPPASTSimpleDeclSpecifier.class), BodyTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTTypeId.class,
                CPPASTSimpleDeclSpecifier.class), NestedTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTParameterDeclaration.class,
                CPPASTNamedTypeSpecifier.class), ParamTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTFunctionDefinition.class,
                CPPASTNamedTypeSpecifier.class), ReturnTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTSimpleDeclaration.class,
                CPPASTNamedTypeSpecifier.class), BodyTransformAction.class);
        registry_.put(new ASTKeyPair(CPPASTTypeId.class,
                CPPASTNamedTypeSpecifier.class), NestedTransformAction.class);
    }

    /**
     * Since we can not change the visitor we have to manually check whether an
     * exception occurred while traversing the AST.
     * 
     * @return True if an exception occurred during the visit process, otherwise
     *         false.
     */
    public boolean hasException() {
        return exception_ != null;
    }

    /**
     * Returns the exception or null if there were no exception.
     * 
     * @return Exception.
     */
    public Exception getException() {
        return exception_;
    }

    /**
     * Change to the second run.
     */
    public void enableSecondRun() {
        secondRun_ = true;
    }

    /**
     * Returns the actions and type informations as a map.
     * 
     * @return Map of types and actions.
     */
    public Map<TypeInformation, List<TransformAction>> getActionMap() {
        if (!secondRun_) {
            throw new IllegalStateException(
                    "Reading action map before second run not allowed!");
        }
        return actions_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int visit(IASTDeclSpecifier declSpec) {
        try {
            if (secondRun_) {
                performSecondRun(declSpec);
            } else {
                performFirstRun(declSpec);
            }
        } catch (Exception e) {
            exception_ = e;
            return PROCESS_ABORT;
        }
        return PROCESS_CONTINUE;
    }

    /**
     * Handle the first run visiting.
     * 
     * @param declSpec
     *            Declaration specifier.
     */
    private void performFirstRun(IASTDeclSpecifier declSpec) {
        if (!hasAction(declSpec)) {
            return;
        }
        typeInformations_.add(createTypeInfo(declSpec));
    }

    /**
     * Handle the second run visiting.
     * 
     * @param declSpec
     *            Declaration specifier.
     * @throws IllegalAccessException
     *             Reflection.
     * @throws InstantiationException
     *             Reflection.
     */
    private void performSecondRun(IASTDeclSpecifier declSpec)
            throws InstantiationException, IllegalAccessException {
        if (typeInfoIterator_ == null) {
            typeInfoIterator_ = typeInformations_.iterator();
            secondRun_ = true;
        }
        if (!hasAction(declSpec)) {
            return;
        }
        if (!typeInfoIterator_.hasNext()) {
            throw new IllegalArgumentException(
                    "Not traversing the same AST structure!");
        }
        TypeInformation typeInfo = typeInfoIterator_.next();
        if (!actions_.containsKey(typeInfo)) {
            actions_.put(typeInfo, new ArrayList<TransformAction>());
        }
        actions_.get(typeInfo).add(createAction(declSpec));
    }

    /**
     * Create an action for the declaration node specifier.
     * 
     * @param declSpec
     *            Declaration specifier.
     * @return Action.
     * @throws IllegalAccessException
     *             Reflection.
     * @throws InstantiationException
     *             Reflection.
     */
    private TransformAction createAction(IASTDeclSpecifier declSpec)
            throws InstantiationException, IllegalAccessException {
        TransformAction action = registry_.get(new ASTKeyPair(declSpec))
                .newInstance();
        action.setNode(declSpec);
        return action;
    }

    /**
     * Create a type information for the declaration specifier.
     * 
     * @param declSpec
     *            Declaration specifier.
     * @return Type information.
     */
    private TypeInformation createTypeInfo(IASTDeclSpecifier declSpec) {
        return new TypeInformation(CPPVisitor.createType(declSpec));
    }

    /**
     * Check if an action could be created for the declaration specifier.
     * 
     * @param declSpec
     *            Declaration specifier.
     * @return True if an action could be created, otherwise false.
     */
    private boolean hasAction(IASTDeclSpecifier declSpec) {
        return registry_.containsKey(new ASTKeyPair(declSpec));
    }
}
