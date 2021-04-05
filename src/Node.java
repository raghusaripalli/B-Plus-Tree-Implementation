/*
 *  Base class for Internal and Leaf Node
 */
public class Node {
    // Parent is common to both Internal and leaf nodes
    // and that parent can be always an Internal node.
    // Thus the only field here is the parent.
    InternalNode parent;
}
