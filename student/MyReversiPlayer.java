package student;

import client.PlayerProtocol;
import java.io.*;
import java.util.*;
import reversi.*;

public class MyReversiPlayer extends ReversiPlayer {

    //private final Random _random = new Random();
    private Player _player;
    private Board _board;
    //private Log log;
    private int timeout;
    private ListOfNodes legalMoves = new ListOfNodes();
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
                timeout = (int) (timeout * 0.33);
                config.close();
            } catch (IOException ioe) {
                System.out.println("ERROR!!! Config file corupted.");
            }
        }
    }

    public synchronized void end() {
        end = true;
    }
    
    public synchronized boolean isEnd(){
        return end;
    }

    @Override
    public Position getMove() {

        synchronized(this){
            end = false;
        }
        
        // TODO: consider using one Timer thread
        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(timeout);
                } catch (InterruptedException ex) {}
                end();
            }
        };
        timer.start();
        
        int max = -65;
        int moveValue;
        Position move;
        
        List<Position> moves = _board.legalMoves(_player);
        //move = moves.get(_random.nextInt(moves.size()));
        
        if(legalMoves.isEmpty()){
            for (Position currMove : moves) {
                Board newBoard = _board.clone();
                newBoard.makeMove(_player, currMove);
                legalMoves.addNode(newBoard, currMove);
            }
            lastCompleteLevel = legalMoves;
            currPlayer = _player.opponent();
        }

        while(!isEnd()){
            for(Node node: lastCompleteLevel){
                Board board = node.getBoard();
                List<Position> currLegalMoves = board.legalMoves(currPlayer);

                for(Position pos: currLegalMoves){
                    Board newBoard = board.clone();
                    newBoard.makeMove(currPlayer, pos);
                    node.addChildren(newBoard, pos);
                    if (isEnd()){
                        // TODO: temporary we are clearing children because of consistency,
                        // in future expanding decision tree should be continued
                        node.clearChildren();
                        break;
                    }
                }
                currentLevel.appendList(node.getChildren());
                if(isEnd()){
                    break;
                }
            }
            if(!isEnd()){
                lastCompleteLevel = currentLevel;
                currPlayer = currPlayer.opponent();
            }
            currentLevel.clear();    
        }
        int i = 0;
        int bestMove = 0;
        Node bestNode = null;
        for(Node node: legalMoves){
            moveValue = calculateValue(_player, node);
            if(moveValue > max){
                max = moveValue;
                bestMove = i;
                bestNode = node;
            }
            i++;
        }
        
        if(bestNode != null)
            legalMoves = bestNode.getChildren();
        
        move = moves.get(bestMove);
        
        _board.makeMove(_player, move);

        return move;
    }

    private int calculateValue(Player player, Node node){
        if(node.getChildren().isEmpty()){
            return BoardUtil.calculateBoardValue(node.getBoard(), player);
        } else {
            int max = -65;
            int min = 65;
            int value;
            for(Node children: node.getChildren()){
                value = calculateValue(player.opponent(), children);
                if(player == _player){
                    if(value > max){
                        max = value;
                    }
                } else {
                    if(value < min){
                        min = value;
                    }
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
        if(!legalMoves.isEmpty()){
            for(Node node: legalMoves){
                if(node.getMove() == position){
                    legalMoves = node.getChildren();
                    Node curr = legalMoves.getFirst().getChildren().getFirst();
                    Node prev = null;
                    while(curr != null){
                        prev = curr;
                        curr = curr.getChildren().getFirst();
                    }
                    lastCompleteLevel.setFirst(prev);
                }
            }  
        }
    }

    public static void main(String[] args) {
        PlayerProtocol player = new ReversiPlayerProtocol(new MyReversiPlayer());
        player.gameStart();
    }
}
