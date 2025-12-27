package com.example.appweather;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Volley Imports
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ForecastActivity extends AppCompatActivity {

    private ImageButton backButton;
    private RecyclerView forecastRecyclerView;
    private ForecastAdapter adapter;
    private List<ForecastDay> forecastList = new ArrayList<>();
    private RequestQueue requestQueue;

    // Configuration
    private static final String API_KEY = "42152a65c9cc46de99b2e96ef12cd8a9";
    private static final double LATITUDE = 29.1706;
    private static final double LONGITUDE = 83.9482;

    private static final String FORECAST_URL =
            "https://api.openweathermap.org/data/2.5/forecast?lat=" + LATITUDE +
                    "&lon=" + LONGITUDE +
                    "&units=metric&appid=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        requestQueue = Volley.newRequestQueue(this);

        backButton = findViewById(R.id.backButton);
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);

        // Setup RecyclerView
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ForecastAdapter(forecastList);
        forecastRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Returns to MainActivity
            }
        });

        // Update Title
        TextView title = findViewById(R.id.forecastTitle);
        title.setText("7-DAY FORECAST (Lo Manthang)");

        fetchForecastData();
    }

    private void fetchForecastData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                FORECAST_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            forecastList.clear();
                            processAndDisplayForecast(response.getJSONArray("list"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ForecastActivity.this, "Error processing forecast data.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForecastActivity.this, "Failed to fetch forecast. Check internet or API key.", Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void processAndDisplayForecast(JSONArray forecastArray) throws JSONException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US); // e.g., "Mon", "Tue"

        List<ForecastDay> dailySummaries = new ArrayList<>();
        if (forecastArray.length() == 0) return;

        // Initialize with the first forecast item
        JSONObject firstItem = forecastArray.getJSONObject(0);
        long firstTimestamp = firstItem.getLong("dt") * 1000;
        calendar.setTimeInMillis(firstTimestamp);

        String currentDay = dayFormat.format(calendar.getTime());
        double minTemp = firstItem.getJSONObject("main").getDouble("temp_min");
        double maxTemp = firstItem.getJSONObject("main").getDouble("temp_max");
        String iconCode = firstItem.getJSONArray("weather").getJSONObject(0).getString("icon");
        String dayCondition = firstItem.getJSONArray("weather").getJSONObject(0).getString("description");


        // Loop through the rest of the forecast entries
        for (int i = 1; i < forecastArray.length(); i++) {
            JSONObject item = forecastArray.getJSONObject(i);
            long timestamp = item.getLong("dt") * 1000;
            calendar.setTimeInMillis(timestamp);
            String day = dayFormat.format(calendar.getTime());

            // If it's a new day, save the summary of the previous day
            if (!day.equals(currentDay)) {
                // Get the integer resource ID for the icon
                int iconResource = getWeatherIconResource(iconCode); // <-- Use the new method
                dailySummaries.add(new ForecastDay(
                        currentDay,
                        iconResource, // Pass the integer ID
                        String.format(Locale.US, "%.0f°C", maxTemp),
                        String.format(Locale.US, "%.0f°C", minTemp),
                        dayCondition
                ));

                // Reset for the new day
                currentDay = day;
                minTemp = item.getJSONObject("main").getDouble("temp_min");
                maxTemp = item.getJSONObject("main").getDouble("temp_max");
                iconCode = item.getJSONArray("weather").getJSONObject(0).getString("icon");
                dayCondition = item.getJSONArray("weather").getJSONObject(0).getString("description");
            } else {
                // It's the same day, so just update min/max temps
                minTemp = Math.min(minTemp, item.getJSONObject("main").getDouble("temp_min"));
                maxTemp = Math.max(maxTemp, item.getJSONObject("main").getDouble("temp_max"));
                // Optionally update icon/condition to the last one for the day
                iconCode = item.getJSONArray("weather").getJSONObject(0).getString("icon");
                dayCondition = item.getJSONArray("weather").getJSONObject(0).getString("description");
            }
        }

        // After the loop, for the very last day
        int lastIconResource = getWeatherIconResource(iconCode); // <-- Use the new method
        dailySummaries.add(new ForecastDay(
                currentDay,
                lastIconResource, // Pass the integer ID
                String.format(Locale.US, "%.0f°C", maxTemp),
                String.format(Locale.US, "%.0f°C", minTemp),
                dayCondition
        ));

        // Update the RecyclerView
        forecastList.addAll(dailySummaries);
        adapter.notifyDataSetChanged();

        if (dailySummaries.size() > 0) {
            Toast.makeText(this, dailySummaries.size() + " days of forecast loaded!", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to map API icon codes to animated SVG URLs
    //// In ForecastActivity.java

    // Helper method to map API icon codes to your local drawable PNGs
    private int getWeatherIconResource(String iconCode) {
        switch (iconCode) {
            // DAY
            case "01d": return R.drawable.clear_sky;
            case "02d": return R.drawable.partly_cloudy;
            case "03d": return R.drawable.cloudy; // Or a specific scattered clouds image
            case "09d": return R.drawable.rain; // Or a specific drizzle image
            case "11d": return R.drawable.thunder;
            case "13d": return R.drawable.snow;
            case "50d": return R.drawable.fog; // Or mist

            // NIGHT (You can create specific night versions if you want)
            case "01n": return R.drawable.clear_sky; // Or a 'clear_night.png'
            case "02n": return R.drawable.partly_cloudy; // Or a 'cloudy_night.png'
            case "03n": return R.drawable.cloudy;
            case "10n": return R.drawable.rain;
            case "11n": return R.drawable.thunder;
            case "13n": return R.drawable.snow;
            case "50n": return R.drawable.fog;

            default:
                return R.drawable.unknown; // Your fallback image
        }
    }


}
