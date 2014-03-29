package ch.hsr.ifs.mockator.plugin.linker.wrapfun;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.common.LinkerWrapFun;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.GnuOptionLinkerWrapFun;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.LdPreloadLinkerWrapFun;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;

public class LinkerWrapFunDelegate extends MockatorDelegate {

	@Override
	protected boolean arePreconditionsSatisfied() {
		return getLinkWrapHandler().arePreconditionsSatisfied();
	}

	@Override
	protected void execute() {
		getLinkWrapHandler().performWork();
	}

	private LinkerWrapFun getLinkWrapHandler() {
		switch (getProjectType()) {
		case Executable:
		case StaticLib:
			return new GnuOptionLinkerWrapFun(cProject, selection, cElement);
		case SharedLib:
			return new LdPreloadLinkerWrapFun(cProject, selection, cElement, getCppStd());
		default:
			throw new MockatorException("Unrecognized project type");
		}
	}

	private CdtManagedProjectType getProjectType() {
		return CdtManagedProjectType.fromProject(cProject.getProject());
	}
}
