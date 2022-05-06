package com.example.iotfinalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class UserHomeActivity extends AppCompatActivity {
    private TextView gsr_value = null;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int delay = 1000; //One second = 1000 milliseconds.
    private int val = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        gsr_value = (TextView) findViewById (R.id.gsr_value);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        initGraph(graph);
    }
    public void initGraph(GraphView graph) {
        int numDataPoints = 0;

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();

        series.setShape(PointsGraphSeries.Shape.POINT);
//        int yMax = ((int)(series.getHighestValueY()+ 99) / 100 ) * 100 + 100;
        series.setSize(10);
        series.setTitle("Duration");
        graph.addSeries(series);

//        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 0),
//                new DataPoint(Math.max(11, numDataPoints + 1), 0)
//        });
//        series2.setColor(Color.RED);
//        graph.addSeries(series2);
//        series2.setTitle("Average");


        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10);
        graph.getLegendRenderer().setVisible(false);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(Math.min(11, numDataPoints + 1));
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("GSR Value");

    }

    public void getGSRValue() {
        val += 1;
        String response = String.valueOf(val);
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                gsr_value.setText("Current GSR Value: " + response);
            }
        }));
//        try {
//            Socket s = new Socket("192.168.1.126", 65432);
//            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//            String response = in.readLine();
//            response = String.valueOf(i);
//            runOnUiThread(new Thread(new Runnable() {
//                public void run() {
//                    gsr_value.setText("Current GSR Value: " + response);
//                }
//            }));
//
//            s.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        getGSRValue();
                    }
                });

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    // If onPause() is not included the threads will double up when you reload the activity
    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
}

