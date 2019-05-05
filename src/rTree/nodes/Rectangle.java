package rTree.nodes;

import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

public class Rectangle implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int x, y, height, width;
    private static final int max_vertix = 500000;
    private static final int max_length = 100;

    public Rectangle() {
        Random rand = new Random();
        this.x = rand.nextInt(max_length);
        this.y = rand.nextInt(max_vertix);
        this.height = rand.nextInt(max_length) + 1;
        this.width = rand.nextInt(max_length) + 1;
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public int getArea(){
        return  height*width;
    }

    public boolean intersects(Rectangle rectangle){
        return (this.x <= rectangle.x + rectangle.width) &&
                (this.x + width >= rectangle.x) &&
                (this.y <= rectangle.y + rectangle.height) &&
                (this.y + height >= rectangle.y);
    }

    public Rectangle getIntersectionRect(Rectangle rectangle) {
        int upX = x + width;
        int upY = y + height;
        int rectX = rectangle.x + rectangle.width;
        int rectY = rectangle.y + rectangle.height;

        int bbX = Math.max(x, rectangle.x);
        int bbY = Math.max(y, rectangle.y);

        int bbWidth = Math.min(upX, rectX) - bbX;
        int bbHeight = Math.min(upY, rectY) - bbY;

        return new Rectangle(bbX, bbY, bbWidth, bbHeight);
    }

    public Rectangle minimumBoundingRectangle(Rectangle rectangle) {

        int upX = x + width;
        int upY = y + height;
        int rectX = rectangle.x + rectangle.width;
        int rectY = rectangle.y + rectangle.height;

        int bbX = Math.min(x, rectangle.x);
        int bbY = Math.min(y, rectangle.y);

        int bbWidth = Math.max(upX, rectX) - bbX;
        int bbHeight = Math.max(upY, rectY) - bbY;

        return new Rectangle(bbX, bbY, bbWidth, bbHeight);
    }

    public int differenceArea(Rectangle rect) {
        return Math.abs(this.getArea() - rect.getArea());
    }

    @Override
    public String toString() {

        return "(" + x + ", " + y + ")(" + (x + width) + ", " + (y + height) + ")";
    }

    public int[] getVertices() {
        return new int[]{x, y, x + width, y + height};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) obj;
            return x == rectangle.x &&
                    y == rectangle.y &&
                    width == rectangle.width &&
                    height == rectangle.height;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    public int[] getDimension(int dimension) {
        if (dimension == 1) {
            return new int[]{x, x + width};
        } else {
            return new int[]{y, y + height};
        }
    }
}
