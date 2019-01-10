#ifndef ASSIGNMENT3_READKEYBOARD_H
#define ASSIGNMENT3_READKEYBOARD_H

#include "BGSEncoder.h"
#include "ConnectionHandler.h"


class ReadKeyboard {
private:
    std::string _input;
    BGSEncoder _encdec;
    ConnectionHandler &_handler;
public:
    ReadKeyboard(ConnectionHandler &handler);
    void operator()();
};

#endif
