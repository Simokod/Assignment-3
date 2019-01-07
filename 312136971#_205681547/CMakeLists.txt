cmake_minimum_required(VERSION 3.12)
project(312136971__205681547)

set(CMAKE_CXX_STANDARD 14)

include_directories(Client/include)

add_executable(312136971__205681547
        Client/include/BGSEncoder.h
        Client/include/ConnectionHandler.h
    Client/include/ReadKeyboard.h
    Client/include/ReadSocket.h
        Client/src/BGSEncoder.cpp
        Client/src/ConnectionHandler.cpp
    Client/src/echoClient.cpp
    Client/src/ReadKeyboard.cpp
    Client/src/ReadSocket.cpp)
