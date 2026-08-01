cmake_minimum_required(VERSION 3.22.1)
project("wavelauncher")

add_library(wavelauncher SHARED native-lib.cpp)

find_library(log-lib log)
find_library(gles-lib GLESv2)

target_link_libraries(wavelauncher ${log-lib} ${gles-lib})
