package ch.hsr.ifs.templator.plugin.viewdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGeneratorWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import ch.hsr.ifs.templator.plugin.asttools.data.ResolvedName;

public class FindNodeRegionsVisitor extends ChangeGeneratorWriterVisitor {

	private List<ResolvedName> subNames;

	private Map<Integer, IRegion> foundRegions = new HashMap<Integer, IRegion>();

	private ASTWriter writer = new ASTWriter();

	public FindNodeRegionsVisitor(List<ResolvedName> subNames) {
		super(new ASTModificationStore(), null, new NodeCommentMap());
		this.subNames = subNames;
	}

	@Override
	public int visit(IASTName name) {

		processNode(name);

		return super.visit(name);
	}

	private void processNode(IASTName name) {

		// TODO: here, some performance optimizations could be done
		for (int i = 0; i < subNames.size(); i++) {

			IASTName nameToTest = subNames.get(i).getOriginalName();
			if (name.getOriginalNode().equals(nameToTest)) {

				String nameString = writer.write(name);

				int offset = scribe.toString().length();
				int length = nameString.length();

				IRegion region = new Region(offset, length);

				foundRegions.put(i, region);
				break;
			}
		}
	}

	public Map<Integer, IRegion> getNodeRegions() {
		return foundRegions;
	}
}
