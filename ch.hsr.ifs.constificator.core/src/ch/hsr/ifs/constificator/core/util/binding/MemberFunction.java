package ch.hsr.ifs.constificator.core.util.binding;

import static ch.hsr.ifs.constificator.core.util.type.Arrays.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

public class MemberFunction {

	public static boolean shadows(ICPPMethod suspect, ICPPMethod candidate) {
		ICPPClassType suspectOwner;
		ICPPClassType candidateOwner;

		if ((suspectOwner = as(ICPPClassType.class, suspect.getOwner())) == null
				|| (candidateOwner = as(ICPPClassType.class, candidate.getOwner())) == null) {
			return false;
		}

		if (suspectOwner.equals(candidateOwner)) {
			return false;
		}

		if (!isAnyOf(candidateOwner, suspectOwner.getBases())) {
			return false;
		}

		if(candidate.isVirtual()) {
			return suspect.getType().isConst() != candidate.getType().isConst();
		}

		return true;
	}

	public static boolean overrides(ICPPMethod suspect, ICPPMethod candidate) {
		ICPPClassType suspectOwner;
		ICPPClassType candidateOwner;

		if ((suspectOwner = as(ICPPClassType.class, suspect.getOwner())) == null
				|| (candidateOwner = as(ICPPClassType.class, candidate.getOwner())) == null) {
			return false;
		}

		if (suspectOwner.equals(candidateOwner)) {
			return false;
		}

		if (!isAnyOf(candidateOwner, suspectOwner.getBases())) {
			return false;
		}

		if(candidate.isVirtual()) {
			return suspect.getType().isSameType(candidate.getType());
		}

		return false;
	}

}
