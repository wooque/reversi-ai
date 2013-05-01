package student;

import client.PlayerProtocol;
import java.util.*;
import reversi.*;

public class vm090011r extends ReversiPlayer {

    private final Random _random = new Random();
    private Player _player;
    private Board _board;
    private int depth;
    private static final int MAXDEPTH = 4;
    private boolean firstMove = true;
    private boolean played = false;
    //private Log log;
    private ArrayList<Position> allMoves;
    private ArrayList<Player> playersOrder;

    @Override
    public void init(Player player) {

        _player = player;
        _board = new Board();
        //log = new Log("reversi.log");
        allMoves = new ArrayList<>();
        playersOrder = new ArrayList<>();
    }

    @Override
    public Position getMove() {

        depth = 0;
        int max = -65;
        int moveValue;
        Position move;

        //log.println("board:");
        //log.printBoard(_board);

        if (firstMove && !played) {
            move = new Position(5, 4);

        } else {
            List<Position> moves = _board.legalMoves(_player);
            move = moves.get(_random.nextInt(moves.size()));

            depth++;
            //log.levelUp();

            for (Position curr : moves) {
                Board newBoard = BoardUtil.makeBoard(allMoves, playersOrder); 
                newBoard.makeMove(_player, curr);
                ArrayList<Position> newAllMoves = (ArrayList<Position>)allMoves.clone();
                newAllMoves.add(curr);
                ArrayList<Player> newPlayersOrder = (ArrayList<Player>)playersOrder.clone();
                newPlayersOrder.add(_player);
                //log.printBoard(newBoard);
                moveValue = calculateMove(_player.opponent(), newBoard, newAllMoves, newPlayersOrder);

                if (moveValue > max) {
                    max = moveValue;
                    move = curr;
                }
            }
            depth--;
            //log.levelDown();
        }
        firstMove = false;
        _board.makeMove(_player, move);
        
        allMoves.add(move);
        playersOrder.add(_player);
        
        //log.println("max: " + max);
        //log.println("me played:");
        //log.printBoard(_board);
        return move;
    }

    private int calculateMove(Player player, Board board, ArrayList<Position> allMoves, ArrayList<Player> playersOrder) {

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
                Board newBoard = BoardUtil.makeBoard(allMoves, playersOrder);
                newBoard.makeMove(player, curr);
                ArrayList<Position> newAllMoves = (ArrayList<Position>)allMoves.clone();
                newAllMoves.add(curr);
                ArrayList<Player> newPlayersOrder = (ArrayList<Player>)playersOrder.clone();
                newPlayersOrder.add(player);
                //log.printBoard(newBoard);
                moveValue = calculateMove(player.opponent(), newBoard, newAllMoves, newPlayersOrder);
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
        allMoves.add(position);
        playersOrder.add(_player.opponent());
        
        if (firstMove) {
            played = true;
        }

        //log.println("enemy played:");
        //log.printBoard(_board);
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new vm090011r());
        player.gameStart();
    }
}