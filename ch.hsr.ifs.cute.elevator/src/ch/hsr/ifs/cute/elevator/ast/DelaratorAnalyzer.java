package ch.hsr.ifs.cute.elevator.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;

import ch.hsr.ifs.cute.elevator.Activator;


/**
 * Checks if a specified declaration can be elevated to C++11 initializer.
 */
public class DelaratorAnalyzer {
    private final IASTDeclarator declarator;
    private final NodeProperties declaratorProperties;

    public DelaratorAnalyzer(IASTDeclarator declarator) {
        this.declarator = declarator;
        this.declaratorProperties = new NodeProperties(declarator);
    }
    
    public boolean isElevationCandidate() {
        return
            !isAlreadyElevated()  &&
            !isClassMember() &&
            !isTemplateTypeSpecifier() &&
            !requiresTypeConversion() &&
            !isParameterDeclaration() &&
            !hasAutoType() &&
            !isPartOfCastExpression() &&
            !hasConstructorWithInitializerListArgument() && 
            !isPartOfTypedef() &&
            !isInitializedAsRunVarInForLoop() &&
            !isTypeId() &&
            !isPartOfEqualsInitializationWithoutConstructorCall() &&
            !isUninitializedReference();
    }

    private boolean isTypeId() {
        return declaratorProperties.hasAncestor(IASTTypeId.class) && !isNewExpression();
    }

    private boolean isUninitializedReference() {
        return !hasInitializer() && isReference();
    }

    private boolean hasInitializer() {
        return declarator.getInitializer() != null;
    }
    
    private boolean isInitializedAsRunVarInForLoop() {
        return hasInitializer() && declaratorProperties.hasAncestor(ICPPASTForStatement.class) && declaratorProperties.getDistanceToAncestor(ICPPASTForStatement.class) == 3;
    }

    private boolean isPartOfEqualsInitializationWithoutConstructorCall() {
        if (isEqualsInitializer()) {
            IASTInitializerClause clause = ((IASTEqualsInitializer)declarator.getInitializer()).getInitializerClause();
            if (clause instanceof ICPPASTFunctionCallExpression) {
                IASTImplicitName[] implicitNames = ((ICPPASTFunctionCallExpression)(clause)).getImplicitNames();
                return implicitNames.length == 0 || !(implicitNames[0].resolveBinding() instanceof ICPPConstructor);
            }
        }
        return false;
    }
    
    private boolean hasConstructorWithInitializerListArgument() {
        if (declarator instanceof IASTImplicitNameOwner) {
            IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) declarator).getImplicitNames();  
            for (IASTImplicitName name : implicitNames) {
                if (name.getBinding() instanceof CPPConstructor) {       
                    ICPPConstructor[] constructors = ((CPPConstructor)name.getBinding()).getClassOwner().getConstructors();
                    for (ICPPConstructor constructor : constructors) {
                        if (cotainsInitializerList(constructor)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean cotainsInitializerList(ICPPConstructor constructor) {
        for (ICPPParameter parameter : constructor.getParameters()) {
            if (parameter.getType() instanceof ICPPClassType) {
                ICPPClassType type = (ICPPClassType) parameter.getType();
                try {
                    return type.getScope().toString().equals("std") && type.getName().startsWith("initializer_list");
                } catch (DOMException e) {
                    Activator.log(e);
                }
            }
        }
        return false;
    }

    private boolean isPartOfTypedef() {
        return declaratorProperties.hasAncestor(IASTSimpleDeclaration.class) && declaratorProperties.getAncestor(IASTSimpleDeclaration.class).getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef;
    }
   
    private boolean isNewExpression() {
        return declaratorProperties.hasAncestor(ICPPASTNewExpression.class);
    }
    
    private boolean isPartOfCastExpression() {
        return declaratorProperties.hasAncestor(ICPPASTCastExpression.class);
    }
    
    private boolean isConstructorInitializer() {
        return declarator.getInitializer() instanceof ICPPASTConstructorInitializer;
    }

    private boolean isEqualsInitializer() {
        return declarator.getInitializer() instanceof IASTEqualsInitializer;
    }
     
    private boolean isAlreadyElevated() {
       return hasInitializerListInitializer() || isElevatedNewExpression();
    }

    private boolean hasInitializerListInitializer() {
        return declarator.getInitializer() != null && declarator.getInitializer() instanceof IASTInitializerList;
    }
    
    private boolean isElevatedNewExpression() {   
        return isNewExpression() && declaratorProperties.getAncestor(ICPPASTNewExpression.class).getInitializer() instanceof IASTInitializerList;
    }

    private boolean isParameterDeclaration() {
        return declarator.getParent() instanceof ICPPASTParameterDeclaration;
    }

    private boolean isTemplateTypeSpecifier() {
        return declaratorProperties.hasAncestor(ICPPASTTemplateId.class);
    }
    
    private boolean hasAutoType() {
        NodeProperties properties = new NodeProperties(declarator);
        if (!properties.hasAncestor(IASTSimpleDeclaration.class)) {
            return false;
        }
        IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) properties.getAncestor(IASTSimpleDeclaration.class);
        IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
        return (declSpecifier instanceof IASTSimpleDeclSpecifier && ((IASTSimpleDeclSpecifier)declSpecifier).getType() == IASTSimpleDeclSpecifier.t_auto);
    }
    
    private boolean isClassMember() {
        // Parent is the IASTSimpleDeclaration, grandparent is potentially a class
        return (declarator.getParent().getParent() instanceof ICPPASTCompositeTypeSpecifier);
    }
    
    private boolean isReference() {
            return (declarator.getPointerOperators().length > 0) && (declarator.getPointerOperators()[0] instanceof ICPPASTReferenceOperator);                   
    }
     
    private IType getType() {
        IBinding x = declarator.getName().resolveBinding();
        return ((IVariable) x).getType();
    } 
    
    /**
     * extracts the real type from references and qualifiers.
     */
    private IType getAbsoluteType(IType type) {
        if (type instanceof ICPPReferenceType) {
            type = ((ICPPReferenceType) type).getType();
        }
        if (type instanceof IQualifierType) {
            type = ((IQualifierType) type).getType();
        }
        return type;
    }
    
    /**
     * Inspect target type constructors taking a single argument, and return
     * list of found types.
     */
    private List<List<IType>> getConvertibleTargetTypes() {
        List<List<IType>> convertibleTypes = new ArrayList<List<IType>>();
        final IType absoluteTargetType = getAbsoluteType(getType());
        if (absoluteTargetType instanceof ICPPBasicType) {
            ArrayList<IType> typeList = new ArrayList<IType>();
            typeList.add(absoluteTargetType);
            convertibleTypes.add(typeList);
        }

        if (absoluteTargetType instanceof ICPPClassType) {
            ICPPConstructor[] constructors = getTypeConstructors();
            if (constructors.length == 0) {
                // FIXME this should be moved to getTargetTypeConstructors after
                // adjusting the return value to transport types instead of
                // constructors
                if (absoluteTargetType instanceof ICPPTemplateInstance) {
                    ArrayList<IType> typeList = new ArrayList<IType>();
                    
                    for (ICPPTemplateArgument argument : ((ICPPTemplateInstance) absoluteTargetType).getTemplateArguments()) {
                        if (argument.getTypeValue() != null) {
                            typeList.add(argument.getTypeValue());
                        }
                    }
                    convertibleTypes.add(typeList);
                }
                return convertibleTypes;
            }
            for (ICPPConstructor decl : constructors) {
                ArrayList<IType> typeList = new ArrayList<IType>();
                for (ICPPParameter param : decl.getParameters()) {
                    IType type = getAbsoluteType(param.getType());
                    typeList.add(type);
                }
                convertibleTypes.add(typeList);
            }
        }
        return convertibleTypes;
    }
    
    /**
     * Gets the constructors of the target type of a declaration.
     */
    private ICPPConstructor[] getTypeConstructors() {
        IType type = getType();
        /*
         * While we should be using a test for ICPPClassType and not
         * CPPClassType, this results in an exception on getConstructors().
         */
        return type instanceof CPPClassType ? ((ICPPClassType) type).getConstructors() : new ICPPConstructor[0];
    }
    
    private boolean isTypeCompatible(List<List<IType>> convertibleTypes, List<IType> sourceTypes) {
        for (List<IType> constructorTypes : convertibleTypes) {
            if (constructorTypes.size() == sourceTypes.size() && hasEqualType(sourceTypes, constructorTypes)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasEqualType(List<IType> sourceTypes, List<IType> constructorTypes) {
        for (int i = 0; i < constructorTypes.size(); i++) {
            IType targetType = constructorTypes.get(i);
            IType sourceType = sourceTypes.get(i);
            if (targetType instanceof IBasicType && sourceType instanceof IBasicType) {
                if (hasNarrowingTypeConversion(((IBasicType) targetType), (IBasicType) sourceType)) {
                    return false;
                }
            } else if (!targetType.equals(sourceType)) {
                return false;
            }
        }
        return true;
    }
    
    /*
     * Narrowing type conversion: conversion with precision loss, e.g. int x = 3.2;
     * In contrast to copy initialization, initializer lists do not support narrowing conversions.
     * int x = 3.2;  // valid
     * int x { 3.2 }; // invalid
     * @param target Target Type
     * @param source Source Type
     * @return true if source type gets narrowed down.
     */
    private boolean hasNarrowingTypeConversion(IBasicType target, IBasicType source) {
        return !(isEqualType(target, source) || isFloatToDoubleConversion(target, source));
    }

    private boolean isEqualType(IBasicType target, IBasicType source) {
        return target.getKind() == source.getKind();
    }
    
    private boolean isFloatToDoubleConversion(IBasicType target, IBasicType source) {
        return target.getKind() == IBasicType.Kind.eDouble && source.getKind() == IBasicType.Kind.eFloat;
    }
    
    private boolean requiresTypeConversion() {
        if (!isEqualsInitializer() && !isConstructorInitializer()) {
            return false;
        }
        List<IType> sourceTypes = getSourceTypes();
        return sourceTypes.isEmpty() || !isTypeCompatible(getConvertibleTargetTypes(), sourceTypes);
    }   
    

    private List<IType> getSourceTypes() {
        if (isEqualsInitializer()) {
            return getEqualsInitializerSourceTypes();
        } 
        if (isConstructorInitializer()) {
            return getConstructorInitializerSourceTypes();
        }
        return Collections.emptyList();
    }
    
    private List<IType> getConstructorInitializerSourceTypes() {
        ArrayList<IType> sourceTypes = new ArrayList<IType>();   
        for (IASTInitializerClause clause : ((ICPPASTConstructorInitializer) declarator.getInitializer()).getArguments()) {
            if (clause instanceof IASTExpression) {
                sourceTypes.add(((IASTExpression) clause).getExpressionType());
            }
        }
        return sourceTypes;
    }

    private List<IType> getEqualsInitializerSourceTypes() {
        List<IType> sourceTypes = new ArrayList<IType>();   
        IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer) declarator.getInitializer();
        IASTInitializerClause clause = equalsInitializer.getInitializerClause();
        if (clause instanceof IASTExpression) {
            sourceTypes.add(((IASTExpression) clause).getExpressionType());
        }
        return sourceTypes;
    }
}