package com.fachriza.imagequadtree.image.errormeasuremethod;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.utils.ImageUtil;

public class SSIMError extends VarianceError {

    public SSIMError(ImageData imageData) {
        super(imageData);
    }

    @Override
    public float getErrorValue(
            float[] mean,
            int x,
            int y,
            int width,
            int height) {
        /**
         * C2 = ((K2)(L))^2
         *
         * where:
         * K2 << 1, 0.03 was recommended
         * L = color dynamic range, 255 for 8-bit channel
         * thus:
         * C2 = ((0.03)(255))^2
         * C2 = 58.5225
         */
        final float C2 = 58.5225f;

        float[] variance = getVariance(mean, x, y, width, height);

        float[] SSIM = {
                C2 / (variance[0] + C2),
                C2 / (variance[1] + C2),
                C2 / (variance[2] + C2) };

        float perceivedSSIM = (SSIM[0] * ImageUtil.SRGB_LUMINANCE_CONSTANTS[0]
                + SSIM[1] * ImageUtil.SRGB_LUMINANCE_CONSTANTS[1]
                + SSIM[2] * ImageUtil.SRGB_LUMINANCE_CONSTANTS[2]);

        // Because SSIM denotes "similarity" and not "error value", we invert the value
        return (1.0f - perceivedSSIM);
    }

    @Override
    public float getMaxErrorValue() {
        return 1.0f;
    }

}
