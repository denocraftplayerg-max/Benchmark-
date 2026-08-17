cmake_minimum_required(VERSION 3.22.1)
project("gpu_engine")

add_library(gpu_engine SHARED hardware_querier.cpp)

find_library(log-lib log)
find_library(gles-lib GLESv3)
find_library(egl-lib EGL)

target_link_libraries(gpu_engine ${log-lib} ${gles-lib} ${egl-lib})
