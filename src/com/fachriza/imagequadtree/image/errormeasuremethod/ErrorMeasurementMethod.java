package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;

public abstract class ErrorMeasurementMethod {
    protected ImageData imageData;

    public ErrorMeasurementMethod(ImageData imageData) {
        this.imageData = imageData;
    }

    public abstract float getErrorValue(
            float[] mean,
            int x,
            int y,
            int width,
            int height);

    public abstract float getMaxErrorValue();
}
