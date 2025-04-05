package com.fachriza.imagequadtree.image.errormeasuremethod;

import java.util.HashMap;
import java.util.Map;

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
        return 8.0f;
    }

    protected float[] getEntropy(
            int x,
            int y,
            int width,
            int height) {

        Map<Byte, Integer> mapR = new HashMap<Byte, Integer>();
        Map<Byte, Integer> mapG = new HashMap<Byte, Integer>();
        Map<Byte, Integer> mapB = new HashMap<Byte, Integer>();
        float[] entropy = { 0, 0, 0 };

        int count = width * height;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                byte red = imageData.getRed(j, i);
                byte green = imageData.getGreen(j, i);
                byte blue = imageData.getBlue(j, i);
                mapR.put(red, mapR.getOrDefault(red, 0) + 1);
                mapG.put(green, mapG.getOrDefault(green, 0) + 1);
                mapB.put(blue, mapB.getOrDefault(blue, 0) + 1);
            }
        }

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                float red = (float) mapR.get(imageData.getRed(j, i)) / count;
                float green = (float) mapG.get(imageData.getGreen(j, i)) / count;
                float blue = (float) mapB.get(imageData.getBlue(j, i)) / count;

                entropy[0] += (red * log2(red));
                entropy[1] += (green * log2(green));
                entropy[2] += (blue * log2(blue));
            }
        }

        entropy[0] *= -1;
        entropy[1] *= -1;
        entropy[2] *= -1;

        return entropy;
    }

    private float log2(float x) {
        return (float) (Math.log(x) / Math.log(2));

    }

}
