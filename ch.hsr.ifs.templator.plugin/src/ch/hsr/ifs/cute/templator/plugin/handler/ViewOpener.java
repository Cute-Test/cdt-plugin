package ch.hsr.ifs.cute.templator.plugin.handler;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.hsr.ifs.cute.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.ResolvedName;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorLogger;
import ch.hsr.ifs.cute.templator.plugin.util.EclipseUtil;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.ITemplateView;
import ch.hsr.ifs.cute.templator.plugin.view.tree.TreeTemplateView;
import ch.hsr.ifs.cute.templator.plugin.viewdata.ViewData;

public final class ViewOpener {

	private ViewOpener() {
	}

	public static void showTemplateInfoUnderCursor() {
		TreeTemplateView treeView = openView(TreeTemplateView.VIEW_ID);
		initTemplateView(treeView);
	}

	private static void initTemplateView(ITemplateView templateView) {

		try {
			ResolvedName resolvedName = getNameInfoUnderCursor();
			if (resolvedName != null) {
				ViewData viewData = new ViewData(resolvedName);
				templateView.setRootData(viewData);
			}
		} catch (TemplatorException e) {
			TemplatorLogger.errorDialogWithStackTrace("Name Resolving Error",
					"An error has occured while trying to resolve a name.", e);
			e.printStackTrace();
		} catch (Exception e) {
			TemplatorLogger.errorDialogWithStackTrace("Name Resolving Error",
					"Unexpected error while trying to resolve a name.", e);
			e.printStackTrace();
		}

	}

	private static ResolvedName getNameInfoUnderCursor() throws TemplatorException {
		IIndex index = EclipseUtil.getIndexFromCurrentCProject();
		IASTTranslationUnit ast = EclipseUtil.getASTFromIndex(index);
		ASTAnalyzer analyzer = new ASTAnalyzer(index, ast);

		IASTName selectedName = EclipseUtil.getNameUnderCursor(ast);
		if (selectedName == null) {
			return null;
		}

		AbstractResolvedNameInfo createdResolvedName = AbstractResolvedNameInfo.create(selectedName, false, analyzer);
		if (createdResolvedName != null) {
			return new ResolvedName(selectedName, createdResolvedName);
		}

		throw new TemplatorException("Could not visualize selected name: " + selectedName
				+ ". The selected name either has no definition, is not yet supported (like normal classes) or depends on a template argument. "
				+ "Please select a name that does not yet depent on a template argument.");
	}

	@SuppressWarnings("unchecked")
	private static <T extends ViewPart> T openView(String viewId) {
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();

		try {
			IViewPart view = page.showView(viewId);
			return (T) view;
		} catch (PartInitException e) {
			TemplatorLogger.logError("error opening " + viewId + " view part init", e);
		} catch (ClassCastException e) {
			TemplatorLogger.logError("error opening template view, cast to ", e);
		}
		return null;
	}
}