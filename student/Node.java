package student;

import java.util.LinkedList;
import reversi.*;

public class Node {

    private Board board;
    private Position move;
    private LinkedList<Node> children;
    private Node next;

    public Node(Board board, Position move) {
        this.board = board;
        this.move = move;
        children = new LinkedList<>();
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

    public LinkedList<Node> getChildren() {
        return children;
    }

    public void addChildren(Board board, Position move) {
        children.add(new Node(board, move));
    }
    
    public void clearChildren(){
        children.clear();
    }

    public Position getMove() {
        return move;
    }
}
