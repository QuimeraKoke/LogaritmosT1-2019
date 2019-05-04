package rTree.model.splits;

import rTree.model.INode;
import rTree.model.InnerNode;
import rTree.model.Leaf;

import java.io.Serializable;

public interface Split extends Serializable {

    INode[] split(INode node);

    INode[] splitLeaf(Leaf leaf);

    INode[] splitNode(InnerNode node);

}
