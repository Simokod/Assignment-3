cmake_minimum_required(VERSION 3.13)
project(Assignment3)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)
include_directories(bin)
include_directories(include)
include_directories(src)

add_executable(Assignment3
        include/BGSEncoder.h
        include/ConnectionHandler.h
        include/ReadKeyboard.h
        include/ReadSocket.h
        src/BGSEncoder.cpp
        src/BGSClient.cpp
        src/ConnectionHandler.cpp
        src/ReadKeyboard.cpp
        src/ReadSocket.cpp
        makefile )
