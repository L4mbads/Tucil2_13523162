package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.utils.ImageUtil;

public class SSIMError extends VarianceError {
    public SSIMError(ImageData imageData) {
        super(imageData);
    }

    @Override
    public float getErrorValue(float[] mean, int x, int y, int width, int height) {
        final float C2 = 0.01f;
        float[] variance = getVariance(mean, x, y, width, height);
        float[] SSIM = {
                C2 / (variance[0] + C2),
                C2 / (variance[1] + C2),
                C2 / (variance[2] + C2) };
        float perceivedSSIM = (SSIM[0] * ImageUtil.SRGB_LUMINANCE_CONSTANTS[0]
                + SSIM[1] * ImageUtil.SRGB_LUMINANCE_CONSTANTS[1]
                + SSIM[2] * ImageUtil.SRGB_LUMINANCE_CONSTANTS[2]);

        return (1.0f - perceivedSSIM);
    }

    @Override
    public float getMaxErrorValue() {
        return 1.0f;
    }

}
