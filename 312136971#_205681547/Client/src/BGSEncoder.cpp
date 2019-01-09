#include <string>
#include <vector>
#include <iostream>
#include <codecvt>
#include <locale>
#include "BGSEncoder.h"
using namespace std;

//      Encoders
vector<char> *BGSEncoder::encode (string &line) {
    string type = line.substr(0, line.find(' '));
    line = line.substr(line.find(' ')+1, line.length()-1);       // trimming the command from the input for easier access
    switch (getENum(type)) {
        case REGISTER: return encodeRegister(line);
        case LOGIN: return encodeLogin(line);
        case LOGOUT: return encodeLogout(line);
        case FOLLOW: return encodeFollow(line);
        case POST: return encodePost(line);
        case PM: return encodePM(line);
        case USERLIST: return encodeUserlist(line);
        case STAT: return encodeStat(line);
        default: return nullptr;
    }
}
//      Register
vector<char> *BGSEncoder::encodeRegister(string &line) {
    getOpCodeBytes(1);

    string userName = line.substr(0, line.find(' ')+1);
    getStringBytes(userName);
    bytesEnc->push_back('\0');

    string password = line.substr(line.find(' ')+1);
    getStringBytes(password);
    bytesEnc->push_back('\0');

    return bytesEnc;
}
//      Login
vector<char> *BGSEncoder::encodeLogin(string &line) {
    getOpCodeBytes(2);

    string userName = line.substr(0, line.find(' ')+1);
    getStringBytes(userName);
    bytesEnc->push_back('\0');

    string password = line.substr(line.find(' ')+1);
    getStringBytes(password);
    bytesEnc->push_back('\0');

    return bytesEnc;
}
//      Logout
vector<char> *BGSEncoder::encodeLogout(string &line) {
    getOpCodeBytes(3);
    return bytesEnc;
}
//      Follow
vector<char> *BGSEncoder::encodeFollow(string &line) {
    getOpCodeBytes(4);

    string followUn = line.substr(0,1);
    getStringBytes(followUn);

    line = line.substr(1);
    string numOfFollows = line.substr(0,line.find(' '));
    getStringBytes(numOfFollows);
    bytesEnc->pop_back();

    line = line.substr(line.find(' ')+1);
    for(int i=0; i<stoi(numOfFollows); i++)
    {
        string user = line.substr(0, line.find(' '));
        line = line.substr(line.find(' ')+1);
        getStringBytes(user);
        bytesEnc->push_back('\0');
    }
    return bytesEnc;
}
//      Post
vector<char> *BGSEncoder::encodePost(string &line) {
    getOpCodeBytes(5);
    getStringBytes(line);
    bytesEnc->push_back('\0');
    return bytesEnc;
}
//      PM
vector<char> *BGSEncoder::encodePM(string &line) {
    getOpCodeBytes(6);
    string userName= line.substr(0, line.find(' '));
    getStringBytes(userName);
    bytesEnc->push_back('\0');

    string message=line.substr(line.find(' ')+1);
    getStringBytes(message);
    bytesEnc->push_back('\0');
    return bytesEnc;
}
//      UserList
vector<char> *BGSEncoder::encodeUserlist(string &line) {
    getOpCodeBytes(7);
    return bytesEnc;
}
//      Stat
vector<char> *BGSEncoder::encodeStat(string &line) {
    getOpCodeBytes(8);
    getStringBytes(line);
    bytesEnc->push_back('\0');
    return bytesEnc;
}

//// private methods
void BGSEncoder::shortToBytes (short num, char* bytesArr){
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}
//// adding the bytes of the opCode to bytes
void BGSEncoder::getOpCodeBytes(short opCode) {
    char opBytes[2];
    shortToBytes(opCode, opBytes);
    (*bytesEnc)[0] = opBytes[0];
    (*bytesEnc)[1] = opBytes[1];
}
//// encoding a string to bytes and adding it to the bytes vector
void BGSEncoder::getStringBytes(string &str) {
    vector<char> bytes(str.begin(), str.end());
    bytesEnc->insert(bytesEnc->end(), bytes.begin(), bytes.end());
}
//// returning the corresponding enum
msgType BGSEncoder::getENum(string &type){
    if(type == "REGISTER")
        return REGISTER;
    if(type == "LOGIN")
        return LOGIN;
    if(type == "LOGOUT")
        return LOGOUT;
    if(type == "FOLLOW")
        return FOLLOW;
    if(type == "POST")
        return POST;
    if(type == "PM")
        return PM;
    if(type == "USERLIST")
        return USERLIST;
    if(type == "STAT")
        return STAT;
    return UNDEFINED;
}