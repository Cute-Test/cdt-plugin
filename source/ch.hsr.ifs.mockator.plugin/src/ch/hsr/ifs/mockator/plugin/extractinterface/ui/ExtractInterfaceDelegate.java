package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import static ch.hsr.ifs.mockator.plugin.base.i18n.I18N.ExtractInterfaceDialogTitle;

import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;


public class ExtractInterfaceDelegate extends MockatorDelegate {

   @Override
   protected void execute() {
      final ExtractInterfaceAction action = new ExtractInterfaceAction(ExtractInterfaceDialogTitle, cProject);
      action.setEditor(window.getActivePage().getActiveEditor());
      action.run();
   }
}
