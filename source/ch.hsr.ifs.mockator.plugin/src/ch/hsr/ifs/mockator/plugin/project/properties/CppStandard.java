package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.CompilerFlagHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.DiscoveryOptionsHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;

@SuppressWarnings("restriction")
public enum CppStandard implements PropertyTypeWithDefault {

  Cpp03Std("C++03", I18N.Cpp03Desc) {
    @Override
    public void toggleCppStdSupport(IProject project) {
      String cpp11ExperimentalFlag = getCpp11ExperimentalFlag(project);
      new CompilerFlagHandler(project).removeCompilerFlag(cpp11ExperimentalFlag);
      new DiscoveryOptionsHandler(project).removeCpp11Support();
    }

    @Override
    public Pattern getInitializerPattern() {
      return CPP03_INITIALIZER_PATTERN;
    }

    @Override
    public IASTLiteralExpression getNullPtr() {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
    }

    @Override
    public IASTInitializerClause getCtorCall(String ctorName, Collection<IASTInitializerClause> args) {
      IASTName newCtorName = nodeFactory.newName(ctorName.toCharArray());
      IASTIdExpression ctorCall = nodeFactory.newIdExpression(newCtorName);
      IASTInitializerClause[] newArgs = args.toArray(new IASTInitializerClause[args.size()]);
      return nodeFactory.newFunctionCallExpression(ctorCall, newArgs);
    }

    @Override
    public IASTInitializer getEmptyInitializer() {
      return nodeFactory.newConstructorInitializer(new IASTInitializerClause[] {});
    }

    @Override
    public IASTInitializer getInitializer(IASTInitializerClause clause) {
      return nodeFactory.newConstructorInitializer(new IASTInitializerClause[] {clause});
    }

    @Override
    public Pattern getFunExpectationsPattern() {
      return CPP03_FUN_EXPECTATION_PATTERN;
    }

    @Override
    public Pattern getAllFunArgsPattern() {
      return CPP03_ALL_FUN_ARGS_PATTERN;
    }

    @Override
    public boolean isDefault() {
      return false;
    }

    @Override
    public String getExpectationDelimiter() {
      return ")";
    }
  },
  Cpp11Std("C++11", I18N.Cpp11Desc) {
    @Override
    public void toggleCppStdSupport(IProject project) {
      new CompilerFlagHandler(project).addCompilerFlag(getCpp11ExperimentalFlag(project));
      new DiscoveryOptionsHandler(project).addCpp11Support();
    }

    @Override
    public Pattern getInitializerPattern() {
      return CPP11_INITIALIZER_PATTERN;
    }

    @Override
    public IASTLiteralExpression getNullPtr() {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, NULLPTR);
    }

    @Override
    public IASTInitializer getEmptyInitializer() {
      return nodeFactory.newInitializerList();
    }

    @Override
    public IASTInitializer getInitializer(IASTInitializerClause clause) {
      ICPPASTInitializerList initializerList = nodeFactory.newInitializerList();
      initializerList.addClause(clause);
      return initializerList;
    }

    @Override
    public IASTInitializerClause getCtorCall(String ctorName, Collection<IASTInitializerClause> args) {
      IASTName newCtorName = nodeFactory.newName(ctorName.toCharArray());
      ICPPASTNamedTypeSpecifier nameSpec = nodeFactory.newTypedefNameSpecifier(newCtorName);
      ICPPASTInitializerList newArgs = nodeFactory.newInitializerList();
      for (IASTInitializerClause arg : args) {
        newArgs.addClause(arg);
      }
      return nodeFactory.newSimpleTypeConstructorExpression(nameSpec, newArgs);
    }

    @Override
    public Pattern getFunExpectationsPattern() {
      return CPP11_FUN_EXPECTATION_PATTERN;
    }

    @Override
    public Pattern getAllFunArgsPattern() {
      return CPP11_ALL_FUN_ARGS_PATTERN;
    }

    @Override
    public boolean isDefault() {
      return true;
    }

    @Override
    public String getExpectationDelimiter() {
      return "}";
    }
  };

  public abstract void toggleCppStdSupport(IProject project);

  public abstract IASTLiteralExpression getNullPtr();

  public abstract IASTInitializer getEmptyInitializer();

  public abstract IASTInitializer getInitializer(IASTInitializerClause clause);

  public abstract IASTInitializerClause getCtorCall(String ctorName,
      Collection<IASTInitializerClause> args);

  public abstract Pattern getInitializerPattern();

  public abstract Pattern getFunExpectationsPattern();

  public abstract Pattern getAllFunArgsPattern();

  public abstract String getExpectationDelimiter();

  public static final QualifiedName QF_NAME = new QualifiedName(MockatorPlugin.PLUGIN_ID, "C++Std");

  private static Pattern CPP11_INITIALIZER_PATTERN = Pattern.compile("\\{\\s*(\"[^\"]*\")\\s*\\}");
  private static Pattern CPP03_INITIALIZER_PATTERN = Pattern.compile("\\(\\s*(\".*?\")\\s*\\)");

  private static final Pattern CPP11_FUN_EXPECTATION_PATTERN = Pattern
      .compile("([\\s\\S]*?)(?:\"\\s*\\}(\\s*,\\s*)?|\\}\\s*\\}(\\s*,\\s*)?)");
  private static final Pattern CPP03_FUN_EXPECTATION_PATTERN = Pattern
      .compile("([\\s\\S]*?)(?:\"\\s*\\)(\\s*,\\s*)?|\\)\\s*\\)(\\s*,\\s*)?)");

  private static final Pattern CPP11_ALL_FUN_ARGS_PATTERN = Pattern
      .compile("\"\\s*,\\s*([\\s\\S]*?\\})\\s*\\}");
  private static final Pattern CPP03_ALL_FUN_ARGS_PATTERN = Pattern
      .compile("\"\\s*,\\s*([\\s\\S]*?\\))\\s*\\)");

  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private static final String NULLPTR = "nullptr";
  private static final Map<String, CppStandard> STRING_TO_ENUM = unorderedMap();

  static {
    for (CppStandard standard : values()) {
      STRING_TO_ENUM.put(standard.toString(), standard);
    }
  }

  private final String stdAbbrv;
  private final String description;

  private CppStandard(String stdAbbrv, String description) {
    this.stdAbbrv = stdAbbrv;
    this.description = description;
  }

  @Override
  public String toString() {
    return stdAbbrv;
  }

  public String getDescription() {
    return description;
  }

  public static CppStandard getDefaultCppStd() {
    return DefaultPropertyHandler.getDefault(CppStandard.class);
  }

  public static CppStandard fromCompilerFlags(IProject project) {
    String cppCompilerFlags = new CompilerFlagHandler(project).getCompilerFlags();
    String cpp11ExperimentalFlag = getCpp11ExperimentalFlag(project);

    if (cppCompilerFlags == null || cpp11ExperimentalFlag == null)
      return getDefaultCppStd();

    return cppCompilerFlags.contains(cpp11ExperimentalFlag) ? Cpp11Std : Cpp03Std;
  }

  public static void storeInProjectSettings(IProject project, CppStandard cppStd) {
    new ProjectPropertiesHandler(project).setProjectProperty(QF_NAME, cppStd.toString());
    cppStd.toggleCppStdSupport(project);
  }

  public static CppStandard fromProjectSettings(IProject project) {
    String cppStd = new ProjectPropertiesHandler(project).getProjectProperty(QF_NAME);

    if (cppStd == null)
      return getDefaultCppStd();

    return fromName(cppStd);
  }

  public static CppStandard fromName(String name) {
    CppStandard result = STRING_TO_ENUM.get(name);
    Assert.notNull(result, String.format("Unknown C++ Standard '%s'", name));
    return result;
  }

  private static String getCpp11ExperimentalFlag(IProject project) {
    for (ToolChain optTc : ToolChain.fromProject(project))
      return optTc.getCdtProjectVariables().getCpp11ExperimentalFlag();

    return null;
  }
}
