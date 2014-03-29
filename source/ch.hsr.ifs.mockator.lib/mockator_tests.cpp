#include "mockator/mockator.h"
#include "cute.h"
#include "cute_runner.h"
#include "ostream_listener.h"
#include "ide_listener.h"
#include "cute_counting_listener.h"
#include "file_output_listener.h"

namespace {
	using namespace mockator;

	void puts_call_content_to_given_stream() {
		call c("foo(int i)", 3);
		std::ostringstream os;
		os << c;
		ASSERT_EQUAL("foo(int i),3", os.str());
	}

	void call_trace_is_accessible() {
		call c("foo(std::string s, double d)", "mockator", 3.1415);
		ASSERT_EQUAL("foo(std::string s, double d),mockator,3.1415", c.getTrace());
	}

	void puts_call_vector_content_to_given_stream() {
#if defined(USE_STD11)
		calls cc = { {"foo(int i)", 42}, {"bar(char c)", 'x'} };
#else
		INIT_MOCKATOR();
		calls cc;
		cc +=   call("foo(int i)", 42),
				call("bar(char c)", 'x');
#endif
		std::ostringstream os;
		os << cc;
		ASSERT_EQUAL("[\nfoo(int i),42\nbar(char c),x\n]", os.str());
	}

	void object_equality_based_on_funsig_and_params() {
		call c1("foo(std::string s, double d)", "mockator", 3.1415);
		call c2("foo(std::string s, double d)", "mockator", 3.1415);
		call c3("foo(std::string s, double d)", "isolator", 3.1415);
		ASSERT_EQUAL(c1, c2);
		ASSERT(not (c2 == c3));
	}

	void object_equality_order_independent() {
#if defined(USE_STD11)
		calls cc1 = {
				{"foo(int i)", 42},
				{"bar(char c)", 'x'},
				{"foo(std::string s, double d)", "mockator", 3.1415}
		};
		calls cc2 = {
				{"bar(char c)", 'x'},
				{"foo(std::string s, double d)", "mockator", 3.1415},
				{"foo(int i)", 42}
		};
#else
		INIT_MOCKATOR();
		calls cc1;
		cc1 +=  call("foo(int i)", 42),
				call("bar(char c)", 'x'),
				call("foo(std::string s, double d)", "mockator", 3.1415);
		calls cc2;
		cc2 +=  call("bar(char c)", 'x'),
				call("foo(std::string s, double d)", "mockator", 3.1415),
				call("foo(int i)", 42);
#endif

		ASSERT_ANY_ORDER(cc1, cc2);
		assert(equalsAnyOrder(cc1, cc2));
	}

	struct Person {
		unsigned int age;
	};

	void warning_when_user_class_without_stream_op() {
		Person person = {42};
		call c("foo(Person p)", person);
		std::string expected = "foo(Person p),operator<< not defined for type (anonymous namespace)::Person";
		ASSERT_EQUAL(expected, c);
	}

	void flatten_with_empty_std_map() {
		std::map<std::string,std::string> m;
		call c("foo(std::map<std::string, std::string> m)", m);
		std::string expected = "foo(std::map<std::string, std::string> m),std::map<std::string, std::string, "
				"std::less<std::string>, std::allocator<std::pair<std::string const, std::string> > >{}";
		ASSERT_EQUAL(expected, c);
	}

	void flatten_with_filled_std_map() {
		std::map<std::string, int> m;
		m["hugo"] = 24;
		m["eva"] = 45;
		m["paul"] = 35;
		call c("foo(std::map<std::string, int> m)", m);
		std::string expected = "foo(std::map<std::string, int> m),std::map<std::string, int, std::less<std::string>, "
				"std::allocator<std::pair<std::string const, int> > >{"
				"\n[eva -> 45],\n[hugo -> 24],\n[paul -> 35]}";
		ASSERT_EQUAL(expected, c);
	}

	void flatten_with_std_pair() {
		std::pair<std::string,int> theAnswer("answer", 42);
		call c("foo(std::pair<std::string,int> p)", theAnswer);
		std::string expected = "foo(std::pair<std::string,int> p),[answer -> 42]";
		ASSERT_EQUAL(expected, c);
	}

	void flatten_with_vector_pair() {
		std::vector<std::pair<int, int> > v;
		v.push_back(std::make_pair(42, 3));
		call c("foo(std::vector<std::pair<int, int>> v)", v);
		std::string expected = "foo(std::vector<std::pair<int, int>> v),std::vector<std::pair<int, int>, "
				"std::allocator<std::pair<int, int> > >{\n[42 -> 3]}";
		ASSERT_EQUAL(expected, c);
	}

	void flatten_with_empty_vector_set_int() {
		std::vector<std::set<int> > v;
		call c("foo(std::vector<std::set<int>> v)", v);
		std::string expected = "foo(std::vector<std::set<int>> v),std::vector<std::set<int, std::less<int>, "
				"std::allocator<int> >, std::allocator<std::set<int, std::less<int>, std::allocator<int> > > >{}";
		ASSERT_EQUAL(expected, c);
	}

	struct Share {
		std::string symbol;
	};

	std::ostream& operator<<(std::ostream& os, Share const* s) {
		return os << s->symbol;
	}

	void considers_provided_user_class_stream_op() {
		const Share share = {"FB"};
		INIT_MOCKATOR();

#if defined(USE_STD11)
		calls cc = { {"foo(Share* s)", &share} };
#else
		calls cc;
		cc += call("foo(Share* s)", &share);
#endif

		std::ostringstream os;
		os << cc;
		ASSERT_EQUAL("[\nfoo(Share* s),FB\n]", os.str());
	}

	namespace mock_Ns {
		template<typename STOCKEXCHANGE>
		struct Trader {
			void sell(std::string const& symbol, unsigned int amount) {
				STOCKEXCHANGE s;
				Share share = {symbol};
				s.sell(&share, amount);
			}
		};

		INIT_MOCKATOR()
		std::vector<calls> allCalls(1);
		struct MockExchange {
			MockExchange() : mock_id(reserveNextCallId(allCalls)) {
				allCalls[mock_id].push_back(call("MockExchange()"));
			}

			void sell(Share const* share, unsigned int amount) const {
				allCalls[mock_id].push_back(call("sell(Share const*, unsigned int) const", share, amount));
			}
			const int mock_id;
		};
	}

	void with_real_mock_object() {
		using namespace mock_Ns;
		Trader<MockExchange> trader;
		trader.sell("FB", 1000000);
#if defined(USE_STD11)
		calls expected = { {"MockExchange()"}, {"sell(Share const*, unsigned int) const", "FB", 1000000} };
#else
		calls expected;
		expected += call("MockExchange()"), call("sell(Share const*, unsigned int) const", "FB", 1000000);
#endif
		ASSERT_EQUAL(expected, mock_Ns::allCalls[1]);
	}

	const std::string regex = "^^sell(Share const\\*, unsigned int) const,[A-Z]\\{2\\},[0-9]\\{1,10\\}$";

	void with_matching_regex() {
		using namespace mock_Ns;
		Trader<MockExchange> trader;
		trader.sell("FB", 1000000);
#if defined(USE_STD11)
		calls expected = {{"MockExchange()"}, {regex}};
#else
		calls expected;
		expected += call("MockExchange()"), call(regex);
#endif
		ASSERT_MATCHES(expected, mock_Ns::allCalls[2]);
	}

	void non_matching_regex_should_yield_exception() {
		using namespace mock_Ns;
		Trader<MockExchange> trader;
		trader.sell("F", 1000000);
#if defined(USE_STD11)
		calls expected = {{"MockExchange()"}, {regex}};
#else
		calls expected;
		expected += call("MockExchange()"), call(regex);
#endif
		ASSERT_THROWS(ASSERT_MATCHES(expected, mock_Ns::allCalls[3]), mockator::RegexMatchingFailure);
	}
}

void runSuite(char **argv) {
	//prepare test suite
	cute::suite s;
	s.push_back(CUTE(puts_call_content_to_given_stream));
	s.push_back(CUTE(call_trace_is_accessible));
	s.push_back(CUTE(puts_call_vector_content_to_given_stream));
	s.push_back(CUTE(object_equality_based_on_funsig_and_params));
	s.push_back(CUTE(object_equality_order_independent));
	s.push_back(CUTE(warning_when_user_class_without_stream_op));
	s.push_back(CUTE(considers_provided_user_class_stream_op));
	s.push_back(CUTE(flatten_with_empty_std_map));
	s.push_back(CUTE(flatten_with_filled_std_map));
	s.push_back(CUTE(flatten_with_std_pair));
	s.push_back(CUTE(flatten_with_vector_pair));
	s.push_back(CUTE(flatten_with_empty_vector_set_int));
	s.push_back(CUTE(with_real_mock_object));
	s.push_back(CUTE(with_matching_regex));
	s.push_back(CUTE(non_matching_regex_should_yield_exception));

	//run tests
	std::string kind(argv[1]);
	cute::file_output_listener<cute::ide_listener> lis(kind);
	std::string runnerInfo = "Mockator Tests for " + kind;
	cute::makeRunner(lis)(s, runnerInfo.c_str());
}

int main(int argc, char **argv) {
	if (argc != 2) {
		std::cerr << "usage: " << argv[0] << " [C++03 | C++11]\n";
		return 1;
	}

	runSuite(argv);
}
