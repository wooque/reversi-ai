package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private final Random _random = new Random();
    private Player _player;
    private Board _board;
    //private int depth;
    //private static final int MAXDEPTH = 4;
    //private Log log;
    private int timeout;
    private LinkedList<Node> lastCompleteLevel = new LinkedList<>();
    private LinkedList<Node> currentLevel = new LinkedList<>();
    private Player currPlayer;
    private int expandedLevel;
    private boolean end;

    @Override
    public void init(Player player) {

        _player = player;
        _board = new Board();

        //log = new Log("reversi.log");

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
                timeout = (int) (timeout * 0.985);
                config.close();
            } catch (IOException ioe) {
                System.out.println("ERROR!!! Config file corupted.");
            }
        }
    }

    public void end() {
        end = true;
    }

    @Override
    public Position getMove() {

        end = false;
        
        // TODO: consider using one Timer thread
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(timeout);
                } catch (InterruptedException ex) {}
                end();
            }
        };
        
        //depth = 0;
        int max = -65;
        int min;
        int moveValue;
        Position move;

        //log.println("board:");
        //log.printBoard(_board);

        List<Position> moves = _board.legalMoves(_player);
        move = moves.get(_random.nextInt(moves.size()));

        //depth++;
        //log.levelUp();

        for (Position curr : moves) {
            Board newBoard = _board.clone();
            newBoard.makeMove(_player, curr);
            Node node = new Node(newBoard);
            currentLevel.add(node);
            moveValue = BoardUtil.calculateBoardValue(node.getBoard(), _player);
            //log.printBoard(newBoard);
            //moveValue = calculateMove(_player.opponent(), newBoard);

            if (moveValue > max) {
                max = moveValue;
                move = curr;
            }
        }
        
        lastCompleteLevel = currentLevel;
        currentLevel.clear();
        currPlayer = _player.opponent();
        max = -65;
        min = 65;
        for(Node node: lastCompleteLevel){
            Board board = node.getBoard();
            List<Position> legalMoves = board.legalMoves(currPlayer);
            
            for(Position pos: legalMoves){
                Board newBoard = board.clone();
                newBoard.makeMove(currPlayer, pos);
                Node newNode = new Node(newBoard);
                currentLevel.add(newNode);
                moveValue = BoardUtil.calculateBoardValue(newBoard, currPlayer);
                if(currPlayer == _player){
                    if(moveValue > max){
                        max = moveValue;
                    }
                } else {
                    if(moveValue < min){
                        min = moveValue;
                    }    
                }
            }
        }
        //depth--;
        //log.levelDown();

        _board.makeMove(_player, move);

        //log.println("max: " + max);
        //log.println("me played:");
        //log.printBoard(_board);
        return move;
    }

//    private int calculateMove(Player player, Board board) {
//
//        int min = 65;
//        int max = -65;
//        int moveValue;
//
//        if (depth == MAXDEPTH) {
//            moveValue = BoardUtil.calculateBoardValue(board, _player);
//
//            //log.println("value: " + moveValue);
//
//            return moveValue;
//
//        } else {
//            depth++;
//            //log.levelUp();
//
//            List<Position> moves = board.legalMoves(player);
//
//            for (Position curr : moves) {
//                Board newBoard = board.clone();
//                newBoard.makeMove(player, curr);
//                //log.printBoard(newBoard);
//                moveValue = calculateMove(player.opponent(), newBoard);
//                if (_player.equals(player)) {
//                    if (moveValue > max) {
//                        max = moveValue;
//                    }
//                } else {
//                    if (moveValue < min) {
//                        min = moveValue;
//                    }
//                }
//            }
//            depth--;
//            //log.levelDown();
//            if (_player.equals(player)) {
//                //log.println("max: " + max);
//                return max;
//            } else {
//                //log.println("min: " + min);
//                return min;
//            }
//        }
//    }

    @Override
    public void opponentsMove(Position position) {
        //log.println("board: ");
        //log.printBoard(_board);

        _board.makeMove(_player.opponent(), position);

        //log.println("enemy played:");
        //log.printBoard(_board);
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
