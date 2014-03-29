package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;

public enum MarkMissingMemFuns implements PropertyTypeWithDefault {

  AllMemFuns(I18N.AllMemFuns) {
    @Override
    public boolean isDefault() {
      return true;
    }
  },
  OnlyReferencedFromTest(I18N.OnlyReferencedFromTest) {
    @Override
    public boolean isDefault() {
      return false;
    }
  };

  private static final Map<String, MarkMissingMemFuns> STRING_TO_ENUM = unorderedMap();

  static {
    for (MarkMissingMemFuns standard : values()) {
      STRING_TO_ENUM.put(standard.name(), standard);
    }
  }

  public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID,
      "MarkMissingMemFuns");

  private final String description;

  private MarkMissingMemFuns(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static MarkMissingMemFuns getDefault() {
    return DefaultPropertyHandler.getDefault(MarkMissingMemFuns.class);
  }

  public static MarkMissingMemFuns fromProjectSettings(IProject project) {
    String markMissingMemFunStrategy =
        new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

    if (markMissingMemFunStrategy == null)
      return getDefault();

    return fromName(markMissingMemFunStrategy);
  }

  public static void storeInProjectSettings(IProject project, MarkMissingMemFuns markMissingMemFuns) {
    new ProjectPropertiesHandler(project)
        .setProjectProperty(QF_NAME, markMissingMemFuns.toString());
  }

  public static MarkMissingMemFuns fromName(String name) {
    MarkMissingMemFuns result = STRING_TO_ENUM.get(name);
    Assert.notNull(result, String.format("Unknown mark missing memfun strategy '%s'", name));
    return result;
  }
}
