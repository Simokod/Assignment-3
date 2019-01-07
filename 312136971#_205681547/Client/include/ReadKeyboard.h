#ifndef ASSIGNMENT3_READKEYBOARD_H
#define ASSIGNMENT3_READKEYBOARD_H

#include "BGSEncoder.h"
#include "ConnectionHandler.h"
using namespace std;

class ReadKeyboard {
private:
    std::string input;
    BGSEncoderDecoder encdec;
    ConnectionHandler *handler;
public:
    explicit ReadKeyboard(ConnectionHandler *handler);
    void operator()();
};

#endif
