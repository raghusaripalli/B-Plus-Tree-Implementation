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
}
