package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    private final Random _random = new Random();
    private Player _player;
    private Board _board;
    //private Log log;
    private int timeout;
    private ListOfNodes lastCompleteLevel = new ListOfNodes();
    private ListOfNodes currentLevel = new ListOfNodes();
    private Player currPlayer;
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
        
        int max = -65;
        int min;
        int moveValue;
        Position move;

        List<Position> moves = _board.legalMoves(_player);
        move = moves.get(_random.nextInt(moves.size()));

        for (Position curr : moves) {
            Board newBoard = _board.clone();
            newBoard.makeMove(_player, curr);
            currentLevel.addNode(newBoard);
            moveValue = BoardUtil.calculateBoardValue(newBoard, _player);

            if (moveValue > max) {
                max = moveValue;
                move = curr;
            }
        }
        
        lastCompleteLevel = currentLevel;
        currentLevel.setFirst(null);
        currPlayer = _player.opponent();
        max = -65;
        min = 65;
        for(Node node: lastCompleteLevel){
            Board board = node.getBoard();
            List<Position> legalMoves = board.legalMoves(currPlayer);
            
            for(Position pos: legalMoves){
                Board newBoard = board.clone();
                newBoard.makeMove(currPlayer, pos);
                node.addChildren(newBoard);
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
            currentLevel.appendList(node.getChildren());
        }

        _board.makeMove(_player, move);

        return move;
    }

    @Override
    public void opponentsMove(Position position) {
        
        _board.makeMove(_player.opponent(), position);
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
