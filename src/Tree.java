import java.util.ArrayList;
import java.util.List;

/**
 * B + Tree Implementation
 * APIs: Insert, Search, Search in range and Delete
 */

public class Tree {
    // Class Variables
    int m;
    LeafNode firstLeaf;
    static InternalNode root;
    final SearchUtils su = new SearchUtils();

    // Constructor
    public Tree(int m) {
        this.m = m;
        InsertUtils.m = m;
        CommonUtils.m = m;
        root = null;
    }

    /**
     * Inserts a key-value pair into the B+ Tree.
     *
     * @param key   unique integer value
     * @param value value corresponding to that key
     */
    public void insert(int key, double value) {
        // If first leaf is empty
        // create a new leaf node with provided key and value
        // and assign to first leaf
        if (firstLeaf == null) {
            this.firstLeaf = new LeafNode(this.m, new KeyValuePair(key, value));
            return;
        }

        // Find leaf node to insert into
        LeafNode leafNode = (root == null) ? this.firstLeaf : su.searchLeafNode(key);

        // If Insertion is success then return;
        if (leafNode.insert(new KeyValuePair(key, value)))
            return;

        // Insertion fails due to overflow, then
        // Sort all the dictionary pairs with the included pair to be inserted
        leafNode.getDictionary()[leafNode.getNumPairs()] = new KeyValuePair(key, value);
        leafNode.setNumPairs(leafNode.getNumPairs() + 1);
        CommonUtils.orderPairsInAscending(leafNode.getDictionary());

        // divide the pairs into two halves
        int midIndex = CommonUtils.findMidIndex();
        KeyValuePair[] remainingPairs = InsertUtils.bifurcatePairs(leafNode, midIndex);

        if (leafNode.parent == null) {
            InsertUtils.newParentNode(leafNode, remainingPairs);
        } else {
            InsertUtils.updateParent(leafNode, remainingPairs[0]);
        }

        InsertUtils.createNewLeafNodeUsingRemainingPairs(leafNode, remainingPairs);

        // Root is null then parent will be new root
        if (root == null) {
            root = leafNode.parent;
            return;
        }

        InternalNode in = leafNode.parent;
        while (in != null) {
            if (in.getDegree() == in.getMaxDegree() + 1) {
                InsertUtils.splitInternalNode(in);
            } else {
                break;
            }
            in = in.parent;
        }
    }

    /**
     * Searches for the given key in B+ tree
     *
     * @param key unique integer value
     * @return value if key is found else null
     */
    public Double search(int key) {
        // Return Null when tree is empty
        if (firstLeaf == null)
            return null;

        // get leaf node in which value may exist
        LeafNode leafNode = (root == null) ? this.firstLeaf : su.searchLeafNode(key);

        // do binary search to find the value
        int index = CommonUtils.binarySearch(leafNode.getDictionary(), leafNode.getNumPairs(), key);

        // Return null if index is negative else value present at the index
        return index < 0 ? null : leafNode.getDictionary()[index].value;
    }

    /**
     * Searches for all the keys in range [lowerBound, upperBound]
     *
     * @param lowerBound first key value in range
     * @param upperBound last key value in range
     * @return all values for the keys found in the range lowerBound <= key <= upperBound
     */
    public List<Double> searchRange(int lowerBound, int upperBound) {
        List<Double> result = new ArrayList<>();
        LeafNode currNode = (root == null) ? this.firstLeaf : su.searchLeafNode(lowerBound);

        // Iterate from first leaf till last leaf
        while (currNode != null) {
            for (KeyValuePair pair : currNode.getDictionary()) {
                if (pair == null)
                    break;
                int key = pair.key;
                double value = pair.value;
                // Add value to result if lowerBound <= key <= upperBound
                if (lowerBound <= key && key <= upperBound)
                    result.add(value);
            }
            currNode = currNode.getRightSibling();
        }
        return result;
    }


    /**
     * Delete provided key from B+ tree if key is present
     * @param key unique integer value
     */
    public void delete(int key) {
        // If tree is empty, then print error.
        if (firstLeaf == null) {
            System.err.println("Delete failed. Reason: Can't delete from an empty tree.");
            return;
        }

        // Find Leaf node
        LeafNode leafNode = (root == null) ? this.firstLeaf : su.searchLeafNode(key);

        // binary search in that node for key
        int pairIndex = CommonUtils.binarySearch(leafNode.getDictionary(), leafNode.getNumPairs(), key);

        // if key not found print error
        if (pairIndex < 0) {
            System.err.println("Delete Failed. Reason: key(" + key + ") not found");
            return;
        }

        // delete the pair
        leafNode.delete(pairIndex);
        CommonUtils.orderPairsInAscending(leafNode.getDictionary());

        // Check for deficiency
        if (leafNode.getNumPairs() < leafNode.getMinNumPairs()) {
            LeafNode sibling;
            InternalNode parent = leafNode.parent;

            // Borrow from right sibling
            if (leafNode.getRightSibling() != null && leafNode.getRightSibling().parent == leafNode.parent && leafNode.getRightSibling().getNumPairs() > leafNode.getRightSibling().getMinNumPairs()) {
                DeleteUtils.borrowFromRightLeafNode(leafNode, parent);
            }
            // Else borrow from left sibling
            else if (leafNode.getLeftSibling() != null && leafNode.getLeftSibling().parent == leafNode.parent && leafNode.getLeftSibling().getNumPairs() > leafNode.getLeftSibling().getMinNumPairs()) {
                DeleteUtils.borrowFromLeftLeafNode(leafNode, parent);
            }
            // Merge with left sibling
            else if (leafNode.getLeftSibling() != null && leafNode.getLeftSibling().parent == leafNode.parent && leafNode.getLeftSibling().getNumPairs() == leafNode.getLeftSibling().getMinNumPairs()) {
                DeleteUtils.mergeWithLeftLeafNode(leafNode, parent);
            } else if (leafNode.getRightSibling() != null && leafNode.getRightSibling().parent == leafNode.parent && leafNode.getRightSibling().getNumPairs() == leafNode.getRightSibling().getMinNumPairs()) {
                sibling = leafNode.getRightSibling();
                int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.getChildren(), leafNode);

                // Remove key and child pointer from parent
                parent.removeKeyAndShiftLeft(pointerIndex);
                parent.removeChildPointerAndShiftLeft(pointerIndex);

                // Update sibling pointer
                sibling.setLeftSibling(leafNode.getLeftSibling());
                if (sibling.getLeftSibling() == null) {
                    firstLeaf = sibling;
                }
                if (leafNode.getLeftSibling() != null) {
                    leafNode.getLeftSibling().setRightSibling(sibling);
                }

                for (int i = leafNode.getNumPairs() - 1; i >= 0; i--) {
                    sibling.prependPair(leafNode.getDictionary()[i]);
                }

                CommonUtils.orderPairsInAscending(sibling.getDictionary());

                if (parent.getDegree() < parent.getMinDegree()) {
                    DeleteUtils.handleDeficiency(parent);
                }
            } else if (root == null && this.firstLeaf.getNumPairs() == 0) {
                this.firstLeaf = null;
            }
        } else if (root == null && this.firstLeaf.getNumPairs() == 0) {
            this.firstLeaf = null;
        } else {
            CommonUtils.orderPairsInAscending(leafNode.getDictionary());
        }
    }
}
