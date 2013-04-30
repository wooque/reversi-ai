package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class vm090011r extends ReversiPlayer {

    private final Random _random = new Random();
    private Player _player;
    private Board _board;
    private int depth;
    private static final int MAXDEPTH = 2;
    private boolean firstMove = true;
    private boolean played = false;
    private PrintWriter log;
    private String ident = "";

    @Override
    public void init(Player player) {

        _player = player;
        _board = new Board();

        try {
            FileWriter fw = new FileWriter(new File("reversi.log"));
            log = new PrintWriter(fw, true);
        } catch (FileNotFoundException ex) {
            System.out.println("Error opening log, no loging available.");
        } catch (IOException ex) {
            System.out.println("Error opening log, no loging available.");
        }
    }

    private Board copyBoard(Board board, Player player) {
        Board newBoard = new Board();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if ((i == 3 && j == 3) || (i == 3 && j == 4) || (i == 4 && j == 3) || (i == 4 && j == 4)) {
                    continue;
                }
                Position currPosition = new Position(i, j);
                try {
                    Field currField = board.getField(currPosition);
                    if (currField.equals(player)) {
                        newBoard.makeMove(player, currPosition);
                    } else if (currField.equals(player.opponent())) {
                        newBoard.makeMove(player.opponent(), currPosition);
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
        }
        return newBoard;
    }
    
    private void printBoard(Board board) {

        for (int i = 0; i < 8; ++i) {

            log.print(ident);
            for (int j = 0; j < 8; ++j) {

                Field f = null;
                try {
                    f = board.getField(new Position(i, j));
                    if (f.equals(Field.EMPTY)) {
                        log.print(" .");
                    } else if (f.equals(Field.BLACK)) {
                        log.print(" X");
                    } else {
                        log.print(" O");
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
            log.println();
        }
        log.println(ident + "------------------");
    }
    
    private int calculateBoardValue(Board board, Player player) {
        int value = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                try {
                    Field f = board.getField(new Position(i, j));
                    if (f.equals(player)) {
                        value++;
                    } else if (f.equals(player.opponent())) {
                        value--;
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
        }
        return value;
    }

    @Override
    public Position getMove() {

        depth = 0;
        int max = -1;
        int moveValue = 0;
        Position move = null;

        printBoard(_board);

        if (firstMove && !played) {
            move = new Position(5, 4);

        } else {
            List<Position> moves = _board.legalMoves(_player);
            move = moves.get(_random.nextInt(moves.size()));

            depth++;
            ident += "  ";

            for (Position curr : moves) {
                Board newBoard = copyBoard(_board, _player);
                newBoard.makeMove(_player, curr);
                printBoard(newBoard);
                moveValue = calculateMove(_player.opponent(), newBoard);

                if (moveValue > max) {
                    max = moveValue;
                    move = curr;
                }
            }
            depth--;
            ident = ident.substring(0, ident.length() - 2);
        }
        firstMove = false;
        _board.makeMove(_player, move);
        log.println("me:");
        printBoard(_board);
        return move;
    }

    private int calculateMove(Player player, Board board) {

        int min = 65;
        int max = -65;
        int moveValue;

        if (depth == MAXDEPTH) {
            moveValue = calculateBoardValue(board, player);

            printBoard(board);
            log.println(ident + "value: " + moveValue);

            if (_player.equals(player)) {
                if (moveValue > max) {
                    max = moveValue;
                }
            } else {
                if (moveValue < min) {
                    min = moveValue;
                }
            }

        } else {
            depth++;
            ident += "  ";

            List<Position> moves = board.legalMoves(player);

            for (Position curr : moves) {
                Board newBoard = copyBoard(board, player);
                newBoard.makeMove(player, curr);
                printBoard(newBoard);
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
            ident = ident.substring(0, ident.length() - 2);
        }

        if (_player.equals(player)) {
            return max;
        } else {
            return min;
        }
    }

    @Override
    public void opponentsMove(Position position) {
        _board.makeMove(_player.opponent(), position);

        if (firstMove) {
            played = true;
        }

        log.println("enemy:");
        printBoard(_board);
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new vm090011r());
        player.gameStart();
    }
}
