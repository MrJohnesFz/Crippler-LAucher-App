#include <jni.h>
#include <string>
#include <fstream>
#include <sstream>
#include <cstdio>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <vector>
#include <cmath>

#define LOG_TAG "WaveLauncherNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_custom_launcher_MainActivity_getSystemStats(JNIEnv* env, jobject) {
    long totalRam = 0, freeRam = 0;
    std::ifstream meminfo("/proc/meminfo");
    std::string line;
    if (meminfo.is_open()) {
        while (std::getline(meminfo, line)) {
            if (line.find("MemTotal:") == 0) sscanf(line.c_str(), "MemTotal: %ld kB", &totalRam);
            if (line.find("MemAvailable:") == 0) sscanf(line.c_str(), "MemAvailable: %ld kB", &freeRam);
        }
    }

    // Lecture CPU (Restreint sous Android 8+, placeholder basique)
    // Lecture Temperature
    long temp = 0;
    std::ifstream tempinfo("/sys/class/thermal/thermal_zone0/temp");
    if (tempinfo.is_open()) {
        tempinfo >> temp;
    }

    char buffer[128];
    snprintf(buffer, sizeof(buffer), "RAM: %ld / %ld MB\nCPU: -- %%\nTEMP: %.1f C",
             (totalRam - freeRam) / 1024, totalRam / 1024, temp / 1000.0);

    return env->NewStringUTF(buffer);
}

// === OPENGL ES 2.0 PARTICLE RENDERER ===
GLuint programObject;
float timeVal = 0.0f;
std::vector<float> particles;

GLuint loadShader(GLenum type, const char* shaderSrc) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &shaderSrc, NULL);
    glCompileShader(shader);
    return shader;
}

extern "C" JNIEXPORT void JNICALL
Java_com_custom_launcher_WaveRenderer_initGL(JNIEnv* env, jobject) {
    const char* vShader = 
        "attribute vec4 vPosition;"
        "uniform float uTime;"
        "void main() {"
        "  vec4 pos = vPosition;"
        "  pos.y += sin(pos.x * 5.0 + uTime) * 0.15;"
        "  gl_Position = pos;"
        "  gl_PointSize = 6.0;"
        "}";
        
    const char* fShader = 
        "precision mediump float;"
        "void main() {"
        "  gl_FragColor = vec4(0.0, 0.8, 1.0, 0.8);"
        "}";

    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, vShader);
    GLuint fragmentShader = loadShader(GL_FRAGMENT_SHADER, fShader);

    programObject = glCreateProgram();
    glAttachShader(programObject, vertexShader);
    glAttachShader(programObject, fragmentShader);
    glLinkProgram(programObject);

    // Generation de la grille de particules
    for (float x = -1.0f; x <= 1.0f; x += 0.08f) {
        for (float y = -0.5f; y <= 0.5f; y += 0.3f) {
            particles.push_back(x);
            particles.push_back(y);
            particles.push_back(0.0f);
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_custom_launcher_WaveRenderer_resizeGL(JNIEnv* env, jobject, jint width, jint height) {
    glViewport(0, 0, width, height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_custom_launcher_WaveRenderer_stepGL(JNIEnv* env, jobject) {
    glClearColor(0.05f, 0.05f, 0.1f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(programObject);

    timeVal += 0.04f; // Vitesse de la vague
    GLint timeLoc = glGetUniformLocation(programObject, "uTime");
    glUniform1f(timeLoc, timeVal);

    GLint posLoc = glGetAttribLocation(programObject, "vPosition");
    glVertexAttribPointer(posLoc, 3, GL_FLOAT, GL_FALSE, 0, particles.data());
    glEnableVertexAttribArray(posLoc);

    glDrawArrays(GL_POINTS, 0, particles.size() / 3);
}
