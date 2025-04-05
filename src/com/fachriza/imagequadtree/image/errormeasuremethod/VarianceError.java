package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;

public class VarianceError extends ErrorMeasurementMethod {

    public VarianceError(ImageData imageData) {
        super(imageData);
    }

    @Override
    public float getErrorValue(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {

        float[] variance = getVariance(mean, x, y, width, height);
        float avgVariance = (variance[0] + variance[1] + variance[2]) / 3;
        return avgVariance;
    }

    @Override
    public float getMaxErrorValue() {
        return 16256.25f;
    }

    protected float[] getVariance(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {

        float[] variance = { 0, 0, 0 };
        int count = width * height;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                float varR = (imageData.getRed(j, i) & 0xff) - mean[0];
                float varG = (imageData.getGreen(j, i) & 0xff) - mean[1];
                float varB = (imageData.getBlue(j, i) & 0xff) - mean[2];
                variance[0] += (varR * varR);
                variance[1] += (varG * varG);
                variance[2] += (varB * varB);
            }
        }

        for (int i = 0; i < 3; i++) {
            variance[i] /= count;
        }

        return variance;
    }
}
