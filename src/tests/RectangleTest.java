package tests;

import org.junit.Before;
import org.junit.Test;
import rTree.model.geometric.Rectangle;

import static org.junit.Assert.assertTrue;

public class RectangleTest {

    private Rectangle rect1;
    private Rectangle rect2;

    @Before
    public void setUp() throws Exception {
        rect1 = new Rectangle(1, 1, 1, 1);
        rect2 = new Rectangle(2, 2, 2, 2);
    }

    @Test
    public void intersects() throws Exception {
        assertTrue(rect1.intersects(rect2));
        assertTrue(rect2.intersects(rect1));
        assertTrue(rect2.intersects(rect2));
    }

}