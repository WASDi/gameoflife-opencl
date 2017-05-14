#define WEIGHT 5.0f

const sampler_t wasdSampler = CLK_FILTER_LINEAR | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP;

uchar colorClamp(float value, float low, float high) {
    return (uchar) clamp((value-low) * (255.0/(high-low)), 0.0f, 255.0f);
}

__kernel void sandbox(
    __write_only image2d_t targetImage,
    const int sizeX,
    const int sizeY,
    const float step)
{
    int gidX = get_global_id(0);
    int gidY = get_global_id(1);

    float2 pos = {((float)gidX)/sizeX, ((float)gidY)/sizeY};
    float2 target = (float2){0.5f, 0.5f};

    float x_dist = target.x - pos.x;
    float y_dist = target.y - pos.y;

    float euclidean_dist_2 = x_dist * x_dist +
                             y_dist * y_dist;
    euclidean_dist_2 *= (SHRINK * SHRINK);
    float euclidean_dist = sqrt(euclidean_dist_2);

    float weight = WEIGHT * exp2(-euclidean_dist_2) * cos(step*3.0f);


    float x_from = pos.x + x_dist * weight;
    float y_from = pos.y + y_dist * weight;

    float2 posInF = {x_from * sizeX , y_from * sizeY};
    int2 posOut = {gidX, gidY};

    //uint4 color = read_imageui(sourceImage, wasdSampler, posInF);

//    uchar color = (uchar) clamp((y_dist*10.0f) * 255, 0.0f, 255.0f);
    uchar color = colorClamp(y_dist * weight, -1.0f, 1.0f);
    uint4 colorRGB = (uint4){color, color, color, 255};

    write_imageui(targetImage, posOut, colorRGB);
}