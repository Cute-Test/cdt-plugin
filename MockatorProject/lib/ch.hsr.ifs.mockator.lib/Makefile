CC = g++
CXXFLAGS = -Wall -Wextra -Werror -ansi -pedantic
LDFLAGS = -lboost_regex

ifeq ($(USE_STD11), 1)
	CXXFLAGS += -std=c++11
endif


all: mockator_tests
		  
mockator_tests: mockator_tests.o
	$(CC) -o $@ $^ $(LDFLAGS)

mockator_tests.o: mockator_tests.cpp mockator/mockator.h
	$(CC) $(CXXFLAGS) -isystem CUTE/cute/ -c -o $@ $<
	
clean:
	rm -f mockator_tests mockator_tests.o test_results*.xml

.PHONY: all clean
