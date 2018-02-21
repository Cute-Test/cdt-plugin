package ch.hsr.ifs.mockator.plugin.project.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


public enum FunctionsToAnalyze implements PropertyTypeWithDefault {

   AllFunctions(I18N.AllFunctionsDesc) {

   @Override
   public boolean shouldConsider(final IASTFunctionDefinition function) {
      return true;
   }

   @Override
   public boolean isDefault() {
      return false;
   }
   },
   OnlyTestFunctions(I18N.TestFunctionsDesc) {

   @Override
   public boolean shouldConsider(final IASTFunctionDefinition function) {
      return isNiladicFunction(function);
   }

   @Override
   public boolean isDefault() {
      return true;
   }
   };

   private static final Map<String, FunctionsToAnalyze> STRING_TO_ENUM = new HashMap<>();

   static {
      for (final FunctionsToAnalyze standard : values()) {
         STRING_TO_ENUM.put(standard.name(), standard);
      }
   }

   public abstract boolean shouldConsider(IASTFunctionDefinition function);

   public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID, "FunctionsToAnalyze");

   private final String description;

   private FunctionsToAnalyze(final String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public static FunctionsToAnalyze getDefault() {
      return DefaultPropertyHandler.getDefault(FunctionsToAnalyze.class);
   }

   public static FunctionsToAnalyze fromProjectSettings(final IProject project) {
      final String functionsToAnalyze = new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

      if (functionsToAnalyze == null) return getDefault();

      return fromName(functionsToAnalyze);
   }

   public static void storeInProjectSettings(final IProject project, final FunctionsToAnalyze functionsToAnalyze) {
      new ProjectPropertiesHandler(project).setProjectProperty(QF_NAME, functionsToAnalyze.toString());
   }

   public static FunctionsToAnalyze fromName(final String name) {
      final FunctionsToAnalyze result = STRING_TO_ENUM.get(name);
      ILTISException.Unless.notNull(result, String.format("Unkown function strategy '%s'", name));
      return result;
   }

   // Niladic: zero arguments (see Robert C. Martin's Clean Code book)
   private static boolean isNiladicFunction(final IASTFunctionDefinition function) {
      final IASTFunctionDeclarator funDecl = function.getDeclarator();

      if (!(funDecl instanceof ICPPASTFunctionDeclarator)) return false;

      final ICPPASTFunctionDeclarator cppFunDecl = (ICPPASTFunctionDeclarator) funDecl;
      return cppFunDecl.getParameters() == null || cppFunDecl.getParameters().length == 0;
   }

}
