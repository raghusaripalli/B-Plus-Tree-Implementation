/*
 * Helper Methods to delete the desired key in B+ tree
 */
public class DeleteUtils {
    // Borrow the right most pair from the left leaf node
    // and prepend it to cur leaf node
    // Update the parent pointer
    public static void borrowFromLeftLeafNode(LeafNode leafNode, InternalNode parent) {
        LeafNode sibling;
        sibling = leafNode.getLeftSibling();
        KeyValuePair lentFromLeft = sibling.getDictionary()[sibling.getNumPairs() - 1];

        leafNode.prependPair(lentFromLeft);
        CommonUtils.orderPairsInAscending(leafNode.getDictionary());
        sibling.deleteAndShiftLeft(sibling.getNumPairs() - 1);

        // Update key in parent if necessary
        int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.getChildren(), leafNode);
        if (!(lentFromLeft.key >= parent.getKeys()[pointerIndex - 1])) {
            parent.getKeys()[pointerIndex - 1] = leafNode.getDictionary()[0].key;
        }
    }

    // Borrow leftmost pair from the right leaf node
    // and append to the cur lead node
    // Update the parent pointer when necessary
    public static void borrowFromRightLeafNode(LeafNode leafNode, InternalNode parent) {
        LeafNode sibling;
        sibling = leafNode.getRightSibling();
        KeyValuePair borrowedFromRight = sibling.getDictionary()[0];

        leafNode.insert(borrowedFromRight);
        sibling.deleteAndShiftLeft(0);
        CommonUtils.orderPairsInAscending(sibling.getDictionary());

        // Update key in parent if necessary
        int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.getChildren(), leafNode);
        if (!(borrowedFromRight.key < parent.getKeys()[pointerIndex])) {
            parent.getKeys()[pointerIndex] = sibling.getDictionary()[0].key;
        }
    }

    // remove the cur leaf node from the parent
    // append the left out pairs to the left sibling
    // handle deficiency in parent
    public static void mergeWithLeftLeafNode(LeafNode leafNode, InternalNode parent) {
        LeafNode sibling;
        sibling = leafNode.getLeftSibling();
        int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.getChildren(), leafNode);

        // Remove key and child pointer from parent
        parent.removeKeyAndShiftLeft(pointerIndex - 1);
        parent.removeChildPointerAndShiftLeft(pointerIndex);

        for (int i = 0; i < leafNode.getNumPairs(); i++) {
            sibling.insert(leafNode.getDictionary()[i]);
        }

        CommonUtils.orderPairsInAscending(sibling.getDictionary());

        sibling.setRightSibling(leafNode.getRightSibling());
        if (leafNode.getRightSibling() != null)
            leafNode.getRightSibling().setLeftSibling(sibling);

        // Check for deficiencies in parent
        if (parent.getDegree() < parent.getMinDegree())
            DeleteUtils.handleDeficiency(parent);
    }

    // Handle deficiency in internal nodes of B+ tree
    public static void handleDeficiency(InternalNode in) {

        InternalNode parent = in.parent;

        // Remedy deficient root node
        if (Tree.root == in) {
            if (Tree.root.getDegree() > 1)
                return;
            for (int i = 0; i < in.getChildren().length; i++) {
                if (in.getChildren()[i] != null) {
                    if (in.getChildren()[i] instanceof InternalNode) {
                        Tree.root = (InternalNode) in.getChildren()[i];
                        Tree.root.parent = null;
                    } else if (in.getChildren()[i] instanceof LeafNode) {
                        Tree.root = null;
                    }
                }
            }
        }
        // Borrow from right sibling
        else if (in.getRightSibling() != null && in.getRightSibling().getDegree() > in.getRightSibling().getMinDegree() && in.getRightSibling().parent == in.parent) {
            borrowFromRightInternalNode(in, parent);
        }
        // else borrow from left sibling
        else if (in.getLeftSibling() != null && in.getLeftSibling().getDegree() > in.getLeftSibling().getMinDegree() && in.getLeftSibling().parent == in.parent) {
            borrowFromLeftInternalNode(in, parent);
        }
        // Merge with left sibling
        else if (in.getLeftSibling() != null && in.getLeftSibling().getDegree() == in.getLeftSibling().getMinDegree() && in.getLeftSibling().parent == in.parent) {
            mergeWithLeftInternalNode(in, parent);
            // merge with right sibling
        } else if (in.getRightSibling() != null && in.getRightSibling().getDegree() == in.getRightSibling().getMinDegree() && in.getRightSibling().parent == in.parent) {
            mergeWthRightInternalNode(in, parent);
        }

        // Handle deficiency by leveling it up
        if (parent != null && parent.getDegree() < parent.getMinDegree()) {
            handleDeficiency(parent);
        }
    }

    // prepend parent key, all the cur Node keys and children to right sibling
    // remove cur node from parent
    // update sibling pointers
    private static void mergeWthRightInternalNode(InternalNode in, InternalNode parent) {
        InternalNode sibling;
        sibling = in.getRightSibling();

        int idx = parent.findIndexOfChildPointer(in);

        sibling.prependKey(parent.getKeys()[idx]);

        for (int i = in.getDegree() - 2; i >= 0; i--) {
            sibling.prependKey(in.getKeys()[i]);
        }

        for (int i = in.getDegree() - 1; i >= 0; i--) {
            sibling.prependChildPointer(in.getChildren()[i]);
            in.getChildren()[i].parent = sibling;
        }

        parent.removeKeyAndShiftLeft(idx);
        parent.removeChildPointerAndShiftLeft(idx);

        sibling.setLeftSibling(in.getLeftSibling());
        if (in.getLeftSibling() != null)
            in.getLeftSibling().setRightSibling(sibling);
    }

    // append parent key, all the cur Node keys and children to left sibling
    // remove cur node from parent
    // update sibling pointers
    private static void mergeWithLeftInternalNode(InternalNode in, InternalNode parent) {
        InternalNode sibling;
        sibling = in.getLeftSibling();
        int idx = parent.findIndexOfChildPointer(in);

        sibling.getKeys()[sibling.getDegree() - 1] = parent.getKeys()[idx - 1];

        // Copy all keys and children from in to left sibling.
        for (int i = 0, j = sibling.getDegree(); i < in.getDegree() - 1; i++, j++) {
            sibling.getKeys()[j] = in.getKeys()[i];
        }

        for (int i = 0; i < in.getDegree(); i++) {
            sibling.appendChildPointer(in.getChildren()[i]);
            in.getChildren()[i].parent = sibling;
        }

        parent.removeKeyAndShiftLeft(idx - 1);
        parent.removeChildPointerAndShiftLeft(idx);

        sibling.setRightSibling(in.getRightSibling());
        if (in.getRightSibling() != null)
            in.getRightSibling().setLeftSibling(sibling);
    }

    // Borrow first key in the right sibling and update it in parent
    // parent key will be appended to cur Internal node
    // Append the child pointers of first key to newly appended key in internal node.
    private static void borrowFromRightInternalNode(InternalNode in, InternalNode parent) {
        InternalNode sibling;
        sibling = in.getRightSibling();

        int borrowedKey = sibling.getKeys()[0];
        Node childPointer = sibling.getChildren()[0];

        int idx = parent.findIndexOfChildPointer(in);

        in.getKeys()[in.getDegree() - 1] = parent.getKeys()[idx];
        in.appendChildPointer(childPointer);
        childPointer.parent = in;

        parent.getKeys()[idx] = borrowedKey;

        sibling.removeKeyAndShiftLeft(0);
        sibling.removeChildPointerAndShiftLeft(0);
    }

    // Borrow the last key from left sibling and update it in the parent
    // And the key in parent is prepended to keys in cur Internal node
    // Children pointers of last key in left sibling are prepended to cur Internal node
    private static void borrowFromLeftInternalNode(InternalNode in, InternalNode parent) {
        InternalNode sibling;
        sibling = in.getLeftSibling();

        int borrowedKey = sibling.getKeys()[sibling.getDegree() - 2];
        Node borrowedChildPointer = sibling.getChildren()[sibling.getDegree() - 1];

        int idx = parent.findIndexOfChildPointer(in);

        in.prependKey(parent.getKeys()[idx - 1]);
        in.prependChildPointer(borrowedChildPointer);
        borrowedChildPointer.parent = in;

        parent.getKeys()[idx - 1] = borrowedKey;

        sibling.removeKeyAndShiftLeft(sibling.getDegree() - 1);
        sibling.removeChildPointerAndShiftLeft(sibling.getDegree());
    }
}
