package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private Player _player;
    private Board _board;
    //private Log log;
    private long timeout;
    private static final double TIMEOUT_COEF = 0.9;
    private long start;
    private Node root;
    private int level;
    private Node bestNode;
    private boolean end;
    private static int[][] fieldValues = new int[][]{{99, -8, 8, 6, 6, 8, -8,99},
                                                     {-8,-24,-4,-3,-3,-4,-24,-8},
                                                     { 8, -4, 7, 4, 4, 7, -4, 8},
                                                     { 6, -3, 4, 0, 0, 4, -3, 6},
                                                     { 6, -3, 4, 0, 0, 4, -3, 6},
                                                     { 8, -4, 7, 4, 4, 7, -4, 8},
                                                     {-8,-24,-4,-3,-3,-4,-24,-8},
                                                     {99, -8, 8, 6, 6, 8, -8,99}};

    private class Node {

        Board board;
        int value;
        LinkedList<Node> children;
        List<Position> moves;
        int expanding;
        Position move;

        public Node(Board board, Position move) {
            this.board = board;
            this.value = -9999;
            this.move = move;
            children = new LinkedList<>();
        }
    }

    private static int calculateNodeValue(Node node, Player nextPlayer, Player me) {
        int value = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                try {
                    Field f = node.board.getField(new Position(i, j));
                    if (equals(f, me)) {
                        value += fieldValues[i][j];
                    } else if (equals(f, me.opponent())) {
                        value -= fieldValues[i][j];
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
        }
        if (nextPlayer == me) {
            value *= (0.5 + 0.075 * (node.moves.size() - 1));
        } else {
            value *= (2 - 0.075 * (node.moves.size() - 1));
        }
        return value;
    }

    private static boolean equals(Field f, Player p) {
        if (f == Field.BLACK && p == Player.BLACK) {
            return true;
        } else if (f == Field.WHITE && p == Player.WHITE) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean equals(Position first, Position second) {
        return (first.getX() == second.getX() && first.getY() == second.getY());
    }
    
    private boolean isTimeRanOut(){
        if(System.currentTimeMillis() - start > timeout){
            System.out.println("ENDE!");
            end = true;
            return true;
        }
        return false;
    }

    private int abminimax(Node node, Player player, int maxDepth, int currDepth, Integer alpha, Integer beta) {
        if (isTimeRanOut() || maxDepth == currDepth) {
            if (node.value == -9999) {
                node.value = calculateNodeValue(node, player, _player);
            }
            return node.value;
        }
        int bestValue;
        if (player == _player) {
            bestValue = -9999;
        } else {
            bestValue = 9999;
        }
        if (node.moves.isEmpty()) {
            Node child;
            Board newBoard = node.board.clone();
            child = new Node(newBoard, null);
            child.moves = child.board.legalMoves(player.opponent());
            node.children.add(child);

            bestValue = abminimax(child, player.opponent(), maxDepth, currDepth + 1, alpha, beta);
            bestNode = child;
//            if (player == _player) {
//                if (bestValue > beta) {
//                    return bestValue;
//                }
//                alpha = Math.max(alpha, bestValue);
//            } else {
//                if (bestValue < alpha) {
//                    return bestValue;
//                }
//                beta = Math.min(beta, bestValue);
//            }
        } else {
            for (int i = 0; i < node.moves.size() && !end; i++) {
                //System.out.println("moves: " + node.moves.size());
                //System.out.println("expanding: " + node.expanding);
                Node child;
                if (i >= node.expanding) {
                    Board newBoard = node.board.clone();
                    Position newMove = node.moves.get(node.expanding);
                    newBoard.makeMove(player, newMove);
                    child = new Node(newBoard, newMove);
                    child.moves = child.board.legalMoves(player.opponent());
                    node.children.add(child);
                    node.expanding++;
                } else {
                    child = node.children.get(i);
                }
                
                int value = abminimax(child, player.opponent(), maxDepth, currDepth + 1, alpha, beta);

                if (player == _player) {
                    if (value > bestValue) {
                        bestValue = value;
                        bestNode = child;
                        if (bestValue > beta) {
                            return bestValue;
                        }
                        alpha = Math.max(alpha, bestValue);
                    }
                } else {
                    if (value < bestValue) {
                        bestValue = value;
                        bestNode = child;
                        if (bestValue < alpha) {
                            return bestValue;
                        }
                        beta = Math.min(beta, bestValue);
                    }
                }
            }
        }
        return bestValue;
    }

    @Override
    public void init(Player player) {
        
        start = System.currentTimeMillis();

        _player = player;
        _board = new Board();

        //log = new Log("reversi" + System.currentTimeMillis() + ".log");

        try {
            FileReader configFile = new FileReader("src/student/config.properties");
            BufferedReader config = new BufferedReader(configFile);
            StringTokenizer line = new StringTokenizer(config.readLine(), "=");
            while (true) {
                if (line.nextToken().equals("timeout")) {
                    timeout = Integer.parseInt(line.nextToken());
                    break;
                }
                line = new StringTokenizer(config.readLine(), "=");
            }
            timeout = (int) (timeout * TIMEOUT_COEF);
            config.close();
        } catch (IOException | NumberFormatException e) {
            System.out.println("ERROR!!! Config file not found or corupted.");
        }

        root = new Node(_board, null);
        root.moves = _board.legalMoves(Player.BLACK);
        abminimax(root, Player.BLACK, 1, 0, -9999, 9999);
        end = false;
        level = 1;
    }

    @Override
    public Position getMove() {

        start = System.currentTimeMillis();
        end = false;

        bestNode = null;
        Position move = null;

        while (level < 60) {
            abminimax(root, _player, level, 0, -9999, 9999);
            if (!end) {
                move = bestNode.move;
                level++;
            } else{
                break;
            }
        }
        System.out.println("level: " + level);
        root = bestNode;

        while (root.move == null) {
            root = root.children.get(0);
        }

        _board.makeMove(_player, move);

        return move;
    }

    @Override
    public void opponentsMove(Position position) {

        _board.makeMove(_player.opponent(), position);

        for (Node children : root.children) {
            if (equals(children.move, position)) {
                root = children;
                break;
            }
        }

        while (root.move == null) {
            root = root.children.get(0);
        }
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
