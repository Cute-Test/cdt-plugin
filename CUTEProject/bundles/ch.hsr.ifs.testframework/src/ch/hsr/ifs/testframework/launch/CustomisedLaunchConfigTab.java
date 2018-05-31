/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch;

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.model.Model;


/**
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class CustomisedLaunchConfigTab extends CLaunchConfigurationTab {

   private static final String EMPTY_STRING        = "";
   public static final String  CUSTOM_SRC_PATH     = "customSrcPath";
   public static final String  USE_CUSTOM_SRC_PATH = "useCustomSrcPath";
   protected Label             descriptionLabel;
   private Button              fLocalRadioButton;
   private Button              fCustomSrcRadioButton;
   private Text                fCustomSrcLocationText;
   private Button              fCustomSrcLocationButton;

   protected Model cm = TestFrameworkPlugin.getModel();

   @Override
   public void createControl(Composite parent) {
      Font font = parent.getFont();
      Composite comp = new Composite(parent, SWT.NONE);
      createVerticalSpacer(comp, 1);

      GridLayout layout = new GridLayout(1, true);
      layout.numColumns = 2;
      layout.marginHeight = 0;
      layout.marginWidth = 5;
      comp.setLayout(layout);
      comp.setFont(font);
      GridData gd;

      String text = LaunchConfigurationsMessages.getString("CustomisedLaunchConfigTab.SourceFolderSelection");
      Group group = SWTFactory.createGroup(comp, text, 3, 2, GridData.FILL_HORIZONTAL);
      Composite n_comp = SWTFactory.createComposite(group, parent.getFont(), 3, 3, GridData.FILL_BOTH, 0, 0);
      descriptionLabel = new Label(n_comp, SWT.NONE);
      descriptionLabel.setText(LaunchConfigurationsMessages.getString("CustomisedLaunchConfigTab.SourceDescText"));
      gd = new GridData();
      gd.horizontalSpan = 3;
      descriptionLabel.setLayoutData(gd);

      fLocalRadioButton = createRadioButton(n_comp, LaunchConfigurationsMessages.getString("CustomisedLaunchConfigTab.Default"));
      gd = new GridData();
      gd.horizontalSpan = 3;
      fLocalRadioButton.setLayoutData(gd);

      fCustomSrcRadioButton = createRadioButton(n_comp, LaunchConfigurationsMessages.getString("CustomisedLaunchConfigTab.CustomPath"));
      fCustomSrcRadioButton.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent evt) {
            handleSharedRadioButtonSelected();
         }
      });
      fCustomSrcLocationText = SWTFactory.createSingleText(n_comp, 1);
      fCustomSrcLocationText.addModifyListener(fBasicModifyListener);
      fCustomSrcLocationButton = createPushButton(n_comp, EMPTY_STRING, null);
      fCustomSrcLocationButton.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent evt) {
            handleSharedLocationButtonSelected();
         }
      });
      setControl(comp);
   }

   private final ModifyListener fBasicModifyListener = evt -> updateLaunchConfigurationDialog();

   private void handleSharedRadioButtonSelected() {
      setSharedEnabled(isShared());
      updateLaunchConfigurationDialog();
   }

   private void setSharedEnabled(boolean enable) {
      fCustomSrcLocationText.setEnabled(enable);
      fCustomSrcLocationButton.setEnabled(enable);
   }

   private boolean isShared() {
      return fCustomSrcRadioButton.getSelection();
   }

   private void handleSharedLocationButtonSelected() {
      String currentContainerString = fCustomSrcLocationText.getText();
      IContainer currentContainer = getContainer(currentContainerString);
      SharedLocationSelectionDialog dialog = new SharedLocationSelectionDialog(getShell(), currentContainer, false, EMPTY_STRING);
      dialog.showClosedProjects(false);
      dialog.open();
      Object[] results = dialog.getResult();
      if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
         IPath path = (IPath) results[0];
         String containerName = path.toOSString();
         fCustomSrcLocationText.setText(containerName);
      }
   }

   private IContainer getContainer(String path) {
      Path containerPath = new Path(path);
      return (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(containerPath);
   }

   class SharedLocationSelectionDialog extends ContainerSelectionDialog {

      private final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SHARED_LAUNCH_CONFIGURATON_DIALOG";

      public SharedLocationSelectionDialog(Shell parentShell, IContainer initialRoot, boolean allowNewContainerName, String message) {
         super(parentShell, initialRoot, allowNewContainerName, message);
      }

      @Override
      protected IDialogSettings getDialogBoundsSettings() {
         IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(SETTINGS_ID);
         if (section == null) {
            section = settings.addNewSection(SETTINGS_ID);
         }
         return section;
      }
   }

   @Override
   public String getName() {
      return LaunchConfigurationsMessages.getString("CustomisedLaunchConfigTab.LookupPath");
   }

   @Override
   public void initializeFrom(ILaunchConfiguration configuration) {
      try {
         boolean flag = configuration.getAttribute(USE_CUSTOM_SRC_PATH, false);
         if (flag) {
            fCustomSrcRadioButton.setSelection(true);
            setSharedEnabled(true);
            fCustomSrcLocationText.setText(configuration.getAttribute(CUSTOM_SRC_PATH, EMPTY_STRING));
         } else {
            fLocalRadioButton.setSelection(true);
            setSharedEnabled(false);
         }
      } catch (CoreException ce) {
         TestFrameworkPlugin.getDefault().getLog().log(ce.getStatus());
      }
   }

   @Override
   public void performApply(ILaunchConfigurationWorkingCopy configuration) {
      configuration.setAttribute(USE_CUSTOM_SRC_PATH, isShared());
      configuration.setAttribute(CUSTOM_SRC_PATH, fCustomSrcLocationText.getText());
   }

   @Override
   public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
      configuration.setAttribute(USE_CUSTOM_SRC_PATH, false);
   }

   @Override
   public boolean isValid(ILaunchConfiguration launchConfig) {
      if (isShared() && fCustomSrcLocationText.getText().equals(EMPTY_STRING)) {
         setErrorMessage(LaunchConfigurationsMessages.getString("CustomisedLaunchConfigTab.NoSourcePathSelected"));
         return false;
      }
      return true;
   }
}
