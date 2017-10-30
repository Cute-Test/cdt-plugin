package ch.hsr.ifs.mockator.plugin.extractinterface.context;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.base.misc.Builder;


@SuppressWarnings("restriction")
public final class ExtractInterfaceContext {

   private final ICProject                              cProject;
   private final ITextSelection                         selection;
   private final ITranslationUnit                       tuOfSelection;
   private CRefactoringContext                          refContext;
   private IProgressMonitor                             pm;
   private Collection<IASTDeclaration>                  chosenFunctions;
   private Collection<IASTDeclaration>                  availablePublicMemFuns;
   private Collection<IASTDeclaration>                  usedPublicMemFuns;
   private String                                       newInterfaceNameProposal;
   private IASTTranslationUnit                          tuOfChosenClass;
   private IASTTranslationUnit                          tuOfInterface;
   private ModificationCollector                        collector;
   private String                                       chosenInterfaceName;
   private RefactoringStatus                            status;
   private boolean                                      replaceAllOccurences;
   private Collection<IASTPreprocessorIncludeStatement> includes;
   private Collection<IASTSimpleDeclaration>            classFwdDecls;
   private Collection<IASTSimpleDeclaration>            typeDefDecls;
   private ICPPASTCompositeTypeSpecifier                chosenClass;
   private ICPPASTCompositeTypeSpecifier                sutClass;
   private String                                       interfaceFilePath;
   private IASTName                                     selectedName;

   public static class ContextBuilder implements Builder<ExtractInterfaceContext> {

      private final ICProject        cProject;
      private final ITextSelection   selection;
      private final ITranslationUnit tuOfSelection;
      private RefactoringStatus      status;
      private boolean                replaceAllOccurences;
      private CRefactoringContext    context;
      private String                 newInterfaceName;

      public ContextBuilder(final ITranslationUnit tuOfSelection, final ICProject cProject, final ITextSelection selection) {
         this.tuOfSelection = tuOfSelection;
         this.cProject = cProject;
         this.selection = selection;
      }

      public ContextBuilder withRefactoringStatus(final RefactoringStatus status) {
         this.status = status;
         return this;
      }

      public ContextBuilder replaceAllOccurences(final boolean doIt) {
         replaceAllOccurences = doIt;
         return this;
      }

      public ContextBuilder withRefactoringContext(final CRefactoringContext context) {
         this.context = context;
         return this;
      }

      public ContextBuilder withNewInterfaceName(final String newInterfaceName) {
         this.newInterfaceName = newInterfaceName;
         return this;
      }

      @Override
      public ExtractInterfaceContext build() {
         return new ExtractInterfaceContext(this);
      }
   }

   private ExtractInterfaceContext(final ContextBuilder builder) {
      cProject = builder.cProject;
      selection = builder.selection;
      status = builder.status;
      tuOfSelection = builder.tuOfSelection;
      refContext = builder.context;
      chosenInterfaceName = builder.newInterfaceName;
      replaceAllOccurences = builder.replaceAllOccurences;
      chosenFunctions = list();
      availablePublicMemFuns = list();
      usedPublicMemFuns = list();
   }

   public void setSelectedName(final Optional<IASTName> name) {
      OptHelper.doIfPresentElse(name, (oName) -> selectedName = oName, () -> status.addFatalError("A valid name has to be selected!"));
   }

   public ITranslationUnit getTuOfSelection() {
      return tuOfSelection;
   }

   public IASTName getSelectedName() {
      return selectedName;
   }

   public ICPPASTCompositeTypeSpecifier getChosenClass() {
      return chosenClass;
   }

   public void setChosenClass(final ICPPASTCompositeTypeSpecifier klass) {
      chosenClass = klass;
   }

   public ICPPASTCompositeTypeSpecifier getSutClass() {
      return sutClass;
   }

   public void setSutClass(final ICPPASTCompositeTypeSpecifier sutClass) {
      this.sutClass = sutClass;
   }

   public Collection<IASTDeclaration> getChosenMemFuns() {
      return chosenFunctions;
   }

   public void setChosenMemFuns(final Collection<IASTDeclaration> funs) {
      chosenFunctions = funs;
   }

   public String getNewInterfaceNameProposal() {
      return newInterfaceNameProposal;
   }

   public void setNewInterfaceNameProposal(final String name) {
      newInterfaceNameProposal = name;
   }

   public Collection<IASTDeclaration> getAvailablePupMemFuns() {
      return availablePublicMemFuns;
   }

   public void setAvailablePubMemFuns(final Collection<IASTDeclaration> publicMemFuns) {
      availablePublicMemFuns = publicMemFuns;
   }

   public Collection<IASTDeclaration> getUsedPublicMemFuns() {
      return usedPublicMemFuns;
   }

   public void setUsedPublicMemFuns(final Collection<IASTDeclaration> publicMemFuns) {
      usedPublicMemFuns = publicMemFuns;
   }

   public void setTuOfChosenClass(final IASTTranslationUnit tu) {
      tuOfChosenClass = tu;
   }

   public IASTTranslationUnit getTuOfChosenClass() {
      return tuOfChosenClass;
   }

   public IASTTranslationUnit getTuOfInterface() {
      return tuOfInterface;
   }

   public void setTuOfInterface(final IASTTranslationUnit tu) {
      tuOfInterface = tu;
   }

   public void setModificationCollector(final ModificationCollector collector) {
      this.collector = collector;
   }

   public ASTRewrite getRewriterFor(final IASTTranslationUnit tu) {
      return collector.rewriterForTranslationUnit(tu);
   }

   public String getNewInterfaceName() {
      return chosenInterfaceName;
   }

   public void setNewInterfaceName(final String interfaceName) {
      chosenInterfaceName = interfaceName;
   }

   public CRefactoringContext getCRefContext() {
      return refContext;
   }

   public void setCRefContext(final CRefactoringContext refContext) {
      this.refContext = refContext;
   }

   public ICProject getCProject() {
      return cProject;
   }

   public void setStatus(final RefactoringStatus status) {
      this.status = status;
   }

   public RefactoringStatus getStatus() {
      return status;
   }

   public boolean shouldReplaceAllOccurences() {
      return replaceAllOccurences;
   }

   public void setShouldReplaceAllOccurences(final boolean replaceAllOccurences) {
      this.replaceAllOccurences = replaceAllOccurences;
   }

   public void setIncludes(final Collection<IASTPreprocessorIncludeStatement> includes) {
      this.includes = includes;
   }

   public Collection<IASTPreprocessorIncludeStatement> getIncludes() {
      return includes;
   }

   public void setClassFwdDecls(final Collection<IASTSimpleDeclaration> fwdDecls) {
      classFwdDecls = fwdDecls;
   }

   public Collection<IASTSimpleDeclaration> getClassFwdDecls() {
      return classFwdDecls;
   }

   public void setTypeDefDecls(final Collection<IASTSimpleDeclaration> typeDefDecls) {
      this.typeDefDecls = typeDefDecls;
   }

   public Collection<IASTSimpleDeclaration> getTypeDefDecls() {
      return typeDefDecls;
   }

   public IProgressMonitor getProgressMonitor() {
      return pm;
   }

   public void setProgressMonitor(final IProgressMonitor pm) {
      this.pm = pm;
   }

   public void setInterfaceFilePath(final String interfaceFilePath) {
      this.interfaceFilePath = interfaceFilePath;
   }

   public String getInterfaceFilePath() {
      return interfaceFilePath;
   }

   public ITextSelection getSelection() {
      return selection;
   }
}
