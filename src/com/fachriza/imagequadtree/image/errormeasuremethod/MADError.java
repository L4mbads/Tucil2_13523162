package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;

public class MADError extends ErrorMeasurementMethod {

    public MADError(ImageData imageData) {
        super(imageData);
    }

    @Override
    public float getErrorValue(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {

        float[] absoluteDifference = getAbsoluteDifference(mean, x, y, width, height);
        float avgDifference = (absoluteDifference[0] + absoluteDifference[1] + absoluteDifference[2]) / 3;

        return avgDifference;
    }

    @Override
    public float getMaxErrorValue() {
        return 127.5f;
    }

    protected float[] getAbsoluteDifference(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {

        float[] absoluteDifference = { 0, 0, 0 };
        int count = width * height;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                absoluteDifference[0] += Math.abs((imageData.getRed(j, i) & 0xff) - mean[0]);
                absoluteDifference[1] += Math.abs((imageData.getGreen(j, i) & 0xff) - mean[1]);
                absoluteDifference[2] += Math.abs((imageData.getBlue(j, i) & 0xff) - mean[2]);
            }
        }

        for (int i = 0; i < 3; i++) {
            absoluteDifference[i] /= count;
        }

        return absoluteDifference;
    }

}
