package com.fachriza.imagequadtree.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageData {

    private byte[] redBuffer;
    private byte[] greenBuffer;
    private byte[] blueBuffer;

    public final int width;
    public final int height;
    public final String format;

    public ImageData(BufferedImage image, String format) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.format = format;

        // Get color array
        // int[] packedBuffer = new int[width * height];
        // img.getRGB(0, 0, width, height, packedBuffer, 0, width); // Convert to array

        // I think this is faster
        // BufferedImage image = new BufferedImage(width, height,
        // BufferedImage.TYPE_INT_RGB);
        // Graphics2D g = image.createGraphics();
        // g.drawImage(img, 0, 0, null);
        // g.dispose();
        // int[] packedBuffer = ((DataBufferInt)
        // image.getRaster().getDataBuffer()).getData();
        // for (int i = 0; i < packedBuffer.length; i++) {
        // int rgb = packedBuffer[i];
        // redBuffer[i] = (byte) ((rgb >> 16) & 0xFF);
        // greenBuffer[i] = (byte) ((rgb >> 8) & 0xFF);
        // blueBuffer[i] = (byte) (rgb & 0xFF);
        // }

        // No this is faster. Wtf was that code
        int size = width * height;

        redBuffer = new byte[size];
        greenBuffer = new byte[size];
        blueBuffer = new byte[size];
        byte[] pixelData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int numberOfValues = hasAlphaChannel ? 4 : 3;
        int valueIndex = hasAlphaChannel ? 1 : 0;

        for (int i = 0; valueIndex + 2 < pixelData.length; valueIndex += numberOfValues, i++) {
            blueBuffer[i] = pixelData[valueIndex];
            greenBuffer[i] = pixelData[valueIndex + 1];
            redBuffer[i] = pixelData[valueIndex + 2];
        }
    }

    public int getBufferIndex(int x, int y) {
        return y * width + x;
    }

    public byte getRed(int x, int y) {
        return redBuffer[getBufferIndex(x, y)];
    }

    public byte getGreen(int x, int y) {
        return greenBuffer[getBufferIndex(x, y)];
    }

    public byte getBlue(int x, int y) {
        return blueBuffer[getBufferIndex(x, y)];
    }

}
