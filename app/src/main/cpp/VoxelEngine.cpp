#include <jni.h>
#include <vector>
#include <cstdint>
#include <cmath>

static uint32_t hash3d(uint32_t x, uint32_t y, uint32_t z) {
    uint32_t h = 0x811c9dc5u;
    h ^= x; h *= 0x01000193u;
    h ^= y; h *= 0x01000193u;
    h ^= z; h *= 0x01000193u;
    return h;
}

static float noise3d(float x, float y, float z) {
    int ix = (int)floorf(x), iy = (int)floorf(y), iz = (int)floorf(z);
    float fx = x-ix, fy = y-iy, fz = z-iz;
    float u = fx*fx*(3.0f-2.0f*fx);
    float v = fy*fy*(3.0f-2.0f*fy);
    float w = fz*fz*(3.0f-2.0f*fz);
    float n000=(hash3d(ix,  iy,  iz  )&0xFF)/255.0f;
    float n100=(hash3d(ix+1,iy,  iz  )&0xFF)/255.0f;
    float n010=(hash3d(ix,  iy+1,iz  )&0xFF)/255.0f;
    float n110=(hash3d(ix+1,iy+1,iz  )&0xFF)/255.0f;
    float n001=(hash3d(ix,  iy,  iz+1)&0xFF)/255.0f;
    float n101=(hash3d(ix+1,iy,  iz+1)&0xFF)/255.0f;
    float n011=(hash3d(ix,  iy+1,iz+1)&0xFF)/255.0f;
    float n111=(hash3d(ix+1,iy+1,iz+1)&0xFF)/255.0f;
    float nx0=n000+(n100-n000)*u;
    float nx1=n010+(n110-n010)*u;
    float nxy0=nx0+(nx1-nx0)*v;
    float nx2=n001+(n101-n001)*u;
    float nx3=n011+(n111-n011)*u;
    float nxy1=nx2+(nx3-nx2)*v;
    return nxy0+(nxy1-nxy0)*w;
}

// x:6 y:6 z:6 face:3 block:8 ao:2 vIdx:3 = 34 bits -> cabe em uint32 com bits livres
// layout: [0-5]=x [6-11]=y [12-17]=z [18-20]=face [21-28]=block [29-30]=ao [27-29] CONFLITO
// Layout correto: x:6 y:6 z:6 face:3 block:8 ao:2 vIdx:3 = 34 bits
// Usar: bits 0-5=x, 6-11=y, 12-17=z, 18-20=face, 21-28=block, 29-30=ao, 31=unused
// vIdx separado no byte superior — reorganizar:
// bits: 0-5=x(6) 6-11=y(6) 12-17=z(6) 18-20=face(3) 21-22=ao(2) 23-25=vIdx(3) 26-29=block(4 baixo) overflow!
// Usar uint64 via dois int ou simplificar block para 4 bits por ora (16 tipos):
// FINAL: x:6 y:6 z:6 face:3 ao:2 vIdx:3 block:6 = 32 bits exatos
// bits: 0-5=x 6-11=y 12-17=z 18-20=face 21-22=ao 23-25=vIdx 26-31=block
static inline uint32_t packVoxel(uint32_t x,uint32_t y,uint32_t z,
                                  uint32_t face,uint32_t block,
                                  uint32_t ao,uint32_t vIdx){
    return (x&0x3F)
         |((y&0x3F)<<6)
         |((z&0x3F)<<12)
         |((face&0x07)<<18)
         |((ao&0x03)<<21)
         |((vIdx&0x07)<<23)
         |((block&0x3F)<<26);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_gpu_devstudio_engine_VoxelEngine_generateChunk(JNIEnv* env, jobject){
    const int S = 16;
    std::vector<uint8_t> voxels(S*S*S, 0);

    for(int x=0;x<S;x++) for(int z=0;z<S;z++){
        float h = noise3d(x*0.15f, 0.0f, z*0.15f)*6.0f + 5.0f;
        int height = (int)h;
        if(height<1) height=1;
        if(height>=S) height=S-1;
        for(int y=0;y<S;y++){
            if(y==0)               voxels[x+y*S+z*S*S]=3; // bedrock
            else if(y<height)      voxels[x+y*S+z*S*S]=1; // dirt
            else if(y==height)     voxels[x+y*S+z*S*S]=2; // grass
        }
    }

    auto get=[&](int x,int y,int z)->uint8_t{
        if(x<0||x>=S||y<0||y>=S||z<0||z>=S) return 0;
        return voxels[x+y*S+z*S*S];
    };

    // face dirs: +Z,-Z,+X,-X,+Y,-Y
    const int dx[6]={0,0,1,-1,0,0};
    const int dy[6]={0,0,0,0,1,-1};
    const int dz[6]={1,-1,0,0,0,0};
    const uint32_t triIdx[6]={0,1,2,0,2,3};

    std::vector<uint32_t> mesh;
    mesh.reserve(S*S*S*6);

    for(int x=0;x<S;x++) for(int y=0;y<S;y++) for(int z=0;z<S;z++){
        uint8_t b=get(x,y,z);
        if(b==0) continue;
        for(int f=0;f<6;f++){
            if(get(x+dx[f],y+dy[f],z+dz[f])!=0) continue;
            for(int v=0;v<6;v++){
                mesh.push_back(packVoxel(x,y,z,f,b,3,triIdx[v]));
            }
        }
    }

    jintArray res=env->NewIntArray((jsize)mesh.size());
    env->SetIntArrayRegion(res,0,(jsize)mesh.size(),
                           reinterpret_cast<const jint*>(mesh.data()));
    return res;
}
