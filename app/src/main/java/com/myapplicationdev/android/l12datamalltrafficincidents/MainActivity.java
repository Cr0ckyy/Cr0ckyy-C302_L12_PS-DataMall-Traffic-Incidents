package com.myapplicationdev.android.l12datamalltrafficincidents;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Incident> incidents;
    IncidentAdapter incidentAdapter;
    AsyncHttpClient client;
    FirebaseFirestore fireStore;
    CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.lv);
        incidents = new ArrayList<>();
        incidentAdapter = new IncidentAdapter(this, R.layout.incident_row, incidents);
        listView.setAdapter(incidentAdapter);
        fireStore = FirebaseFirestore.getInstance();
        collectionReference = fireStore.collection("incidents");
        getData();

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("incidentSelected", incidents.get(position));
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle action bar item clicks here.
        int menuItemID = item.getItemId();

        if (menuItemID == R.id.itemGoogleMap) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            return true;

        } else if (menuItemID == R.id.itemUploadFirebase) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Proceed with the upload to the Firebase Firestore")
                    .setTitle("Upload to the Firebase");

            builder.setPositiveButton("Upload", (DialogInterface dialog, int id1) ->
                    collectionReference.get().addOnCompleteListener((Task<QuerySnapshot> task) -> {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                DocumentReference documentReference = collectionReference.document(document.getId());
                                documentReference.delete().addOnSuccessListener(
                                        (Void aVoid) ->
                                                Log.d("MainActivity", "Successfully deleted from Firebase Firestore")).addOnFailureListener(
                                        (Exception e) ->
                                                Log.d("MainActivity", "Failed to delete from into Firebase Firestore"));
                            }
                            for (Incident incident : incidents) {
                                collectionReference.add(incident)
                                        .addOnSuccessListener((DocumentReference documentReference) ->
                                                Log.d("MainActivity", "Firebase Firestore has been successfully added"))
                                        .addOnFailureListener((Exception e) ->
                                                Log.d("MainActivity", "Failure to add to Firebase Firestore"));
                            }
                        } else {
                            Log.d("MainActivity", "Successfully added into Firebase Firestore");
                        }
                    }));
            builder.setNegativeButton("Cancel", (DialogInterface dialog, int id12) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (menuItemID == R.id.itemReload) {
            getData();
            Toast.makeText(MainActivity.this, "The information has been reloaded.", Toast.LENGTH_SHORT).show();

        } else if (menuItemID == R.id.itemChart) {
            Intent intent = new Intent(MainActivity.this, ChartActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getData() {
        client = new AsyncHttpClient();
        client.addHeader("AccountKey", "GQ0cDI3gSsGPJ8gHZzLMSg==");
        client.addHeader("accept", "application/json");

        client.get("http://datamall2.mytransport.sg/ltaodataservice/TrafficIncidents",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        JSONObject incidentJSONObject = null;
                        incidents.clear();
                        try {
                            JSONArray incidentJSONArray = response.getJSONArray("value");
                            Log.i("incidentJSONArray", incidentJSONArray.toString());

                            for (int i = 0; i < incidentJSONArray.length(); i++) {
                                incidentJSONObject = (JSONObject) incidentJSONArray.get(i);
                                String type = incidentJSONObject.getString("Type");
                                double latitude = incidentJSONObject.getDouble("Latitude");
                                double longitude = incidentJSONObject.getDouble("Longitude");
                                String message = incidentJSONObject.getString("Message");

                                @SuppressLint("SimpleDateFormat") DateFormat dateformat =
                                        new SimpleDateFormat("(dd/MM)HH:mm");
                                String dateString = message.split(" ")[0];
                                Date date = dateformat.parse(dateString);

                                Incident newIncident = new Incident(type, latitude, longitude, message, date);
                                incidents.add(newIncident);
                            }
                            incidentAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}