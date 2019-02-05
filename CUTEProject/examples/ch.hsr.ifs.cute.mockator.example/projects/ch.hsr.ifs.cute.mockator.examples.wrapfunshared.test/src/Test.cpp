#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"
#include "Die.h"


struct GameFourWins {
	void play(std::ostream& os = std::cout) {
		if (die.roll() == 4) {
			os << "You won!" << std::endl;
		} else {
			os << "You lost!" << std::endl;
		}
	}
private:
	Die die;
};

void testGameFourWins() {
	std::ostringstream oss;
	GameFourWins game;
	game.play(oss);
	ASSERT_EQUAL("You won!\n", oss.str());
}

void runSuite() {
	cute::suite s;
	s.push_back(CUTE(testGameFourWins));
	cute::ide_listener<> lis;
	cute::makeRunner(lis)(s, "The Suite");
}

int main() {
	runSuite();
	return 0;
}
