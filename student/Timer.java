package student;

public class Timer extends Thread {
    
    private MyReversiPlayer player;
    private int timeout;
    
    public Timer(MyReversiPlayer player, int timeout){
        this.timeout = timeout;
        this.player = player;
    }
    
    @Override
    public void run(){
        try {
            sleep(timeout);
        } catch (InterruptedException ex) {}
        player.end();
    }
}
