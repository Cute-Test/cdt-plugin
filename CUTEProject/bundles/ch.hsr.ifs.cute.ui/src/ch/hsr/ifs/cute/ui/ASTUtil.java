/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;


/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */
public class ASTUtil {

    public static boolean isTestFunction(IASTDeclaration declaration) {
        if (declaration instanceof IASTFunctionDefinition) {
            IASTFunctionDefinition funcDef = (IASTFunctionDefinition) declaration;
            if (hasNoParameters(funcDef)) {
                if (containsAssert(funcDef)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsAssert(IASTFunctionDefinition funcDef) {
        IASTStatement functionBody = getFunctionBody(funcDef);
        if (functionBody == null) {
            return false;
        }
        AssertStatementCheckVisitor checker = new AssertStatementCheckVisitor();
        functionBody.accept(checker);
        return checker.hasAssertStmt;
    }

    public static boolean containsIndirectAssert(IASTFunctionDefinition funcDef) {
        IASTStatement functionBody = getFunctionBody(funcDef);
        if (functionBody == null) {
            return false;
        }
        IndirectAssertStatementCheckVisitor checker = new IndirectAssertStatementCheckVisitor();
        functionBody.accept(checker);
        return checker.hasIndirectAssertStmt;
    }

    private static IASTStatement getFunctionBody(IASTFunctionDefinition funcDef) {
        if (funcDef != null) {
            return funcDef.getBody();
        }
        return null;
    }

    public static boolean hasNoParameters(IASTFunctionDefinition funcDef) {
        IASTFunctionDeclarator declarator = funcDef.getDeclarator();
        if (declarator instanceof IASTStandardFunctionDeclarator) {
            IASTStandardFunctionDeclarator stdFuncDecl = (IASTStandardFunctionDeclarator) declarator;
            if (stdFuncDecl.getParameters() == null || stdFuncDecl.getParameters().length == 0) {
                return true;
            }
        }
        return false;
    }

}
