#include <string>
#include <iostream>

#include "ReadKeyboard.h"
#include "BGSEncoder.h"
#include "ConnectionHandler.h"
using namespace std;

ReadKeyboard::ReadKeyboard(ConnectionHandler &connectionHandler): _input(), _encdec(),
                        _handler(connectionHandler) {}

void ReadKeyboard::operator()(){
    while(!_handler.ShouldTerminate()) {
        const short bufsize = 1024;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string line(buf);

        vector<char> bytes = _encdec.encode(line);

        int size = (int) bytes.size();
        char bytesArr[size];
        copy(bytes.begin(), bytes.end(), bytesArr);

        if (!_handler.sendBytes(bytesArr, size)) {
            _handler.terminate();
            break;
        }
    }
}
