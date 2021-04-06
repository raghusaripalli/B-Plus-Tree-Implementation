/*
 * Utilities used for searching in B+ tree
 */
public class SearchUtils {
    // returns index of key or the next element of key in keys array
    int getIndex(Integer[] keys, Integer key, int size) {
        int i;
        for (i = 0; i < size; i++)
            if (key < keys[i])
                break;
        return i;
    }

    // Given the key return the leaf node in which it might exist
    LeafNode searchLeafNode(int key) {

        // find appropriate index based on key
        int index = getIndex(Tree.root.getKeys(), key, Tree.root.getDegree() - 1);

        // If child is leaf node return it, else continue search
        Node child = Tree.root.getChildren()[index];
        if (child instanceof LeafNode)
            return (LeafNode) child;
        else
            return searchLeafNode((InternalNode) child, key);
    }

    // Given internal node and key, return the leaf node in which key might be present
    LeafNode searchLeafNode(InternalNode node, int key) {
        int index = getIndex(node.getKeys(), key, node.getDegree() - 1);

        // If child is leaf node return it, else continue search
        Node childNode = node.getChildren()[index];
        if (childNode instanceof LeafNode)
            return (LeafNode) childNode;
        else
            return searchLeafNode((InternalNode) node.getChildren()[index], key);
    }
}
