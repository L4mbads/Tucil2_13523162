package com.fachriza.imagequadtree.quadtree;

public class ImageQuadTree {
    private byte[] averageColor;
    private ImageQuadTree[] children;
    private float error;
    // public boolean isChildrenValid;

    public ImageQuadTree(byte red, byte green, byte blue, float error) {
        this.averageColor = new byte[] { red, green, blue };
        this.children = null;
        this.error = error;
        // isChildrenValid = true;
    }

    public ImageQuadTree[] getChildrenArray() {
        return children;
    }

    public void setChildrenArray(ImageQuadTree[] children) {
        this.children = children;
    }

    public ImageQuadTree getChildren(int idx) {
        if (children == null)
            return null;
        return children[idx];
    }

    public void setChildren(int idx, ImageQuadTree child) {
        if (children == null) {
            children = new ImageQuadTree[4];
        }
        children[idx] = child;
    }

    public byte[] getAverageColor() {
        return averageColor;
    }

    public float getError() {
        return error;
    }

    public void setError(float error) {
        this.error = error;
    }

    public boolean isLeafNode() {
        return children == null;
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
}
