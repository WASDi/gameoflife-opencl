#define LOCAL_CELL_SIZE 14
#define FINAL_LOCAL_INDEX 15
#define LOCAL_ACTUAL_SIZE 16

#define MEMORY_SIZE_Y SIZE_Y / 4

__inline char shouldIBeAlive(char currentStatus, char aliveNeighbours) {
    return aliveNeighbours == 3 || (currentStatus == 1 && aliveNeighbours == 2);
}

int getGlobalPixelIndex(int x, int y) {
    x = (x == -1) ? SIZE_X-1 : x;
    y = (y == -1) ? MEMORY_SIZE_Y-1 : y;

    x = (x == SIZE_X) ? 0 : x;
    y = (y == MEMORY_SIZE_Y) ? 0 : y;
    return y*SIZE_X + x;
}

__inline int getLocalPixelIndex(int x, int y) {
    return y*LOCAL_ACTUAL_SIZE + x;
}

__kernel void game_step(
    __global char4 *global_input,
    __global char4 *output)
{
    int x = get_global_id(0) - 2 * get_group_id(0) - 1;
    int y = get_global_id(1) - 2 * get_group_id(1) - 1;

    int group_x_start = get_group_id(0) * LOCAL_CELL_SIZE;
    int group_y_start = get_group_id(1) * LOCAL_CELL_SIZE;

    int local_x = get_local_id(0);
    int local_y = get_local_id(1);

    __local char4 local_input[LOCAL_ACTUAL_SIZE*LOCAL_ACTUAL_SIZE];

    if(x <= SIZE_X && y <= MEMORY_SIZE_Y){
        local_input[getLocalPixelIndex(local_x, local_y)]
                = global_input[getGlobalPixelIndex(group_x_start + local_x - 1,
                                                   group_y_start + local_y - 1)];
    }

    barrier(CLK_LOCAL_MEM_FENCE);

    //index 0 and final doesn't process cells
    if(local_x != 0 && local_x != FINAL_LOCAL_INDEX &&
       local_y != 0 && local_y != FINAL_LOCAL_INDEX &&
       x < SIZE_X && y < MEMORY_SIZE_Y){
       char aliveNeighbours = 0;
       char currentStatus = local_input[getLocalPixelIndex(local_x, local_y)].s0;

       aliveNeighbours += local_input[getLocalPixelIndex(local_x - 1, local_y - 1)].s0;
       aliveNeighbours += local_input[getLocalPixelIndex(local_x, local_y - 1)].s0;
       aliveNeighbours += local_input[getLocalPixelIndex(local_x + 1, local_y - 1)].s0;

       aliveNeighbours += local_input[getLocalPixelIndex(local_x - 1, local_y)].s0;
       aliveNeighbours += local_input[getLocalPixelIndex(local_x + 1, local_y)].s0;

       aliveNeighbours += local_input[getLocalPixelIndex(local_x - 1, local_y + 1)].s0;
       aliveNeighbours += local_input[getLocalPixelIndex(local_x, local_y + 1)].s0;
       aliveNeighbours += local_input[getLocalPixelIndex(local_x + 1, local_y + 1)].s0;

       //output[y*SIZE_X + x] = 0; //TODO fix this part
       output[y*SIZE_X + x] = shouldIBeAlive(currentStatus, aliveNeighbours);
    }
}

__kernel void render_image(
    __global const char4 *input,
    __write_only image2d_t image,
    int pixelSize)
{
    int x = get_global_id(0);
    int y = get_global_id(1);
    int y_index = y*4;

    char4 value = input[y*SIZE_X + x];
    char4 color = (char) 255*((char)1-value);
    uint4 pixel0 = {color.s0, color.s0, color.s0, 0};
    uint4 pixel1 = {color.s1, color.s1, color.s1, 0};
    uint4 pixel2 = {color.s2, color.s2, color.s2, 0};
    uint4 pixel3 = {color.s3, color.s3, color.s3, 0};

    for (int dy = 0; dy < pixelSize; dy++) {
        for (int dx = 0; dx < pixelSize; dx++) {
            int2 posOut0 = {x*pixelSize+dx, y_index*pixelSize+dy};
            int2 posOut1 = {x*pixelSize+dx, y_index*pixelSize+dy+1*pixelSize};
            int2 posOut2 = {x*pixelSize+dx, y_index*pixelSize+dy+2*pixelSize};
            int2 posOut3 = {x*pixelSize+dx, y_index*pixelSize+dy+3*pixelSize};

            write_imageui(image, posOut0, pixel0);
            write_imageui(image, posOut1, pixel1);
            write_imageui(image, posOut2, pixel2);
            write_imageui(image, posOut3, pixel3);
        }
    }
}
