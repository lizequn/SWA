package uk.ac.ncl.cs.zequn.swa;

import uk.ac.ncl.cs.zequn.swa.core.InMemoryStore;
import uk.ac.ncl.cs.zequn.swa.core.MainController;
import uk.ac.ncl.cs.zequn.swa.core.ResultOutput;
import uk.ac.ncl.cs.zequn.swa.core.Strategy;
import uk.ac.ncl.cs.zequn.swa.model.SingleInput;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitorListener;

import java.util.Random;

/**
 * @author ZequnLi
 *         Date: 14-4-3
 */
public class Test {
    public static void main(String [] args){
        long id = 0;
        Random random = new Random();
        MainController mainController = new MainController(Strategy.AVG,1,1000*60*60,new ResultOutput() {
            @Override
            public void output(String string) {
                //System.out.println(string);
            }
        });

        while(true){
            mainController.offer(new SingleInput(id++,random.nextInt(10)));
        }

    }
}
