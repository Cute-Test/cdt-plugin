package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class IncludeDirectiveCollector implements F1V<ExtractInterfaceContext> {

   @Override
   public void apply(ExtractInterfaceContext c) {
      IASTPreprocessorIncludeStatement[] includes = c.getTuOfChosenClass().getIncludeDirectives();
      c.setIncludes(list(includes));
   }
}
