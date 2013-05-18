package student;

import reversi.Board;

public class Node {
        
    private Board board;
    private Node children;
    private Node next;

    public Node(Board board){
        this.board = board;
    }

    public Node getChildren() {
        return children;
    }

    public void setChildren(Node children) {
        this.children = children;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }   

    public Board getBoard() {
        return board;
    }
}
