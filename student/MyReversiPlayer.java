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
    private LinkedList<Node> legalMoves = new LinkedList<>();
    private LinkedList<Node> lastCompleteLevel = new LinkedList<>();
    private int lastCompleteLevelDepth;
    private int lastExpandedNode;
    private int nextExpandingChildren;
    private LinkedList<Node> currentLevel = new LinkedList<>();
    //private int currentLevelDepth;
    private Player currPlayer;
    private boolean end;
    private int level;

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
    }

    @Override
    public Position getMove() {

        long start = System.currentTimeMillis();

        int max = -65;
        int moveValue;
        Position move;

        List<Position> moves = _board.legalMoves(_player);
        move = moves.get(0);

        // TO DO: consider moving this to init method
        if (legalMoves.isEmpty()) {
            for (Position currMove : moves) {
                Board newBoard = _board.clone();
                newBoard.makeMove(_player, currMove);
                legalMoves.add(new Node(newBoard, currMove));
            }
            lastCompleteLevel = legalMoves;
            lastExpandedNode = 0;
            nextExpandingChildren = 0;
            currPlayer = _player.opponent();
        }
        //-------------------------------------------

        level = 0;
        log.println("lastLevel: " + lastCompleteLevel.size());
        while (!end) {
            while (lastExpandedNode < lastCompleteLevel.size() && !end) {
                Node currNode = lastCompleteLevel.get(lastExpandedNode);
                if (currNode.getMoves() == null) {
                    currNode.setMoves(currNode.getBoard().legalMoves(currPlayer));
                }
                LinkedList<Node> currChildren = currNode.getChildren();
                Board currBoard = currNode.getBoard();
                List<Position> currMoves = currNode.getMoves();
                if (currNode.getMoves().isEmpty()) {
                    Board childrenBoard = currBoard.clone();
                    Node child = new Node(childrenBoard, null);
                    currNode.addChildren(child);
                    currentLevel.add(child);
                    if ((System.currentTimeMillis() - start) > timeout) {
                        end = true;
                    }
                } else {
                    while (nextExpandingChildren < currChildren.size() && !end) {
                        Board childrenBoard = currBoard.clone();
                        Position childrenMove = currMoves.get(nextExpandingChildren);
                        childrenBoard.makeMove(currPlayer, childrenMove);
                        Node child = new Node(childrenBoard, childrenMove);
                        currNode.addChildren(child);
                        currentLevel.add(child);
                        nextExpandingChildren++;
                        if ((System.currentTimeMillis() - start) > timeout) {
                            end = true;
                        }
                    }
                    if (nextExpandingChildren == currChildren.size()) {
                        nextExpandingChildren = 0;
                    }
                }
                lastExpandedNode++;
                if (nextExpandingChildren == 0) {
                    lastCompleteLevel = currentLevel;
                    lastCompleteLevelDepth++;
                    currentLevel = new LinkedList<>();
                    //currentLevelDepth++;
                    currPlayer = currPlayer.opponent();
                    level++;
                }
            }
            if (lastExpandedNode == lastCompleteLevel.size()) {
                lastExpandedNode = 0;
            }

            if (end) {
                break;
            }

            Node bestNode = null;
            Position bestMove = null;
            for (Node node : legalMoves) {
                moveValue = calculateValue(_player, node);
                if (moveValue > max) {
                    bestNode = node;
                    max = moveValue;
                    bestMove = node.getMove();
                }
                if (end) {
                    break;
                }
            }

            legalMoves = bestNode.getChildren();
            //currentLevelDepth--;
            lastCompleteLevelDepth--;
            if (!legalMoves.isEmpty() && (lastCompleteLevelDepth > 0)) {
                Node firstCurr = legalMoves.getFirst().getChildren().getFirst();
                Node lastCurr = legalMoves.getLast().getChildren().getLast();
                int depth = lastCompleteLevelDepth - 1;
                while (depth != 0) {
                    firstCurr = firstCurr.getChildren().getFirst();
                    lastCurr = lastCurr.getChildren().getLast();
                    depth--;
                }
                int first = lastCompleteLevel.indexOf(firstCurr);
                int last = lastCompleteLevel.indexOf(lastCurr);
                log.println("first: " + first + " last: " + last);
                lastCompleteLevel = new LinkedList<>(lastCompleteLevel.subList(first, last));
                log.println("remaining: " + lastCompleteLevel.size());
                if (first > lastExpandedNode) {
                    lastExpandedNode = 0;
                    nextExpandingChildren = 0;
                    currentLevel = new LinkedList<>();
                } else if (last < lastExpandedNode) {
                    lastExpandedNode = 0;
                    nextExpandingChildren = 0;
                    firstCurr = legalMoves.getFirst().getChildren().getFirst();
                    lastCurr = legalMoves.getLast().getChildren().getLast();
                    first = currentLevel.indexOf(firstCurr);
                    last = currentLevel.indexOf(lastCurr);
                    currentLevel = new LinkedList<>(currentLevel.subList(first, last));
                } else {
                    firstCurr = legalMoves.getFirst().getChildren().getFirst();
                    first = currentLevel.indexOf(firstCurr);
                    currentLevel = new LinkedList<>(currentLevel.subList(first, currentLevel.size() - 1));
                }
            }

            if (!end) {
                move = bestMove;
            }
        }

        log.println("depth: " + level);

        _board.makeMove(_player, move);

        return move;
    }

    private int calculateValue(Player player, Node node) {
        if (node.getChildren().isEmpty()) {
            return BoardUtil.calculateBoardValue(node.getBoard(), player);
        } else {
            int max = -65;
            int min = 65;
            int value;
            for (Node children : node.getChildren()) {
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

    @Override
    public void opponentsMove(Position position) {

        _board.makeMove(_player.opponent(), position);
        log.println("legal: " + legalMoves.size());
        if (!legalMoves.isEmpty()) {
            for (Node node : legalMoves) {
                if (BoardUtil.equals(node.getMove(), position)) {
                    legalMoves = node.getChildren();
                    //currentLevelDepth--;
                    lastCompleteLevelDepth--;
                    if (!legalMoves.isEmpty() && (lastCompleteLevelDepth > 0)) {
                        Node firstCurr = legalMoves.getFirst().getChildren().getFirst();
                        Node lastCurr = legalMoves.getLast().getChildren().getLast();
                        int depth = lastCompleteLevelDepth - 1;
                        while (depth != 0) {
                            firstCurr = firstCurr.getChildren().getFirst();
                            lastCurr = lastCurr.getChildren().getLast();
                            depth--;
                        }
                        int first = lastCompleteLevel.indexOf(firstCurr);
                        int last = lastCompleteLevel.indexOf(lastCurr);
                        log.println("first: " + first + " last: " + last);
                        lastCompleteLevel = new LinkedList<>(lastCompleteLevel.subList(first, last));
                        log.println("remaining: " + lastCompleteLevel.size());
                        if (first > lastExpandedNode) {
                            lastExpandedNode = 0;
                            nextExpandingChildren = 0;
                            currentLevel = new LinkedList<>();
                        } else if (last < lastExpandedNode) {
                            lastExpandedNode = 0;
                            nextExpandingChildren = 0;
                            firstCurr = legalMoves.getFirst().getChildren().getFirst();
                            lastCurr = legalMoves.getLast().getChildren().getLast();
                            first = currentLevel.indexOf(firstCurr);
                            last = currentLevel.indexOf(lastCurr);
                            currentLevel = new LinkedList<>(currentLevel.subList(first, last));
                        } else {
                            firstCurr = legalMoves.getFirst().getChildren().getFirst();
                            first = currentLevel.indexOf(firstCurr);
                            currentLevel = new LinkedList<>(currentLevel.subList(first, currentLevel.size() - 1));
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
