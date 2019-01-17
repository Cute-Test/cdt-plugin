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
public class TestframeworkPrefPage : FieldEditorPreferencePage(GRID), IWorkbenchPreferencePage {

   private val msg = TestFrameworkPlugin.messages;

   init {
      setPreferenceStore(TestFrameworkPlugin.default.getPreferenceStore());
      setDescription(msg.getString("CutePrefPage.CuteRefPage"));
   }

   protected override fun createFieldEditors() {
      addField(BooleanFieldEditor(SHOW_WHITESPACES, msg.getString("CutePrefPage.ShowWhiteSpaces"), getFieldEditorParent()));

   }

   override fun init(workbench: IWorkbench) {}

   override fun getImage() =
      TestFrameworkPlugin.imageProvider.getImage(ImageProvider.APP_LOGO)?.createImage();

}
