package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class IncludeDirectiveCollector implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext c) {
      final IASTPreprocessorIncludeStatement[] includes = c.getTuOfChosenClass().getIncludeDirectives();
      c.setIncludes(list(includes));
   }
}
