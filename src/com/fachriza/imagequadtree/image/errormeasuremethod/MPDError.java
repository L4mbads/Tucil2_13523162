package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;

public class MPDError extends ErrorMeasurementMethod {

    public MPDError(ImageData imageData) {
        super(imageData);
    }

    @Override
    public float getErrorValue(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {

        float[] minMaxDifference = getMinMaxDifference(x, y, width, height);
        float avgDifference = (minMaxDifference[0] + minMaxDifference[1] + minMaxDifference[2]) / 3;
        return avgDifference;
    }

    @Override
    public float getMaxErrorValue() {
        return 255.0f;
    }

    protected float[] getMinMaxDifference(
            int x,
            int y,
            int width,
            int height) {

        float[] minVal = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
        float[] maxVal = { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                float red = (imageData.getRed(j, i) & 0xff);
                minVal[0] = red < minVal[0] ? red : minVal[0];
                maxVal[0] = red > maxVal[0] ? red : maxVal[0];

                float green = (imageData.getGreen(j, i) & 0xff);
                minVal[1] = green < minVal[1] ? green : minVal[1];
                maxVal[1] = green > maxVal[1] ? green : maxVal[1];

                float blue = (imageData.getBlue(j, i) & 0xff);
                minVal[2] = blue < minVal[2] ? blue : minVal[2];
                maxVal[2] = blue > maxVal[2] ? blue : maxVal[2];
            }
        }

        for (int i = 0; i < 3; i++) {
            maxVal[i] -= minVal[i];
        }

        return maxVal;
    }

}
