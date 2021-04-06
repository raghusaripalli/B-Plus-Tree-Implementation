import java.util.Arrays;

public class InsertUtils {
    static int m;

    public static void updateParent(LeafNode leafNode, KeyValuePair keyValuePair) {
        // Add new key to parent for proper indexing
        int newParentKey = keyValuePair.key;
        leafNode.parent.getKeys()[leafNode.parent.getDegree() - 1] = newParentKey;
        Arrays.sort(leafNode.parent.getKeys(), 0, leafNode.parent.getDegree());
    }

    public static void newParentNode(LeafNode leafNode, KeyValuePair[] halfDict) {
        // Create internal node to serve as parent, use dictionary midpoint key
        Integer[] parent_keys = new Integer[m];
        parent_keys[0] = halfDict[0].key;
        InternalNode parent = new InternalNode(m, parent_keys);
        leafNode.parent = parent;
        parent.appendChildPointer(leafNode);
    }

    // From given index copy pairs from leaf node to new array
    // Also delete the copied pairs from the leaf node
    static KeyValuePair[] bifurcatePairs(LeafNode leafNode, int index) {
        KeyValuePair[] remainingPairs = new KeyValuePair[m];
        for (int i = index; i < leafNode.getDictionary().length; i++) {
            remainingPairs[i - index] = leafNode.getDictionary()[i];
            leafNode.delete(i);
        }
        return remainingPairs;
    }

    // From given index copy keys from keys to new array
    // Also delete the copied keys from the keys array
    public static Integer[] bifurcateKeys(Integer[] keys, int index) {
        Integer[] remainingKeys = new Integer[m];
        keys[index] = null;
        for (int i = index + 1; i < keys.length; i++) {
            remainingKeys[i - index - 1] = keys[i];
            keys[i] = null;
        }
        return remainingKeys;
    }

    // From given index copy children from in.children to new Node array
    // Also delete the copied child pointers from the in.children array
    private static Node[] bifurcateChildPointers(InternalNode in, int index) {
        Node[] remainingPointers = new Node[m + 1];
        for (int i = index + 1; i < in.getChildren().length; i++) {
            remainingPointers[i - index - 1] = in.getChildren()[i];
            in.removePointer(i);
        }
        return remainingPointers;
    }

    // Internal Node is divided and update the parent appropriately
    static void bifurcateInternalNode(InternalNode in) {
        // Acquire parent
        InternalNode parent = in.parent;

        // Split keys and pointers in half
        int midpoint = Tree.midIndex;
        int newParentKey = in.getKeys()[midpoint];
        Integer[] remainingKeys = InsertUtils.bifurcateKeys(in.getKeys(), midpoint);
        Node[] remainingPointers = bifurcateChildPointers(in, midpoint);

        // Change degree of original InternalNode in
        in.setDegree(CommonUtils.firstIndexOfNull(in.getChildren()));

        // Create new sibling internal node and add half of keys and pointers
        InternalNode sibling = new InternalNode(m, remainingKeys, remainingPointers);
        for (Node pointer : remainingPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        // Make internal nodes siblings of one another
        sibling.setRightSibling(in.getRightSibling());
        if (sibling.getRightSibling() != null) {
            sibling.getRightSibling().setLeftSibling(sibling);
        }
        in.setRightSibling(sibling);
        sibling.setLeftSibling(in);

        if (parent == null) {
            newRoot(in, newParentKey, sibling);
            return;
        }
        addToExistingParent(in, parent, newParentKey, sibling);
    }

    // Add key to parent existing
    // Set up pointer to new sibling
    private static void addToExistingParent(InternalNode in, InternalNode parent, int newParentKey, InternalNode sibling) {
        parent.getKeys()[parent.getDegree() - 1] = newParentKey;
        Arrays.sort(parent.getKeys(), 0, parent.getDegree());


        int pointerIndex = parent.findIndexOfChildPointer(in) + 1;
        parent.insertChildPointer(sibling, pointerIndex);
        sibling.parent = parent;
    }

    // Creates new parent
    // Add pointers from children to parent
    // Make the parent as root
    private static void newRoot(InternalNode in, int newParentKey, InternalNode sibling) {
        Integer[] keys = new Integer[m];
        keys[0] = newParentKey;
        InternalNode newRoot = new InternalNode(m, keys);
        newRoot.appendChildPointer(in);
        newRoot.appendChildPointer(sibling);
        Tree.root = newRoot;

        in.parent = newRoot;
        sibling.parent = newRoot;
    }

    // Create new LeafNode that holds the other half
    // Update child pointers of parent node
    // Make leaf nodes siblings of one another
    public static void createNewLeafNodeUsingRemainingPairs(LeafNode leafNode, KeyValuePair[] remainingPairs) {
        LeafNode newLeafNode = new LeafNode(m, remainingPairs, leafNode.parent);
        int pointerIndex = leafNode.parent.findIndexOfChildPointer(leafNode) + 1;
        leafNode.parent.insertChildPointer(newLeafNode, pointerIndex);
        newLeafNode.setRightSibling(leafNode.getRightSibling());
        if (newLeafNode.getRightSibling() != null) {
            newLeafNode.getRightSibling().setLeftSibling(newLeafNode);
        }
        leafNode.setRightSibling(newLeafNode);
        newLeafNode.setLeftSibling(leafNode);
    }
}
