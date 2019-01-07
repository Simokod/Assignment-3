
#include <include/ReadSocket.h>

#include "ReadSocket.h"

ReadSocket::ReadSocket(ConnectionHandler *connectionHandler): handler(connectionHandler) {

}

void ReadSocket::operator()(){
    while(!handler->ShouldTerminate())
    {
        char opBytes[2];
        if(!handler->getBytes(opBytes, 2)) {
            handler->terminate();
            break;
        }
        switch (decodeOpCode(opBytes)){
            case NOTIFICATION:
                if(!decodeNotification())
                    handler->terminate();
                break;
            case ACK:
                if(!decodeACK())
                    handler->terminate();
                break;
            case ERROR:
                if(!decodeError())
                    handler->terminate();
                break;
            default:;
        }
    }
}

msgType ReadSocket::decodeOpCode(char* bytes) {
    short op=bytesToShort(bytes);
    switch(op){
        case 9: return NOTIFICATION;
        case 10: return ACK;
        default: return ERROR;
    }
}
short ReadSocket::bytesToShort(char* bytesArr){
    short result=(short)((bytesArr[0]&0xff)<<8);
    result+=(short)(bytesArr[1]&0xff);
    return result;

}

bool ReadSocket::decodeNotification() {
    char type;
    if(!handler->getBytes(&type, 1)) {
        handler->terminate();
    }
    string msgType;
    if(type == '0')
        msgType = "PM";
    else
        msgType = "Public";
    // reading posting user
    string postingUser;
    if(!handler->getFrameAscii(postingUser, '\0')) {
        handler->terminate();
    }
    // reading content
    string content;
    if(!handler->getFrameAscii(content, '\0')) {
        handler->terminate();
    }

    cout << "NOTIFICATION " + msgType + " " + postingUser + " " + content << endl;
    }

bool ReadSocket::decodeError() {
    char msgOpCode[2];
    if(!handler->getBytes(msgOpCode, 2)) {
        handler->terminate();
    }
    cout << "ERROR" + to_string(msgOpCode[0]) + to_string(msgOpCode[1]) << endl;
}

bool ReadSocket::decodeACK() {
    char msgOpCode[2];
    if(!handler->getBytes(msgOpCode, 2)) {
        handler->terminate();
    }
    switch(decodeOpCode(msgOpCode)){
        case REGISTER:
            cout << "registered successfully!" << endl;
            break;

        case LOGOUT:
            cout << "logged out" << endl;
            handler->terminate();
            break;

        case FOLLOW:
            decodeACKFollowOrUserlist(4);
            break;

        case USERLIST:
            decodeACKFollowOrUserlist(7);
            break;

        case STAT:
            decodeACKStat();
            break;
        default:;
    }
}

void ReadSocket::decodeACKFollowOrUserlist(short opCode) {
    char numOfUsersBytes[2];
    if(!handler->getBytes(numOfUsersBytes,2))
        handler->terminate();
    short numOfUsers = bytesToShort(numOfUsersBytes);

    string userList;
    for(int i=0; i < numOfUsers; i++)
    {
        string user;
        if(!handler->getFrameAscii(user,'\0'))
            handler->terminate();
        userList += user + " ";
    }
    cout << "ACK " + to_string(opCode) + " " + userList << endl;
}

void ReadSocket::decodeACKStat() {
    char bytes[2];
    if(!handler->getBytes(bytes,2))
        handler->terminate();
    short numPosts = bytesToShort(bytes);

    if(!handler->getBytes(bytes,2))
        handler->terminate();
    short numFollowers = bytesToShort(bytes);

    if(!handler->getBytes(bytes,2))
        handler->terminate();
    short numFollowing = bytesToShort(bytes);

    cout << "ACK " << "8 " + to_string(numPosts) + " " + to_string(numFollowers) + " " + to_string(numFollowing) << endl;
}

