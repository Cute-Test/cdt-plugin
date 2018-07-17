package ch.hsr.ifs.cute.mockator.extractinterface.preconditions;

import java.util.function.Consumer;

import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;


public class NewInterfaceNameProposal implements Consumer<ExtractInterfaceContext> {

   private static final String INTERFACE_NAME_PROPOSAL_SUFFIX = "Interface";

   @Override
   public void accept(final ExtractInterfaceContext c) {
      final String proposedName = c.getChosenClass().getName().toString() + INTERFACE_NAME_PROPOSAL_SUFFIX;
      c.setNewInterfaceNameProposal(proposedName);
   }
}
