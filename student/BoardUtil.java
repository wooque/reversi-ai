package student;

import reversi.*;

public class BoardUtil {
    
    public static Board copyBoard(Board board, Player player) {
        Board newBoard = new Board();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
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
