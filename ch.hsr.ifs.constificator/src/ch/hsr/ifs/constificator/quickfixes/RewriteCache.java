package ch.hsr.ifs.constificator.quickfixes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.ltk.core.refactoring.Change;

public class RewriteCache implements Iterable<Change>{

	private final Map<IASTTranslationUnit, ASTRewrite> m_cache = new HashMap<>();

	private class ChangeIterator implements Iterator<Change> {

		private final Iterator<ASTRewrite> m_cacheIterator;

		public ChangeIterator() {
			m_cacheIterator = m_cache.values().iterator();
		}

		@Override
		public boolean hasNext() {
			return m_cacheIterator.hasNext();
		}

		@Override
		public Change next() {
			ASTRewrite next = m_cacheIterator.next();
			return next.rewriteAST();
		}

	}

	public ASTRewrite get(IASTTranslationUnit tu) {
		ASTRewrite rewrite;

		if(m_cache.containsKey(tu)) {
			rewrite =  m_cache.get(tu);
		} else {
			rewrite = ASTRewrite.create(tu);
			m_cache.put(tu, rewrite);
		}

		return rewrite;
	}

	@Override
	public Iterator<Change> iterator() {
		return new ChangeIterator();
	}

}
