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

    float3 pos = {((float)gidX)/size, ((float)gidY)/size, step};

    float greatest_dot_closeness = 0;

    for(int i = 0; i < num_dots; i++){
        float3 dot = dots[i];
        dot.z *= 10; //make everything Z more distant

        float3 dist_2 = fast_float3_pow2(dot - pos);
        float distance_to_dot = sqrt( dist_2.x + dist_2.y + dist_2.z );
        // FASTER http://stackoverflow.com/questions/5381397/openclnearest-neighbour-using-euclidean-distance

        float closeness_to_dot = max(1-distance_to_dot, 0.0f);
        greatest_dot_closeness = max(greatest_dot_closeness, closeness_to_dot);
    }

    //ADJUST
    greatest_dot_closeness = max(2*greatest_dot_closeness-1, 0.0f);

    uchar w = (uchar) clamp((greatest_dot_closeness) * 255, 0.0f, 255.0f);
    uint4 pixel = {w, w, w, 0};

    int2 posOut = {gidX, gidY};
    write_imageui(targetImage, posOut, pixel);
}