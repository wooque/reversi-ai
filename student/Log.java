package student;

import java.io.*;
import reversi.*;

public class Log {
    
    private PrintWriter log;
    private String ident = "";
    
    public Log(String logname){
        try {
            FileWriter fw = new FileWriter(new File(logname));
            log = new PrintWriter(fw, true);
        } catch (FileNotFoundException ex) {
            System.out.println("Error opening log, no loging available.");
        } catch (IOException ex) {
            System.out.println("Error opening log, no loging available.");
        }
    }
    
    public void levelUp(){
        ident+="  |";
    }
    
    public void levelDown(){
        ident = ident.substring(0, ident.length() - 3);
    }
    
    public void println(String s){
        log.println(ident + s);
    }
    
    public void printBoard(Board board) {
        for (int i = 0; i < 8; ++i) {

            log.print(ident);
            for (int j = 0; j < 8; ++j) {

                Field f;
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
}
