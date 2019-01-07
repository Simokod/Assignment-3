#ifndef ASSIGNMENT3_READSOCKET_H
#define ASSIGNMENT3_READSOCKET_H

#include "ConnectionHandler.h"
#include "BGSEncoder.h"

using namespace std;


class ReadSocket {
private:
    ConnectionHandler *handler;

    msgType decodeOpCode(char* bytes);
    short bytesToShort(char* bytesArr);
    bool decodeNotification();
    bool decodeError();
    bool decodeACK();
    void decodeACKFollowOrUserlist(short opCode);
    void decodeACKStat();

public:
    explicit ReadSocket(ConnectionHandler *handler);
    void operator()();
};


#endif
