#define LOCAL_CELL_SIZE 14
#define FINAL_LOCAL_INDEX 15
#define LOCAL_ACTUAL_SIZE 16

__inline int shouldIBeAlive(int currentStatus, int aliveNeighbours) {
    return aliveNeighbours == 3 || (currentStatus == 1 && aliveNeighbours == 2);
}

int getGlobalPixelIndex(int x, int y, int sizeX, int sizeY) {
    //TODO replace modulo with ghost lines
    int xPos = (x + sizeX) % sizeX;
    int yPos = (y + sizeY) % sizeY;
    return yPos*sizeX + xPos;
}

int getLocalPixelIndex(int x, int y) {
    return y*LOCAL_BOUNDARY + x;
}

__kernel void game_step(
    __global int *global_input,
    __global int *output,
    int sizeX,
    int sizeY)
{
    int x = get_global_id(0) - 2 * get_group_id(0) - 1;
    int y = get_global_id(1) - 2 * get_group_id(1) - 1;

    int group_x_start = get_group_id(0) * LOCAL_CELL_SIZE;
    int group_y_start = get_group_id(1) * LOCAL_CELL_ SIZE;

    int local_x = get_local_id(0);
    int local_y = get_local_id(1);

    __local int local_input[LOCAL_ACTUAL_SIZE*LOCAL_ACTUAL_SIZE];
    local_input[getLocalPixelIndex(local_x, local_y)]
        = global_input[getGlobalPixelIndex(group_x_start + local_x - 1,
                                           group_y_start + local_y - 1,
                                           sizeX,
                                           sizeY)];

    barrier(CLK_LOCAL_MEM_FENCE);

    //index 0 and final doesn't process cells
    if(local_x != 0 && local_x != FINAL_LOCAL_INDEX &&
       local_y != 0 && local_y != FINAL_LOCAL_INDEX){
       int aliveNeighbours = 0;
       int currentStatus = local_input[getLocalPixelIndex(local_x, local_y)];

       aliveNeighbours += local_input[getLocalPixelIndex(local_x - 1, local_y - 1)];
       aliveNeighbours += local_input[getLocalPixelIndex(local_x, local_y - 1)];
       aliveNeighbours += local_input[getLocalPixelIndex(local_x + 1, local_y - 1)];

       aliveNeighbours += local_input[getLocalPixelIndex(local_x - 1, local_y)];
       aliveNeighbours += local_input[getLocalPixelIndex(local_x + 1, local_y)];

       aliveNeighbours += local_input[getLocalPixelIndex(local_x - 1, local_y + 1)];
       aliveNeighbours += local_input[getLocalPixelIndex(local_x, local_y + 1)];
       aliveNeighbours += local_input[getLocalPixelIndex(local_x + 1, local_y + 1)];

       output[y*sizeX + x] = shouldIBeAlive(currentStatus, aliveNeighbours);
    }
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