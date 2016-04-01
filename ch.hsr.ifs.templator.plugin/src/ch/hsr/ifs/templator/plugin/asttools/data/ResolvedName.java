package ch.hsr.ifs.templator.plugin.asttools.data;

import org.eclipse.cdt.core.dom.ast.IASTName;

public class ResolvedName {
	private IASTName originalName;
	private AbstractResolvedNameInfo info;

	public ResolvedName(IASTName originalName, AbstractResolvedNameInfo info) {
		super();
		this.originalName = originalName;
		this.info = info;
	}

	public IASTName getOriginalName() {
		return originalName;
	}

	public AbstractResolvedNameInfo getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return "FoundName [originalName=" + originalName + ", info=" + info + "]";
	}

}
