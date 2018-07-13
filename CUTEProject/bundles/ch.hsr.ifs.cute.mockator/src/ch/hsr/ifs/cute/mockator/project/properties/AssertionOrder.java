package ch.hsr.ifs.cute.mockator.project.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.MockatorPlugin;
import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;


public enum AssertionOrder implements PropertyTypeWithDefault {

   OrderDependent(I18N.OrderDependentDesc) {

   @Override
   public String getAssertionCommand() {
      return MockatorConstants.CUTE_ASSERT_EQUAL;
   }

   @Override
   public LinkedEditModeStrategy getLinkedEditModeStrategy(final IProject project) {
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
   public LinkedEditModeStrategy getLinkedEditModeStrategy(final IProject project) {
      return LinkedEditModeStrategy.ChooseArguments;
   }

   @Override
   public boolean isDefault() {
      return false;
   }
   };

   private static final Map<String, AssertionOrder> STRING_TO_ENUM = new HashMap<>();

   static {
      for (final AssertionOrder order : values()) {
         STRING_TO_ENUM.put(order.name(), order);
      }
   }

   public abstract String getAssertionCommand();

   public abstract LinkedEditModeStrategy getLinkedEditModeStrategy(IProject project);

   public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID, "AssertionOrder");

   private final String description;

   private AssertionOrder(final String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public static AssertionOrder getDefault() {
      return DefaultPropertyHandler.getDefault(AssertionOrder.class);
   }

   public static AssertionOrder fromProjectSettings(final IProject project) {
      final String assertionOrder = new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

      if (assertionOrder == null) return getDefault();

      return fromName(assertionOrder);
   }

   public static void storeInProjectSettings(final IProject project, final AssertionOrder order) {
      new ProjectPropertiesHandler(project).setProjectProperty(QF_NAME, order.toString());
   }

   public static AssertionOrder fromName(final String name) {
      final AssertionOrder result = STRING_TO_ENUM.get(name);
      ILTISException.Unless.notNull(String.format("Unknown assertion order strategy '%s'", name), result);
      return result;
   }
}
