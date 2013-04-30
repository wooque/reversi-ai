package student;

import java.util.ArrayList;
import reversi.*;

public class BoardUtil {
    
    public static Board copyBoard(ArrayList<Position> allMoves, ArrayList<Player> players) {
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
}
