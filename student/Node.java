package student;

import reversi.*;

public class Node {

    private Board board;
    private Position move;
    private ListOfNodes children;
    private Node next;

    public Node(Board board, Position move) {
        this.board = board;
        this.move = move;
        children = new ListOfNodes();
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

    public ListOfNodes getChildren() {
        return children;
    }

    public void addChildren(Board board, Position move) {
        children.addNode(board, move);
    }
    
    public void clearChildren(){
        children.clear();
    }

    public Position getMove() {
        return move;
    }
}
