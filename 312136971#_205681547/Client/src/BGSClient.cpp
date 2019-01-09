#include <iostream>
#include <thread>
#include <mutex>
#include <stdlib.h>

#include "ConnectionHandler.h"
#include "ReadKeyboard.h"
#include "ReadSocket.h"

using namespace std;

int main (int argc, char *argv[]) {
    if (argc < 3) {
        cerr << "Usage: " << argv[0] << " host port" << endl << endl;
        return -1;
    }
    cout << "connected to server" << endl;
    string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler handler(host, port);
    if (!handler.connect()) {
        cerr << "Cannot connect to " << host << ":" << port << endl;
        return 1;
    }
    mutex mutex;

    ReadSocket socketReader(handler, mutex);
    ReadKeyboard keyboardReader(handler, mutex);

    thread keyboardThread(&ReadKeyboard::operator(), keyboardReader);
    thread socketThread(&ReadSocket::operator(), socketReader);

    keyboardThread.join();
    socketThread.join();

    return  0;
}
