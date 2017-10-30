package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class NewInterfaceNameProposal implements F1V<ExtractInterfaceContext> {

   private static final String INTERFACE_NAME_PROPOSAL_SUFFIX = "Interface";

   @Override
   public void apply(ExtractInterfaceContext c) {
      String proposedName = c.getChosenClass().getName().toString() + INTERFACE_NAME_PROPOSAL_SUFFIX;
      c.setNewInterfaceNameProposal(proposedName);
   }
}
