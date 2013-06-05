package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private Player _player;
    private Board _board;
    private long timeout;
    private static final double TIMEOUT_COEF = 0.85;
    private long start;
    private Node root;
    private Node bestNode;
    private int rootlevel;
    private int levelOverall;
    private boolean end;
    private static int[][] fieldValues = new int[][]{{32, -8, 8, 8, 8, 8, -8,32},
                                                     {-8,-16,-4,-4,-4,-4,-16,-8},
                                                     { 8, -4, 8, 4, 4, 8, -4, 8},
                                                     { 8, -4, 4, 0, 0, 4, -4, 8},
                                                     { 8, -4, 4, 0, 0, 4, -4, 8},
                                                     { 8, -4, 8, 4, 4, 8, -4, 8},
                                                     {-8,-16,-4,-4,-4,-4,-16,-8},
                                                     {32, -8, 8, 8, 8, 8, -8,32}};

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

    private static int calculateNodeValue(Node node, Player nextPlayer, Player me) {
        int value = 0;
        Field [][] fields = new Field[8][8];
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                try {
                    fields[i][j] = node.board.getField(new Position(i, j));
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }    
            }
        }
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                Field f = fields[i][j];
                double coef = 1;
                if(i != 0 && i != 7 && j != 0 && j != 7)
                    if ((fields[i-1][j-1] == Field.EMPTY) ^ (fields[i+1][j+1] == Field.EMPTY)){
                        coef -= 0.125;
                    }
                if (j != 0 && j != 7)
                    if ((fields[i][j-1] == Field.EMPTY) ^ (fields[i][j+1] == Field.EMPTY)){
                        coef -= 0.125;
                    }
                if (i != 0 && i != 7)
                    if ((fields[i-1][j] == Field.EMPTY) ^ (fields[i-1][j] == Field.EMPTY)){
                        coef -= 0.125;
                    }
                if(i != 0 && i != 7 && j != 0 && j != 7)
                    if ((fields[i-1][j+1] == Field.EMPTY) ^ (fields[i+1][j-1] == Field.EMPTY)){
                        coef -= 0.125;
                    }
                double newValue;
                if(fieldValues[i][j] < 0)
                    newValue = fieldValues[i][j] / coef;
                else
                    newValue = fieldValues[i][j] * coef;
                
                if (equals(f, me)) {
                    value += newValue;
                } else if (equals(f, me.opponent())) {
                    value -= newValue;
                }
                fields[i][j] = f;
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
    
    private static void printBoard(Board board) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                Field f;
                try {
                    f = board.getField(new Position(i,j));
                    if (f.equals(Field.EMPTY)) {
                        System.out.print(" .");
                    } else if (f.equals(Field.BLACK)) {
                        System.out.print(" X");
                    } else {
                        System.out.print(" O");
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
            System.out.println();
        }
        System.out.println("------------------");
    }
    
    private boolean isTimeRanOut(){
        if(System.currentTimeMillis() - start > timeout){
            //System.out.println("ENDING on "+(System.currentTimeMillis() - start));
            end = true;
            return true;
        }
        return false;
    }

    private int abminimax(Node node, Player player, int maxDepth, int currDepth, int alphaParam, int betaParam) {
        if (isTimeRanOut()) {
            if(player == _player){
                return 9999;
            } else {
                return -9999;
            }
        }
        if (maxDepth == currDepth || (node.moves.isEmpty() && node.board.legalMoves(player.opponent()).isEmpty())) {
            if (node.value == -9999) {
                node.value = calculateNodeValue(node, player, _player);
            }
            return node.value;
        }
        int alpha = alphaParam;
        int beta = betaParam;
        int bestValue;
        if (player == _player) {
            bestValue = -9999;
        } else {
            bestValue = 9999;
        }
        if (node.moves.isEmpty()) {
            Node child;
            if(node.children.isEmpty()){
                Board newBoard = node.board.clone();
                child = new Node(newBoard, null);
                child.moves = child.board.legalMoves(player.opponent());
                node.children.add(child);
            } else {
                child = node.children.get(0);
            }

            bestValue = abminimax(child, player.opponent(), maxDepth, currDepth + 1, alpha, beta);

        } else {
            for (int i = 0; i < node.moves.size() && !end; i++) {
                Node child = null;
                if (i > node.children.size()){
                    System.out.println("FATAL ERROR!");
                } else{
                    if (i == node.children.size()) {
                        Board newBoard = node.board.clone();
                        Position newMove = node.moves.get(i);
                        newBoard.makeMove(player, newMove);
                        child = new Node(newBoard, newMove);
                        child.moves = child.board.legalMoves(player.opponent());
                        node.children.add(child);
                    } else {
                        child = node.children.get(i);
                    }
                }
                int value = abminimax(child, player.opponent(), maxDepth, currDepth + 1, alpha, beta);

                if (player == _player) {
                    if (value > bestValue) {
                        bestValue = value;
                        if (currDepth == 0) {
                            bestNode = child;
                        }
                        if (bestValue >= beta) {
                            return bestValue;
                        }
                        alpha = Math.max(alpha, bestValue);
                    }
                } else {
                    if (value < bestValue) {
                        bestValue = value;
                        if (bestValue <= alpha) {
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
        bestNode = null;
        end = false;
        abminimax(root, Player.BLACK, 1, 0, -9999, 9999);
        rootlevel = 0;
        levelOverall= 1;
    }

    @Override
    public Position getMove() {

        start = System.currentTimeMillis();
        end = false;

        bestNode = null;
        abminimax(root, _player, 1, 0, -9999, 9999);
        Node nodeToPlay = bestNode;
        
        abminimax(root, _player, levelOverall - rootlevel, 0, -9999, 9999);
        if (!end) {
            nodeToPlay = bestNode;
        }
        
        while (levelOverall <= 70 && !end) {
            abminimax(root, _player, levelOverall - rootlevel + 1, 0, -9999, 9999);
            if (!end) {
                nodeToPlay = bestNode;
                levelOverall++;
            } else {
                //System.out.println("ENDING: "+(System.currentTimeMillis() - start));
            }
        }
        root = nodeToPlay;
        rootlevel++;

        if (root.moves.isEmpty() && !root.children.isEmpty()) {
            root = root.children.get(0);
            rootlevel++;
        }

        _board.makeMove(_player, nodeToPlay.move);

        return nodeToPlay.move;
    }

    @Override
    public void opponentsMove(Position position) {

        _board.makeMove(_player.opponent(), position);

        if(root.moves.size()-root.children.size()>0){
            abminimax(root, _player.opponent(), 1, 0, -9999, 9999);
            rootlevel++;
        }
        for (Node children : root.children) {
            if (equals(children.move, position)) {
                root = children;
                rootlevel++;
                break;
            }
        }

        if (root.moves.isEmpty() && !root.children.isEmpty()) {
            root = root.children.get(0);
            rootlevel++;
        }
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
