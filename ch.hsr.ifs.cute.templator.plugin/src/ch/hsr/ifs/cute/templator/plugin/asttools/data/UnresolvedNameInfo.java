package ch.hsr.ifs.cute.templator.plugin.asttools.data;

import java.util.Objects;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;

import ch.hsr.ifs.cute.templator.plugin.asttools.type.finding.RelevantNameType;

public class UnresolvedNameInfo {
	private IASTName originalName;
	private IASTName resolvingName;
	private IBinding binding;
	private NameTypeKind type;
	private RelevantNameType nameType;

	public UnresolvedNameInfo(IASTName originalName) {
		this.originalName = originalName;
	}

	public IASTName getOriginalName() {
		return originalName;
	}

	public IASTName getResolvingName() {
		return resolvingName;
	}

	public void setResolvingName(IASTName resolvingName) {
		this.resolvingName = resolvingName;
	}

	public IBinding getBinding() {
		return binding;
	}

	public void setBinding(IBinding binding) {
		setBinding(binding, false);
	}

	public void setBinding(IBinding binding, boolean alsoSetType) {
		this.binding = binding;
		if (alsoSetType) {
			setType(binding);
		}
	}

	public void setType(NameTypeKind type) {
		this.type = type;
	}

	public void setType(IBinding binding) {
		setType(NameTypeKind.getType(binding));
	}

	public NameTypeKind getType() {
		return type;
	}

	public boolean isRelevant() {
		return type != null;
	}

	public RelevantNameType getNameType() {
		return nameType;
	}

	public void setNameType(RelevantNameType nameType) {
		this.nameType = nameType;
	}

	/** For debug purposes only. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(originalName);
		sb.append(" (");
		sb.append("resolvingName: " + Objects.toString(resolvingName) + ", ");

		if (binding instanceof ICPPSpecialization) {
			sb.append("argument map: ");
			sb.append(((ICPPSpecialization) binding).getTemplateParameterMap());
		} else {
			sb.append("binding: ");
			sb.append(Objects.toString(binding));
		}
		sb.append(", type: " + type);
		sb.append(")");

		sb.append("\n\n");
		sb.append("grandparent: " + new ASTWriter().write(originalName.getParent().getParent()));

		return sb.toString();
	}
}
