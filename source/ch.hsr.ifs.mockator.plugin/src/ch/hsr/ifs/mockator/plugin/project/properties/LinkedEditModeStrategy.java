package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


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

   private static final Map<String, LinkedEditModeStrategy> STRING_TO_ENUM = unorderedMap();

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
      Assert.notNull(result, String.format("Unknown linked edit strategy '%s'", name));
      return result;
   }

   public static LinkedEditModeStrategy getDefault() {
      return DefaultPropertyHandler.getDefault(LinkedEditModeStrategy.class);
   }
}
