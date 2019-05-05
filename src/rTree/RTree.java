package rTree;

import rTree.nodes.AbstractNode;
import rTree.nodes.ExternalNode;
import rTree.nodes.InternalNode;
import rTree.nodes.Rectangle;
import rTree.splits.Split;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import rTree.Main;

public class RTree implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Split heuristic;

    private int rootId;


    public RTree(Split heuristic) {
        AbstractNode root = new ExternalNode();
        this.heuristic = heuristic;
        rootId = root.id;
        //System.out.println("rootId " + rootId);
        root.writeToDisk();
        Main.DISK_ACCESSES++;
    }

    public RTree(Split heuristic, AbstractNode root) {
        rootId = root.id;
        this.heuristic = heuristic;
        root.writeToDisk();
        Main.DISK_ACCESSES++;
    }

    public AbstractNode getRoot() {
        return Main.readFromDisk(rootId);
    }

    public List<Rectangle> search(Rectangle rectangle) {
        return searchIn(rootId, rectangle);
    }

    private List<Rectangle> searchIn(int nodeId, Rectangle rectangle) {

        List<Rectangle> results = new ArrayList<>();
        AbstractNode node = Main.readFromDisk(nodeId);
        Main.DISK_ACCESSES++;

        if (node.mbr.intersects(rectangle)) {
            if (node.isExternalNode()) {
                return node.rectangles.stream().
                        filter(rect -> rect.intersects(rectangle)).
                        collect(Collectors.toList());
            } else {
                for (Integer childId : node.childrenIds) {
                    results.addAll(searchIn(childId, rectangle));
                }
            }
        }

        return results;
    }

    public void insert(Rectangle rectangle) {
    	
    	//System.out.println("insert id " + rootId);
        AbstractNode[] newNodes = insertIn(rootId, rectangle);
        if (newNodes[1] == null) {
            rootId = newNodes[0].id;
            //System.out.println("rootID insert if " +rootId);
        } else {
            AbstractNode newRoot = new InternalNode(new ArrayList<>(Arrays.asList(newNodes[0].id, newNodes[1].id)),
                    new ArrayList<>(Arrays.asList(newNodes[0].mbr , newNodes[1].mbr)));
            rootId = newRoot.id;
            //System.out.println("rootID insert else " +rootId);
            newRoot.writeToDisk();
            Main.DISK_ACCESSES++;
        }
    }

    private AbstractNode[] insertIn(int nodeId, Rectangle rectangle) {
    	//System.out.println("insertin id " + nodeId);
        AbstractNode node = Main.readFromDisk(nodeId);
        Main.DISK_ACCESSES++;

        // node is a ExternalNode, we just insert
        if (node.isExternalNode()) {
            node.addRectangle(rectangle);

        } else {

            int originalIndex;
            AbstractNode[] theNewNodes;

            List<Integer> differences = new ArrayList<>();

            List<Rectangle> childMBRs = node.rectangles;

            // get all new possible MBRs and get how much they would grow
            for (Rectangle childMBR : childMBRs) {
                Rectangle newMBR = childMBR.minimumBoundingRectangle(rectangle);
                differences.add(childMBR.differenceArea(newMBR));
            }
            // get the minimum growth
            int minGrowth = Collections.min(differences);
            //get all indexes with that minimum growth
            List<Integer> indexesOfMin = indexOfAll(minGrowth, differences);

            // if there is more than 1 possible node in which to insert
            if (indexesOfMin.size() > 1) {
                // decide by the minimum area
                List<Integer> areas = new ArrayList<>();
                for (Integer index : indexesOfMin) {
                    areas.add(childMBRs.get(index).getArea());
                }
                // get the minimum area
                int minArea = Collections.min(areas);
                //get all indexes with that minimum area
                List<Integer> indexesOfMinArea = indexOfAll(minArea, areas);

                // if there is still a draw
                if (indexesOfMinArea.size() > 1) {
                    // choose a random one
                    Random rand = new Random();
                    int randomIndex = rand.nextInt(indexesOfMinArea.size());

                    originalIndex = indexesOfMin.get(indexesOfMinArea.get(randomIndex));
                    //newNodeId = insertIn(node.childrenIds.get(originalIndex), rectangle);
                    theNewNodes = insertIn(node.childrenIds.get(originalIndex), rectangle);

                } else { //only one min area
                    // get the original index
                    originalIndex = indexesOfMin.get(indexesOfMinArea.get(0));
                    //newNodeId = insertIn(node.childrenIds.get(originalIndex), rectangle);
                    theNewNodes = insertIn(node.childrenIds.get(originalIndex), rectangle);
                }

            } else { //only one minimum
                // override the old node
                originalIndex = indexesOfMin.get(0);
                //newNodeId = insertIn(node.childrenIds.get(originalIndex), rectangle);
                theNewNodes = insertIn(node.childrenIds.get(originalIndex), rectangle);

            }

            // update the changes
            node.childrenIds.set(originalIndex, theNewNodes[0].id);
            node.rectangles.set(originalIndex, theNewNodes[0].mbr);
            //check if there is a new node
            if (theNewNodes[1] != null) {
                node.addNode(theNewNodes[1].id, theNewNodes[1].mbr);
            }

            // update the MBR
            node.updateMBR();
        }

        node.writeToDisk();
        Main.DISK_ACCESSES++;

        // TODO: FIX PLS
        AbstractNode[] newNodes = new AbstractNode[]{node, null};
        // check if overflow
        if (node.isOverflow()) {
            newNodes = heuristic.split(node);
        }

        return newNodes;
    }

    public void clear() {
        AbstractNode root = new ExternalNode();
        rootId = root.id;
        root.writeToDisk();
    }

    private List<Integer> indexOfAll(int number, List<Integer> list) {
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
            if (number == list.get(i))
                indexList.add(i);
        return indexList;
    }

}