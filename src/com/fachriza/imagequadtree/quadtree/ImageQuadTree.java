package com.fachriza.imagequadtree.quadtree;

public class ImageQuadTree {

    public final byte[] averageColor;
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    private ImageQuadTree[] children;

    public ImageQuadTree(float[] averageColor, int x, int y, int width, int height) {
        this.averageColor = new byte[] {
                (byte) averageColor[0],
                (byte) averageColor[1],
                (byte) averageColor[2]
        };
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.children = null;
    }

    public ImageQuadTree[] getChildrenArray() {
        return children;
    }

    public void setChildrenArray(ImageQuadTree[] children) {
        this.children = children;
    }

    public boolean isLeafNode() {
        return children == null;
    }

    public int getDepth() {
        if (children != null) {
            int maxDepth = 0;
            for (ImageQuadTree child : children) {
                int thisDepth = child.getDepth();
                maxDepth = thisDepth > maxDepth ? thisDepth : maxDepth;
            }
            return 1 + maxDepth;
        }
        return 1;
    }

}
