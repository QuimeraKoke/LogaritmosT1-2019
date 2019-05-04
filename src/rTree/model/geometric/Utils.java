package rTree.model.geometric;

import java.util.List;

public final class Utils {

    public static Rectangle minBoundingRectangle(List<Rectangle> rectangles) {

        Rectangle result = new Rectangle(0,0,0,0);

        if (!rectangles.isEmpty()) {
            result = rectangles.get(0);
            if (rectangles.size() > 1) {
                for (int i = 1; i < rectangles.size(); i++) {
                    result = result.minimumBoundingRectangle(rectangles.get(i));
                }
            }
        }
        return result;
    }

}
