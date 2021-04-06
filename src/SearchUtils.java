public class SearchUtils {
    int getIndex(Integer[] keys, Integer key, int size) {
        int i;
        for (i = 0; i < size; i++)
            if (key < keys[i])
                break;
        return i;
    }

    LeafNode searchLeafNode(int key) {

        // find appropriate index based on key
        int index = getIndex(bplustree.root.keys, key, bplustree.root.degree - 1);

        // If child is leaf node return it, else continue search
        Node child = bplustree.root.children[index];
        if (child instanceof LeafNode)
            return (LeafNode) child;
        else
            return searchLeafNode((InternalNode) child, key);
    }

    LeafNode searchLeafNode(InternalNode node, int key) {
        int index = getIndex(node.keys, key, node.degree - 1);

        // If child is leaf node return it, else continue search
        Node childNode = node.children[index];
        if (childNode instanceof LeafNode)
            return (LeafNode) childNode;
        else
            return searchLeafNode((InternalNode) node.children[index], key);
    }
}
