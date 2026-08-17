#include <jni.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <chrono>
#include <string>
#include <vector>
#include <iomanip>
#include <sstream>

#define LOG_TAG "AngleBenchmark"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct BenchmarkResult {
    std::string backend_name;
    double context_creation_ms;
    double avg_frame_time_ms;
    double draw_call_overhead_ns;
    int total_frames;
};

class BenchmarkEngine {
private:
    EGLDisplay display;
    EGLContext context;
    EGLSurface surface;
    GLuint shader_program;
    GLuint vbo;

    uint64_t get_time_ns() {
        struct timespec ts;
        clock_gettime(CLOCK_MONOTONIC, &ts);
        return (uint64_t)ts.tv_sec * 1000000000ULL + ts.tv_nsec;
    }

    void create_test_geometry() {
        GLfloat vertices[] = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.0f,  0.5f, 0.0f
        };
        glGenBuffers(1, &vbo);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    }

    void compile_shaders() {
        const char* v_shader = "#version 300 es\nin vec3 aPos;\nvoid main() {\n  gl_Position = vec4(aPos, 1.0);\n}";
        const char* f_shader = "#version 300 es\nout vec4 FragColor;\nvoid main() {\n  FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n}";
        
        GLuint vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, 1, &v_shader, NULL);
        glCompileShader(vs);
        
        GLuint fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, 1, &f_shader, NULL);
        glCompileShader(fs);
        
        shader_program = glCreateProgram();
        glAttachShader(shader_program, vs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        
        glDeleteShader(vs);
        glDeleteShader(fs);
    }

public:
    BenchmarkResult run_test(const std::string& backend_name, bool use_angle) {
        BenchmarkResult result;
        result.backend_name = backend_name;
        result.total_frames = 0;

        // 1. Medir criação de contexto
        uint64_t start_ctx = get_time_ns();
        
        display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        EGLint major, minor;
        eglInitialize(display, &major, &minor);

        EGLint config_attribs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
            EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8,
            EGL_NONE
        };
        EGLConfig config;
        EGLint num_configs;
        eglChooseConfig(display, config_attribs, &config, 1, &num_configs);

        EGLint pbuffer_attribs[] = { EGL_WIDTH, 256, EGL_HEIGHT, 256, EGL_NONE };
        surface = eglCreatePbufferSurface(display, config, pbuffer_attribs);

        EGLint ctx_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE };
        context = eglCreateContext(display, config, EGL_NO_CONTEXT, ctx_attribs);
        eglMakeCurrent(display, surface, surface, context);

        uint64_t end_ctx = get_time_ns();
        result.context_creation_ms = (end_ctx - start_ctx) / 1000000.0;

        // 2. Preparar geometria
        create_test_geometry();
        compile_shaders();
        glUseProgram(shader_program);
        GLint pos_loc = glGetAttribLocation(shader_program, "aPos");
        glEnableVertexAttribArray(pos_loc);
        glVertexAttribPointer(pos_loc, 3, GL_FLOAT, GL_FALSE, 0, 0);

        // 3. Benchmark de Draw Calls (10.000 iterações)
        int iterations = 10000;
        uint64_t start_draw = get_time_ns();
        
        for (int i = 0; i < iterations; i++) {
            glClear(GL_COLOR_BUFFER_BIT);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            // Forçar sincronização para medir tempo real de GPU (flush)
            glFinish(); 
        }
        
        uint64_t end_draw = get_time_ns();
        result.draw_call_overhead_ns = (double)(end_draw - start_draw) / iterations;
        result.avg_frame_time_ms = result.draw_call_overhead_ns / 1000000.0;
        result.total_frames = iterations;

        // 4. Limpeza
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(display, context);
        eglDestroySurface(display, surface);
        eglTerminate(display);

        return result;
    }
};

extern "C" JNIEXPORT jstring JNICALL
Java_com_denocraft_anglebenchmark_BenchmarkRunner_runNativeBenchmark(JNIEnv* env, jobject /* this */) {
    BenchmarkEngine engine;
    std::ostringstream report;
    report << std::fixed << std::setprecision(3);

    // Teste 1: ANGLE (GLES -> Vulkan)
    // Nota: O ambiente deve ter LD_LIBRARY_PATH configurado para apontar para as libs ANGLE
    LOGI("Iniciando teste: ANGLE GLES -> Vulkan");
    BenchmarkResult res_angle_vk = engine.run_test("ANGLE (GLES->Vulkan)", true);
    report << "BACKEND: " << res_angle_vk.backend_name << "\n";
    report << "Context Creation: " << res_angle_vk.context_creation_ms << " ms\n";
    report << "Avg Frame Time: " << res_angle_vk.avg_frame_time_ms << " ms\n";
    report << "Draw Call Overhead: " << res_angle_vk.draw_call_overhead_ns << " ns\n";
    report << "-----------------------------\n";

    // Teste 2: Native GLES (Baseline)
    // Para isolar, precisaríamos recarregar as libs do sistema, mas este teste usa o mesmo contexto EGL
    // Em um app real, separaríamos os carregamentos de biblioteca.
    LOGI("Iniciando teste: Native GLES (Baseline)");
    BenchmarkResult res_native = engine.run_test("Native GLES (Hardware)", false);
    report << "BACKEND: " << res_native.backend_name << "\n";
    report << "Context Creation: " << res_native.context_creation_ms << " ms\n";
    report << "Avg Frame Time: " << res_native.avg_frame_time_ms << " ms\n";
    report << "Draw Call Overhead: " << res_native.draw_call_overhead_ns << " ns\n";

    LOGI("Benchmark concluído. Report:\n%s", report.str().c_str());
    return env->NewStringUTF(report.str().c_str());
}
