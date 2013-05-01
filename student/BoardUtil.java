package student;

import java.util.ArrayList;
import reversi.*;

public class BoardUtil {
    
    public static Board makeBoard(ArrayList<Position> allMoves, ArrayList<Player> players) {
        Board newBoard = new Board();
        for (int i = 0; i < allMoves.size(); ++i) {
            newBoard.makeMove(players.get(i), allMoves.get(i));
        }
        return newBoard;
    }
    
    public static int calculateBoardValue(Board board, Player player) {
        int value = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                try {
                    Field f = board.getField(new Position(i, j));
                    if (BoardUtil.equals(f, player)) {
                        value++;
                    } else if (BoardUtil.equals(f, player.opponent())) {
                        value--;
                    }
                } catch (InvalidPositionException ex) {
                    System.out.println(ex);
                }
            }
        }
        return value;
    }
    
    public static boolean equals(Field f, Player p){
        if(f == Field.BLACK && p == Player.BLACK)
            return true;
        else if(f == Field.WHITE && p == Player.WHITE)
            return true;
        else
            return false;
    }
}
