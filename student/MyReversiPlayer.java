package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private final Random _random = new Random();
    private Player _player;
    private Board _board;
    private int depth;
    private static final int MAXDEPTH = 4;
    //private Log log;
    private int timeout;
    private Node lastCompleteLevel;
    private Node currentLevel;
    private int expandedLevel;

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

    @Override
    public Position getMove() {

        depth = 0;
        int max = -65;
        int moveValue;
        Position move;

        //log.println("board:");
        //log.printBoard(_board);

        List<Position> moves = _board.legalMoves(_player);
        move = moves.get(_random.nextInt(moves.size()));

        depth++;
        //log.levelUp();

        for (Position curr : moves) {
            Board newBoard = _board.clone();
            newBoard.makeMove(_player, curr);
            //log.printBoard(newBoard);
            moveValue = calculateMove(_player.opponent(), newBoard);

            if (moveValue > max) {
                max = moveValue;
                move = curr;
            }
        }
        depth--;
        //log.levelDown();

        _board.makeMove(_player, move);

        //log.println("max: " + max);
        //log.println("me played:");
        //log.printBoard(_board);
        return move;
    }

    private int calculateMove(Player player, Board board) {

        int min = 65;
        int max = -65;
        int moveValue;

        if (depth == MAXDEPTH) {
            moveValue = BoardUtil.calculateBoardValue(board, _player);

            //log.println("value: " + moveValue);

            return moveValue;

        } else {
            depth++;
            //log.levelUp();

            List<Position> moves = board.legalMoves(player);

            for (Position curr : moves) {
                Board newBoard = board.clone();
                newBoard.makeMove(player, curr);
                //log.printBoard(newBoard);
                moveValue = calculateMove(player.opponent(), newBoard);
                if (_player.equals(player)) {
                    if (moveValue > max) {
                        max = moveValue;
                    }
                } else {
                    if (moveValue < min) {
                        min = moveValue;
                    }
                }
            }
            depth--;
            //log.levelDown();
            if (_player.equals(player)) {
                //log.println("max: " + max);
                return max;
            } else {
                //log.println("min: " + min);
                return min;
            }
        }
    }

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
