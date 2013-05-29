package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private Player _player;
    private Board _board;
    private Log log;
    private long timeout;
    private static final double TIMEOUT_COEF = 0.9;
    private long start;
    private LinkedList<Node> legalMoves = new LinkedList<>();
    private LinkedList<Node> lastCompleteLevel = new LinkedList<>();
    private int level;
    private int lastCompleteLevelDepth;
    private int lastExpandedNode;
    private int nextExpandingChildren;
    private LinkedList<Node> currentLevel = new LinkedList<>();
    private boolean isMinimaxInterrupted;
    private Player currPlayer;
    private boolean end;
    private static int [][] fieldValues = new int[][]{{ 99, -8, 8, 6, 6, 8, -8,99},
                                                      { -8,-24,-4,-3,-3,-4,-24,-8},
                                                      {  8, -4, 7, 4, 4, 7, -4, 8},
                                                      {  6, -3, 4, 0, 0, 4, -3, 6},
                                                      {  6, -3, 4, 0, 0, 4, -3, 6},
                                                      {  8, -4, 7, 4, 4, 7, -4, 8},
                                                      { -8,-24,-4,-3,-3,-4,-24,-8},
                                                      { 99, -8, 8, 6, 6, 8, -8,99}}; 

    private class Node {

        Board board;
        int value;
        LinkedList<Node> children;
        List<Position> moves;
        Position move;

        public Node(Board board, Position move) {
            this.board = board;
            this.value = -9999;
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
                        value+=fieldValues[i][j];
                    } else if (equals(f, player.opponent())) {
                        value-=fieldValues[i][j];
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
    
    private void expand() {
        log.println("-------------------EXPAND------------------");
        log.println("lastLevel before expanding: "+lastCompleteLevel.size());
        log.println("lastExpandedNode: "+lastExpandedNode);
        while ((lastExpandedNode < lastCompleteLevel.size()) && !end) {
            log.println("Node time: "+(System.currentTimeMillis()-start)+" ms");
            Node currNode = lastCompleteLevel.get(lastExpandedNode);
            if (currNode.moves == null) {
                currNode.moves  = currNode.board.legalMoves(currPlayer);
            }
            Board currBoard = currNode.board;
            List<Position> currMoves = currNode.moves;
            if (currNode.moves.isEmpty()) {
                log.println("Child time: "+(System.currentTimeMillis()-start)+" ms");
                if ((System.currentTimeMillis() - start) > timeout) {
                    end = true;
                    log.println("----------------ENDING ON " + (System.currentTimeMillis()-start) + " ms-----------------");
                }
                //log.println("Hit no posible moves to play...");
                Board childrenBoard = currBoard.clone();
                Node child = new Node(childrenBoard, null);
                //child.moves = childrenBoard.legalMoves(currPlayer.opponent());
                currNode.children.add(child);
                currentLevel.add(child);
                lastExpandedNode++;
                
            } else {
                if(nextExpandingChildren != 0)
                    log.println("nextExpandingChildren: " + nextExpandingChildren);
                while ((nextExpandingChildren < currMoves.size()) && !end) {
                    log.println("Child time: "+(System.currentTimeMillis()-start)+" ms");
                    if ((System.currentTimeMillis() - start) > timeout) {
                        end = true;
                        log.println("----------------ENDING ON " + (System.currentTimeMillis()-start) + " ms-----------------");
                        break;
                    }
                    Board childrenBoard = currBoard.clone();
                    Position childrenMove = currMoves.get(nextExpandingChildren);
                    childrenBoard.makeMove(currPlayer, childrenMove);
                    Node child = new Node(childrenBoard, childrenMove);
                    //child.moves = childrenBoard.legalMoves(currPlayer.opponent());
                    log.println("Child before lists: "+(System.currentTimeMillis()-start)+" ms");
                    currNode.children.add(child);
                    currentLevel.add(child);
                    nextExpandingChildren++;
                    log.println("Child end time: "+(System.currentTimeMillis()-start)+" ms");
                }
                if (nextExpandingChildren == currMoves.size()) {
                    nextExpandingChildren = 0;
                    lastExpandedNode++;
                }
            }
        }
        if (lastExpandedNode == lastCompleteLevel.size()) {
            lastExpandedNode = 0;
            log.println("lastCompleteLevel expanding is finished!");
        }
        if (lastExpandedNode == 0 && nextExpandingChildren == 0) {
            lastCompleteLevel = currentLevel;
            lastCompleteLevelDepth++;
            level++;
            currentLevel = new LinkedList<>();
            currPlayer = currPlayer.opponent();
            log.println("COMPLETELEVEL BECOMES LASTCOMPLETELEVEL");
        }
        log.println("lastExpandedNode: "+lastExpandedNode);
        log.println("nextExpandingChild: "+nextExpandingChildren);
        log.println("--------------------EXPAND END-----------------");
    }

    private Position minimax() {
        int moveValue;
        int max = -9999;
        Position bestMove = null;
        Position move = null;
        log.println("--------------------MINIMAX--------------------");
        log.println("moves to choose from: " + legalMoves.size());
        for (Node node : legalMoves) {
            moveValue = calculateValue(_player.opponent(), node);
            if (moveValue > max) {
                max = moveValue;
                bestMove = node.move;
            }
            if (end) {
                break;
            }
        }
        if (!end) {
            move = bestMove;
            isMinimaxInterrupted = false;
        }
        log.println("--------------------MINIMAX END-----------------");
        return move;
    }
    
    private int calculateValue(Player player, Node node) {
        if (node.children.isEmpty()) {
            if(node.value == -9999){
                node.value = calculateBoardValue(node.board, _player);
                if(node.moves == null){
                    node.moves = node.board.legalMoves(currPlayer);
                }
                if(player == _player){
                    node.value *= (0.5 + 0.075 * (node.moves.size()-1));
                } else {
                    node.value *= (2 - 0.075 * (node.moves.size()-1));
                }
            }
//            if((System.currentTimeMillis() - start) > timeout){
//                end = true;
//                isMinimaxInterrupted = true;
//                log.println("----------------ENDING ON " + (System.currentTimeMillis()-start) + " ms-----------------");
//            }
            return node.value;
        } else {
            int max = -9999;
            int min = 9999;
            int value;
            for (Node children : node.children) {
                if((System.currentTimeMillis() - start) > timeout){
                    end = true;
                    isMinimaxInterrupted = true;
                    log.println("----------------ENDING ON " + (System.currentTimeMillis()-start) + " ms-----------------");
                    break;
                }
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
//                if (end) {
//                    break;
//                }
            }
            if (player == _player) {
                return max;
            } else {
                return min;
            }
        }
    }
    
    private void cut(Position move){
        log.println("--------------------CUTTING--------------------");
        log.println("LegalMoves before "+legalMoves.size());
        log.println("lastCompleteLevel size before "+lastCompleteLevel.size());
        log.println("currentLevel size before: " + currentLevel.size());
        for (Node node : legalMoves) {
            if ((move == null && node.move == null) || (equals(node.move, move))) {
                legalMoves = node.children;
                log.println("LegalMoves after "+legalMoves.size());
                if(!legalMoves.isEmpty())
                    lastCompleteLevelDepth--;
                if (!legalMoves.isEmpty() && (lastCompleteLevelDepth > 0)) {
                    Node firstCurr = legalMoves.getFirst().children.getFirst();
                    Node lastCurr = legalMoves.getLast().children.getLast();
                    int depth = lastCompleteLevelDepth - 1;
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
                        level++;
                        currentLevel = new LinkedList<>();
                        currPlayer = currPlayer.opponent();
                    } else {
                        lastExpandedNode-=first;
                        log.println("lastExpandedNode modified to "+lastExpandedNode);
                        if(currentLevel.size()>0){
                            firstCurr = firstCurr.children.getFirst();
                            first = currentLevel.indexOf(firstCurr);
                            log.println("currentLevel: first: " + first + ", last/size-1: " + (currentLevel.size()-1));
                            currentLevel = new LinkedList<>(currentLevel.subList(first, currentLevel.size()));
                            log.println("currentLevel: remaining after cut: " + currentLevel.size());
                        }
                    }
                    log.println("lastExpandedNode: " + lastExpandedNode);
                    log.println("nextExpandingChildren: " + nextExpandingChildren);
                    log.println("currentLevel size after: " + currentLevel.size());
                    log.println("--------------------CUTTING END-----------------");
                }
            }
        }
    }
    
    @Override
    public void init(Player player) {

        _player = player;
        _board = new Board();

        log = new Log("reversi"+System.currentTimeMillis()+".log");

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
            Node n = new Node(newBoard, currMove);
            //n.moves = n.board.legalMoves(Player.WHITE);
            legalMoves.add(n);
        }
        lastCompleteLevel = legalMoves;
        currPlayer = Player.WHITE;
        level = 1;
        lastCompleteLevelDepth = 0;
        lastExpandedNode = 0;
        nextExpandingChildren = 0;
        currentLevel = new LinkedList<>();
        isMinimaxInterrupted = false;
        end = false;
    }

    @Override
    public Position getMove() {

        start = System.currentTimeMillis();
        end = false;
        
        Position move;

        log.println("--------------GET MOVE---------------");
        log.println("legalMoves to chose from: " + legalMoves.size());
        
        if(legalMoves.isEmpty()){
            List<Position> moves = _board.legalMoves(_player);
            for (Position currMove : moves) {
                Board newBoard = _board.clone();
                newBoard.makeMove(_player, currMove);
                Node n = new Node(newBoard, currMove);
                //n.moves = n.board.legalMoves(_player.opponent());
                legalMoves.add(n);
            }
            lastCompleteLevel = legalMoves;
            currPlayer = _player.opponent();
        }
        int max = -9999;
        Node maxNode = null;
        for(Node node: legalMoves){
            int value = node.value;
            if(value > max){
                max = value;
                maxNode = node;
            }
        }
        if(maxNode != null){
            move = maxNode.move;
        } else {
            move = legalMoves.get(0).move;
        }

        while (!end && level < 60) {
            if(!isMinimaxInterrupted){
                expand();
            }
            if (end) {
                break;
            }
            Position tempMove = minimax();
            if(tempMove != null)
                move = tempMove;
        }

        cut(move);

        while((legalMoves.size() == 1)&&(legalMoves.get(0).move == null)) {
            cut(null);
        }
        
        _board.makeMove(_player, move);

        log.println("--------------GET MOVE END---------------");
        return move;
    }
    
    @Override
    public void opponentsMove(Position position) {

        _board.makeMove(_player.opponent(), position);
        log.println("-------------OPPONENTS MOVE--------------");
        log.println("moves to choose from: " + legalMoves.size());
        cut(position);
        while((legalMoves.size() == 1)&&(legalMoves.get(0).move == null)) {
            cut(null);
        }
        log.println("-------------OPPONENTS MOVE END-----------");
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
