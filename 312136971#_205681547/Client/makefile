cmake_minimum_required(VERSION 3.13)
project(client)

set(CMAKE_CXX_STANDARD 14)


find_package(Boost COMPONENTS system filesystem thread REQUIRED)
IF (Boost_FOUND)
    include_directories(${Boost_INCLUDE_DIR})
endif()

SET(CMAKE_CXX_FLAGS -pthread)
SET(CMAKE_CXX_FLAGS -lboost_thread)

include_directories(include)
add_executable(client
        include/BGSEncoder.h
        include/ConnectionHandler.h
        include/ReadKeyboard.h
        include/ReadSocket.h
        src/BGSEncoder.cpp
        src/client.cpp
        src/ConnectionHandler.cpp
        src/ReadKeyboard.cpp
        src/ReadSocket.cpp
        makefile)

target_link_libraries( client LINK_PUBLIC ${Boost_LIBRARIES} )
