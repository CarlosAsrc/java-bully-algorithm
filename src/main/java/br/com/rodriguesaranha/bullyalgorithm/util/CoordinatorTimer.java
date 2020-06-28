package br.com.rodriguesaranha.bullyalgorithm.Model;

import java.util.Timer;
import java.util.TimerTask;

public class CoordinatorTimer {
    Timer timer;

    public CoordinatorTimer(int seconds) {
        timer = new Timer();
        timer.schedule(new RemindTask(), seconds*1000);
    }

    class RemindTask extends TimerTask {
        public void run() {
            System.out.println("Processo coordenador terminado!");
            System.exit(0);
            timer.cancel();
        }
    }

}
