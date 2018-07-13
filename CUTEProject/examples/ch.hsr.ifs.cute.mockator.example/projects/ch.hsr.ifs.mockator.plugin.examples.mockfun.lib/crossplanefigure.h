#ifndef CROSSPLANEFIGURE_H_
#define CROSSPLANEFIGURE_H_
#include <string>
#include <vector>

struct CrossPlaneFigure {
	CrossPlaneFigure() : m_nX(10), m_nY(10), m_bShadowBox(false), m_pchLabel("Mockator") {
	}
	void rerender();
private:
	int getClipLen() {return 2;}
	int getDropLen() {return int();}
	int m_nX;
	int m_nY;
	bool m_bShadowBox;
	std::string m_pchLabel;
	std::vector<int> edges;
};

#endif /* CROSSPLANEFIGURE_H_ */
