
#include "ReadSocket.h"

ReadSocket::ReadSocket(ConnectionHandler &connectionHandler:
                                    _handler(connectionHandler) {}

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
        case 1: return REGISTER;
        case 2: return LOGIN;
        case 3: return LOGOUT;
        case 4: return FOLLOW;
        case 5: return POST;
        case 6: return PM;
        case 7: return USERLIST;
        case 8: return STAT;
        case 9: return NOTIFICATION;
        case 10: return ACK;
        case 11: return ERROR;
        default: return UNDEFINED;
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
    cout << "ERROR " + to_string(msgOpCode[0]) + to_string(msgOpCode[1]) << endl;
    return true;
}

bool ReadSocket::decodeACK() {
    char msgOpCode[2];
    if(!_handler.getBytes(msgOpCode, 2))
        return false;

    switch(decodeOpCode(msgOpCode)){
        case REGISTER:
            cout << "ACK 1" << endl;
            return true;
        case LOGIN:
            cout << "ACK 2" << endl;
            return true;
        case LOGOUT:
            cout << "ACK 3" << endl;
            sendDisconnect();
            return false;
        case POST:
            cout << "ACK 5" << endl;
            return true;
        case PM:
            cout << "ACK 6" << endl;
            return true;
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
        user="";
    }
    cout << "ACK " + to_string(opCode) + " " + to_string(numOfUsers) + " " + userList << endl;
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

    cout << "ACK 8 " + to_string(numPosts) + " " + to_string(numFollowers) + " " + to_string(numFollowing) << endl;
    return true;
}
// sends the server a signal when the client disconnetcs
void ReadSocket::sendDisconnect() {
    short num = 99;
    char logoutBytes[2];
    logoutBytes[0] = ((num >> 8) & 0xFF);
    logoutBytes[1] = (num & 0xFF);
    cout << "sending" << endl;//TODO
    _handler.sendBytes(logoutBytes, 2);
}

