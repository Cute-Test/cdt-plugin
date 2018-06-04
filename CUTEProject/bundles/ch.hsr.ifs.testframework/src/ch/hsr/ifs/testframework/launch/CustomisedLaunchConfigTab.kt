/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.internal.ui.DebugUIPlugin
import org.eclipse.debug.internal.ui.SWTFactory
import org.eclipse.debug.ui.IDebugUIConstants
import org.eclipse.jface.dialogs.IDialogSettings
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.dialogs.ContainerSelectionDialog

import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.Model


private const val EMPTY_STRING = ""
public const val CUSTOM_SRC_PATH = "customSrcPath"
public const val USE_CUSTOM_SRC_PATH = "useCustomSrcPath"

/**
 * @since 3.0
 */
//@SuppressWarnings("restriction")
class CustomisedLaunchConfigTab : CLaunchConfigurationTab() {

   protected lateinit var descriptionLabel: Label

   private lateinit var fLocalRadioButton: Button
   private lateinit var fCustomSrcRadioButton: Button
   private lateinit var fCustomSrcLocationText: Text
   private lateinit var fCustomSrcLocationButton: Button

   protected val cm = TestFrameworkPlugin.getModel()

   @Override
   override fun createControl(parent: Composite) {
      val font = parent.getFont()
      val comp = Composite(parent, SWT.NONE)
      createVerticalSpacer(comp, 1)

      val layout = GridLayout(1, true)
      layout.numColumns = 2
      layout.marginHeight = 0
      layout.marginWidth = 5
      comp.setLayout(layout)
      comp.setFont(font)

      val text = LaunchConfigurationsMessages["CustomisedLaunchConfigTab.SourceFolderSelection"]
      val group = SWTFactory.createGroup(comp, text, 3, 2, GridData.FILL_HORIZONTAL)
      val n_comp = SWTFactory.createComposite(group, parent.getFont(), 3, 3, GridData.FILL_BOTH, 0, 0)
      descriptionLabel = Label(n_comp, SWT.NONE)
      descriptionLabel.setText(LaunchConfigurationsMessages["CustomisedLaunchConfigTab.SourceDescText"])
      GridData().let{
         it.horizontalSpan = 3
         descriptionLabel.layoutData = it
      }

      fLocalRadioButton = createRadioButton(n_comp, LaunchConfigurationsMessages["CustomisedLaunchConfigTab.Default"])
      GridData().let{
         it.horizontalSpan = 3
         fLocalRadioButton.layoutData = it
      }

      fCustomSrcRadioButton = createRadioButton(n_comp, LaunchConfigurationsMessages["CustomisedLaunchConfigTab.CustomPath"])
      fCustomSrcRadioButton.addSelectionListener(object : SelectionAdapter() {
         override fun widgetSelected(evt: SelectionEvent) =
            handleSharedRadioButtonSelected()
      })

      fCustomSrcLocationText = SWTFactory.createSingleText(n_comp, 1)
      fCustomSrcLocationText.addModifyListener{ updateLaunchConfigurationDialog() }
      fCustomSrcLocationButton = createPushButton(n_comp, EMPTY_STRING, null)
      fCustomSrcLocationButton.addSelectionListener(object : SelectionAdapter() {
         override fun widgetSelected(evt: SelectionEvent) =
            handleSharedLocationButtonSelected()
      })
      setControl(comp)
   }

   private fun handleSharedRadioButtonSelected() {
      setSharedEnabled(isShared())
      updateLaunchConfigurationDialog()
   }

   private fun setSharedEnabled(enable: Boolean) {
      fCustomSrcLocationText.setEnabled(enable)
      fCustomSrcLocationButton.setEnabled(enable)
   }

   private fun isShared() =
         fCustomSrcRadioButton.getSelection()

   private fun handleSharedLocationButtonSelected() {
      val currentContainerString = fCustomSrcLocationText.getText()
      val currentContainer = getContainer(currentContainerString)
      val dialog = SharedLocationSelectionDialog(getShell(), currentContainer, false, EMPTY_STRING)
      dialog.showClosedProjects(false)
      dialog.open()
      val results = dialog.getResult()
      results?.let{
         if(it.size > 0 && it[0] is IPath) {
            fCustomSrcLocationText.text = (it[0] as IPath).toOSString()
         }
      }
   }

   private fun getContainer(path: String) =
         Path(path).let{
            ResourcesPlugin.getWorkspace().getRoot().findMember(it) as IContainer
         }

   private class SharedLocationSelectionDialog(parent: Shell, root: IContainer, allowNewContainerName: Boolean, message:String) : ContainerSelectionDialog(parent, root, allowNewContainerName, message) {

      private val SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SHARED_LAUNCH_CONFIGURATON_DIALOG"

      override protected fun getDialogBoundsSettings() = 
            DebugUIPlugin.getDefault().getDialogSettings().let{
               it.getSection(SETTINGS_ID) ?: it.addNewSection(SETTINGS_ID)
            }
   }

   override fun getName() = LaunchConfigurationsMessages["CustomisedLaunchConfigTab.LookupPath"]

   override fun initializeFrom(configuration: ILaunchConfiguration) {
      try {
         val flag = configuration.getAttribute(USE_CUSTOM_SRC_PATH, false)
         if (flag) {
            fCustomSrcRadioButton.selection = true
            setSharedEnabled(true)
            fCustomSrcLocationText.text = configuration.getAttribute(CUSTOM_SRC_PATH, EMPTY_STRING)
         } else {
            fLocalRadioButton.selection = true
            setSharedEnabled(false)
         }
      } catch (ce: CoreException) {
         TestFrameworkPlugin.getDefault().getLog().log(ce.getStatus())
      }
   }

   override fun performApply(configuration: ILaunchConfigurationWorkingCopy) {
      configuration.setAttribute(USE_CUSTOM_SRC_PATH, isShared())
      configuration.setAttribute(CUSTOM_SRC_PATH, fCustomSrcLocationText.getText())
   }

   override fun setDefaults(configuration: ILaunchConfigurationWorkingCopy) =
         configuration.setAttribute(USE_CUSTOM_SRC_PATH, false)

   override fun isValid(launchConfig: ILaunchConfiguration) =
      if (isShared() && fCustomSrcLocationText.getText().equals(EMPTY_STRING)) {
         setErrorMessage(LaunchConfigurationsMessages["CustomisedLaunchConfigTab.NoSourcePathSelected"])
         false
      } else {
         true
      }
}