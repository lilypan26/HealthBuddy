package com.example.iotfinalproject;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

public class UserHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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

}

