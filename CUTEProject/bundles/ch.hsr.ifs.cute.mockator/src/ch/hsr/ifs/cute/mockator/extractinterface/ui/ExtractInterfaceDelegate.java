package ch.hsr.ifs.cute.mockator.extractinterface.ui;

import static ch.hsr.ifs.cute.mockator.base.i18n.I18N.ExtractInterfaceDialogTitle;

import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorDelegate;


public class ExtractInterfaceDelegate extends MockatorDelegate {

    @Override
    protected void execute() {
        final ExtractInterfaceAction action = new ExtractInterfaceAction(ExtractInterfaceDialogTitle, cProject);
        action.setEditor(window.getActivePage().getActiveEditor());
        action.run();
    }
}
