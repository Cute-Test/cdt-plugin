package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;

interface DepInjectInfoCollector {

  Optional<Pair<IASTName, IType>> collectDependencyInfos(IASTName problemArgName);
}
