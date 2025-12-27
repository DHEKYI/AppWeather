package com.example.appweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

// --- REQUIRED IMPORTS FOR VOICE/TTS ---
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import android.speech.RecognizerIntent; // Required for Voice Input (STT)
import java.util.ArrayList; // Required for handling voice results
import java.util.Locale;

// Required Volley Imports for Networking
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

// Required JSON Imports for Parsing Data
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;





// The main class that controls our screen
public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFS_NAME = "AppWeatherPrefs";
    public static final String KEY_USER_POINTS = "userPoints";
    // --- ADD THIS LINE ---
    public static final String KEY_LAST_FORECAST_DATE = "lastForecastDate";



    //...for the point score
    private TextView pointsTextView;
    private int userPoints = 0;
    private String lastUpdatedTime = " ";



    // --- 1. CONFIGURATION CONSTANTS (API KEY AND LOCATION) ---


    private static final String API_KEY = "42152a65c9cc46de99b2e96ef12cd8a9";
    // **CRITICAL FIX: This constant is declared here correctly.**
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    // Coordinates for Charing, Upper Mustang, Nepal (Approximate)
    private static double CURRENT_LATITUDE = 29.1706;
    private static double CURRENT_LONGITUDE = 83.9482;
    private static String CURRENT_LOCATION_NAME = "Lo Manthang, Upper Mustang";

    private static final String BASE_WEATHER_URL =
            "https://api.openweathermap.org/data/2.5/weather?";
    private static final String BASE_GEOCODING_URL =
            "https://api.openweathermap.org/geo/1.0/direct?q=";

    // --- 2. TTS & DATA DECLARATION ---
    private TextToSpeech tts;
    private boolean isTtsInitialized = false;
    private String currentWeatherDescription = "Loading weather...";
    private String currentTemperature = "0";

    // --- 3. UI DECLARATIONS ---
    private TextView cityTextView;
    private TextView tempTextView;
    private ImageView weatherIconImageView;

    private TextView windTextView, humidityTextView, visibilityTextView;
    private ImageButton searchButton, speakerIcon;
    private Button forecastButton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        // --- UI INITIALIZATION ---
        cityTextView = findViewById(R.id.cityTextView);
        tempTextView = findViewById(R.id.tempTextView);
        weatherIconImageView = findViewById(R.id.weatherIconImageView);
        windTextView = findViewById(R.id.windTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        visibilityTextView = findViewById(R.id.visibilityTextView);
        searchButton = findViewById(R.id.searchButton);
        speakerIcon = findViewById(R.id.speakerIcon);
        forecastButton = findViewById(R.id.forecastButton);

        cityTextView.setText(CURRENT_LOCATION_NAME);
        //for point
        //for point
        pointsTextView = findViewById(R.id.pointsTextView);
        Button forecastButton = findViewById(R.id.forecastButton);

// --- LOAD SAVED POINTS ---
        loadPoints();
        updatePointsDisplay(); // Update the screen with the loaded points




        // --- TTS INITIALIZATION ---
        initializeTtsEngine();

        // --- BUTTON LISTENERS ---
        // Search Button now triggers Voice Input (STT)
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }

        });

        // Speaker Button triggers TTS
        speakerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTtsInitialized) {
                    speakNepaliWeather();
                } else {
                    promptTtsInstallation();
                }
            }
        });

        forecastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // --- ADD THE NEW DAILY POINT LOGIC HERE ---

                // 1. Get today's date in a simple "YYYY-MM-DD" format
                String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

                // 2. Get the last date the user earned points
                SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
                String lastClaimedDate = prefs.getString(KEY_LAST_FORECAST_DATE, "");

                // 3. Check if today is a new day
                if (!today.equals(lastClaimedDate)) {
                    // It's a new day! Award the points.
                    userPoints += 15;
                    updatePointsDisplay();
                    savePoints(); // Don't forget to save the total points

                    // Save today's date so they can't claim again today
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_LAST_FORECAST_DATE, today);
                    editor.apply();

                    Toast.makeText(MainActivity.this, "+15 points for checking the forecast!", Toast.LENGTH_SHORT).show();
                } else {
                    // It's the same day. Inform the user.
                    Toast.makeText(MainActivity.this, "You've already earned forecast points for today!", Toast.LENGTH_SHORT).show();
                }

                // The logic to start the new activity remains the same
                Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                startActivity(intent);
            }
        });

        // --- START FETCHING DATA FOR DEFAULT LOCATION ---
        fetchWeatherByCoordinates(CURRENT_LATITUDE, CURRENT_LONGITUDE, CURRENT_LOCATION_NAME);
    }

    private void updatePointsDisplay() {
        pointsTextView.setText("Points: " + userPoints);
    }
    private void savePoints() {
        // 1. Get a reference to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        // 2. Get an editor to write data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 3. Put the integer value with our key
        editor.putInt(KEY_USER_POINTS, userPoints);
        // 4. Apply the changes to save the file
        editor.apply();
    }

    // --- ADD THIS METHOD TO LOAD THE POINTS ---
    private void loadPoints() {
        // 1. Get a reference to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        // 2. Read the integer value. If it's not found, default to 0.
        userPoints = sharedPreferences.getInt(KEY_USER_POINTS, 0);
    }


    // ************************************************************
    // **************** VOICE INPUT (STT) LOGIC *******************
    // ************************************************************

    private void startVoiceInput() {
        // 1. Create the Intent for speech recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the city name you want to search.");


        try {
            // 2. Start the speech recognition activity
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception a) {
            Toast.makeText(getApplicationContext(),
                    "Speech recognition not supported on this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // This method handles the spoken text result returned by the device
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                // Get the list of recognized text results
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                String spokenCity = result.get(0);
                Toast.makeText(this, "Searching for: " + spokenCity, Toast.LENGTH_LONG).show();

                // Now use the spoken text to find the coordinates
                fetchCoordinatesForCity(spokenCity);
            }
        }
    }

    // ************************************************************
    // ****************** GEOCoding API LOGIC *********************
    // ************************************************************

    // New method to fetch Latitude/Longitude from a spoken city name
    private void fetchCoordinatesForCity(String cityName) {
        // Encode the city name for the URL
        String encodedCity = cityName.trim().replace(" ", "%20");
        String geocodingUrl = BASE_GEOCODING_URL + encodedCity
                + "&limit=1&appid=" + API_KEY;

        JsonObjectRequest geocodingRequest = new JsonObjectRequest(
                Request.Method.GET,
                geocodingUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            // The Geocoding response is an array of JSON objects
                            JSONArray jsonArray = new JSONArray(response.toString());

                            if (jsonArray.length() == 0) {
                                Toast.makeText(MainActivity.this, "City not found.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            JSONObject firstResult = jsonArray.getJSONObject(0);
                            double lat = firstResult.getDouble("lat");
                            double lon = firstResult.getDouble("lon");
                            String name = firstResult.getString("name");

                            // Update global variables
                            CURRENT_LATITUDE = lat;
                            CURRENT_LONGITUDE = lon;

                            // Set the new name on the screen
                            CURRENT_LOCATION_NAME = name;
                            cityTextView.setText(CURRENT_LOCATION_NAME);

                            // Now fetch the weather using the new coordinates
                            fetchWeatherByCoordinates(lat, lon, name);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Could not find coordinates for that city.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WeatherApp", "GeoCoding Error: " + error.toString());
                        Toast.makeText(MainActivity.this, "Network error during city search.", Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(geocodingRequest);
    }

    // ************************************************************
    // ********************** TTS LOGIC ***************************
    // ************************************************************

    private void initializeTtsEngine() {
        // Initialization code remains the same
        tts = new TextToSpeech(this, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true;
                    // Attempt to set Nepali first (ne_NP)
                    int result = tts.setLanguage(new Locale("ne", "NP"));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("TTS", "Nepali not supported. Falling back to English.");
                        tts.setLanguage(Locale.US);

                        if (tts.isLanguageAvailable(Locale.US) <= TextToSpeech.LANG_MISSING_DATA) {
                            Toast.makeText(MainActivity.this, "TTS Data Missing. Tap speaker to install!", Toast.LENGTH_LONG).show();
                            isTtsInitialized = false;
                        }
                    } else {
                        Log.i("TTS", "Nepali language successfully set.");
                    }
                } else {
                    Log.e("TTS", "TTS Initialization failed!");
                    isTtsInitialized = false;
                }
            }
        });
    }

    private void promptTtsInstallation() {
        Toast.makeText(this, "Launching TTS settings to download voice data.", Toast.LENGTH_LONG).show();
        try {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not launch voice settings.", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakNepaliWeather() {
        if (!isTtsInitialized || tts.isSpeaking()) {
            return;
        }

        // 1. Create the base English phrase (for console/debug)
        String englishPhrase = "The current temperature in " + CURRENT_LOCATION_NAME + " is " + currentTemperature + " degrees Celsius, with " + currentWeatherDescription + ".";

        // 2. Lookup the ACCURATE Nepali Translation
        // This uses the lookup table in your NepaliTranslator.java file:
        String nepaliPhrase = NepaliTranslator.buildFullNepaliPhrase(currentTemperature, currentWeatherDescription);

        String textToSpeak = nepaliPhrase; // Start by setting nepali as the default (safest)

        // Determine which language to speak
        Locale currentLocale = tts.getLanguage();

        // CRITICAL: If the language is successfully set to Nepali ('ne'), use the translated phrase.
        if (currentLocale != null && currentLocale.getLanguage().equals("ne")) {
            textToSpeak = nepaliPhrase; // <-- THIS IS THE FIX: We use the translated phrase
        }

        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);

        // Confirmation Toast
        Toast.makeText(this, "Speaking Nepali: " + textToSpeak.substring(0, Math.min(textToSpeak.length(), 40)) + "...", Toast.LENGTH_SHORT).show();

    }


    // --- Cleanup when the app closes (IMPORTANT for TTS resources) ---
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // ************************************************************
    // ********************* NETWORK LOGIC ************************
    // ************************************************************

    // Consolidated method to fetch weather using coordinates
    private void fetchWeatherByCoordinates(double lat, double lon, String locationName) {
        tempTextView.setText("...");
        windTextView.setText("Loading...");

        String weatherUrl = BASE_WEATHER_URL + "lat=" + lat + "&lon=" + lon +
                "&units=metric&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                weatherUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            updateUI(response, locationName);// passing locationNamw to updateUI
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing weather data.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tempTextView.setText("Err");
                        windTextView.setText("Connect Fail");
                        Toast.makeText(MainActivity.this, "Failed to connect to weather service.", Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    // --- JSON PARSING AND UI UPDATE METHOD ---
    private void updateUI(JSONObject response, String locationName) throws JSONException {
        // JSON parsing logic
        JSONObject main = response.getJSONObject("main");
        double temp = main.getDouble("temp");
        int humidity = main.getInt("humidity");

        JSONObject wind = response.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");

        JSONObject weatherObject = response.getJSONArray("weather").getJSONObject(0);
        String currentWeatherDescription = weatherObject.getString("description");
        String weatherIconId = weatherObject.getString("icon");

        int visibilityMeters = response.getInt("visibility");
        double visibilityKm = visibilityMeters / 1000.0;

        //ui element for alert system
        LinearLayout alertBanner = findViewById(R.id.alertBanner);
        TextView alertTextView = findViewById(R.id.alertTextView);
        ImageView alertIcon = findViewById(R.id.alertIcon);

        //risk assessment logic
        boolean isSnowing = currentWeatherDescription.toLowerCase().contains("snow");
        boolean isStrongWind = windSpeed > 10; //threshold for strong wind

        if (isSnowing || isStrongWind) {
            alertBanner.setBackgroundColor(android.graphics.Color.parseColor("#ff0000"));
            alertIcon.setImageResource(R.drawable.alert); // R DRAWABLE IN PNG
            alertIcon.setColorFilter(android.graphics.Color.parseColor("#D32F2F"));
            alertTextView.setTextColor(android.graphics.Color.parseColor("#D32F2F"));

            //specific warning
            if(isSnowing && isStrongWind){
                alertTextView.setText("Critical:!!! snow and high wind");
            } else if (isSnowing) {
                alertTextView.setText("Critical:!!! heavy snow");
            } else {
                alertTextView.setText("Critical:!!! Strong wind");
            }

        } else {
            //green state
            alertBanner.setBackgroundColor(android.graphics.Color.parseColor("#1b5e20")); // Light Green
            alertIcon.setImageResource(R.drawable.safe);
            alertIcon.setColorFilter(android.graphics.Color.parseColor("#1b5e20"));
            alertTextView.setTextColor(android.graphics.Color.parseColor("#1b5e20"));
            alertTextView.setText("STATUS: Normal. No Structural Risks.");
        }

        //--- icon loading of my drawable image ---
        int iconResource = getWeatherIconResource(weatherIconId);
        weatherIconImageView.setImageResource(iconResource);

        // --- UI Updates ---
        String currentTemperature = String.format(Locale.US, "%.0f", temp);
        tempTextView.setText(currentTemperature + "°C");
        windTextView.setText(String.format(Locale.US, "%.1f m/s", windSpeed));
        humidityTextView.setText(String.format(Locale.US, "%d%%", humidity));
        visibilityTextView.setText(String.format(Locale.US, "%.0f km", visibilityKm));

        Toast.makeText(this, "Weather Updated for " + locationName, Toast.LENGTH_SHORT).show();

    }

    // Helper Method to map API icon codes to your local drawable PNGs
    private int getWeatherIconResource(String iconCode) {
        if (iconCode == null) return R.drawable.unknown;

        switch (iconCode) {
            case "01d": case "01n": return R.drawable.clear_sky;
            case "02d": case "02n": return R.drawable.partly_cloudy;
            case "03d": case "03n": case "04d": case "04n": return R.drawable.cloudy;
            case "09d": case "09n": case "10d": case "10n": return R.drawable.rain;
            case "11d": case "11n": return R.drawable.thunder;
            case "13d": case "13n": return R.drawable.snow;
            case "50d": case "50n": return R.drawable.fog;
            default: return R.drawable.unknown;
        }
        }


}
