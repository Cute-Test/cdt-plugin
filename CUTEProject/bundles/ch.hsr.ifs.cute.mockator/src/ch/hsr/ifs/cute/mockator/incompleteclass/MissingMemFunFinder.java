package ch.hsr.ifs.cute.mockator.incompleteclass;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;


public interface MissingMemFunFinder {

   Collection<? extends MissingMemberFunction> findMissingMemberFunctions(ICPPASTCompositeTypeSpecifier clazz);
}
