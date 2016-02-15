package ch.hsr.ifs.constificator.core.util.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;

public class DOM {

	private static class TranslationUnitCache {

		private static final Map<ITranslationUnit, IASTTranslationUnit> m_cache = new HashMap<>();

		private IASTTranslationUnit get(IIndexFileLocation file, IIndex index, ICProject project) throws CoreException {
			ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(file, project);
			return get(tu, index);
		}

		private IASTTranslationUnit get(ITranslationUnit tu, IIndex index) throws CoreException {
			IASTTranslationUnit ast = null;

			if (m_cache.containsKey(tu) && m_cache.get(tu).getOriginatingTranslationUnit().isConsistent()) {
				ast = m_cache.get(tu);
			} else {
				try {
					index.acquireReadLock();
					ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
					index.releaseReadLock();
					m_cache.put(tu, ast);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return ast;
		}

		private void purge(IASTTranslationUnit ast) {
			Iterator<Entry<ITranslationUnit, IASTTranslationUnit>> iterator = m_cache.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ITranslationUnit, IASTTranslationUnit> current = iterator.next();

				if (current.getValue().equals(ast)) {
					iterator.remove();
				}
			}
		}

	}

	private static TranslationUnitCache m_cache = new TranslationUnitCache();

	public static <T> Set<T> resolveBindingToNodeSet(Class<T> type, IBinding binding, IIndex index, ICProject project) {
		Set<T> nodes = new HashSet<>();

		try {
			IIndexName[] declarations = index.findNames(binding, IIndex.FIND_DECLARATIONS_DEFINITIONS);

			for (IIndexName declaration : declarations) {
				IIndexFileLocation file = declaration.getFile().getLocation();
				IASTTranslationUnit ast = m_cache.get(file, index, project);
				IASTName name = ast.getNodeSelector(null).findName(declaration.getNodeOffset(),
						declaration.getNodeLength());

				T node;
				if ((node = Relation.getAncestorOf(type, name)) != null) {
					nodes.add(node);
				}
			}
		} catch (CoreException e) {

		}

		return nodes;
	}

	public static void changed(IASTTranslationUnit ast) {
		m_cache.purge(ast);
	}

}
