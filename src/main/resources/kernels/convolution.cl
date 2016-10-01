float3 rgb2hsv(float3 c)
{
    float4 K = (float4)(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    float4 p = c.y < c.z ? (float4)(c.zy, K.wz) : (float4)(c.yz, K.xy);
    float4 q = c.x < p.x ? (float4)(p.xyw, c.x) : (float4)(c.x, p.yzx);

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return (float3)(fabs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

float3 frac(float3 v)
{
  return v - floor(v);
}

float3 hsv2rgb(float3 c)
{
    float4 K = (float4)(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    float3 p = fabs(frac(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

__kernel void convolution(
    __global uchar4 *input,
    __global float *mask,
    __global uchar4 *output,
    const int2 imageSize,
    const int2 maskSize,
    const int2 maskOrigin)
{
    int gx = get_global_id(0);
    int gy = get_global_id(1);

    if (gx >= maskOrigin.x &&
        gy >= maskOrigin.y &&
        gx < imageSize.x - (maskSize.x-maskOrigin.x-1) &&
        gy < imageSize.y - (maskSize.y-maskOrigin.y-1))
    {
        float4 sum = (float4)0;
        for(int mx=0; mx<maskSize.x; mx++)
        {
            for(int my=0; my<maskSize.x; my++)
            {
                int mi = mul24(my, maskSize.x) + mx;
                int ix = gx - maskOrigin.x + mx;
                int iy = gy - maskOrigin.y + my;
                int i = mul24(iy, imageSize.x) + ix;
                sum += convert_float4(input[i]) * mask[mi];
                //sum += (float4)(rgb2hsv(convert_float4(input[i]).xyz) * mask[mi], 1);
            }
        }
        //sum.xyz = rgb2hsv(convert_float4(input[gy*imageSize.x+gx]).xyz).xyz;
        //sum.y -=0.5f;
        //sum.xyz = hsv2rgb(sum.xyz);
        uchar4 result = convert_uchar4_sat(sum);
        output[mul24(gy, imageSize.x)+gx] = result;
    }
    else
    {
        if (gx >= 0 && gx < imageSize.x &&
            gy >= 0 && gy < imageSize.y)
        {
            output[mul24(gy, imageSize.x)+gx] = (uchar4)0;
        }
    }

}