package com.fachriza.imagequadtree.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageData {
    private byte[] redBuffer;
    private byte[] greenBuffer;
    private byte[] blueBuffer;
    private int width;
    private int height;

    private String format;

    public ImageData(BufferedImage img, String format) {
        this.width = img.getWidth();
        this.height = img.getHeight();

        this.format = format;

        // int[] packedBuffer = new int[width * height];
        // img.getRGB(0, 0, width, height, packedBuffer, 0, width); // Convert to array

        // i think this is faster
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        int[] packedBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        int size = width * height;
        redBuffer = new byte[size];
        greenBuffer = new byte[size];
        blueBuffer = new byte[size];
        for (int i = 0; i < packedBuffer.length; i++) {
            int rgb = packedBuffer[i];
            redBuffer[i] = (byte) ((rgb >> 16) & 0xFF);
            greenBuffer[i] = (byte) ((rgb >> 8) & 0xFF);
            blueBuffer[i] = (byte) (rgb & 0xFF);
        }
    }

    public String getFormat() {
        return format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
