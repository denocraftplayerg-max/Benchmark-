cmake_minimum_required(VERSION 3.22.1)
project("voxel_engine")

# Compila APENAS o motor voxel
add_library(voxel_engine SHARED VoxelEngine.cpp)

find_library(log-lib log)
find_library(gles-lib GLESv3)
find_library(egl-lib EGL)

target_link_libraries(voxel_engine ${log-lib} ${gles-lib} ${egl-lib})
