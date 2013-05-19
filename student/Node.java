package student;

import reversi.Board;

public class Node {

    private Board board;
    private ListOfNodes children;
    private Node next;

    public Node(Board board) {
        this.board = board;
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

    public void addChildren(Board board) {
        children.addNode(board);
    }
}
