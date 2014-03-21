package ch.hsr.ifs.cute.macronator.quickassist;

import org.eclipse.cdt.core.dom.ast.IASTName;

public interface RunnableCallback {

    void setSelectedName(IASTName macro);
}
