package com.fachriza.imagequadtree.quadtree;

public class ImageQuadTree {
    private byte[] averageColor;
    private ImageQuadTree[] children;

    public ImageQuadTree(byte red, byte green, byte blue) {
        this.averageColor = new byte[3];
        this.averageColor[0] = red;
        this.averageColor[1] = green;
        this.averageColor[2] = blue;
        this.children = null;
    }

    public ImageQuadTree[] getChildrenArray() {
        return children;
    }

    public ImageQuadTree getChildren(int idx) {
        if (children == null)
            return null;
        return children[idx];
    }

    public void setChildrenArray(ImageQuadTree[] children) {
        this.children = children;
    }

    public void setChildren(int idx, ImageQuadTree child) {
        if (children == null) {
            children = new ImageQuadTree[4];
        }
        children[idx] = child;
    }

    public int getDepth() {
        if (children != null) {
            int maxDepth = 0;
            for (ImageQuadTree child : children) {
                int thisDepth = child.getDepth();
                if (thisDepth > maxDepth) {
                    maxDepth = thisDepth;
                }
            }
            return 1 + maxDepth;
        }
        return 1;
    }

    public byte[] getAverageColor() {
        return averageColor;
    }

    public boolean isLeafNode() {
        return children == null;
    }
}
