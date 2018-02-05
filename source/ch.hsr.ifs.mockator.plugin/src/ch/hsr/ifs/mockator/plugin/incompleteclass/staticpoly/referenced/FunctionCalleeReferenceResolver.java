package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.referenced;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;

// Code partially taken from org.eclipse.cdt.internal.ui.callhierarchy.CHQueries
// Does not work properly for references made through dependent names because
// the CDT parser does not actually instantiate the function body to resolve names
// therein at this point. See CDT Bugs 326070 and 332430.
@SuppressWarnings("restriction")
class FunctionCalleeReferenceResolver {
  private final IIndex index;
  private final ICProject cProject;

  public FunctionCalleeReferenceResolver(IIndex index, ICProject cProject) {
    this.index = index;
    this.cProject = cProject;
  }

  public Collection<IASTName> findCallers(IBinding binding) {
    Assert.notNull(binding, "Binding must not be null");
    try {
      List<IASTName> callers = new ArrayList<IASTName>();
      findCallersRecursively(binding, callers);
      return callers;
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private void findCallersRecursively(IBinding binding, List<IASTName> callers)
      throws CoreException {
    CalledByResult result = new CalledByResult();
    findCalledBy(binding, result);
    List<ICElement> elements = result.getElements();
    if (elements.isEmpty())
      return;
    IIndexBinding calleeBinding =
        IndexUI.elementToBinding(index, elements.get(0), ILinkage.CPP_LINKAGE_ID);
    for (IASTName optName : findDeclaration(calleeBinding)) {
      callers.add(optName);
    }
    findCallersRecursively(calleeBinding, callers);
  }

  private Maybe<IASTName> findDeclaration(IIndexBinding calleeBinding) {
    NodeLookup nodeLookup = new NodeLookup(cProject, new NullProgressMonitor());
    Collection<IASTName> declarations = nodeLookup.findDeclarations(calleeBinding, index);
    return head(declarations);
  }

  private void findCalledBy(IBinding calleeBinding, CalledByResult result) throws CoreException {
    findCalledBy1(calleeBinding, true, result);
    if (!(calleeBinding instanceof ICPPMethod))
      return;
    for (IBinding overridden : findOverriders(calleeBinding)) {
      findCalledBy1(overridden, false, result);
    }
  }

  private ICPPMethod[] findOverriders(IBinding calleeBinding) {
    return ClassTypeHelper.findOverridden((ICPPMethod) calleeBinding);
  }

  private void findCalledBy1(IBinding callee, boolean includeOrdinaryCalls, CalledByResult result)
      throws CoreException {
    findCalledBy2(callee, includeOrdinaryCalls, result);
    for (IBinding spec : IndexUI.findSpecializations(index, callee)) {
      findCalledBy2(spec, includeOrdinaryCalls, result);
    }
  }

  private void findCalledBy2(IBinding callee, boolean includeOrdinaryCalls, CalledByResult result)
      throws CoreException {
    for (IIndexName rname : findReferences(callee)) {
      if (includeOrdinaryCalls || rname.couldBePolymorphicMethodCall()) {
        IIndexName caller = rname.getEnclosingDefinition();
        if (caller == null) {
          continue;
        }
        ICElement elem = getICElement(caller);
        if (elem != null) {
          result.add(elem, rname);
        }
      }
    }
  }

  private ICElementHandle getICElement(IIndexName caller) throws CoreException {
    return IndexUI.getCElementForName(cProject, index, caller);
  }

  private IIndexName[] findReferences(IBinding callee) throws CoreException {
    return index.findReferences(callee);
  }

  private static class CalledByResult {
    private final Map<ICElement, List<IIndexName>> elementReferences;

    public CalledByResult() {
      elementReferences = new HashMap<ICElement, List<IIndexName>>();
    }

    public List<ICElement> getElements() {
      return new ArrayList<ICElement>(elementReferences.keySet());
    }

    public void add(ICElement elem, IIndexName ref) {
      List<IIndexName> list = elementReferences.get(elem);
      if (list == null) {
        list = new ArrayList<IIndexName>();
        elementReferences.put(elem, list);
      }
      list.add(ref);
    }
  }
}
