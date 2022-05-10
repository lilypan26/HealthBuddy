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

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONObject;

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

import GsrDataID.GsrandroidClient;
import GsrDataID.model.GsrData;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class UserHomeActivity extends OverflowMenuNavigator {
    private TextView gsr_value = null;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private final int delay = 1000; //One second = 1000 milliseconds.
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private final int numDataPoints = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        HomeNavigation.createHomeNavigation(this);

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

        Button aws_button = findViewById(R.id.aws);
        aws_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("https://6u341cmnni.execute-api.us-east-1.amazonaws.com/GsrData" + "/gsr")
                                    .build();

                            Call call = client.newCall(request);
                            Response response = null;
                            try {
                                response = call.execute();
                                System.out.println(response.body().string());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

//                            ApiClientFactory factory = new ApiClientFactory();
//                            final GsrandroidClient client = factory.credentialsProvider(null).build(GsrandroidClient.class);
//
//                            try {
//                                GsrData output = client.gsrGet();
//                                System.out.println(output.getGsrSensorTable());
//
//                            } catch (Exception e) {
//                                System.out.println(e);
//                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

            }
        });

    }

    public void initGraph(GraphView graph) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i < datapoints.size(); i++) {
            dataPoints.add(new DataPoint(i, datapoints.get(i)));
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
        graph.getViewport().setMaxX(Math.min(numDataPoints+1, numDataPoints + 2));
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
            if (datapoints.size() > numDataPoints)
                datapoints.remove(0);
            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    gsr_value.setText("Current GSR Value: " + response);
                }
            }));
            s.close();
        } catch (Exception e) {
//            e.printStackTrace();
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
                graph.removeAllSeries();
                initGraph(graph);
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

