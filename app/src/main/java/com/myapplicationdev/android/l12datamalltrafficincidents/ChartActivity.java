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
import java.util.Objects;

public class ChartActivity extends AppCompatActivity {

    FirebaseFirestore fireStore;
    final String TAG = "ChartActivity";
    AnyChartView anyChartView;
    ArrayList<DataEntry> data;
    Cartesian vertical;
    Hashtable<String, Integer> incidents;
    Set set;
    Mapping barData;
    Bar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        anyChartView = findViewById(R.id.cv);
        anyChartView.setProgressBar(findViewById(R.id.pb));
        fireStore = FirebaseFirestore.getInstance();
        data = new ArrayList<>();
        incidents = new Hashtable<>();
        vertical = AnyChart.vertical();
        set = Set.instantiate();


        fireStore.collection("incidents").get()
                .addOnCompleteListener((Task<QuerySnapshot> task) -> {

                    if (task.isSuccessful()) {
                        incidents.clear();
                        for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                            String type = documentSnapshot.get("type", String.class);

                            if (!incidents.containsKey(type)) {
                                incidents.put(type, 1);
                            } else {
                                int value = incidents.get(type);
                                value += 1;
                                incidents.put(type, value);
                            }
                        }


                        for (String key : incidents.keySet()) {
                            data.add(new CustomDataEntry(key, incidents.get(key)));
                        }


                        // setting of the data
                        vertical.animation(true).title("Display of the traffic incident informations");
                        set.data(data);
                        barData = set.mapAs("{ x: 'x', value: 'value' }");
                        bar = vertical.bar(barData);
                        bar.labels().format("{%Value}");

                        // setting how the data to be displayed in the chart
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
                        // debugging message for errors
                        Log.d(TAG, "Error in getting documents:", task.getException());
                    }
                });

    }

    static class CustomDataEntry extends ValueDataEntry {
        public CustomDataEntry(String x, Number value) {
            super(x, value);
        }
    }
}