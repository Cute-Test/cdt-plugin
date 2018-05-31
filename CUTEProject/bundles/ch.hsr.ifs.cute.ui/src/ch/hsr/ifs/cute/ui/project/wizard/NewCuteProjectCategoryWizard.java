/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;


/**
 * @author Emanuel Graf
 *
 */
public class NewCuteProjectCategoryWizard extends AbstractCWizard {

   protected static final String ID   = "ch.hsr.ifs.cutesuitegroup";
   private static final String   NAME = "CUTE";

   @Override
   public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
      CuteWizardHandler handler = getHandler(wizard);
      IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MBSWizardHandler.ARTIFACT, new CuteBuildPropertyValue().getId(), false);
      for (IToolChain curToolChain : tcs) {
         if (isValid(curToolChain, supportedOnly, wizard)) {
            handler.addTc(curToolChain);
         }
      }
      EntryDescriptor data = getEntryDescriptor(handler);
      data.setDefaultForCategory(true);
      return new EntryDescriptor[] { data };
   }

   protected EntryDescriptor getEntryDescriptor(CuteWizardHandler handler) {
      return new EntryDescriptor(ID, null, NAME, true, handler, null);
   }

   protected CuteWizardHandler getHandler(IWizard wizard) {
      return new CuteWizardHandler(parent, wizard);

   }

}
