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
    
    public Node getFirst(){
        return first;
    }
    
    public Node getLast(){
        return last;
    }
    
    public void appendList(ListOfNodes list){
        last.setNext(list.getFirst());
        last = list.getLast();
        if(first == null){
            first = last;
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return new ListOfNodesIterator(this);
    }
}
