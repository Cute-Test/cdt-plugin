package ch.hsr.ifs.cute.mockator.refsupport.functions.params;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

import ch.hsr.ifs.cute.mockator.refsupport.utils.TypeCreator;


public class FunctionParamTypeCollector {

    private final ICPPASTFunctionDeclarator function;

    public FunctionParamTypeCollector(final ICPPASTFunctionDeclarator function) {
        this.function = function;
    }

    public List<IType> getParameterTypes() {
        final List<IType> paramTypes = new ArrayList<>();

        for (final ICPPASTParameterDeclaration param : function.getParameters()) {
            paramTypes.add(getTypeOfParam(param));
        }

        return paramTypes;
    }

    private static IType getTypeOfParam(final ICPPASTParameterDeclaration param) {
        return TypeCreator.byDeclarator(param.getDeclarator());
    }
}
