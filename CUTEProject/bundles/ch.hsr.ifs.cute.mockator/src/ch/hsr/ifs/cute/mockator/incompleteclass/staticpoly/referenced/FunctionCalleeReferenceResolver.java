package ch.hsr.ifs.cute.mockator.incompleteclass.staticpoly.referenced;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.head;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
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

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.refsupport.lookup.NodeLookup;


// Code partially taken from org.eclipse.cdt.internal.ui.callhierarchy.CHQueries
// Does not work properly for references made through dependent names because
// the CDT parser does not actually instantiate the function body to resolve names
// therein at this point. See CDT Bugs 326070 and 332430.
@SuppressWarnings("restriction")
class FunctionCalleeReferenceResolver {

    private final IIndex    index;
    private final ICProject cProject;

    public FunctionCalleeReferenceResolver(final IIndex index, final ICProject cProject) {
        this.index = index;
        this.cProject = cProject;
    }

    public Collection<IASTName> findCallers(final IBinding binding, final IASTNode point) {
        ILTISException.Unless.notNull("Binding must not be null", binding);
        try {
            final List<IASTName> callers = new ArrayList<>();
            findCallersRecursively(binding, callers, point);
            return callers;
        } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private void findCallersRecursively(final IBinding binding, final List<IASTName> callers, final IASTNode point) throws CoreException {
        final CalledByResult result = new CalledByResult();
        findCalledBy(binding, result, point);
        final List<ICElement> elements = result.getElements();
        if (elements.isEmpty()) {
            return;
        }

        final IIndexBinding calleeBinding = IndexUI.elementToBinding(index, elements.get(0), ILinkage.CPP_LINKAGE_ID);
        findDeclaration(calleeBinding).ifPresent((name) -> callers.add(name));

        findCallersRecursively(calleeBinding, callers, point);
    }

    private Optional<IASTName> findDeclaration(final IIndexBinding calleeBinding) {
        final NodeLookup nodeLookup = new NodeLookup(cProject, new NullProgressMonitor());
        return head(nodeLookup.findDeclarations(calleeBinding, index));
    }

    private void findCalledBy(final IBinding calleeBinding, final CalledByResult result, final IASTNode point) throws CoreException {
        findCalledBy1(calleeBinding, true, result, point);
        if (!(calleeBinding instanceof ICPPMethod)) {
            return;
        }
        for (final IBinding overridden : findOverriders(calleeBinding)) {
            findCalledBy1(overridden, false, result, point);
        }
    }

    private ICPPMethod[] findOverriders(final IBinding calleeBinding) {
        return ClassTypeHelper.findOverridden((ICPPMethod) calleeBinding);
    }

    private void findCalledBy1(final IBinding callee, final boolean includeOrdinaryCalls, final CalledByResult result, final IASTNode point)
            throws CoreException {
        findCalledBy2(callee, includeOrdinaryCalls, result);
        for (final IBinding spec : IndexUI.findSpecializations(index, callee)) {
            findCalledBy2(spec, includeOrdinaryCalls, result);
        }
    }

    private void findCalledBy2(final IBinding callee, final boolean includeOrdinaryCalls, final CalledByResult result) throws CoreException {
        for (final IIndexName rname : findReferences(callee)) {
            if (includeOrdinaryCalls || rname.couldBePolymorphicMethodCall()) {
                final IIndexName caller = rname.getEnclosingDefinition();
                if (caller == null) {
                    continue;
                }
                final ICElement elem = getICElement(caller);
                if (elem != null) {
                    result.add(elem, rname);
                }
            }
        }
    }

    private ICElementHandle getICElement(final IIndexName caller) throws CoreException {
        return IndexUI.getCElementForName(cProject, index, caller);
    }

    private IIndexName[] findReferences(final IBinding callee) throws CoreException {
        return index.findReferences(callee);
    }

    private static class CalledByResult {

        private final Map<ICElement, List<IIndexName>> elementReferences;

        public CalledByResult() {
            elementReferences = new HashMap<>();
        }

        public List<ICElement> getElements() {
            return new ArrayList<>(elementReferences.keySet());
        }

        public void add(final ICElement elem, final IIndexName ref) {
            List<IIndexName> list = elementReferences.get(elem);
            if (list == null) {
                list = new ArrayList<>();
                elementReferences.put(elem, list);
            }
            list.add(ref);
        }
    }
}
