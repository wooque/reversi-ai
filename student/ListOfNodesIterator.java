package student;

import java.util.Iterator;

class ListOfNodesIterator implements Iterator<Node> {
    
    private Node currNode;
    private ListOfNodes list;

    public ListOfNodesIterator(ListOfNodes list) {
        this.list = list;
        currNode = list.getFirst();
    }

    @Override
    public boolean hasNext() {
        return (currNode != list.getLast());
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
