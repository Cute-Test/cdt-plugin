package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


public enum AssertionOrder implements PropertyTypeWithDefault {

   OrderDependent(I18N.OrderDependentDesc) {

   @Override
   public String getAssertionCommand() {
      return MockatorConstants.CUTE_ASSERT_EQUAL;
   }

   @Override
   public LinkedEditModeStrategy getLinkedEditModeStrategy(IProject project) {
      return LinkedEditModeStrategy.fromProjectSettings(project);
   }

   @Override
   public boolean isDefault() {
      return true;
   }
   },
   OrderIndependent(I18N.OrderIndependentDesc) {

   @Override
   public String getAssertionCommand() {
      return MockatorConstants.ASSERT_ANY_ORDER;
   }

   @Override
   public LinkedEditModeStrategy getLinkedEditModeStrategy(IProject project) {
      return LinkedEditModeStrategy.ChooseArguments;
   }

   @Override
   public boolean isDefault() {
      return false;
   }
   };

   private static final Map<String, AssertionOrder> STRING_TO_ENUM = unorderedMap();

   static {
      for (AssertionOrder order : values()) {
         STRING_TO_ENUM.put(order.name(), order);
      }
   }

   public abstract String getAssertionCommand();

   public abstract LinkedEditModeStrategy getLinkedEditModeStrategy(IProject project);

   public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID, "AssertionOrder");

   private final String description;

   private AssertionOrder(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public static AssertionOrder getDefault() {
      return DefaultPropertyHandler.getDefault(AssertionOrder.class);
   }

   public static AssertionOrder fromProjectSettings(IProject project) {
      String assertionOrder = new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

      if (assertionOrder == null) return getDefault();

      return fromName(assertionOrder);
   }

   public static void storeInProjectSettings(IProject project, AssertionOrder order) {
      new ProjectPropertiesHandler(project).setProjectProperty(QF_NAME, order.toString());
   }

   public static AssertionOrder fromName(String name) {
      AssertionOrder result = STRING_TO_ENUM.get(name);
      Assert.notNull(result, String.format("Unknown assertion order strategy '%s'", name));
      return result;
   }
}
