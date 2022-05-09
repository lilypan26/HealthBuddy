package com.example.iotfinalproject;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class UserHomeActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView gsr_value = null;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private final int delay = 1000; //One second = 1000 milliseconds.
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private List<Integer> lookback = new ArrayList<>();
    private final int numDataPoints = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Button refresh_button = findViewById(R.id.refresh);
        refresh_button.setOnClickListener(this);

        gsr_value = (TextView) findViewById (R.id.gsr_value);

        graph = (GraphView) findViewById(R.id.graph);
        initGraph(graph);

        Button notify_button = findViewById(R.id.notify);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("test_channel", "test_channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }

        notify_button.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     NotificationCompat.Builder builder = new NotificationCompat.Builder(
                             UserHomeActivity.this, "test_channel");
                     builder.setContentTitle("Hydrate");
                     builder.setContentText("Drink some water!");
                     builder.setSmallIcon(R.drawable.drink_water);
                     builder.setAutoCancel(true);

                     NotificationManagerCompat managerCompat = NotificationManagerCompat.from(UserHomeActivity.this);
                     managerCompat.notify(1, builder.build());
                 }
             }
        );

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.refresh:
                graph.removeAllSeries();
                lookback = datapoints.subList(Math.max(datapoints.size() - numDataPoints, 0), datapoints.size());
                initGraph(graph);
                break;
        }
    }

    public void initGraph(GraphView graph) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i < lookback.size(); i++) {
            dataPoints.add(new DataPoint(i, lookback.get(i)));
        }
        DataPoint[] arr = new DataPoint[dataPoints.size()];
        arr = dataPoints.toArray(arr);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(arr);
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph.addSeries(series);
//        series.setTitle("GSR Value");

        graph.getViewport().setYAxisBoundsManual(false);
        graph.getLegendRenderer().setVisible(false);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(Math.min(11, numDataPoints + 1));
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("GSR Value");

    }

    public void getGSRValue() {
        try {
            Socket s = new Socket("192.168.1.126", 65432);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String response = in.readLine();
            Log.i("gsr data:", response);
            datapoints.add(Integer.valueOf(response));
            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    gsr_value.setText("Current GSR Value: " + response);
                }
            }));
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

