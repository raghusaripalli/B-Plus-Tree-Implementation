import java.util.Arrays;

public class InsertUtils {
    static int m;

    public static void updateParent(LeafNode leafNode, KeyValuePair keyValuePair) {
        // Add new key to parent for proper indexing
        int newParentKey = keyValuePair.key;
        leafNode.parent.keys[leafNode.parent.degree - 1] = newParentKey;
        Arrays.sort(leafNode.parent.keys, 0, leafNode.parent.degree);
    }

    public static void newParentNode(LeafNode leafNode, KeyValuePair[] halfDict) {
        // Create internal node to serve as parent, use dictionary midpoint key
        Integer[] parent_keys = new Integer[m];
        parent_keys[0] = halfDict[0].key;
        InternalNode parent = new InternalNode(m, parent_keys);
        leafNode.parent = parent;
        parent.appendChildPointer(leafNode);
    }

    static KeyValuePair[] splitDictionary(LeafNode ln, int split) {
        KeyValuePair[] dictionary = ln.dictionary;
        KeyValuePair[] halfDict = new KeyValuePair[m];

        // Copy half of the values into halfDict
        for (int i = split; i < dictionary.length; i++) {
            halfDict[i - split] = dictionary[i];
            ln.delete(i);
        }

        return halfDict;
    }

    public static Integer[] splitKeys(Integer[] keys, int split) {

        Integer[] halfKeys = new Integer[m];

        // Remove split-indexed value from keys
        keys[split] = null;

        // Copy half of the values into halfKeys while updating original keys
        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i];
            keys[i] = null;
        }
        return halfKeys;
    }

    private static Node[] splitChildPointers(InternalNode in, int split) {
        Node[] pointers = in.children;
        Node[] halfPointers = new Node[m + 1];

        // Copy half of the values into halfPointers while updating original keys
        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            in.removePointer(i);
        }

        return halfPointers;
    }

    static void splitInternalNode(InternalNode in) {
        // Acquire parent
        InternalNode parent = in.parent;

        // Split keys and pointers in half
        int midpoint = CommonUtils.findMidIndex();
        int newParentKey = in.keys[midpoint];
        Integer[] halfKeys = InsertUtils.splitKeys(in.keys, midpoint);
        Node[] halfPointers = splitChildPointers(in, midpoint);

        // Change degree of original InternalNode in
        in.degree = CommonUtils.firstIndexOfNull(in.children);

        // Create new sibling internal node and add half of keys and pointers
        InternalNode sibling = new InternalNode(m, halfKeys, halfPointers);
        for (Node pointer : halfPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        // Make internal nodes siblings of one another
        sibling.rightSibling = in.rightSibling;
        if (sibling.rightSibling != null) {
            sibling.rightSibling.leftSibling = sibling;
        }
        in.rightSibling = sibling;
        sibling.leftSibling = in;

        if (parent == null) {
            // Create new root node and add midpoint key and pointers
            Integer[] keys = new Integer[m];
            keys[0] = newParentKey;
            InternalNode newRoot = new InternalNode(m, keys);
            newRoot.appendChildPointer(in);
            newRoot.appendChildPointer(sibling);
            bplustree.root = newRoot;

            // Add pointers from children to parent
            in.parent = newRoot;
            sibling.parent = newRoot;
        } else {
            // Add key to parent
            parent.keys[parent.degree - 1] = newParentKey;
            Arrays.sort(parent.keys, 0, parent.degree);

            // Set up pointer to new sibling
            int pointerIndex = parent.findIndexOfChildPointer(in) + 1;
            parent.insertChildPointer(sibling, pointerIndex);
            sibling.parent = parent;
        }
    }
}
