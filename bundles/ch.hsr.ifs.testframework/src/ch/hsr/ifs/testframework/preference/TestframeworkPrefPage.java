/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.testframework.ImageProvider;
import ch.hsr.ifs.testframework.Messages;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;


/**
 * @author Emanuel Graf
 *
 */
public class TestframeworkPrefPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

   private final Messages msg = TestFrameworkPlugin.getMessages();

   public TestframeworkPrefPage() {
      super(GRID);
      setPreferenceStore(TestFrameworkPlugin.getDefault().getPreferenceStore());
      setDescription(msg.getString("CutePrefPage.CuteRefPage"));
   }

   @Override
   protected void createFieldEditors() {
      addField(new BooleanFieldEditor(PreferenceConstants.SHOW_WHITESPACES, msg.getString("CutePrefPage.ShowWhiteSpaces"), getFieldEditorParent()));

   }

   @Override
   public void init(IWorkbench workbench) {}

   @Override
   public Image getImage() {
      return TestFrameworkPlugin.getImageProvider().getImage(ImageProvider.APP_LOGO).createImage();
   }

}