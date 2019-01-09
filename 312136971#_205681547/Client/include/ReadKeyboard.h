#ifndef ASSIGNMENT3_READKEYBOARD_H
#define ASSIGNMENT3_READKEYBOARD_H

#include "BGSEncoder.h"
#include "ConnectionHandler.h"
#include <mutex>


class ReadKeyboard {
private:
    std::string _input;
    BGSEncoder _encdec;
    ConnectionHandler &_handler;
    std::mutex &_mutex;
public:
    ReadKeyboard(ConnectionHandler &handler, std::mutex &mutex);
    void operator()();
};

#endif
