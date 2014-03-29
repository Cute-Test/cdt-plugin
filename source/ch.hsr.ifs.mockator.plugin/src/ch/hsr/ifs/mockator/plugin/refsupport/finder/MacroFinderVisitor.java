package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;

public class MacroFinderVisitor extends ASTVisitor {
  private final List<IASTMacroExpansionLocation> macroExpansions;
  private final String macroName;

  {
    shouldVisitNames = true;
  }

  public MacroFinderVisitor(String macroName) {
    this.macroName = macroName;
    macroExpansions = list();
  }

  public Collection<IASTMacroExpansionLocation> getMatchingMacroExpansions() {
    return macroExpansions;
  }

  @Override
  public int visit(IASTName name) {
    for (IASTMacroExpansionLocation loc : getMacroExpansionLocations(name)) {
      if (getMacroName(loc).equals(macroName)) {
        macroExpansions.add(loc);
        return PROCESS_SKIP;
      }
    }
    return PROCESS_CONTINUE;
  }

  private static Collection<IASTMacroExpansionLocation> getMacroExpansionLocations(IASTName name) {
    List<IASTMacroExpansionLocation> macroExpansions = list();

    for (IASTNodeLocation loc : name.getNodeLocations()) {
      if (!(loc instanceof IASTMacroExpansionLocation)) {
        continue;
      }

      macroExpansions.add((IASTMacroExpansionLocation) loc);
    }
    return macroExpansions;
  }

  private static String getMacroName(IASTMacroExpansionLocation expansionLoc) {
    return expansionLoc.getExpansion().getMacroDefinition().getName().toString();
  }
}
