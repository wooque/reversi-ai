package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private Player _player;
    private Board _board;
    private Log log;
    private int timeout;
    private static final double TIMEOUT_COEF = 0.8;
    private long start;
    private LinkedList<Node> legalMoves = new LinkedList<>();
    private LinkedList<Node> lastCompleteLevel = new LinkedList<>();
    private int lastCompleteLevelDepth;
    private int lastExpandedNode;
    private int nextExpandingChildren;
    private LinkedList<Node> currentLevel = new LinkedList<>();
    private boolean isMinimaxInterrupted;
    private Player currPlayer;
    private boolean end;

    private class Node {

        Board board;
        int value;
        LinkedList<Node> children;
        List<Position> moves;
        Position move;

        public Node(Board board, Position move) {
            this.board = board;
            this.value = -65;
            this.move = move;
            children = new LinkedList<>();
        }
    }
         
    private static int calculateBoardValue(Board board, Player player) {
        int value = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                try {
                    Field f = board.getField(new Position(i, j));
                    if (equals(f, player)) {
                        value++;
                    } else if (equals(f, player.opponent())) {
                        value--;
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
        }
        return value;
    }
    
    private static boolean equals(Field f, Player p){
        if(f == Field.BLACK && p == Player.BLACK)
            return true;
        else if(f == Field.WHITE && p == Player.WHITE)
            return true;
        else
            return false;
    }
    
    private static boolean equals(Position first, Position second){
        return (first.getX() == second.getX() && first.getY() == second.getY());
    }
    
    private boolean expand() {
        log.println("getMove(): lastLevel before expanding: " + lastCompleteLevel.size());
        while ((lastExpandedNode < lastCompleteLevel.size()) && !end) {
            Node currNode = lastCompleteLevel.get(lastExpandedNode);
            if (currNode.moves == null) {
                currNode.moves  = currNode.board.legalMoves(currPlayer);
            }
            Board currBoard = currNode.board;
            List<Position> currMoves = currNode.moves;
            if (currNode.moves.isEmpty()) {
                log.println("getMOve(): Oh boy, no posible moves to play...");
                Board childrenBoard = currBoard.clone();
                Node child = new Node(childrenBoard, null);
                currNode.children.add(child);
                currentLevel.add(child);
                lastExpandedNode++;
                if ((System.currentTimeMillis() - start) > timeout) {
                    end = true;
                }
            } else {
                //log.println("getMove(): nextExpandingChildren: " + nextExpandingChildren);
                while ((nextExpandingChildren < currMoves.size()) && !end) {
                    Board childrenBoard = currBoard.clone();
                    Position childrenMove = currMoves.get(nextExpandingChildren);
                    childrenBoard.makeMove(currPlayer, childrenMove);
                    Node child = new Node(childrenBoard, childrenMove);
                    currNode.children.add(child);
                    currentLevel.add(child);
                    nextExpandingChildren++;
                    if ((System.currentTimeMillis() - start) > timeout) {
                        end = true;
                        log.println("Ending on " + (System.currentTimeMillis()-start) + " ms");
                    }
                }
                if (nextExpandingChildren == currMoves.size()) {
                    nextExpandingChildren = 0;
                    lastExpandedNode++;
                }
            }
        }
        if (lastExpandedNode == lastCompleteLevel.size()) {
            lastExpandedNode = 0;
            log.println("getMOve(): lastCompleteLevel expanding is finished!");
        }
        if (lastExpandedNode == 0 && nextExpandingChildren == 0) {
            lastCompleteLevel = currentLevel;
            lastCompleteLevelDepth++;
            currentLevel = new LinkedList<>();
            currPlayer = currPlayer.opponent();
        }
        if (end) {
            log.println("getMove(): Our time is runned out!");
            return true;
        }
        return false;
    }

    private Position minimax() {
        int moveValue;
        int max = -65;
        Position bestMove = null;
        Position move = null;
        log.println("getMove(): minimax: moves to choose from: " + legalMoves.size());
        for (Node node : legalMoves) {
            moveValue = calculateValue(_player, node);
            if (moveValue > max) {
                max = moveValue;
                bestMove = node.move;
            }
            if (end) {
                log.println("getMove(): Our time is runned out!");
                break;
            }
        }
        if (!end) {
            move = bestMove;
            isMinimaxInterrupted = false;
        }
        return move;
    }
    
    private int calculateValue(Player player, Node node) {
        if (node.children.isEmpty()) {
            if(node.value == -65){
                node.value = calculateBoardValue(node.board, player);
            }
            if((System.currentTimeMillis() - start) > timeout){
                end = true;
                isMinimaxInterrupted = true;
            }
            return node.value;
        } else {
            int max = -65;
            int min = 65;
            int value;
            for (Node children : node.children) {
                value = calculateValue(player.opponent(), children);
                if (player == _player) {
                    if (value > max) {
                        max = value;
                    }
                } else {
                    if (value < min) {
                        min = value;
                    }
                }
                if (end) {
                    break;
                }
            }
            if (player == _player) {
                return max;
            } else {
                return min;
            }
        }
    }
    
    private void cut(Position move){
        for (Node node : legalMoves) {
            if (equals(node.move, move)) {
                legalMoves = node.children;
                lastCompleteLevelDepth--;
                if (!legalMoves.isEmpty() && (lastCompleteLevelDepth > 0)) {
                    Node firstCurr = legalMoves.getFirst().children.getFirst();
                    Node lastCurr = legalMoves.getLast().children.getLast();
                    int depth = lastCompleteLevelDepth - 1;
                    log.println("lastCompleteLevel before cut: " + lastCompleteLevelDepth);
                    while (depth != 0) {
                        firstCurr = firstCurr.children.getFirst();
                        lastCurr = lastCurr.children.getLast();
                        depth--;
                    }
                    int first = lastCompleteLevel.indexOf(firstCurr);
                    int last = lastCompleteLevel.indexOf(lastCurr);
                    lastCompleteLevel = new LinkedList<>(lastCompleteLevel.subList(first, last + 1));
                    log.println("lastCompleteLevel: first: " + first + " last: " + last);
                    log.println("lastCompleteLevel: remaining after cut: " + lastCompleteLevel.size());
                    if (first > lastExpandedNode) {
                        lastExpandedNode = 0;
                        nextExpandingChildren = 0;
                        currentLevel = new LinkedList<>();
                        log.println("currentLevel cleared!");
                    } else if (last < lastExpandedNode) {
                        lastExpandedNode = 0;
                        nextExpandingChildren = 0;
                        firstCurr = firstCurr.children.getFirst();
                        lastCurr = lastCurr.children.getLast();
                        first = currentLevel.indexOf(firstCurr);
                        last = currentLevel.indexOf(lastCurr);
                        currentLevel = new LinkedList<>(currentLevel.subList(first, last + 1));
                        log.println("currentLevel: first: " + first + ", last: " + last);
                        log.println("currentLevel: remaining nodes after cut: " + currentLevel.size());
                        lastCompleteLevel = currentLevel;
                        lastCompleteLevelDepth++;
                        currentLevel = new LinkedList<>();
                    } else {
                        lastExpandedNode-=first;
                        if(currentLevel.size()>0){
                            firstCurr = firstCurr.children.getFirst();
                            first = currentLevel.indexOf(firstCurr);
                            log.println("currentLevel: first: " + first + ", last/size-1: " + (currentLevel.size()-1));
                            currentLevel = new LinkedList<>(currentLevel.subList(first, currentLevel.size()));
                            log.println("currentLevel: remaining after cut: " + currentLevel.size());
                        }
                    }
                    log.println("lastCompleteLevel after cut: " + lastCompleteLevelDepth);
                    log.println("currentLevel size after: " + currentLevel.size());
                }
            }
        }
    }
    
    @Override
    public void init(Player player) {

        _player = player;
        _board = new Board();

        log = new Log("reversi.log");

        FileReader configFile = null;
        try {
            configFile = new FileReader("src/student/config.properties");
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR!!! No config file.");
        }

        if (configFile != null) {
            BufferedReader config = new BufferedReader(configFile);
            StringTokenizer line;
            try {
                line = new StringTokenizer(config.readLine(), "=");
                while (true) {
                    if (line.nextToken().equals("timeout")) {
                        timeout = Integer.parseInt(line.nextToken());
                        break;
                    }
                    line = new StringTokenizer(config.readLine(), "=");
                }
                timeout = (int) (timeout * TIMEOUT_COEF);
                config.close();
            } catch (IOException ioe) {
                System.out.println("ERROR!!! Config file corupted.");
            }
        }
        
        List<Position> moves = _board.legalMoves(Player.BLACK);
        for (Position currMove : moves) {
            Board newBoard = _board.clone();
            newBoard.makeMove(Player.BLACK, currMove);
            legalMoves.add(new Node(newBoard, currMove));
        }
        lastCompleteLevel = legalMoves;
        currPlayer = Player.WHITE;
    }

    @Override
    public Position getMove() {

        start = System.currentTimeMillis();
        end = false;
        
        Position move;

        log.println("getMove(): legalMoves on begin: " + legalMoves.size());
        
        int tempMax = -65;
        Node tempNode = null;
        for(Node node: legalMoves){
            int value = node.value;
            if(value > tempMax){
                tempMax = value;
                tempNode = node;
            }
        }
        if(tempNode != null){
            move = tempNode.move;
        } else {
            move = legalMoves.get(0).move;
        }

        log.println("getMove(): lastExpandedNode: " + lastExpandedNode);
        log.println("getMove(): nextExpandingChildren: " + nextExpandingChildren);
        while (!end) {
            if(!isMinimaxInterrupted){
                if (expand()) break;
            }
            Position tempMove = minimax();
            if(tempMove != null)
            move = tempMove;
        }
        log.println("getMove(): lastExpandedNode: " + lastExpandedNode);
        log.println("getMove(): nextExpandingChildren: " + nextExpandingChildren);
        log.println("getMove(): currentLevel size: " + currentLevel.size());
        cut(move);

        _board.makeMove(_player, move);

        return move;
    }
    
    @Override
    public void opponentsMove(Position position) {

        _board.makeMove(_player.opponent(), position);
        log.println("opponentsMove(): moves to choose from: " + legalMoves.size());
        log.println("opponentsMove(): currentLevel size before: " + currentLevel.size());
        if (!legalMoves.isEmpty()) {
            cut(position);
        }
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
