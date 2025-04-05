package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;

public class EntropyError extends ErrorMeasurementMethod {

    public EntropyError(ImageData imageData) {
        super(imageData);
    }

    @Override
    public float getErrorValue(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {

        float[] entropy = getEntropy(x, y, width, height);
        float avgEntropy = (entropy[0] + entropy[1] + entropy[2]) / 3;
        return avgEntropy;
    }

    @Override
    public float getMaxErrorValue() {
        // Maximum entropy for 8-bit color channel
        return 8.0f;
    }

    protected float[] getEntropy(
            int x,
            int y,
            int width,
            int height) {

        int[] redFrequency = new int[256];
        int[] greenFrequency = new int[256];
        int[] blueFrequency = new int[256];

        // Count color frequency
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                byte red = imageData.getRed(j, i);
                byte green = imageData.getGreen(j, i);
                byte blue = imageData.getBlue(j, i);
                redFrequency[red & 0xff]++;
                greenFrequency[green & 0xff]++;
                blueFrequency[blue & 0xff]++;
            }
        }

        int count = width * height;
        float[] entropy = { 0, 0, 0 };

        // Calculate entropy
        for (int i = 0; i < 255; i++) {
            int red = redFrequency[i];
            if (red > 0) {
                float pRed = ((float) red / count);
                entropy[0] += (pRed * log2(pRed));
            }
            int green = greenFrequency[i];
            if (green > 0) {
                float pGreen = ((float) green / count);
                entropy[1] += (pGreen * log2(pGreen));
            }
            int blue = blueFrequency[i];
            if (blue > 0) {
                float pBlue = ((float) blue / count);
                entropy[2] += (pBlue * log2(pBlue));
            }
        }

        entropy[0] *= -1;
        entropy[1] *= -1;
        entropy[2] *= -1;

        return entropy;
    }

    private float log2(float x) {
        /*
         * Calculate using the rule:
         * Log2(x) = Ln(x) / Ln(2)
         */
        return (float) (Math.log(x) / 0.69314718056f);
    }

}
