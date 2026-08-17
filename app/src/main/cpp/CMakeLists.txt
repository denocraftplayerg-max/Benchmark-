cmake_minimum_required(VERSION 3.10.2)
project("anglebenchmark")

# Caminho correto: subir 3 níveis (cpp -> main -> src -> app)
set(ANGLE_LIBS_DIR ${CMAKE_SOURCE_DIR}/../../../libs/arm64-v8a)

add_library(benchmark_engine SHARED benchmark_engine.cpp)

# Linkar bibliotecas do sistema
find_library(log-lib log)
find_library(egl-lib EGL)
find_library(gles-lib GLESv3)

# Linkar bibliotecas ANGLE customizadas
add_library(angle_egl SHARED IMPORTED)
set_target_properties(angle_egl PROPERTIES IMPORTED_LOCATION ${ANGLE_LIBS_DIR}/libEGL_angle.so)

add_library(angle_gles2 SHARED IMPORTED)
set_target_properties(angle_gles2 PROPERTIES IMPORTED_LOCATION ${ANGLE_LIBS_DIR}/libGLESv2_angle.so)

target_link_libraries(benchmark_engine
    angle_egl
    angle_gles2
    ${log-lib}
    ${egl-lib}
    ${gles-lib}
)
