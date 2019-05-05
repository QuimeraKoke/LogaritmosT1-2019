package rTree.splits;

import rTree.nodes.AbstractNode;
import rTree.nodes.InternalNode;
import rTree.nodes.ExternalNode;

import java.io.Serializable;

public interface Split extends Serializable {

    AbstractNode[] split(AbstractNode node);

    AbstractNode[] splitExternalNode(ExternalNode enode);

    AbstractNode[] splitInternalNode(InternalNode inode);

}
