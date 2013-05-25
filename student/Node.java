package student;

import java.util.*;
import reversi.*;

public class Node {

    private Board board;
    private LinkedList<Node> children;
    private List<Position> moves;
    private Position move;

    public Node(Board board, Position move) {
        this.board = board;
        this.move = move;
        children = new LinkedList<>();
    }

    public Board getBoard() {
        return board;
    }

    public LinkedList<Node> getChildren() {
        return children;
    }
    
    public List<Position> getMoves() {
        return moves;
    }
    
    public void setMoves(List<Position> moves) {
        this.moves = moves;
    }
    
    public Position getMove() {
        return move;
    }  
    
    public void addChildren(Node child) {
        children.add(child);
    }
}
