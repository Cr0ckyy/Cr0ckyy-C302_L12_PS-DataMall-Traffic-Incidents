package com.myapplicationdev.android.l12datamalltrafficincidents;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Bar;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.enums.TooltipPositionMode;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class ChartActivity extends AppCompatActivity {

    FirebaseFirestore db;
    final String TAG = "ChartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        db = FirebaseFirestore.getInstance();

        Hashtable<String, Integer> incidents = new Hashtable<>();
        db.collection("incidents").get()
                .addOnCompleteListener((Task<QuerySnapshot> task) -> {
                    if (task.isSuccessful()) {
                        incidents.clear();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String type = document.get("type", String.class);
                            if (!incidents.containsKey(type)) {
                                incidents.put(type, 1);
                            } else {
                                int value = incidents.get(type);
                                value += 1;
                                incidents.put(type, value);
                            }
                        }

                        AnyChartView anyChartView = findViewById(R.id.chartview);
                        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

                        List<DataEntry> data = new ArrayList<>();

                        for (String key : incidents.keySet()) {
                            data.add(new CustomDataEntry(key, incidents.get(key)));
                        }

                        Cartesian vertical = AnyChart.vertical();

                        vertical.animation(true).title("Hello World");

                        Set set = Set.instantiate();
                        set.data(data);
                        Mapping barData = set.mapAs("{ x: 'x', value: 'value' }");

                        Bar bar = vertical.bar(barData);
                        bar.labels().format("{%Value}");

                        vertical.yScale().minimum(0d);

                        vertical.labels(true);

                        vertical.tooltip()
                                .displayMode(TooltipDisplayMode.UNION)
                                .positionMode(TooltipPositionMode.POINT);

                        vertical.interactivity().hoverMode(HoverMode.BY_X);

                        vertical.xAxis(true);
                        vertical.yAxis(true);
                        vertical.yAxis(0).labels().format("{%Value}");

                        anyChartView.setChart(vertical);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

    }

    static class CustomDataEntry extends ValueDataEntry {
        public CustomDataEntry(String x, Number value) {
            super(x, value);
        }
    }
}