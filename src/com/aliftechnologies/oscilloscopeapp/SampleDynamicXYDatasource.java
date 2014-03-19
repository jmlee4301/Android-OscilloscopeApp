package com.aliftechnologies.oscilloscopeapp;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
 
public class SampleDynamicXYDatasource implements Runnable {
 
    // encapsulates management of the observers watching this datasource for update events:
    class MyObservable extends Observable {
    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }
}
 
    private static final int MAX_AMP_SEED = 30000;
    private static final int MIN_AMP_SEED = 10;
    private static final int AMP_STEP = 5;
    public static final int SINE1 = 0;
    public static final int SINE2 = 1;
    private static final int SAMPLE_SIZE = 100;
    private int phase = 0;
    public int sinAmp=0;
    private MyObservable notifier;
 
    {
        notifier = new MyObservable();
    }
 
    //@Override
    public void run() {
        //boolean isRising = true;
		while (true) {
// 
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // decrease or remove to speed up the refresh rate.
               phase++;
//                if (sinAmp >= MAX_AMP_SEED) {
//                    isRising = false;
//                } else if (sinAmp <= MIN_AMP_SEED) {
//                    isRising = true;
//                }
// 
//                if (isRising) {
//                    sinAmp += AMP_STEP;
//                } else {
//                    sinAmp -= AMP_STEP;
//                }
		    notifier.notifyObservers();
         }
    }
 
    public int getItemCount(int series) {
        return 100;
    }
 
    public Number getX(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }
        return index;
    }
 
    public Number getY(int series, int index) {
         if (index >= SAMPLE_SIZE) {
             throw new IllegalArgumentException();
         }
       
         
         //double amp = sinAmp * Math.sin(index*15 + phase + 4);
         
         double amp = sinAmp * Math.sin((series + 1) * ((index*5*Math.PI)) + phase);
         
        // double amp = sinAmp;
        switch (series) {
            case SINE1:
               return amp;
            case SINE2:
                return amp;
            default:
                throw new IllegalArgumentException();
        }
    }
 
    public void addObserver(Observer observer) {
        notifier.addObserver(observer);
    }
 
    public void removeObserver(Observer observer) {
        notifier.deleteObserver(observer);
    }
 
}
