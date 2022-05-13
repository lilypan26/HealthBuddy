package com.example.iotfinalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import GsrDataID.model.GsrDataGsrSensorTableItem;
import GsrDataID.model.GsrData;
import GsrDataID.GsrandroidClient;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryActivity extends OverflowMenuNavigator {
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private List<GsrDataGsrSensorTableItem> gsrData = new ArrayList<>();
    private List<GsrDataObject> simpleGsrData = new ArrayList<>();
    private RangeSlider rangeSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        HomeNavigation.createHomeNavigation(this);

        graph = (GraphView) findViewById(R.id.graph);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        rangeSlider = findViewById(R.id.range_slider);
        rangeSlider.setEnabled(false);
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return simpleGsrData.get((int)value).getTimestamp().toString();
            }
        });
        rangeSlider.addOnSliderTouchListener( new RangeSlider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> values = slider.getValues();
                graph.removeAllSeries();
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> values = slider.getValues();
                initGraph(graph, Math.round(values.get(0)), Math.round(values.get(1)));
            }
        });

        Executors.newSingleThreadExecutor().execute(new Runnable() {
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
                        String response_string = response.body().string();
                        StringBuilder sb = new StringBuilder(response_string);
                        sb.deleteCharAt(response_string.length()-9);
                        String cleaned = sb.toString();
                        Gson gson = new Gson();
                        GsrData gsrDataList = gson.fromJson(cleaned, GsrData.class);
                        gsrData = gsrDataList.getGsrSensorTable();
                        List<Float> gsrAverages = processData();
                        runOnUiThread(new Thread(new Runnable() {
                            public void run() {
                                rangeSlider.setEnabled(true);

                            }
                        }));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public List<Float> processData() {
        for (GsrDataGsrSensorTableItem datapoint : gsrData) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS");
                Date parsedDate = dateFormat.parse(datapoint.getTimestamp());
                Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                simpleGsrData.add(new GsrDataObject(timestamp, datapoint.getGsr()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        simpleGsrData.sort((o1,o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        List<Float> gsrAverages = new ArrayList<>();
        for (int i = 0; i < simpleGsrData.size()-20; i+=20) {
            float sum = 0;
            for (int j = 0; j < 20; j++) {
                sum += simpleGsrData.get(i+j).getGsr();
            }
            Float avg = sum / (float)20;
            gsrAverages.add(avg);
        }
        return gsrAverages;
    }

    public void initGraph(GraphView graph, int start, int end) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (int i = start; i < end; i++) {
            dataPoints.add(new DataPoint(i, simpleGsrData.get(i).getGsr()));
        }
        DataPoint[] arr = new DataPoint[dataPoints.size()];
        arr = dataPoints.toArray(arr);
        int numDataPoints = arr.length;
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(arr);
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph.addSeries(series);
//        series.setTitle("GSR Value");
//        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
//
        graph.getViewport().setYAxisBoundsManual(false);
        graph.getLegendRenderer().setVisible(false);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(Math.min(11, numDataPoints));
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("GSR Value");
//        graph.getGridLabelRenderer().setHumanRounding(false);
//
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
