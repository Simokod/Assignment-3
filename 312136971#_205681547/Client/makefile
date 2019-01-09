CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread -lboost_locale -lboost_thread

all: BGSclient
	gcc -o bin/BGSClient bin/ConnectionHandler.o bin/BGSEncoder.o bin/ReadSocket bin/ReadKeyboard.o bin/BGSClient.o  $(LDFLAGS)

BGSclient: bin/BGSClient bin/ConnectionHandler.o bin/BGSEncoder.o bin/ReadSocket bin/ReadKeyboard.o bin/BGSClient.o
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	gcc $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/BGSClient.o: src/BGSClient.cpp
	gcc $(CFLAGS) -o bin/BGSClient.o src/BGSClient.cpp

bin/ReadKeyboard.o: src/ReadKeyboard.cpp
	gcc $(CFLAGS) -o bin/ReadKeyboard.o src/ReadKeyboard.cpp

bin/ReadSocket.o: src/ReadSocket.cpp
	gcc $(CFLAGS) -o bin/ReadSocket.o src/ReadSocket.cpp

bin/BGSEncoder.o: src/BGSEncoder.cpp
	gcc $(CFLAGS) -o bin/BGSEncoder.o src/BGSEncoder.cpp
	
.PHONY: clean
clean:
	rm -f bin/*