#define WEIGHT 1.0f

const sampler_t samplerIn =
    CLK_NORMALIZED_COORDS_FALSE |
    CLK_ADDRESS_CLAMP |
    CLK_FILTER_NEAREST;

const sampler_t s_nearest = CLK_FILTER_NEAREST | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE;
const sampler_t s_linear = CLK_FILTER_LINEAR | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE;
const sampler_t s_repeat = CLK_FILTER_NEAREST | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_REPEAT;

const sampler_t wasdSampler = CLK_FILTER_LINEAR | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP;
//https://www.fixstars.com/en/opencl/book/OpenCLProgrammingBook/opencl-c/

__kernel void warp(
    __read_only  image2d_t sourceImage,
    __write_only image2d_t targetImage,
    const int sizeX,
    const int sizeY,
    const float step)
{
    int gidX = get_global_id(0);
    int gidY = get_global_id(1);

    float2 pos = {((float)gidX)/sizeX, ((float)gidY)/sizeY};
    //float2 target = {0.5f + cos(step)*0.25f,
    //                 0.5f + sin(step)*0.25f};
    float2 target = (float2){0.5f, 0.5f};

    float x_dist = target.x - pos.x;
    float y_dist = target.y - pos.y;

    float euclidean_dist_2 = x_dist * x_dist +
                             y_dist * y_dist;
    euclidean_dist_2 *= (SHRINK * SHRINK);
    float euclidean_dist = sqrt(euclidean_dist_2);

    //GAUSSIAN
    float weight = WEIGHT * exp2(-euclidean_dist_2) * cos(step*3.0f);

    //SIN^2
    //float weight = sin(euclidean_dist * 15 - step) * 0.15f + sin(step*0.2f)*0.5f;
    //weight = weight * weight * (0.01f - cos(step)*0.01f);

    //SIN(x^x)
    //euclidean_dist *= 3;
    //float weight = sin(pow(euclidean_dist, euclidean_dist)) * 0.1f;


    float x_from = pos.x + x_dist * weight;
    float y_from = pos.y + y_dist * weight;

    //int2 posIn = {(int)(x_from * sizeX + 0.5f), (int)(y_from * sizeY + 0.5f)};
    float2 posInF = {x_from * sizeX , y_from * sizeY};
    int2 posOut = {gidX, gidY};

    uint4 pixel = read_imageui(sourceImage, wasdSampler, posInF);
    write_imageui(targetImage, posOut, pixel);
}