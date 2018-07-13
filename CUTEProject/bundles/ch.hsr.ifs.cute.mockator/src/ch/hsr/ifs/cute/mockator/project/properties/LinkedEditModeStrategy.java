package ch.hsr.ifs.cute.mockator.project.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.cute.mockator.MockatorPlugin;
import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;


public enum LinkedEditModeStrategy implements PropertyTypeWithDefault {

   ChooseFunctions(I18N.ChooseFunctionsDesc) {

   @Override
   public boolean isDefault() {
      return false;
   }
   },
   ChooseArguments(I18N.ChooseArgumentsDesc) {

   @Override
   public boolean isDefault() {
      return true;
   }
   };

   private static final Map<String, LinkedEditModeStrategy> STRING_TO_ENUM = new HashMap<>();

   static {
      for (final LinkedEditModeStrategy order : values()) {
         STRING_TO_ENUM.put(order.name(), order);
      }
   }

   public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID, "LinkedEditModeStrategy");

   private final String description;

   private LinkedEditModeStrategy(final String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public static LinkedEditModeStrategy fromProjectSettings(final IProject project) {
      final String linkedEditStrategy = new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

      if (linkedEditStrategy == null) return getDefault();

      return fromName(linkedEditStrategy);
   }

   public static void storeInProjectSettings(final IProject project, final LinkedEditModeStrategy linkedEdit) {
      new ProjectPropertiesHandler(project).setProjectProperty(QF_NAME, linkedEdit.toString());
   }

   public static LinkedEditModeStrategy fromName(final String name) {
      final LinkedEditModeStrategy result = STRING_TO_ENUM.get(name);
      ILTISException.Unless.notNull(String.format("Unknown linked edit strategy '%s'", name), result);
      return result;
   }

   public static LinkedEditModeStrategy getDefault() {
      return DefaultPropertyHandler.getDefault(LinkedEditModeStrategy.class);
   }
}
