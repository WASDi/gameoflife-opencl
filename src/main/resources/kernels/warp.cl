#define SHRINK 5
#define WEIGHT 0.4f

const sampler_t samplerIn =
    CLK_NORMALIZED_COORDS_FALSE |
    CLK_ADDRESS_CLAMP |
    CLK_FILTER_NEAREST;

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
    float weight = WEIGHT * exp2(-euclidean_dist_2) * cos(step*5);

    //SIN^2
    //float weight = sin(euclidean_dist * euclidean_dist * 20);
    //weight = weight * weight * (0.01f - cos(step)*0.01f);

    //SIN(x^x)
    //euclidean_dist *= 3;
    //float weight = sin(pow(euclidean_dist, euclidean_dist)) * 0.1f;


    //TODO iterera över flera och bara addera ihop samtliga dist*weight?
    float x_from = pos.x + x_dist * weight;
    float y_from = pos.y + y_dist * weight;

    int2 posIn = {(int)(x_from * sizeX + 0.5f), (int)(y_from * sizeY + 0.5f)}; //FIXME pixligt, gör AVG på pixlar eller annan teknik för zoom?
    int2 posOut = {gidX, gidY};

    uint4 pixel = read_imageui(sourceImage, samplerIn, posIn);
    write_imageui(targetImage, posOut, pixel);
}