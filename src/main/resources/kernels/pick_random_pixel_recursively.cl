#define WEIGHT 1.0f
#define SHRINK 2.0f

const sampler_t samplerIn =
    CLK_NORMALIZED_COORDS_FALSE |
    CLK_ADDRESS_CLAMP |
    CLK_FILTER_NEAREST;

const sampler_t s_nearest = CLK_FILTER_NEAREST | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE;
const sampler_t s_linear = CLK_FILTER_LINEAR | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE;
const sampler_t s_repeat = CLK_FILTER_NEAREST | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_REPEAT;

const sampler_t wasdSampler = CLK_FILTER_NEAREST | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE;
//https://www.fixstars.com/en/opencl/book/OpenCLProgrammingBook/opencl-c/

int rand(int seed) // 1 <= *seed < m
{
    return (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
}

__kernel void myCoolFunction(
    __read_only image2d_t sourceImage,
    __write_only image2d_t targetImage,
    const int sizeX,
    const int sizeY,
    const int step,
    global int* seed_memory
    )
{
    int gidX = get_global_id(0);
    int gidY = get_global_id(1);
    int global_id = gidY * get_global_size(0) + gidX;

    int seed = seed_memory[global_id];
    int r1 = abs(rand(seed));
    int r2 = abs(rand(seed+1));
    seed_memory[global_id] = seed+2;

    float2 pos = {((float)gidX)/sizeX, ((float)gidY)/sizeY};

    float weight = 0.000003f * (cos(step*0.05f)+1.5f);
    float weightX = weight * ((r1%1001)-500);
    float weightY = weight * ((r2%1001)-500);

    // TODO pick using perlin noise
    float x_from = pos.x + weightX;
    float y_from = pos.y + weightY;

    float2 posInF = {x_from * sizeX + 0.5f , y_from * sizeY + 0.5f}; // +0.5f when not using NEAREST and not LINEAR
    int2 posOut = {gidX, gidY};

    uint4 pixel = read_imageui(sourceImage, wasdSampler, posInF);
    write_imageui(targetImage, posOut, pixel);
}