package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig.RunConfigEnvManager;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.LinkerLibraryHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;


public class TogglePreloadFunctionAction implements IObjectActionDelegate, IMenuCreator {

   private IAction  delegateAction;
   private boolean  fillMenu;
   private IProject project;

   public TogglePreloadFunctionAction() {
      fillMenu = true;
   }

   @Override
   public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {}

   @Override
   public void dispose() {}

   @Override
   public Menu getMenu(final Control parent) {
      return null;
   }

   @Override
   public Menu getMenu(final Menu parent) {
      final Menu menu = new Menu(parent);
      // necessary to re-populate the menu each time
      menu.addMenuListener(new MenuAdapter() {

         @Override
         public void menuShown(final MenuEvent e) {
            if (!fillMenu) { return; }

            final Menu m = (Menu) e.widget;

            for (final MenuItem item : m.getItems()) {
               item.dispose();
            }

            fillMenu(m);
            fillMenu = false;
         }

         private void fillMenu(final Menu m) {
            for (final IProject proj : getReferencedShLibProjectsWithDlSupport()) {
               final Action preloadShLibToogle = createMenuAction(proj);
               final ActionContributionItem item = new ActionContributionItem(preloadShLibToogle);
               item.fill(m, -1);
            }
         }
      });

      return menu;
   }

   private Action createMenuAction(final IProject sharedLibProj) {
      final String sharedLibPath = getSharedLibPath(sharedLibProj);
      final boolean preloadActivated = isPreloadActivatedFor(sharedLibPath, sharedLibProj);
      final Action preloadShLibToggle = new Action(sharedLibProj.getName(), IAction.AS_CHECK_BOX) {

         @Override
         public void run() {
            final RunConfigEnvManager runConfigEnvManager = new RunConfigEnvManager(project, sharedLibProj);

            if (preloadActivated) {
               runConfigEnvManager.removePreloadLaunchConfig(sharedLibPath);
            } else {
               runConfigEnvManager.addPreloadLaunchConfig(sharedLibPath);
            }
         }
      };
      preloadShLibToggle.setChecked(preloadActivated);
      return preloadShLibToggle;
   }

   private boolean isPreloadActivatedFor(final String sharedLibPath, final IProject sharedLibProj) {
      return new RunConfigEnvManager(project, sharedLibProj).hasPreloadLaunchConfig(sharedLibPath);
   }

   private static String getSharedLibPath(final IProject project) {
      return new LibraryPathResolver(project).getLibraryWorkspacePath();
   }

   private Collection<IProject> getReferencedShLibProjectsWithDlSupport() {
      try {
         return Arrays.asList(project.getReferencedProjects()).stream().filter((project) -> CdtManagedProjectType.fromProject(
               project) != CdtManagedProjectType.SharedLib ? false : usesDynamicLibrary(project)).collect(Collectors.toList());
      } catch (final CoreException e) {
         return new ArrayList<>();
      }
   }

   @Override
   public void run(final IAction action) {
      // Never called because this is a menu.
   }

   private static Boolean usesDynamicLibrary(final IProject project) {
      return new LinkerLibraryHandler(project).hasLibrary(MockatorConstants.DYNAMIC_LIB_NAME);
   }

   @Override
   public void selectionChanged(final IAction action, final ISelection selection) {
      if (!(selection instanceof IStructuredSelection)) { return; }

      final Object obj = ((IStructuredSelection) selection).getFirstElement();

      if (!(obj instanceof IProject)) { return; }

      project = (IProject) obj;

      if (!isExecutableProject() || !isSupportedToolChain() || getReferencedShLibProjectsWithDlSupport().isEmpty()) {
         action.setEnabled(false);
         return;
      }

      if (delegateAction != action) {
         delegateAction = action;
         delegateAction.setMenuCreator(this);
      }

      fillMenu = true;
      action.setEnabled(true);
   }

   private boolean isExecutableProject() {
      return CdtManagedProjectType.fromProject(project) == CdtManagedProjectType.Executable;
   }

   private boolean isSupportedToolChain() {
      return ToolChain.fromProject(project).map((tc) -> {
         switch (tc) {
         case GnuLinux:
         case GnuMacOSX:
            return true;
         default:
            return false;
         }
      }).orElse(false);
   }
}
