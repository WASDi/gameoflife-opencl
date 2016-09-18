#define LOCAL_SIZE 16

__inline int shouldIBeAlive(int currentStatus, int aliveNeighbours) {
    return aliveNeighbours == 3 || (currentStatus == 1 && aliveNeighbours == 2);
}

int getPixelIndex(int x, int y) {
    return ((y&15)*LOCAL_SIZE + (x&15));
}

__kernel void game_step(
    __global int *global_input,
    __global int *output,
    int sizeX,
    int sizeY)
{
    int x = get_global_id(0);
    int y = get_global_id(1);

    int local_x = get_local_id(0);
    int local_y = get_local_id(1);

    __local int local_input[LOCAL_SIZE*LOCAL_SIZE];
    local_input[local_y*LOCAL_SIZE + local_x] = global_input[y*sizeX + x];
    barrier(CLK_LOCAL_MEM_FENCE);

    int aliveNeighbours = 0;
    int currentStatus = local_input[local_y*LOCAL_SIZE + local_x];

    aliveNeighbours += local_input[getPixelIndex(local_x - 1, local_y - 1)];
    aliveNeighbours += local_input[getPixelIndex(local_x, local_y - 1)];
    aliveNeighbours += local_input[getPixelIndex(local_x + 1, local_y - 1)];

    aliveNeighbours += local_input[getPixelIndex(local_x - 1, local_y)];
    aliveNeighbours += local_input[getPixelIndex(local_x + 1, local_y)];

    aliveNeighbours += local_input[getPixelIndex(local_x - 1, local_y + 1)];
    aliveNeighbours += local_input[getPixelIndex(local_x, local_y + 1)];
    aliveNeighbours += local_input[getPixelIndex(local_x + 1, local_y + 1)];

    output[y*sizeX + x] = shouldIBeAlive(currentStatus, aliveNeighbours);
}

__kernel void render_image(
    __global const int *input,
    __write_only image2d_t image,
    int sizeX,
    int pixelSize)
{
    int x = get_global_id(0);
    int y = get_global_id(1);

    int value = input[y*sizeX + x];
    int color = 255*(1-value);
    uint4 pixel = {color, color, color, 0};

    for (int dy = 0; dy < pixelSize; dy++) {
        for (int dx = 0; dx < pixelSize; dx++) {
            int2 posOut = {x*pixelSize+dx, y*pixelSize+dy};
            write_imageui(image, posOut, pixel);
        }
    }
}