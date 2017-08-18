constant uint default_perm[256] = {
    151, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 99, 37,
    8, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 160, 137, 
    35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 
    134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 
    55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 
    18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 
    250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 
    189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 
    43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 
    97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 
    107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 
    138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 
    140, 36, 103, 30, 227, 47, 16, 58, 69, 17, 209, 76, 132, 187, 45, 127, 
    197, 62, 94, 252, 153, 101, 155, 167, 219, 182, 3, 64, 52, 217, 215, 61, 
    168, 68, 175, 74, 185, 112, 104, 218, 165, 246, 4, 150, 208, 254, 142, 71, 
    230, 220, 105, 92, 145, 235, 249, 14, 41, 239, 156, 180, 226, 89, 203, 117
};

constant char4 grads3d[16] = {
    {1,1,0,0},{-1,1,0,0},{1,-1,0,0},{-1,-1,0,0},
    {1,0,1,0},{-1,0,1,0},{1,0,-1,0},{-1,0,-1,0},
    {0,1,1,0},{0,-1,1,0},{0,1,-1,0},{0,-1,-1,0},
    {1,1,0,0},{-1,1,0,0},{0,-1,1,0},{0,-1,-1,0}
};

float weight_poly5(float weight) {
    return    weight * weight * weight * (weight * (weight * 6 - 15) + 10);
}

#define interp(w,b,c)    mix((b),(c),(w))

float hash_grad_dot3(uint hash, float3 xyz) {
    uint indx = hash & 0x0f;

    float3 grad3 =  convert_float3( grads3d[indx].xyz );

    return dot(xyz, grad3);
}

float Noise_3d(float x, float y, float z) {
    float X = floor(x);
    float Y = floor(y);
    float Z = floor(z);

    float3 vxyz;
    vxyz.x = x - X;
    vxyz.y = y - Y;
    vxyz.z = z - Z;

    float3 vXyz, vXYz, vXyZ, vxYz, vxYZ, vxyZ, vXYZ;
    vXyz = vxyz; vXyz.x -= 1.0f;
    vxYz = vxyz; vxYz.y -= 1.0f;
    vxyZ = vxyz; vxyZ.z -= 1.0f;

    vXYz = vXyz; vXYz.y -= 1.0f;
    vXyZ = vXyz; vXyZ.z -= 1.0f;

    vxYZ = vxYz; vxYZ.z -= 1.0f;

    vXYZ = vXYz; vXYZ.z -= 1.0f;

    int ux = (int)(X);
    int uy = (int)(Y);
    int uz = (int)(Z);
    uint uX = ux + 1;
    uint uY = uy + 1;
    uint uZ = uz + 1;

    uint px    = default_perm[  ux           & 0x0FF ];
    uint pX    = default_perm[  uX           & 0x0FF ];

    uint pxy   = default_perm[ (px  + uy)    & 0x0FF ];
    uint pXy   = default_perm[ (pX  + uy)    & 0x0FF ];
    uint pxY   = default_perm[ (px  + uY)    & 0x0FF ];
    uint pXY   = default_perm[ (pX  + uY)    & 0x0FF ];

    uint pxyz  = default_perm[ (pxy + uz)    & 0x0FF ];
    uint pXyz  = default_perm[ (pXy + uz)    & 0x0FF ];
    uint pxYz  = default_perm[ (pxY + uz)    & 0x0FF ];
    uint pXYz  = default_perm[ (pXY + uz)    & 0x0FF ];
    uint pxyZ  = default_perm[ (pxy + uZ)    & 0x0FF ];
    uint pXyZ  = default_perm[ (pXy + uZ)    & 0x0FF ];
    uint pxYZ  = default_perm[ (pxY + uZ)    & 0x0FF ];
    uint pXYZ  = default_perm[ (pXY + uZ)    & 0x0FF ];

    float gxyz  = hash_grad_dot3( pxyz, vxyz );
    float gXyz  = hash_grad_dot3( pXyz, vXyz );
    float gxYz  = hash_grad_dot3( pxYz, vxYz );
    float gXYz  = hash_grad_dot3( pXYz, vXYz );
    float gxyZ  = hash_grad_dot3( pxyZ, vxyZ );
    float gXyZ  = hash_grad_dot3( pXyZ, vXyZ );
    float gxYZ  = hash_grad_dot3( pxYZ, vxYZ );
    float gXYZ  = hash_grad_dot3( pXYZ, vXYZ );

    float wx = weight_poly5(vxyz.x);
    float wy = weight_poly5(vxyz.y);
    float wz = weight_poly5(vxyz.z);

    float result =
        interp(
            wz,
            interp(
                wy,
                interp(wx, gxyz, gXyz),
                interp(wx, gxYz, gXYz)
            ),
            interp(
                wy,
                interp(wx, gxyZ, gXyZ),
                interp(wx, gxYZ, gXYZ)
            )
        );

    return result;

}

// ----- //

const sampler_t SAMPLER = CLK_FILTER_LINEAR | CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP;
#define ZOOM 1.0f/15.0f
#define AMPLITUDE 10.0f
#define INV_SQRT2 1.0f/sqrt(2.0f)

kernel void
IntegralWarp(
    __read_only  image2d_t sourceImage,
    __write_only image2d_t outputImage,
    int                    iImageWidth,
    int                    iImageHeight,
    float                  slice,
    __global float2*       displacementArr
){
    int x = get_global_id(0);
    int y = get_global_id(1);

    int global_id = y * get_global_size(0) + x;
    float2 displacement = displacementArr[global_id];

    float dx = abs(x-iImageWidth/2);
    float dy = abs(y-iImageHeight/2);
    float distToMid = length((float2){dx, dy});

    float fx = 128 * (float)x * ZOOM / iImageWidth;
    float fy = 72 * (float)y * ZOOM / iImageHeight;
    float fz = slice - distToMid/100.0f;

    // -1 to 1
    float value1 = Noise_3d(fx, fy, fz);
    float value2 = Noise_3d(fx, fy, fz+1.0f);
    float2 values = {value1, value2};

    float offset = INV_SQRT2 * length(values);
    offset = 6 * (offset * offset * offset);
    offset = 1-offset;
    offset = offset < 0 ? 0 : offset;

    values = values * AMPLITUDE;
    displacement += values/5;

    // read displaced pixel
    float2 posInF = {x, y};
    posInF += displacement;
    uint3 rgbInTwist = read_imageui(sourceImage, SAMPLER, posInF).xyz;
    float3 rgbInTwistF = convert_float3(rgbInTwist);

    uint4 pixelOut = (uint4)(convert_uint3_rte(rgbInTwistF.xyz), 255);
    int2 posOut = {x, y};
    write_imageui(outputImage, posOut, pixelOut);

    displacementArr[global_id] = displacement;
}
