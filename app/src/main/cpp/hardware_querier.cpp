#include <jni.h>
#include <string>
#include <GLES3/gl3.h>
#include <android/log.h>

#define LOG_TAG "GPUDevStudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_gpu_devstudio_engine_HardwareQuerier_getGLESInfoNative(JNIEnv* env, jobject /* this */) {
    // Forçar um contexto mínimo se necessário, mas glGetString funciona se houver contexto ativo na thread
    const char* vendor = (const char*)glGetString(GL_VENDOR);
    const char* renderer = (const char*)glGetString(GL_RENDERER);
    const char* version = (const char*)glGetString(GL_VERSION);
    const char* glsl_version = (const char*)glGetString(GL_SHADING_LANGUAGE_VERSION);
    
    GLint max_texture_size = 0;
    glGetIntegerv(GL_MAX_TEXTURE_SIZE, &max_texture_size);
    
    GLint max_viewport_dims[2] = {0, 0};
    glGetIntegerv(GL_MAX_VIEWPORT_DIMS, max_viewport_dims);

    std::string info = "VENDOR: " + std::string(vendor ? vendor : "Unknown") + "\n" +
                       "RENDERER: " + std::string(renderer ? renderer : "Unknown") + "\n" +
                       "VERSION: " + std::string(version ? version : "Unknown") + "\n" +
                       "GLSL VERSION: " + std::string(glsl_version ? glsl_version : "Unknown") + "\n" +
                       "MAX TEXTURE SIZE: " + std::to_string(max_texture_size) + "\n" +
                       "MAX VIEWPORT: " + std::to_string(max_viewport_dims[0]) + "x" + std::to_string(max_viewport_dims[1]);
    
    LOGI("GLES Info retrieved successfully");
    return env->NewStringUTF(info.c_str());
}
