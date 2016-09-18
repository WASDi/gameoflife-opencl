#define PI 3.14159265358979323846f
#define SHRINK 5
#define Z_DISTANCE_MULT 4

__inline float3 fast_float3_pow2(float3 f){
    return f * f;
}

__kernel void magic_dots(
    __write_only image2d_t targetImage,
    const int size,
    __global const float3 *dots,
    const int num_dots,
    const float step)
{
    int gidX = get_global_id(0);
    int gidY = get_global_id(1);
    //vid behov av cache http://stackoverflow.com/questions/17574570/create-local-array-dynamic-inside-opencl-kernel

    float3 pos = {((float)gidX)/size, ((float)gidY)/size, step*0.6f};

    float accumulated = 0;

    for(int i = 0; i < 38; i++){
        float3 dot = dots[i];
        dot.z *= Z_DISTANCE_MULT;

        float3 dist_2 = fast_float3_pow2(dot - pos); //float3 with distance^2 per dimension
        float euclidean_dist_2 = dist_2.x + dist_2.y + dist_2.z;
        euclidean_dist_2 *= (SHRINK * SHRINK);

        float weight = exp2(-euclidean_dist_2);
        accumulated += (1-accumulated) * weight;
    }

    uchar w = (uchar) clamp((1-accumulated) * 255, 0.0f, 255.0f);
    uint4 pixel = {w, w, w, 0};

    int2 posOut = {gidX, gidY};
    write_imageui(targetImage, posOut, pixel);
}