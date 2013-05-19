package student;

import java.util.Iterator;
import reversi.*;

public class ListOfNodes implements Iterable<Node>{
    
    private Node first, last;
    
    public void addNode(Board board, Position move){
        Node node = new Node(board, move);
        if(first == null){
            first = node;
        }
        if(last != null)
            last.setNext(node); 
        last = node;
    }
    
    public Node getFirst(){
        return first;
    }
    
    public void setFirst(Node node){
        first = node;
        if(first == null){
            last = null;
        }
    }
    
    public boolean isEmpty(){
        return (first == null);
    }
    
    public void clear(){
        first = last = null;
    }
    
    public void appendList(ListOfNodes list){
        if(list == null) return;
        if(last != null){
            last.setNext(list.first);
        }
        last = list.last;
        if(first == null){
            first = list.first;
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return new ListOfNodesIterator(this);
    }
}
