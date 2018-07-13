#include "draw_funs.h"
#include "crossplanefigure.h"

void CrossPlaneFigure::rerender() {
	// draw the label
	drawText(m_nX, m_nY, m_pchLabel, getClipLen());
	drawLine(m_nX, m_nY, m_nX + getClipLen(), m_nY);
	drawLine(m_nX, m_nY, m_nX, m_nY + getDropLen());
	if (!m_bShadowBox) {
		drawLine(m_nX + getClipLen(), m_nY, m_nX + getClipLen(),
				m_nY + getDropLen());
		drawLine(m_nX, m_nY + getDropLen(), m_nX + getClipLen(),
				m_nY + getDropLen());
	}
	// draw the figure
	for (size_t n = 0; n < edges.size(); n++) {
		//...
	}
	//...
}
