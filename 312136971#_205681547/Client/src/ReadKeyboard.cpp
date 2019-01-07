#include <string>
#include <iostream>
#include <include/ReadKeyboard.h>

#include "ReadKeyboard.h"
#include "BGSEncoder.cpp"
#include "ConnectionHandler.h"
using namespace std;

ReadKeyboard::ReadKeyboard(ConnectionHandler *connectionHandler): handler(connectionHandler) {

}

void ReadKeyboard::operator()(){
    while(!handler->ShouldTerminate()) {
        cin >> input;
        vector<char> bytes = encdec.encode(input);
        int size = (int) bytes.size();
        char bytesArr[size];
        std::copy(bytes.begin(), bytes.end(), bytesArr);
        bytes.clear();

        if (!handler->sendBytes(bytesArr, size)) {
            handler->terminate();
            break;
        }
    }
}
