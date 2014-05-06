package uk.ac.ncl.cs.zequn.swa;

import uk.ac.ncl.cs.zequn.swa.core.MainController;
import uk.ac.ncl.cs.zequn.swa.core.ResultOutput;
import uk.ac.ncl.cs.zequn.swa.core.Strategy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author ZequnLi
 *         Date: 14-4-3
 */
public class Test {
    public static void main(String [] args) throws SQLException, InterruptedException, IOException {
        final long stopTime = 60*10*1;
        final Random random = new Random();
        final MainController mainController = new MainController(Strategy.AVG,100,1000*60*2,new ResultOutput() {
            @Override
            public void output(String string) {
               // System.out.println(string);
            }
        });

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int i =0;
                while (i<10000){
                    i++;
                    mainController.offer(random.nextInt(10));
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 100);
//        while (true){
//            mainController.offer(new SingleInput(0,random.nextInt(10)));
//        }

        Thread thread = new Thread(new Runnable() {
            boolean stop = false;
            long timeCounter = 0;

            @Override
            public void run() {
                while (!stop){
                    timeCounter++;
                    if(timeCounter>=stopTime){
                        stop = true;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        System.out.println("wait");

        thread.join();
        timer.cancel();
        mainController.end();
        System.out.println("end");
    }
}
