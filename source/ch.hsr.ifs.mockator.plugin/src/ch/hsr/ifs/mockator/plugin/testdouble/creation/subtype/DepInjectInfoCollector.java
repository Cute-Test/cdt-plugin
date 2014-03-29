package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;

interface DepInjectInfoCollector {
  Maybe<Pair<IASTName, IType>> collectDependencyInfos(IASTName problemArgName);
}
