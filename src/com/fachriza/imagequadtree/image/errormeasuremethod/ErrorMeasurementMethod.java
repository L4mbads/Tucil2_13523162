package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;

public abstract class ErrorMeasurementMethod {
    protected ImageData imageData;

    public abstract float getErrorValue(int x, int y, int width, int height);
}
