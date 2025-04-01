package com.fachriza.imagequadtree.utils;

import java.io.File;
import java.io.IOException;

import com.fachriza.imagequadtree.image.ImageData;

public class ImageUtil {
    // in REC. 601-7 / BT.701-7 colorspace
    public static final float[] SRGB_LUMINANCE_CONSTANTS = { 0.299f, 0.587f, 0.114f };

    public static float[] getAverageColor(ImageData imageData, int x, int y, int width, int height) {
        float[] sum = { 0, 0, 0 };
        int count = width * height;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                sum[0] += (imageData.getRed(j, i) & 0xff);
                sum[1] += (imageData.getGreen(j, i) & 0xff);
                sum[2] += (imageData.getBlue(j, i) & 0xff);
            }
        }

        for (int i = 0; i < 3; i++) {
            sum[i] /= count;
        }
        return sum;
    }

    public static String getFormatName(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "jpeg";
        if (fileName.endsWith(".png"))
            return "png";
        return null;
    }

}
