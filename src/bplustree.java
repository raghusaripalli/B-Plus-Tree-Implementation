/*
 * bplustree class implements in-memory B+ tree
 * @author Raghuveer Sharma Saripalli
 */

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class bplustree {

    // Class Variables
    final static String NEWLINE = "\n";
    final static String COMMA = ",";
    int m;
    static InternalNode root;
    LeafNode firstLeaf;
    final SearchUtils su = new SearchUtils();

    // Constructor
    public bplustree(int m) {
        this.m = m;
        root = null;
    }

    /*
        HELPER METHODS
     */


    public int getMidpoint() {
        return (int) Math.ceil((this.m + 1) / 2.0) - 1;
    }


    private void handleDeficiency(InternalNode in) {

        InternalNode sibling;
        InternalNode parent = in.parent;

        // Remedy deficient root node
        if (root == in) {
            if (root.degree > 1)
                return;
            for (int i = 0; i < in.children.length; i++) {
                if (in.children[i] != null) {
                    if (in.children[i] instanceof InternalNode) {
                        root = (InternalNode) in.children[i];
                        root.parent = null;
                    } else if (in.children[i] instanceof LeafNode) {
                        root = null;
                    }
                }
            }
        }
        // Borrow:
        else if (in.leftSibling != null && in.leftSibling.degree > in.leftSibling.minDegree && in.leftSibling.parent == in.parent) {
            sibling = in.leftSibling;

            int borrowedKey = sibling.keys[sibling.degree - 2];
            Node borrowedChildPointer = sibling.children[sibling.degree - 1];

            int idx = parent.findIndexOfChildPointer(in);

            in.prependKey(parent.keys[idx - 1]);
            in.prependChildPointer(borrowedChildPointer);
            borrowedChildPointer.parent = in;

            parent.keys[idx - 1] = borrowedKey;

            sibling.removeKeyAndShiftLeft(sibling.degree - 1);
            sibling.removeChildPointerAndShiftLeft(sibling.degree);

        } else if (in.rightSibling != null && in.rightSibling.degree > in.rightSibling.minDegree && in.rightSibling.parent == in.parent) {
            sibling = in.rightSibling;

            int borrowedKey = sibling.keys[0];
            Node childPointer = sibling.children[0];

            int idx = parent.findIndexOfChildPointer(in);

            in.keys[in.degree - 1] = parent.keys[idx];
            in.appendChildPointer(childPointer);
            childPointer.parent = in;

            parent.keys[idx] = borrowedKey;

            sibling.removeKeyAndShiftLeft(0);
            sibling.removeChildPointerAndShiftLeft(0);
        }

        // Merge:
        else if (in.leftSibling != null && in.leftSibling.degree == in.leftSibling.minDegree && in.leftSibling.parent == in.parent) {
            sibling = in.leftSibling;
            int idx = parent.findIndexOfChildPointer(in);

            sibling.keys[sibling.degree - 1] = parent.keys[idx - 1];

            // Copy all keys and children from in to left sibling.
            for (int i = 0, j = sibling.degree; i < in.degree - 1; i++, j++) {
                sibling.keys[j] = in.keys[i];
            }

            for (int i = 0; i < in.degree; i++) {
                sibling.appendChildPointer(in.children[i]);
                in.children[i].parent = sibling;
            }

            parent.removeKeyAndShiftLeft(idx - 1);
            parent.removeChildPointerAndShiftLeft(idx);

            sibling.rightSibling = in.rightSibling;
            if (in.rightSibling != null)
                in.rightSibling.leftSibling = sibling;

        } else if (in.rightSibling != null && in.rightSibling.degree == in.rightSibling.minDegree && in.rightSibling.parent == in.parent) {
            sibling = in.rightSibling;

            int idx = parent.findIndexOfChildPointer(in);

            sibling.prependKey(parent.keys[idx]);

            for (int i = in.degree - 2; i >= 0; i--) {
                sibling.prependKey(in.keys[i]);
            }

            for (int i = in.degree - 1; i >= 0; i--) {
                sibling.prependChildPointer(in.children[i]);
                in.children[i].parent = sibling;
            }

            parent.removeKeyAndShiftLeft(idx);
            parent.removeChildPointerAndShiftLeft(idx);

            sibling.leftSibling = in.leftSibling;
            if (in.leftSibling != null)
                in.leftSibling.rightSibling = sibling;
        }

        // Handle deficiency a level up if it exists
        if (parent != null && parent.degree < parent.minDegree) {
            handleDeficiency(parent);
        }
    }


    private Node[] splitChildPointers(InternalNode in, int split) {
        Node[] pointers = in.children;
        Node[] halfPointers = new Node[this.m + 1];

        // Copy half of the values into halfPointers while updating original keys
        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            in.removePointer(i);
        }

        return halfPointers;
    }


    private KeyValuePair[] splitDictionary(LeafNode ln, int split) {
        KeyValuePair[] dictionary = ln.dictionary;
        KeyValuePair[] halfDict = new KeyValuePair[this.m];

        // Copy half of the values into halfDict
        for (int i = split; i < dictionary.length; i++) {
            halfDict[i - split] = dictionary[i];
            ln.delete(i);
        }

        return halfDict;
    }

    private void splitInternalNode(InternalNode in) {
        // Acquire parent
        InternalNode parent = in.parent;

        // Split keys and pointers in half
        int midpoint = getMidpoint();
        int newParentKey = in.keys[midpoint];
        Integer[] halfKeys = splitKeys(in.keys, midpoint);
        Node[] halfPointers = splitChildPointers(in, midpoint);

        // Change degree of original InternalNode in
        in.degree = CommonUtils.firstIndexOfNull(in.children);

        // Create new sibling internal node and add half of keys and pointers
        InternalNode sibling = new InternalNode(this.m, halfKeys, halfPointers);
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
            Integer[] keys = new Integer[this.m];
            keys[0] = newParentKey;
            InternalNode newRoot = new InternalNode(this.m, keys);
            newRoot.appendChildPointer(in);
            newRoot.appendChildPointer(sibling);
            root = newRoot;

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

    private Integer[] splitKeys(Integer[] keys, int split) {

        Integer[] halfKeys = new Integer[this.m];

        // Remove split-indexed value from keys
        keys[split] = null;

        // Copy half of the values into halfKeys while updating original keys
        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i];
            keys[i] = null;
        }
        return halfKeys;
    }

    /*
     *  DELETE, INSERT and SEARCH methods
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
        int pairIndex = CommonUtils.binarySearch(leafNode.dictionary, leafNode.numPairs, key);

        // if key not found print error
        if (pairIndex < 0) {
            System.err.println("Delete Failed. Reason: key(" + key + ") not found");
            return;
        }

        // delete the pair
        leafNode.delete(pairIndex);
        CommonUtils.orderPairsInAscending(leafNode.dictionary);

        // Check for deficiency
        if (leafNode.numPairs < leafNode.minNumPairs) {
            LeafNode sibling;
            InternalNode parent = leafNode.parent;

            // Borrow: First, check the left sibling, then the right sibling
            if (leafNode.leftSibling != null &&
                    leafNode.leftSibling.parent == leafNode.parent &&
                    leafNode.leftSibling.numPairs > leafNode.leftSibling.minNumPairs) {

                sibling = leafNode.leftSibling;
                KeyValuePair lentFromLeft = sibling.dictionary[sibling.numPairs - 1];

                leafNode.prependPair(lentFromLeft);
                CommonUtils.orderPairsInAscending(leafNode.dictionary);
                sibling.deleteAndShiftLeft(sibling.numPairs - 1);

                // Update key in parent if necessary
                int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.children, leafNode);
                if (!(lentFromLeft.key >= parent.keys[pointerIndex - 1])) {
                    parent.keys[pointerIndex - 1] = leafNode.dictionary[0].key;
                }

            } else if (leafNode.rightSibling != null &&
                    leafNode.rightSibling.parent == leafNode.parent &&
                    leafNode.rightSibling.numPairs > leafNode.rightSibling.minNumPairs) {

                sibling = leafNode.rightSibling;
                KeyValuePair borrowedFromRight = sibling.dictionary[0];

                leafNode.insert(borrowedFromRight);
                sibling.deleteAndShiftLeft(0);
                CommonUtils.orderPairsInAscending(sibling.dictionary);

                // Update key in parent if necessary
                int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.children, leafNode);
                if (!(borrowedFromRight.key < parent.keys[pointerIndex])) {
                    parent.keys[pointerIndex] = sibling.dictionary[0].key;
                }
            }

            // Merge: First, check the left sibling, then the right sibling
            else if (leafNode.leftSibling != null &&
                    leafNode.leftSibling.parent == leafNode.parent &&
                    leafNode.leftSibling.numPairs == leafNode.leftSibling.minNumPairs) {

                sibling = leafNode.leftSibling;
                int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.children, leafNode);

                // Remove key and child pointer from parent
                parent.removeKeyAndShiftLeft(pointerIndex - 1);
                parent.removeChildPointerAndShiftLeft(pointerIndex);

                for (int i = 0; i < leafNode.numPairs; i++) {
                    sibling.insert(leafNode.dictionary[i]);
                }

                CommonUtils.orderPairsInAscending(sibling.dictionary);

                sibling.rightSibling = leafNode.rightSibling;
                if (leafNode.rightSibling != null)
                    leafNode.rightSibling.leftSibling = sibling;

                // Check for deficiencies in parent
                if (parent.degree < parent.minDegree)
                    handleDeficiency(parent);

            } else if (leafNode.rightSibling != null &&
                    leafNode.rightSibling.parent == leafNode.parent &&
                    leafNode.rightSibling.numPairs == leafNode.rightSibling.minNumPairs) {

                sibling = leafNode.rightSibling;
                int pointerIndex = CommonUtils.getIndexOfLeafNode(parent.children, leafNode);

                // Remove key and child pointer from parent
                parent.removeKeyAndShiftLeft(pointerIndex);
                parent.removeChildPointerAndShiftLeft(pointerIndex);

                // Update sibling pointer
                sibling.leftSibling = leafNode.leftSibling;
                if (sibling.leftSibling == null) {
                    firstLeaf = sibling;
                }
                if (leafNode.leftSibling != null) {
                    leafNode.leftSibling.rightSibling = sibling;
                }

                for (int i = leafNode.numPairs - 1; i >= 0; i--) {
                    sibling.prependPair(leafNode.dictionary[i]);
                }

                CommonUtils.orderPairsInAscending(sibling.dictionary);

                if (parent.degree < parent.minDegree) {
                    handleDeficiency(parent);
                }
            } else if (root == null && this.firstLeaf.numPairs == 0) {
                this.firstLeaf = null;
            }
        } else if (root == null && this.firstLeaf.numPairs == 0) {
            this.firstLeaf = null;
        } else {
            CommonUtils.orderPairsInAscending(leafNode.dictionary);
        }
    }

    public void insert(int key, double value) {
        // If first leaf is empty create a new leaf node with provided key and value
        if (firstLeaf == null) {
            this.firstLeaf = new LeafNode(this.m, new KeyValuePair(key, value));
            return;
        }

        // Find leaf node to insert into
        LeafNode leafNode = (root == null) ? this.firstLeaf : su.searchLeafNode(key);

        // Insert into leaf node fails if node becomes overfull
        if (!leafNode.insert(new KeyValuePair(key, value))) {

            // Sort all the dictionary pairs with the included pair to be inserted
            leafNode.dictionary[leafNode.numPairs] = new KeyValuePair(key, value);
            leafNode.numPairs++;
            CommonUtils.orderPairsInAscending(leafNode.dictionary);

            // Split the sorted pairs into two halves
            int midpoint = getMidpoint();
            KeyValuePair[] halfDict = splitDictionary(leafNode, midpoint);

            if (leafNode.parent == null) {
                newParentNode(leafNode, halfDict);

            } else {
                updateParent(leafNode, halfDict[0]);
            }

            // Create new LeafNode that holds the other half
            LeafNode newLeafNode = new LeafNode(this.m, halfDict, leafNode.parent);

            // Update child pointers of parent node
            int pointerIndex = leafNode.parent.findIndexOfChildPointer(leafNode) + 1;
            leafNode.parent.insertChildPointer(newLeafNode, pointerIndex);

            // Make leaf nodes siblings of one another
            newLeafNode.rightSibling = leafNode.rightSibling;
            if (newLeafNode.rightSibling != null) {
                newLeafNode.rightSibling.leftSibling = newLeafNode;
            }
            leafNode.rightSibling = newLeafNode;
            newLeafNode.leftSibling = leafNode;

            if (root == null) {
                // Set the root of B+ tree to be the parent
                root = leafNode.parent;

            } else {
                InternalNode in = leafNode.parent;
                while (in != null) {
                    if (in.degree == in.maxDegree + 1) {
                        splitInternalNode(in);
                    } else {
                        break;
                    }
                    in = in.parent;
                }
            }
        }
    }

    private void updateParent(LeafNode leafNode, KeyValuePair keyValuePair) {
        // Add new key to parent for proper indexing
        int newParentKey = keyValuePair.key;
        leafNode.parent.keys[leafNode.parent.degree - 1] = newParentKey;
        Arrays.sort(leafNode.parent.keys, 0, leafNode.parent.degree);
    }

    private void newParentNode(LeafNode leafNode, KeyValuePair[] halfDict) {
        // Create internal node to serve as parent, use dictionary midpoint key
        Integer[] parent_keys = new Integer[this.m];
        parent_keys[0] = halfDict[0].key;
        InternalNode parent = new InternalNode(this.m, parent_keys);
        leafNode.parent = parent;
        parent.appendChildPointer(leafNode);
    }

    public Double search(int key) {
        // Return Null when tree is empty
        if (firstLeaf == null)
            return null;

        // Find leaf node in which key might be present
        LeafNode leafNode = (root == null) ? this.firstLeaf : su.searchLeafNode(key);

        // Perform binary search to find index of key within leaf node
        KeyValuePair[] pair = leafNode.dictionary;
        int index = CommonUtils.binarySearch(pair, leafNode.numPairs, key);

        // Return null if index is negative else value of index
        return index < 0 ? null : pair[index].value;
    }

    public List<Double> search(int lowerBound, int upperBound) {
        List<Double> result = new ArrayList<>();
        LeafNode currNode = this.firstLeaf;

        // Iterate from first leaf till last leaf
        while (currNode != null) {
            for (KeyValuePair pair : currNode.dictionary) {
                if (pair == null)
                    break;
                int key = pair.key;
                double value = pair.value;
                // Add value to result if lowerBound <= key <= upperBound
                if (lowerBound <= key && key <= upperBound)
                    result.add(value);
            }
            currNode = currNode.rightSibling;
        }
        return result;
    }

    private static String parseMode(String line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isLetter(c))
                sb.append(c);
        }
        return sb.toString();
    }

    private static double[] parseNumbers(String line) {
        double[] res = new double[2];
        res[1] = Double.MIN_VALUE;
        String data = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
        String[] values = data.split(",");
        res[0] = Integer.parseInt(values[0].trim());
        if (values.length == 2)
            res[1] = Double.parseDouble(values[1].trim());
        return res;
    }

    public static void main(String[] args) {

        // Terminate program if input file argument is missing
        if (args.length != 1) {
            System.err.println("Incorrect number of program arguments entered! Required only one. Terminating program.");
            exit(-1);
        }

        // Display filename argument entered
        String filePath = args[0];
        System.out.println("Filename provided: " + args[0]);

        // try with resources block
        // opened file pointers and scanners will be closed automatically(even when there's an exception)
        try (FileInputStream fis = new FileInputStream(filePath);
             FileWriter fw = new FileWriter("output_file.txt", false);
             Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8)) {
            bplustree tree = new bplustree(1);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String mode = parseMode(line);
                System.out.println(mode);
                double[] values = parseNumbers(line);
                System.out.println(values[0] + ", " + values[1]);
                switch (mode) {
                    case "Initialize":
                        tree = new bplustree((int) values[0]);
                        break;
                    case "Insert":
                        tree.insert((int) values[0], values[1]);
                        break;
                    case "Delete":
                        tree.delete((int) values[0]);
                        break;
                    case "Search":
                        if (values[1] == Double.MIN_VALUE) {
                            Double ans = tree.search((int) values[0]);
                            fw.write((ans == null ? "Null" : ans.toString()) + NEWLINE);
                        } else {
                            List<Double> answers = tree.search((int) values[0], (int) values[1]);
                            List<String> res = answers.stream()
                                    .map(ans -> ans == null ? "Null" : ans.toString())
                                    .collect(Collectors.toList());
                            fw.write(String.join(COMMA, res) + NEWLINE);
                        }
                        break;
                    default:
                        System.out.println("Wrong B+ tree operation:\n\t" + line);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            exit(-1);
        }
        System.out.println("result is written to the file 'output_file.txt'");

        for (int m = 3; m <= 10; m++) {
            bplustree tree = new bplustree(m);
            for (int bound = 1; bound <= 250; bound++) {
                //System.out.println(m + " " + bound);
                for (int key = 1; key <= bound; key++) {
                    tree.insert(key, key);
                    if (key != tree.search(key)) {
                        System.err.printf("m: %d, key: %d, bound: %d. Ins, Search failed", m, key, bound);
                        break;
                    }
                }
                if (bound != tree.search(1, bound).size()) {
                    System.err.printf("m: %d, bound: %d. ins, Search in range failed", m, bound);
                    break;
                }

                for (int key = bound; key >= 1; key--) {
                    tree.delete(key);
                    if (null !=
                            tree.search(key)) {
                        System.err.printf("m: %d, key: %d, bound: %d. Del, Search failed", m, key, bound);
                        break;
                    }
                }
                if (0 != tree.search(1, bound).size()) {
                    System.err.printf("m: %d, bound: %d. del, Search in range failed", m, bound);
                    break;
                }
            }
        }
    }
}
