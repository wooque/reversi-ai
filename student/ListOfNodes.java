package student;

import java.util.Iterator;
import reversi.Board;

public class ListOfNodes implements Iterable<Node>{
    private Node first, last;
    
    public void addNode(Board board){
        Node node = new Node(board);
        if(first == null){
            first = node;
        }
        last = node;
        last.setNext(node); 
    }
    public void setFirst(Node node){
        first = node;
        if(node == null){
            last = null;
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return new ListOfNodesIterator(this);
    }
}
