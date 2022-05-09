package com.example.iotfinalproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends OverflowMenuNavigator implements View.OnClickListener {
    private List<Integer> datapoints = new ArrayList<>();
    private GraphView graph;
    private final int numDataPoints = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        HomeNavigation.createHomeNavigation(this);

        Button refresh_button = findViewById(R.id.refresh);
        refresh_button.setOnClickListener(this);

        graph = (GraphView) findViewById(R.id.graph);
        initGraph(graph);

    }

    public void initGraph(GraphView graph) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
//        for (int i = 0; i < lookback.size(); i++) {
//            dataPoints.add(new DataPoint(i, lookback.get(i)));
//        }
        DataPoint[] arr = new DataPoint[dataPoints.size()];
        arr = dataPoints.toArray(arr);

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
//        switch (v.getId()) {
//
//            case R.id.refresh:
//                graph.removeAllSeries();
//                initGraph(graph);
//                break;
//        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
