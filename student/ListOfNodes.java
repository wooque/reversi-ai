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
    
    public Node getFirst(){
        return first;
    }
    
    public boolean isEmpty(){
        return (first != null);
    }
    
    public void clear(){
        first = last = null;
    }
    
    public void appendList(ListOfNodes list){
        last.setNext(list.first);
        last = list.last;
        if(first == null){
            first = last;
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return new ListOfNodesIterator(this);
    }
}
