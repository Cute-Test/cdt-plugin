package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;


public class MacroFinderVisitor extends ASTVisitor {

   private final List<IASTMacroExpansionLocation> macroExpansions;
   private final String                           macroName;

   {
      shouldVisitNames = true;
   }

   public MacroFinderVisitor(final String macroName) {
      this.macroName = macroName;
      macroExpansions = new ArrayList<>();
   }

   public Collection<IASTMacroExpansionLocation> getMatchingMacroExpansions() {
      return macroExpansions;
   }

   @Override
   public int visit(final IASTName name) {
      for (final IASTMacroExpansionLocation loc : getMacroExpansionLocations(name)) {
         if (getMacroName(loc).equals(macroName)) {
            macroExpansions.add(loc);
            return PROCESS_SKIP;
         }
      }
      return PROCESS_CONTINUE;
   }

   private static Collection<IASTMacroExpansionLocation> getMacroExpansionLocations(final IASTName name) {
      final List<IASTMacroExpansionLocation> macroExpansions = new ArrayList<>();

      for (final IASTNodeLocation loc : name.getNodeLocations()) {
         if (!(loc instanceof IASTMacroExpansionLocation)) {
            continue;
         }

         macroExpansions.add((IASTMacroExpansionLocation) loc);
      }
      return macroExpansions;
   }

   private static String getMacroName(final IASTMacroExpansionLocation expansionLoc) {
      return expansionLoc.getExpansion().getMacroDefinition().getName().toString();
   }
}
