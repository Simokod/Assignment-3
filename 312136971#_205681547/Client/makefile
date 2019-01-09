CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread -lboost_locale -lboost_thread

all: BGSclient
	g++ -o bin/BGSclient bin/ConnectionHandler.o bin/BGSEncoder.o bin/ReadKeyboard.o bin/ReadSocket.o bin/BGSClient.o $(LDFLAGS)

BGSclient: bin/ConnectionHandler.o bin/BGSEncoder.o bin/ReadKeyboard.o bin/ReadSocket.o bin/BGSClient.o
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/BGSEncoder.o: src/BGSEncoder.cpp
	g++ $(CFLAGS) -o bin/BGSEncoder.o src/BGSEncoder.cpp

bin/ReadKeyboard.o: src/ReadKeyboard.cpp
	g++ $(CFLAGS) -o bin/ReadKeyboard.o src/ReadKeyboard.cpp

bin/ReadSocket.o: src/ReadSocket.cpp
	g++ $(CFLAGS) -o bin/ReadSocket.o src/ReadSocket.cpp

bin/BGSClient.o: src/BGSClient.cpp
	g++ $(CFLAGS) -o bin/BGSClient.o src/BGSClient.cpp

.PHONY: clean
clean:
	rm -f bin/*