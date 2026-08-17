#include <jni.h>
#include <vector>
#include <cstdint>
#include <cmath>

uint32_t hash3d(uint32_t x, uint32_t y, uint32_t z) {
    uint32_t h = 0x811c9dc5;
    h ^= x; h *= 0x01000193;
    h ^= y; h *= 0x01000193;
    h ^= z; h *= 0x01000193;
    return h;
}

float noise3d(float x, float y, float z) {
    int ix = (int)floorf(x), iy = (int)floorf(y), iz = (int)floorf(z);
    float fx = x - ix, fy = y - iy, fz = z - iz;
    float u = fx * fx * (3.0f - 2.0f * fx);
    float v = fy * fy * (3.0f - 2.0f * fy);
    float w = fz * fz * (3.0f - 2.0f * fz);
    
    float n000 = (hash3d(ix, iy, iz) & 0xFF) / 255.0f;
    float n100 = (hash3d(ix+1, iy, iz) & 0xFF) / 255.0f;
    float n010 = (hash3d(ix, iy+1, iz) & 0xFF) / 255.0f;
    float n110 = (hash3d(ix+1, iy+1, iz) & 0xFF) / 255.0f;
    float n001 = (hash3d(ix, iy, iz+1) & 0xFF) / 255.0f;
    float n101 = (hash3d(ix+1, iy, iz+1) & 0xFF) / 255.0f;
    float n011 = (hash3d(ix, iy+1, iz+1) & 0xFF) / 255.0f;
    float n111 = (hash3d(ix+1, iy+1, iz+1) & 0xFF) / 255.0f;

    float nx0 = n000 + (n100 - n000) * u;
    float nx1 = n010 + (n110 - n010) * u;
    float nxy0 = nx0 + (nx1 - nx0) * v;
    float nx2 = n001 + (n101 - n001) * u;
    float nx3 = n011 + (n111 - n011) * u;
    float nxy1 = nx2 + (nx3 - nx2) * v;

    return nxy0 + (nxy1 - nxy0) * w;
}

inline uint32_t packVoxel(uint32_t x, uint32_t y, uint32_t z, uint32_t face, uint32_t blockID, uint32_t ao, uint32_t vIdx) {
    return (x & 0x3F) | ((y & 0x3F) << 6) | ((z & 0x3F) << 12) | 
           ((face & 0x07) << 18) | ((blockID & 0xFF) << 21) | 
           ((ao & 0x03) << 29) | ((vIdx & 0x01) << 31);
}

extern "C" JNIEXPORT jintArray JNICALL Java_com_gpu_devstudio_engine_VoxelEngine_generateChunk(JNIEnv* env, jobject thiz) {
    std::vector<uint32_t> mesh;
    int chunkSize = 16;
    std::vector<uint8_t> voxels(chunkSize * chunkSize * chunkSize, 0);
    
    for (int x = 0; x < chunkSize; x++) {
        for (int z = 0; z < chunkSize; z++) {
            float h = noise3d(x * 0.15f, 0, z * 0.15f) * 8.0f + 4.0f;
            int height = (int)h;
            for (int y = 0; y < chunkSize; y++) {
                if (y < height) {
                    voxels[x + y * chunkSize + z * chunkSize * chunkSize] = (y == 0) ? 3 : ((y == height - 1) ? 2 : 1);
                }
            }
        }
    }

    auto getVoxel = [&](int x, int y, int z) -> uint8_t {
        if (x < 0 || x >= chunkSize || y < 0 || y >= chunkSize || z < 0 || z >= chunkSize) return 0;
        return voxels[x + y * chunkSize + z * chunkSize * chunkSize];
    };

    for (int x = 0; x < chunkSize; x++) {
        for (int y = 0; y < chunkSize; y++) {
            for (int z = 0; z < chunkSize; z++) {
                uint8_t block = getVoxel(x, y, z);
                if (block == 0) continue;

                bool faces[6] = {
                    getVoxel(x, y, z + 1) == 0, getVoxel(x, y, z - 1) == 0,
                    getVoxel(x + 1, y, z) == 0, getVoxel(x - 1, y, z) == 0,
                    getVoxel(x, y + 1, z) == 0, getVoxel(x, y - 1, z) == 0
                };

                for (int f = 0; f < 6; f++) {
                    if (!faces[f]) continue;
                    uint32_t vIdxMap[6] = {0, 1, 2, 0, 2, 3};
                    for (int v = 0; v < 6; v++) {
                        mesh.push_back(packVoxel(x, y, z, f, block, 3, vIdxMap[v]));
                    }
                }
            }
        }
    }

    jintArray result = env->NewIntArray(mesh.size());
    env->SetIntArrayRegion(result, 0, mesh.size(), reinterpret_cast<jint*>(mesh.data()));
    return result;
}
