package ch.hsr.ifs.cute.mockator.incompleteclass;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


public interface TestDoubleMemFun {

    String getFunctionSignature();

    Collection<IASTInitializerClause> createDefaultArguments(CppStandard cppStd, LinkedEditModeStrategy linkedEdit);

    boolean isStatic();
}
