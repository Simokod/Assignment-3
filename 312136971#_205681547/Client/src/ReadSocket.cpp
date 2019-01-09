
#include "ReadSocket.h"

ReadSocket::ReadSocket(ConnectionHandler &connectionHandler, mutex &mutex):
                                    _handler(connectionHandler), _mutex(mutex) {}

void ReadSocket::operator()(){
    while(!_handler.ShouldTerminate())
    {
        char opBytes[2];
        if(!_handler.getBytes(opBytes, 2)) {
            _handler.terminate();
            break;
        }
        switch (decodeOpCode(opBytes)){
            case NOTIFICATION:
                if(!decodeNotification())
                    _handler.terminate();
                break;
            case ACK:
                if(!decodeACK())
                    _handler.terminate();
                break;
            case ERROR:
                if(!decodeError())
                    _handler.terminate();
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
    if(!_handler.getBytes(&type, 1))
        return false;
    string msgType;
    if(type == '0')
        msgType = "PM";
    else
        msgType = "Public";
    // reading posting user
    string postingUser;
    if(!_handler.getFrameAscii(postingUser, '\0'))
        return false;
    // reading content
    string content;
    if(!_handler.getFrameAscii(content, '\0'))
        return false;

    cout << "NOTIFICATION " + msgType + " " + postingUser + " " + content << endl;
    return true;
}

bool ReadSocket::decodeError() {
    char msgOpCode[2];
    if(!_handler.getBytes(msgOpCode, 2))
        return false;
    cout << "ERROR" + to_string(msgOpCode[0]) + to_string(msgOpCode[1]) << endl;
    return true;
}

bool ReadSocket::decodeACK() {
    char msgOpCode[2];
    if(!_handler.getBytes(msgOpCode, 2))
        return false;
    switch(decodeOpCode(msgOpCode)){
        case REGISTER:
            cout << "registered successfully!" << endl;
            return true;

        case LOGOUT:
            cout << "logged out" << endl;
            return false;

        case FOLLOW:
            return decodeACKFollowOrUserlist(4);

        case USERLIST:
            return decodeACKFollowOrUserlist(7);

        case STAT:
            return decodeACKStat();
        default: return true;
    }
}

bool ReadSocket::decodeACKFollowOrUserlist(short opCode) {
    char numOfUsersBytes[2];
    if(!_handler.getBytes(numOfUsersBytes,2))
        return false;
    short numOfUsers = bytesToShort(numOfUsersBytes);

    string userList;
    for(int i=0; i < numOfUsers; i++)
    {
        string user;
        if(!_handler.getFrameAscii(user,'\0'))
            _handler.terminate();
        userList += user + " ";
    }
    cout << "ACK " + to_string(opCode) + " " + userList << endl;
    return true;
}

bool ReadSocket::decodeACKStat() {
    char bytes[2];
    if(!_handler.getBytes(bytes,2))
        return false;
    short numPosts = bytesToShort(bytes);

    if(!_handler.getBytes(bytes,2))
        return false;
    short numFollowers = bytesToShort(bytes);

    if(!_handler.getBytes(bytes,2))
        return false;
    short numFollowing = bytesToShort(bytes);

    cout << "ACK " << "8 " + to_string(numPosts) + " " + to_string(numFollowers) + " " + to_string(numFollowing) << endl;
    return true;
}

