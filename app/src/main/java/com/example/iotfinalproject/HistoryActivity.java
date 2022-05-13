package com.example.iotfinalproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import GsrDataID.model.GsrDataGsrSensorTableItem;
import GsrDataID.model.GsrData;
import GsrDataID.GsrandroidClient;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryActivity extends OverflowMenuNavigator implements View.OnClickListener {
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private List<GsrDataGsrSensorTableItem> gsrData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        HomeNavigation.createHomeNavigation(this);

        Button refresh_button = findViewById(R.id.refresh);
        refresh_button.setOnClickListener(this);
        refresh_button.setEnabled(false);

        graph = (GraphView) findViewById(R.id.graph);
//        initGraph(graph);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

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

                        runOnUiThread(new Thread(new Runnable() {
                            public void run() {
                                initGraph(graph);
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

    public void initGraph(GraphView graph) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        int i = 0;
        for (GsrDataGsrSensorTableItem datapoint : gsrData) {
            dataPoints.add(new DataPoint(i, datapoint.getGsr()));
            i++;
        }
        DataPoint[] arr = new DataPoint[dataPoints.size()];
        arr = dataPoints.toArray(arr);
        if (arr.length != 0)
            arr = Arrays.copyOfRange(arr, 0, 20);
        int numDataPoints = arr.length;
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(arr);
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph.addSeries(series);
//        series.setTitle("GSR Value");
//
        graph.getViewport().setYAxisBoundsManual(false);
        graph.getLegendRenderer().setVisible(false);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(Math.min(11, numDataPoints + 1));
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("GSR Value");
//
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.refresh:
                initGraph(graph);
                break;
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
