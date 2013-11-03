package ch.hsr.ifs.cute.namespactor.ui.eudir;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

@SuppressWarnings("restriction")
public class EUDIRMenuHandler extends AbstractHandler {
	private static final String COMMAND_ID = "ch.hsr.ifs.cute.namespactor.ui.eudir.Refactoring";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (!(part instanceof CEditor))
			return null;
		EUDIRRefactoringAction extractAction = new EUDIRRefactoringAction(COMMAND_ID);
		extractAction.setEditor((IEditorPart) part);
		extractAction.run();
		return null;
	}
}
