package ch.hsr.ifs.constificator.core.deciders.util;

import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

public class MemberVariableUtil {

	public static ICPPField[] memberVariablesForOwnerOf(ICPPMethod method) {
		if(method == null) {
			return new ICPPField[] {};
		}
		ICPPClassType cls = method.getClassOwner();
		IField[] fields = cls.getFields();
		ICPPField[] cppFields = new ICPPField[fields.length];

		Object field;
		for(int i = 0; i < fields.length; i++) {
			if(fields[i] instanceof ICPPField) {
				cppFields[i] = (ICPPField)fields[i];
			}
		}

		return cppFields;
	}

}
