package com.antares.dsmith.cclock;


/**
 * Created by dsmith on 24/11/16.
 */
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public final class TimerUpdateTask extends TimerTask {

    public MainActivity activity = null;

    public TimerUpdateTask(MainActivity _activity) {
        activity = _activity;
    }

    @Override
    public void run() {
        // update main display when clock is running
        if (activity.clock_mode)
            activity.refresh();
    }

/*
        // simulate a time consuming task
        private void doSomeWork() {
            try {

                Thread.sleep(10000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public static void main(String args[]) {

            TimerTask timerTask = new TimerTaskExample();
            // running timer task as daemon thread
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(timerTask, 0, 10 * 1000);
            System.out.println("TimerTask begins! :" + new Date());
            // cancel after sometime
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timer.cancel();
            System.out.println("TimerTask cancelled! :" + new Date());
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    * */

    }
