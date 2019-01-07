#include <iostream>
#include <ConnectionHandler.h>
#include "ReadKeyboard.h"
#include "ReadSocket.h"
#include <thread>
using namespace std;



int main (int argc, char *argv[]) {
    if (argc < 3) {
        cerr << "Usage: " << argv[0] << " host port" << endl << endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler handler(host, port);
    if (!handler.connect()) {
        cerr << "Cannot connect to " << host << ":" << port << endl;
        return 1;
    }

    ReadSocket socketReader(&handler);
    ReadKeyboard keyboardReader(&handler);

    thread keyboardThread(ref(keyboardReader));
    thread socketThread(ref(socketReader));

    keyboardThread.join();
    socketThread.join();

    return  0;
}
