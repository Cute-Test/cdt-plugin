package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.iltis.core.data.AbstractPair;


interface DepInjectInfoCollector {

    Optional<DependencyInfo> collectDependencyInfos(IASTName problemArgName);

    class DependencyInfo extends AbstractPair<IASTName, IType> {

        public DependencyInfo(final IASTName name, final IType type) {
            super(name, type);
        }

        public IASTName getName() {
            return first;
        }

        public IType getType() {
            return second;
        }

    }
}
