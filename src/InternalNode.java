class InternalNode extends Node {
    public int maxDegree;
    public int minDegree;
    public int degree;
    public InternalNode leftSibling;
    public InternalNode rightSibling;
    public Integer[] keys;
    public Node[] childPointers;

    private int linearNullSearch(Node[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void appendChildPointer(Node pointer) {
        this.childPointers[degree] = pointer;
        this.degree++;
    }


    public int findIndexOfPointer(Node pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                return i;
            }
        }
        return -1;
    }


    public void insertChildPointer(Node pointer, int index) {
        if (degree - index >= 0) System.arraycopy(childPointers, index, childPointers, index + 1, degree - index);
        this.childPointers[index] = pointer;
        this.degree++;
    }


    public boolean isDeficient() {
        return this.degree < this.minDegree;
    }


    public boolean canLend() {
        return this.degree > this.minDegree;
    }


    public boolean isMergeable() {
        return this.degree == this.minDegree;
    }


    public boolean isOverfull() {
        return this.degree == maxDegree + 1;
    }

    public void prependChildPointer(Node pointer) {
        for (int i = degree - 1; i >= 0; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[0] = pointer;
        this.degree++;
    }

    public void prependKey(int key) {
        int i;
        for (i = this.keys.length - 1; i > 0; i--) {
            this.keys[i] = this.keys[i - 1];
        }
        this.keys[0] = key;
    }


    public void removeKey(int index) {
        this.keys[index] = null;
    }

    public void removeKeyAndShiftLeft(int index) {
        int i;
        for (i = index; i < this.keys.length - 1; i++) {
            this.keys[i] = this.keys[i + 1];
        }
        this.keys[i] = null;
    }


    public void removePointer(int index) {
        this.childPointers[index] = null;
        this.degree--;
    }

    public void removeChildPointerAndShiftLeft(int index) {
        int i;
        for (i = index; i < this.degree - 1; i++) {
            this.childPointers[i] = this.childPointers[i + 1];
        }
        this.childPointers[i] = null;
        this.degree--;
    }


    public void removePointer(Node pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                this.childPointers[i] = null;
            }
        }
        this.degree--;
    }


    public InternalNode(int m, Integer[] keys) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = 0;
        this.keys = keys;
        this.childPointers = new Node[this.maxDegree + 1];
    }

    public InternalNode(int m, Integer[] keys, Node[] pointers) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = linearNullSearch(pointers);
        this.keys = keys;
        this.childPointers = pointers;
    }
}
