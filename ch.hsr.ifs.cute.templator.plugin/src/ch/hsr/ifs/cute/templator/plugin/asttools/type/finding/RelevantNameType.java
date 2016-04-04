package ch.hsr.ifs.cute.templator.plugin.asttools.type.finding;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

public abstract class RelevantNameType {
	protected IASTName definitionName;
	protected IASTName typeName;
	protected IBinding originalBinding;
	protected IBinding typeBinding;

	protected RelevantNameType(IASTName definitionName) {
		this.definitionName = definitionName;
		originalBinding = this.definitionName.resolveBinding();
	}

	protected abstract IASTName getTypeFromDefinition() throws TemplatorException;

	public static RelevantNameType create(IASTName definitionName) throws TemplatorException {
		RelevantNameType type = null;

		IBinding definitionBinding = definitionName.resolveBinding();
		if (definitionBinding instanceof ICPPParameter) {
			type = new Parameter(definitionName);
		} else if (definitionBinding instanceof IVariable || definitionBinding instanceof ITypedef) {
			type = new Variable(definitionName);
		} else if (definitionBinding instanceof ITypedef) {
			type = new Alias(definitionName);
		} else {
			type = new AlreadyRelevantType(definitionName);
		}

		if (type != null) {
			type.typeName = type.getTypeFromDefinition();
			if (type.typeName != null) {
				type.typeBinding = type.typeName.resolveBinding();
			}
		}

		return type;
	}

	// von NameTypeCache
	// private Map<IASTName, NameToType> nameCache = new HashMap<>();
	// private Map<IBinding, NameToType> bindingCache = new HashMap<>();
	//
	// public NameToType getFor(unresolvedNameInfo statement) {
	// NameToType result = getFor(statement.getResolvingName());
	// if (result == null) {
	// result = getFor(statement.getOriginalName().resolveBinding());
	// if (result == null) {
	// result = getFor(statement.getBinding());
	// }
	// }
	//
	// return result;
	// }

	public IASTName getDefinitionName() {
		return definitionName;
	}

	public IASTName getTypeName() {
		return typeName;
	}

	public IBinding getOriginalBinding() {
		return originalBinding;
	}

	public IBinding getTypeBinding() {
		return typeBinding;
	}

	/** For debug purposes only. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("definitionName: " + definitionName);
		sb.append(", typeName: " + typeName);
		sb.append(", typeBinding: " + typeBinding);

		return sb.toString();
	}

}
