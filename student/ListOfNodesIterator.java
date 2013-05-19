package student;

import java.util.Iterator;

class ListOfNodesIterator implements Iterator<Node> {
    
    private Node currNode;

    public ListOfNodesIterator(ListOfNodes list) {
        currNode = list.getFirst();
    }

    @Override
    public boolean hasNext() {
        return (currNode != null);
    }

    @Override
    public Node next() {
        Node returnNode = currNode;
        currNode = currNode.getNext();
        return returnNode;
    }

    @Override
    public void remove() {
        System.out.println("ERROR!!! This method should never be called");
    }   
}
