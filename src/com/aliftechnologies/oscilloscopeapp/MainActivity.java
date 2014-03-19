package com.aliftechnologies.oscilloscopeapp;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;



public class MainActivity extends Activity {
	
	
    // redraws a plot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;
        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }
        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }
 
    private MultitouchPlot dynamicPlot;
    private MultitouchPlot staticPlot;
    private MyPlotUpdater plotUpdater;
    

	 MediaRecorder recorder;
	    File audiofile = null;
	   // private String outputFile = null;
	    private static final String TAG = "MainActivity";
	     private static Canvas Canvas;
	    
	    private View startButton;
	    private View stopButton;
	    
	    public LocalActivityManager activityManager;
	    public LinearLayout contentViewLayout;
	    public LinearLayout.LayoutParams contentViewLayoutParams;
	    private Context context;
	    public Intent nextActivity;
	    Thread maxAmplitude;
	    boolean runAmplitudeThread;
	    String startDate;

	    SampleDynamicXYDatasource data= new SampleDynamicXYDatasource();
	 
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        context = this;
	        activityManager = new LocalActivityManager (this, false);
	        activityManager.dispatchCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        startButton = findViewById(R.id.start);
	        stopButton = findViewById(R.id.stop);

			dynamicPlot = (MultitouchPlot) findViewById(R.id.dynamicPlot);
			 
	        plotUpdater = new MyPlotUpdater(dynamicPlot);
	 

	        
	        

	    }
	    

            

		

	 
	    public void startRecording(View view) throws IOException {
	    	


	        startButton.setEnabled(false);
	        stopButton.setEnabled(true);
	        
	        
	        Calendar c = Calendar.getInstance();
	        System.out.println("Start time => " + c.getTime());

	        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
	        startDate = df.format(c.getTime());
	        

	        
	        // only display whole numbers in domain labels
	        //dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
	 
	        // getInstance and position datasets:
	        
	        //SampleDynamicSeries sine1Series = new SampleDynamicSeries(data, 0, "Sine 1");
	        //SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "Sine 2");
	 
	       // dynamicPlot.addSeries(sine1Series, new LineAndPointFormatter(Color.rgb(0, 0, 0), null, Color.rgb(0, 80, 0), null));
	 
	        // create a series using a formatter with some transparency applied:
//	        LineAndPointFormatter f1 = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(0, 0, 80), null);
//	        f1.getFillPaint().setAlpha(220);
	        //dynamicPlot.addSeries(sine2Series, f1);
	        dynamicPlot.setGridPadding(5, 0, 5, 0);
	 
	        // hook up the plotUpdater to the data model:
	        data.addObserver(plotUpdater);
	 
	        //dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
	        dynamicPlot.getGraphWidget().setDomainValueFormat(new MyDateFormat());
	        dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
	        dynamicPlot.setDomainStepValue(10);
	        

		    
	       // dynamicPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	 
	        // thin out domain/range tick labels so they dont overlap each other:
	        dynamicPlot.setTicksPerDomainLabel(5);
	        dynamicPlot.setTicksPerRangeLabel(6);
	 
	        // freeze the range boundaries:
	        dynamicPlot.setRangeBoundaries(0, 30000, BoundaryMode.FIXED);
	 
	        // kick off the data generating thread:

	        
	        SampleDynamicSeries sine1Series = new SampleDynamicSeries(data, 0, "Sine 1");
	       // SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "Sine 2");
	        dynamicPlot.addSeries(sine1Series, new LineAndPointFormatter(Color.rgb(0, 0, 0), null, Color.rgb(0, 0, 80), null));
	        //dynamicPlot.setDomainStepValue(1000);
	        LineAndPointFormatter f1 = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(0, 0, 80), null);
	        f1.getFillPaint().setAlpha(220);
	        //dynamicPlot.addSeries(sine2Series, f1);

	        new Thread(data).start();
	 
	        File sampleDir = Environment.getExternalStorageDirectory();
	        try {
	            audiofile = File.createTempFile("sound", ".3gp", sampleDir);
	        } catch (IOException e) {
	            Log.e(TAG, "sdcard access error");
	            return;
	        }
	        
//	        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath();
//	        outputFile += "/audiorecordtest.3gp";
	        
	        
	          
	        recorder = new MediaRecorder();
	        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	        recorder.setOutputFile(audiofile.getAbsolutePath());
	        //recorder.setOutputFile(outputFile);
	        try {
	        recorder.prepare();
	        } catch (IOException e) {
	            Log.e(TAG, "prepare() failed");
	        }
	        recorder.start();
	        runAmplitudeThread=true;
     
	        maxAmplitude= new Thread() {

	            public void run() {
	            	
	            	Looper.prepare();
                 
	                while(runAmplitudeThread){
	    	        try {
						amplitude();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                }
	                Looper.loop();
	            }
 };
            
            maxAmplitude.start();

	        Log.e(TAG, "RECORDNG STARTED");



	        
	    }
	 
	    public void stopRecording(View view) {
	        startButton.setEnabled(true);
	        stopButton.setEnabled(false);
	        runAmplitudeThread=false;

	        maxAmplitude.interrupt();
	        maxAmplitude=null;
	        recorder.stop();
	        recorder.release();

	        Log.e(TAG, "RECORDING ENDED");
	       

//	        Intent intent = new Intent(this, MarkerDemo01Activity.class);
//	        startActivity(intent);
	        
	       // addRecordingToMediaLibrary();
	    }
	 
	    protected void addRecordingToMediaLibrary() {
	        ContentValues values = new ContentValues(4);
	        long current = System.currentTimeMillis();
	        values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
	        //values.put(MediaStore.Audio.Media.TITLE, "audio" + outputFile);
	        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
	        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
	        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
	        //values.put(MediaStore.Audio.Media.DATA, outputFile);
	        ContentResolver contentResolver = getContentResolver();
	 
	        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	        Uri newUri = contentResolver.insert(base, values);
	 
	        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
	        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
	    }
	    
	    public void amplitude() throws InterruptedException {
	    	
	    	

	
                if (recorder != null) {
                	if(runAmplitudeThread){
                		
                		//Thread.sleep(00); 

                    int amplitude = recorder.getMaxAmplitude();
                    //b.putLong("currentTime", amplitude);
                   // Log.i("AMPLITUDE", new Integer(amplitude).toString());

                    Random rand = new Random();

                    // nextInt is normally exclusive of the top value,
                    // so add 1 to make it inclusive
                    int randomNum = rand.nextInt((15000) + 1) + 5000;
                    
                    
                 //   Log.i("random",""+ randomNum);
                    
                    data.sinAmp=randomNum;
                    
                	}

                } else {
                   // b.putLong("currentTime", 0);

                }



        }
	    
	

	    
	    private class MyDateFormat extends Format { 


			private static final long serialVersionUID = 1L;   
	        private SimpleDateFormat dateFormat = new SimpleDateFormat("ss.SS");
	        


	        @Override
	        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	            //long timestamp = ((Number) obj).longValue();
	           // Date date = new Date(System.currentTimeMillis());
	            //c.set(Calendar.SECOND, 0);
	        	Calendar c = Calendar.getInstance();
	            Date date = new Date(c.getTimeInMillis());
	            
	            
//	            date.setSeconds(0);
//	           // String date1 = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//	            
            String str = dateFormat.format(date, toAppendTo, pos).toString();
//	            

               


   	        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
   	      String nowdate= df.format(c.getTime());
          DateDifferentExample(nowdate);
   	        

	          //  String formattedDate = df.format(c.getTime());
	            
	           // Log.i("Date",  str);
	            
	            return dateFormat.format(date, toAppendTo, pos);
	            


	        }

	        @Override
	        public Object parseObject(String source, ParsePosition pos) {
	            return null;    
	        }
	    }


	    	 
	    	public void DateDifferentExample(String date) 
	    	{
	
	    		//Log.i("test loop","on ,loop seconds.");
	     
	    		//String dateStart = "01/14/2012 09:29:58";
	    		//Date dateStop = date;
	     
	    		//HH converts hour in 24 hours format (0-23), day calculation
	    		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
	     
	    		java.util.Date d1 = null;
	    		java.util.Date d2 = null;
	    		

	    		
	     
	    		try {
	    			d1 =  format.parse(startDate);
	    			d2 =  format.parse(date);
	     
	    			//in milliseconds
	    			long diff = d2.getTime() - d1.getTime();
	    			
	    			long diffMilliseconds = diff % 1000 ;
	     
	    			long diffSeconds = diff / 1000 % 60;
	    			long diffMinutes = diff / (60 * 1000) % 60;
	    			long diffHours = diff / (60 * 60 * 1000) % 24;
	    			long diffDays = diff / (24 * 60 * 60 * 1000);
	     
	    			//System.out.print(diffDays + " days, ");
	    			//System.out.print(diffHours + " hours, ");
	    			//System.out.print(diffMinutes + " minutes, ");
	    			System.out.print(diffSeconds + " seconds.");
	    			Log.i("test loop",diffMilliseconds+"milliseconds");
	    			Log.i("test loop",diffSeconds+"seconds");
	    			Log.i("test loop",diffMinutes+"minuts");
	    			Log.i("test loop",diffHours+"hours");
	    			Log.i("test loop",diffDays+"Days");


	     
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	     
	    	}
	     
	    }

	    


	  



