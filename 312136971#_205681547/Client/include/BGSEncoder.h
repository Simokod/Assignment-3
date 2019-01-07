#ifndef ASSIGNMENT3_BGSENCODERDECODER_H
#define ASSIGNMENT3_BGSENCODERDECODER_H
#include <string>
#include <vector>
#include <iostream>
#include <codecvt>
#include <locale>
using namespace std;

enum msgType {REGISTER, LOGIN, LOGOUT, FOLLOW, POST, PM, USERLIST, STAT, NOTIFICATION, ACK, ERROR };

class BGSEncoderDecoder{
private:
    vector<char> bytesEnc;

    vector<char> &encodeRegister(string &line);
    vector<char> &encodeLogin(string &line);
    vector<char> &encodeLogout(string &line);
    vector<char> &encodeFollow(string &line);
    vector<char> &encodePost(string &line);
    vector<char> &encodePM(string &line);
    vector<char> &encodeUserlist(string &line);
    vector<char> &encodeStat(string &line);

    void shortToBytes (short num, char* bytesArr);
    void getOpCodeBytes(short opCode);
    void getBytes(string &str);
    msgType getENum(string &type);
public:
    vector<char> encode(string &line);


};
#endif //ASSIGNMENT3_BGSENCODERDECODER_H
