package rTree.splits;

import rTree.nodes.AbstractNode;
import rTree.nodes.ExternalNode;
import rTree.nodes.InternalNode;

import java.io.Serializable;

public interface Split extends Serializable {

    AbstractNode[] split(AbstractNode node);

    AbstractNode[] splitLeaf(ExternalNode leaf);

    AbstractNode[] splitNode(InternalNode node);

}
