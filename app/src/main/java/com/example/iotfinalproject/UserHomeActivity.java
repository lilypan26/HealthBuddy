package com.example.iotfinalproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.google.gson.Gson;
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

import GsrDataID.GsrandroidClient;
import GsrDataID.model.GsrData;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class UserHomeActivity extends OverflowMenuNavigator {
    private TextView gsr_value = null;
    private TextView dehydration_text = null;
    private TextView hunger_text = null;
    private TextView mood_text = null;


    private final Handler handler = new Handler();
    private Runnable runnable;
    private final int delay = 1000; //One second = 1000 milliseconds.
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private final int numDataPoints = 20;

    // Dehydration is above 440
    private final int DEHYDRATION_THRESHOLD = 440;

    // Hunger is shown between 300 and 400
    private final int HUNGER_MAX = 400;
    private final int HUNGER_MIN = 300;

    // Stress is below 300. Above 400 indicates relaxed
    private final int STRESS_THRESHOLD = 300;
    private final int RELAXED_THRESHOLD = 400;

    private boolean isHungry = false;
    private boolean isStressed = false;
    private boolean isRelaxed = false;
    private boolean isDehydrated = false;

    private boolean suspendHydrationNotification = false;
    private boolean suspendHungerNotification = false;
    private boolean suspendMoodNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        HomeNavigation.createHomeNavigation(this);

        gsr_value = findViewById (R.id.gsr_value);
        dehydration_text = findViewById (R.id.dehydration_text);
        hunger_text = findViewById (R.id.hunger_text);
        mood_text = findViewById (R.id.mood_text);

        graph = findViewById(R.id.graph);
        initGraph(graph);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("hydration_channel", "hydration_channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }

//        Button aws_button = findViewById(R.id.aws);
//        aws_button.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try  {
//                            OkHttpClient client = new OkHttpClient();
//                            Request request = new Request.Builder()
//                                    .url("https://6u341cmnni.execute-api.us-east-1.amazonaws.com/GsrData" + "/gsr")
//                                    .build();
//
//                            Call call = client.newCall(request);
//                            Response response = null;
//                            try {
//                                response = call.execute();
//                                System.out.println(response.body().string());
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }

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

//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//                thread.start();
//
//            }
//        });
//
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
//            Socket s = new Socket("192.168.1.126", 65432);
            Socket s = new Socket("100.71.88.14", 65432);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String response = in.readLine();
            Log.i("gsr data:", response);

            int gsr = Integer.valueOf(response);

            if (gsr > DEHYDRATION_THRESHOLD) {
                isDehydrated = true;
            } else {
                isDehydrated = false;
            }

            if (gsr > HUNGER_MIN && gsr < HUNGER_MAX) {
                isHungry = true;
            } else {
                isHungry = false;
            }

            if (gsr < STRESS_THRESHOLD) {
                isStressed = true;
            } else {
                isStressed = false;
            }

            if (gsr > RELAXED_THRESHOLD) {
                isRelaxed = true;
            } else {
                isRelaxed = false;
            }

            datapoints.add(gsr);
            if (datapoints.size() > numDataPoints)
                datapoints.remove(0);

            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    gsr_value.setText("Current GSR Value: " + response);

                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("");
                    ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);
                    ForegroundColorSpan green = new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.green));

                    if (isDehydrated) {
                        String text = "Hydration: Dehydrated";

                        spannableStringBuilder.clear();
                        spannableStringBuilder.append(text);
                        spannableStringBuilder.setSpan(red, 11, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        dehydration_text.setText(spannableStringBuilder);
                    } else {
                        String text = "Hydration: Good";

                        spannableStringBuilder.clear();
                        spannableStringBuilder.append(text);
                        spannableStringBuilder.setSpan(green, 11, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        dehydration_text.setText(spannableStringBuilder);
                    }

                    if (isHungry) {
                        String text = "Hunger level: Hungry";

                        spannableStringBuilder.clear();
                        spannableStringBuilder.append(text);
                        spannableStringBuilder.setSpan(red, 14, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        hunger_text.setText(spannableStringBuilder);
                    } else {
                        String text = "Hunger level: Good";

                        spannableStringBuilder.clear();
                        spannableStringBuilder.append(text);
                        spannableStringBuilder.setSpan(green, 14, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        hunger_text.setText(spannableStringBuilder);
                    }

                    if (isStressed) {
                        String text = "Mood: Stressed";

                        spannableStringBuilder.clear();
                        spannableStringBuilder.append(text);
                        spannableStringBuilder.setSpan(red, 6, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        mood_text.setText(spannableStringBuilder);

                    } else if (isRelaxed) {
                        String text = "Mood: Relaxed";

                        spannableStringBuilder.clear();
                        spannableStringBuilder.append(text);
                        spannableStringBuilder.setSpan(green, 6, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        mood_text.setText(spannableStringBuilder);

                    } else {
                        String text = "Mood: Neutral";

                        mood_text.setText(text);
                    }

                    // Determine whether to send notification
                    if (isDehydrated && !suspendHydrationNotification) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                         UserHomeActivity.this, "hydration_channel");
                         builder.setContentTitle("Hydrate");
                         builder.setContentText("You are likely dehydrated. Consider drinking some water");
                         builder.setSmallIcon(R.drawable.drink_water);
                         builder.setAutoCancel(true);

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(UserHomeActivity.this);
                        managerCompat.notify(1, builder.build());

                        suspendHydrationNotification = true;

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                suspendHydrationNotification = false;
                            }
                        }, 1000 * 60 * 15);     // 15 mins

                    }

                    if (isHungry && !suspendHungerNotification) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                UserHomeActivity.this, "hydration_channel");
                        builder.setContentTitle("Eat");
                        builder.setContentText("You are likely hungry. Consider eating some food");
                        builder.setSmallIcon(R.drawable.drink_water);
                        builder.setAutoCancel(true);

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(UserHomeActivity.this);
                        managerCompat.notify(1, builder.build());

                        suspendHungerNotification = true;

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                suspendHungerNotification = false;
                            }
                        }, 1000 * 60 * 15);     // 15 mins

                    }

                    if (isStressed && !suspendMoodNotification) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                UserHomeActivity.this, "hydration_channel");
                        builder.setContentTitle("Relax");
                        builder.setContentText("You are likely stressed. Consider trying to relax");
                        builder.setSmallIcon(R.drawable.drink_water);
                        builder.setAutoCancel(true);

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(UserHomeActivity.this);
                        managerCompat.notify(1, builder.build());

                        suspendMoodNotification = true;

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                suspendMoodNotification = false;
                            }
                        }, 1000 * 60 * 15);     // 15 mins

                    }

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

