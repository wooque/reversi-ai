package student;

import java.util.Iterator;

class ListOfNodesIterator implements Iterator<Node> {
    
    private ListOfNodes list;
    private Node currNode;

    public ListOfNodesIterator(ListOfNodes list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return (currNode.getNext() != null);
    }

    @Override
    public Node next() {
        currNode = currNode.getNext();
        return currNode;
    }

    @Override
    public void remove() {
        System.out.println("ERROR!!! This method should never be called");
    }   
}
