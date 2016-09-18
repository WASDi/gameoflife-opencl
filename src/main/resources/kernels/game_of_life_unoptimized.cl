__inline int shouldIBeAlive(int currentStatus, int aliveNeighbours) {
    return aliveNeighbours == 3 || (currentStatus == 1 && aliveNeighbours == 2);
}

private int getPixelIndex(int x, int y, int sizeX, int sizeY) {
    int xPos = (x + sizeX) % sizeX;
    int yPos = (y + sizeY) % sizeY;
    return yPos*sizeX + xPos; //0.290 ms
    //return y * sizeX + x; //0.153 ms
    //Om man kör LOKALT är det väl alltid mod 16 och då är det bara &15 = dubbel prestanda!
}

__kernel void game_step(
    __global int *input,
    __global int *output,
    int sizeX,
    int sizeY)
{
    int x = get_global_id(0);
    int y = get_global_id(1);

    int aliveNeighbours = 0;
    int currentStatus = input[y*sizeX + x];

    aliveNeighbours += input[getPixelIndex(x - 1, y - 1, sizeX, sizeY)];
    aliveNeighbours += input[getPixelIndex(x, y - 1, sizeX, sizeY)];
    aliveNeighbours += input[getPixelIndex(x + 1, y - 1, sizeX, sizeY)];

    aliveNeighbours += input[getPixelIndex(x - 1, y, sizeX, sizeY)];
    aliveNeighbours += input[getPixelIndex(x + 1, y, sizeX, sizeY)];

    aliveNeighbours += input[getPixelIndex(x - 1, y + 1, sizeX, sizeY)];
    aliveNeighbours += input[getPixelIndex(x, y + 1, sizeX, sizeY)];
    aliveNeighbours += input[getPixelIndex(x + 1, y + 1, sizeX, sizeY)];

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