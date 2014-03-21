package ch.hsr.ifs.cute.elevator.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;

/**
 * Checks if a specified declaration can be elevated to C++11 initializer.
 *
 */
public class DelaratorAnalyzer {
    private final IASTDeclarator declarator;

    public DelaratorAnalyzer(IASTDeclarator declarator) {
        this.declarator = declarator;
    }
    
    public boolean isElevationCandidate() {
        return !(isTemplateTypeSpecifier() 
        || isClassMember() 
        || hasInitializerListConstructor()
        || requiresTypeConversion()
        || isParameterDeclaration()
        || isAlreadyElevated()    
        || isPartOfCastExpression(declarator)
        || (declarator.getInitializer() == null && isReference()));
    }
    
    private boolean isConstructorInitializer() {
        return declarator.getInitializer() instanceof ICPPASTConstructorInitializer;
    }

    private boolean isEqualsInitializer() {
        return declarator.getInitializer() instanceof IASTEqualsInitializer;
    }
     
    private boolean isAlreadyElevated() {
        return declarator.getInitializer() instanceof IASTInitializerList;
    }

    private boolean isParameterDeclaration() {
        return declarator.getParent() instanceof ICPPASTParameterDeclaration;
    }

    private boolean isTemplateTypeSpecifier() {
        return declarator.getRawSignature().isEmpty();
    }
    
    private boolean isPartOfCastExpression(IASTNode node) {
        if (node == null) {
            return false;
        }
        return (node instanceof ICPPASTCastExpression) ? true : isPartOfCastExpression(node.getParent());
    }
    
    private boolean isClassMember() {
        // Parent is the IASTSimpleDeclaration, grandparent is potentially class
        return (declarator.getParent().getParent() instanceof ICPPASTCompositeTypeSpecifier);
    }
    
    private boolean isReference() {
        if (declarator.getPointerOperators().length > 0 ) {
            if(declarator.getPointerOperators()[0] != null ) {
                if(declarator.getPointerOperators()[0] instanceof ICPPASTReferenceOperator) {
                    return true;
                }
            }
        }               
        return false;
    }
    
    private boolean hasInitializerListConstructor() {
        ICPPConstructor[] constructors = getTargetTypeConstructors();
        if (constructors == null)
            return false;
        for (ICPPConstructor constructor : constructors) {
            for (ICPPParameter param : constructor.getParameters()) {
                if (param.getType().toString().contains("initializer_list")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Gets the constructors of the target type of a declaration.
     */
    private ICPPConstructor[] getTargetTypeConstructors() {
        ICPPConstructor[] constructors = null;
        IType targetType = getTargetType();
        if (targetType == null)
            return constructors;
        /*
         * While we should be using a test for ICPPClassType and not
         * CPPClassType, this results in an exception on getConstructors().
         */
        if (targetType instanceof CPPClassType) {
            constructors = ((ICPPClassType) targetType).getConstructors();
        }
        return constructors;
    }
    
    private IType getTargetType() {
        IBinding x = declarator.getName().resolveBinding();
        return (x instanceof ICPPVariable) ? ((ICPPVariable) x).getType() : null;
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
    
    /**
     * get the absolute type w/o reference and const types.
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
        IType targetType = getTargetType();
        if (targetType == null) {
            return convertibleTypes;
        }
        final IType absoluteTargetType = getAbsoluteType(targetType);
        if (absoluteTargetType instanceof ICPPBasicType) {
            ArrayList<IType> typeList = new ArrayList<IType>();
            typeList.add(absoluteTargetType);
            convertibleTypes.add(typeList);
        }

        if (absoluteTargetType instanceof ICPPClassType) {
            ICPPConstructor[] constructors = getTargetTypeConstructors();
            if (constructors == null) {
                // FIXME this should be moved to getTargetTypeConstructors after
                // adjusting the return value to transport types instead of
                // constructors
                if (absoluteTargetType instanceof ICPPTemplateInstance) {
                    @SuppressWarnings("deprecation")
                    IType[] types = ((ICPPTemplateInstance) absoluteTargetType).getArguments();
                    ArrayList<IType> typeList = new ArrayList<IType>();
                    for (IType type : types) {
                        typeList.add(type);
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
                if (!hasNonNarrowingTypeConversion(((IBasicType) targetType), (IBasicType) sourceType)) {
                    return false;
                }
            } else if (!targetType.equals(sourceType)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Narrowing type conversion: conversion with precisiion loss, e.g. int x = 3.2.
     * In contrast to copy initialization initializer list do not support narrowing conversions.
     * int x = 3.2;  // valid
     * int x { 3.2 }; // invalid
     * @param target Target Type
     * @param source Source Type
     * @return true if source type gets narrowed down.
     */
    private boolean hasNonNarrowingTypeConversion(IBasicType target, IBasicType source) {
        return isEqualType(target, source) || isFloatToDoubleConversion(target, source);
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
        if (sourceTypes.isEmpty()) {
            return true;
        }
        return !isTypeCompatible(getConvertibleTargetTypes(), sourceTypes);
    }   
}