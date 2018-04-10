/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import ch.hsr.ifs.testframework.Messages;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.model.TestCase;
import ch.hsr.ifs.testframework.model.TestFailure;
import ch.hsr.ifs.testframework.model.TestResult;


/**
 * @author Emanuel Graf
 *
 */
public class CuteCompareResultDialog extends TrayDialog {

   private final Messages msg = TestFrameworkPlugin.getMessages();

   private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {

      private String fContent;

      public CompareElement(String content) {
         if (content == null) {
            fContent = "no Data";
         } else {
            fContent = content;
         }
      }

      @Override
      public String getName() {
         return "<no name>";
      }

      @Override
      public Image getImage() {
         return null;
      }

      @Override
      public String getType() {
         return "txt";
      }

      @Override
      public InputStream getContents() {
         try {
            return new ByteArrayInputStream(fContent.getBytes("UTF-8"));
         } catch (UnsupportedEncodingException e) {
            return new ByteArrayInputStream(fContent.getBytes());
         }
      }

      @Override
      public String getCharset() throws CoreException {
         return "UTF-8";
      }

   }

   private CuteTextMergeViewer compareViewer;
   TestCase                    test;

   public CuteCompareResultDialog(Shell shell, TestCase test) {
      super(shell);
      this.test = test;
      setHelpAvailable(false);
      setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);

   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite composite = (Composite) super.createDialogArea(parent);
      ViewForm pane = new ViewForm(composite, SWT.BORDER | SWT.FLAT);
      Control control = createCompareViewer(pane);
      GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
      data.widthHint = convertWidthInCharsToPixels(120);
      data.heightHint = convertHeightInCharsToPixels(13);
      pane.setLayoutData(data);
      ToolBar tb = new ToolBar(pane, SWT.BORDER | SWT.FLAT);
      ToolBarManager tbm = new ToolBarManager(tb);
      ShowWhiteSpaceAction action = new ShowWhiteSpaceAction(compareViewer);
      tbm.add(action);
      tbm.update(true);
      pane.setTopRight(tb);

      pane.setContent(control);
      GridData gd = new GridData(GridData.FILL_BOTH);
      control.setLayoutData(gd);
      return composite;
   }

   private Control createCompareViewer(ViewForm pane) {
      final CompareConfiguration compareConfiguration = new CompareConfiguration();
      compareConfiguration.setLeftLabel(msg.getString("CuteCompareResultDialog.Expected"));
      compareConfiguration.setLeftEditable(false);
      compareConfiguration.setRightLabel(msg.getString("CuteCompareResultDialog.Actual"));
      compareConfiguration.setRightEditable(false);
      compareConfiguration.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);

      compareViewer = new CuteTextMergeViewer(pane, SWT.NONE, compareConfiguration);
      setCompareViewerInput(test);

      Control control = compareViewer.getControl();
      control.addDisposeListener(e -> compareConfiguration.dispose());
      return control;

   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, msg.getString("CuteCompareResultDialog.Ok"), true);
   }

   public void setCompareViewerInput(TestCase test) {
      this.test = test;
      if (!compareViewer.getControl().isDisposed()) {
         TestResult result = test.getResult();
         if (result instanceof TestFailure) {
            TestFailure failure = (TestFailure) result;
            CompareElement expected = new CompareElement(failure.getExpected());
            CompareElement was = new CompareElement(failure.getWas());
            compareViewer.setInput(new DiffNode(expected, was));
         }

      }
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(msg.getString("CuteCompareResultDialog.ResultComparison"));
   }

   public void refresh() {
      compareViewer.refresh();
   }

}
