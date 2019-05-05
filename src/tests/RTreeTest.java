package tests;

import org.junit.Before;
import org.junit.Test;
import rTree.RTree;
import rTree.nodes.Rectangle;
import rTree.splits.LinearSplit;
import rTree.splits.QuadraticSplit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static rTree.Config.MAX_M;
import static rTree.Config.MIN_M;

public class RTreeTest {

    private static final int RANGE_ORIGIN = 500000;
    private static final int RANGE_LENGTH = 100;

    private RTree tree;
    private Rectangle someRect;
    private RTree linearTree;
    private RTree quadraticTree;

    @Before
    public void setUp() throws Exception {
        tree = new RTree(null);
        someRect = new Rectangle(1, 1, 1, 1);
        linearTree = new RTree(new LinearSplit());
        quadraticTree = new RTree(new QuadraticSplit());
    }

    @Test
    public void searchEmpty() throws Exception {
        assertTrue(tree.search(new Rectangle(1, 1, 1, 1)).isEmpty());
    }

    @Test
    public void insert1InLeaf() throws Exception {
        tree.insert(someRect);
        assertEquals(someRect, tree.getRoot().getMBR());
        assertFalse(tree.search(new Rectangle(1, 1, 1, 1)).isEmpty());
    }

    @Test
    public void insert2InLeaf() throws Exception {
        insert1InLeaf();
        Rectangle someOtherRect = new Rectangle(2, 2, 2, 2);
        tree.insert(someOtherRect);
        assertEquals("There should be 2 rectangles in root", 2,
                tree.getRoot().getRectangles().size());

        List<IRectangle> found = tree.search(someRect);
        assertEquals("Should have found 2 rectangles", 2, found.size());

    }

    @Test
    public void insert2InInnerNode() throws Exception {
        List<Integer> childList = new ArrayList<>();
        List<IRectangle> mbrList = new ArrayList<>();
        INode aLeaf = new Leaf();
        aLeaf.writeToDisk();
        childList.add(aLeaf.getId());
        mbrList.add(aLeaf.getMBR());

        INode node = new InnerNode(childList, mbrList);
        node.writeToDisk();

        RTree RTree = new RTree(null, node);
        RTree.insert(new Rectangle(1, 1, 1, 1));
        RTree.insert(new Rectangle(3, 3, 3, 3));
        INode aNode = DiskUtils.readFromDisk(RTree.getRoot().getChildrenIds().get(0));
        assertEquals(aNode.getRectangles().size(), 2);
        assertArrayEquals(aNode.getMBR().getVertices(),
                new Rectangle(1, 1, 5, 5).getVertices());
        assertArrayEquals(RTree.getRoot().getMBR().getVertices(),
                new Rectangle(1, 1, 5, 5).getVertices());
    }

    @Test
    public void insertLinearTree() throws Exception {
        setUp();
        assertTrue(linearTree.getRoot().getRectangles().isEmpty());
        for (int i = 0; i < MAX_M; i++) {
            linearTree.insert(randomRectangle());
        }

        assertEquals(linearTree.getRoot().getRectangles().size(), MAX_M);
        assertTrue(linearTree.getRoot().isLeaf());
        linearTree.insert(randomRectangle());
        assertFalse(linearTree.getRoot().isLeaf());

        assertEquals(linearTree.getRoot().getRectangles().size(), 2);
        INode aNode1 = DiskUtils.readFromDisk(linearTree.getRoot().getChildrenIds().get(0));
        assertTrue(aNode1.getRectangles().size() >= MIN_M);
        INode aNode2 = DiskUtils.readFromDisk(linearTree.getRoot().getChildrenIds().get(1));
        assertTrue(aNode2.getRectangles().size() >= MIN_M);
    }

    @Test
    public void insertQuadraticTree() throws Exception {
        assertTrue(quadraticTree.getRoot().getRectangles().isEmpty());
        for (int i = 0; i < MAX_M; i++) {
            quadraticTree.insert(randomRectangle());
        }

        assertEquals(quadraticTree.getRoot().getRectangles().size(), MAX_M);
        assertTrue(quadraticTree.getRoot().isLeaf());
        quadraticTree.insert(randomRectangle());
        assertFalse(quadraticTree.getRoot().isLeaf());

        assertEquals(quadraticTree.getRoot().getRectangles().size(), 2);
        INode aNode1 = DiskUtils.readFromDisk(quadraticTree.getRoot().getChildrenIds().get(0));
        assertTrue(aNode1.getRectangles().size() >= MIN_M);
        INode aNode2 = DiskUtils.readFromDisk(quadraticTree.getRoot().getChildrenIds().get(1));
        assertTrue(aNode2.getRectangles().size() >= MIN_M);
    }

    private Rectangle randomRectangle() {
        Random rand = new Random();
        return new Rectangle(rand.nextInt(RANGE_ORIGIN), rand.nextInt(RANGE_ORIGIN),
                rand.nextInt(RANGE_LENGTH) + 1, rand.nextInt(RANGE_LENGTH) + 1);
    }
}