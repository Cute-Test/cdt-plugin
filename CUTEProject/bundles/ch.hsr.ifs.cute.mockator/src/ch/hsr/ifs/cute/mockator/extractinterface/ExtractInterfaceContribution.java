package ch.hsr.ifs.cute.mockator.extractinterface;

import java.util.Map;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContribution;


public class ExtractInterfaceContribution extends CRefactoringContribution {

   @Override
   public ExtractInterfaceDescriptor createDescriptor(final String id, final String proj, final String desc, final String comment,
         final Map<String, String> args, final int flags) throws IllegalArgumentException {
      return new ExtractInterfaceDescriptor(ExtractInterfaceRefactoring.ID, proj, desc, comment, args);
   }

}
