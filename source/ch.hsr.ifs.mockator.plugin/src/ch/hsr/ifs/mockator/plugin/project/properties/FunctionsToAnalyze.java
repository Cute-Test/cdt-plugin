package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


public enum FunctionsToAnalyze implements PropertyTypeWithDefault {

   AllFunctions(I18N.AllFunctionsDesc) {

   @Override
   public boolean shouldConsider(IASTFunctionDefinition function) {
      return true;
   }

   @Override
   public boolean isDefault() {
      return false;
   }
   },
   OnlyTestFunctions(I18N.TestFunctionsDesc) {

   @Override
   public boolean shouldConsider(IASTFunctionDefinition function) {
      return isNiladicFunction(function);
   }

   @Override
   public boolean isDefault() {
      return true;
   }
   };

   private static final Map<String, FunctionsToAnalyze> STRING_TO_ENUM = unorderedMap();

   static {
      for (FunctionsToAnalyze standard : values()) {
         STRING_TO_ENUM.put(standard.name(), standard);
      }
   }

   public abstract boolean shouldConsider(IASTFunctionDefinition function);

   public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID, "FunctionsToAnalyze");

   private final String description;

   private FunctionsToAnalyze(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public static FunctionsToAnalyze getDefault() {
      return DefaultPropertyHandler.getDefault(FunctionsToAnalyze.class);
   }

   public static FunctionsToAnalyze fromProjectSettings(IProject project) {
      String functionsToAnalyze = new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

      if (functionsToAnalyze == null) return getDefault();

      return fromName(functionsToAnalyze);
   }

   public static void storeInProjectSettings(IProject project, FunctionsToAnalyze functionsToAnalyze) {
      new ProjectPropertiesHandler(project).setProjectProperty(QF_NAME, functionsToAnalyze.toString());
   }

   public static FunctionsToAnalyze fromName(String name) {
      FunctionsToAnalyze result = STRING_TO_ENUM.get(name);
      Assert.notNull(result, String.format("Unkown function strategy '%s'", name));
      return result;
   }

   // Niladic: zero arguments (see Robert C. Martin's Clean Code book)
   private static boolean isNiladicFunction(IASTFunctionDefinition function) {
      IASTFunctionDeclarator funDecl = function.getDeclarator();

      if (!(funDecl instanceof ICPPASTFunctionDeclarator)) return false;

      ICPPASTFunctionDeclarator cppFunDecl = (ICPPASTFunctionDeclarator) funDecl;
      return cppFunDecl.getParameters() == null || cppFunDecl.getParameters().length == 0;
   }

}
