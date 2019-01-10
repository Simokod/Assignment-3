#ifndef ASSIGNMENT3_READSOCKET_H
#define ASSIGNMENT3_READSOCKET_H

#include "ConnectionHandler.h"
#include "BGSEncoder.h"


using namespace std;


class ReadSocket {
private:
    ConnectionHandler &_handler;

    msgType decodeOpCode(char* bytes);
    short bytesToShort(char* bytesArr);
    bool decodeNotification();
    bool decodeError();
    bool decodeACK();
    bool decodeACKFollowOrUserlist(short opCode);
    bool decodeACKStat();

public:
    ReadSocket(ConnectionHandler &handler);
    void operator()();

    void sendDisconnect();
};


#endif
