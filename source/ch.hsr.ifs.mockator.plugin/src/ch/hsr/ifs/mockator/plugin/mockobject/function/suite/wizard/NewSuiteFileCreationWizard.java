/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.ExceptionUtil;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.LinkSuiteToRunnerRefactoring;


// Copied and adapted from CUTE
@SuppressWarnings("restriction")
public class NewSuiteFileCreationWizard extends Wizard implements INewWizard {

   private NewSuiteFileCreationWizardPage     page;
   private final ICProject                    mockatorCProject;
   private final MockFunctionCommunication    mockFunction;
   private final LinkSuiteToRunnerRefactoring runnerRefactoring;

   public NewSuiteFileCreationWizard(final ICProject mockatorCProject, final MockFunctionCommunication mockFunction,
                                     final LinkSuiteToRunnerRefactoring runnerRefactoring) {
      this.mockatorCProject = mockatorCProject;
      this.mockFunction = mockFunction;
      this.runnerRefactoring = runnerRefactoring;
      setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_SOURCEFILE);
      setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
      setWindowTitle(I18N.NewSuiteWizardNewCuiteSuiteFile);
   }

   @Override
   public void addPages() {
      super.addPages();
      page = new NewSuiteFileCreationWizardPage(mockatorCProject, mockFunction, runnerRefactoring);
      addPage(page);
   }

   @Override
   public boolean performFinish() {
      try {
         final WorkbenchRunnableAdapter adapter = new WorkbenchRunnableAdapter(createWorkspaceRunnable(), CProjectUtil.getWorkspaceRoot());
         getContainer().run(true, true, adapter);
      } catch (final InvocationTargetException e) {
         ExceptionUtil.showException(e);
         return false;
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
         return false;
      }

      return true;
   }

   private IWorkspaceRunnable createWorkspaceRunnable() {
      final IWorkspaceRunnable op = monitor -> page.createNewSuiteLinkedToRunner(monitor);
      return op;
   }

   @Override
   public void init(final IWorkbench workbench, final IStructuredSelection selection) {}
}
