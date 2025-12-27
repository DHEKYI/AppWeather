package com.example.appweather;

public class ForecastDay {
    private String dayOfWeek;
    private int weatherIcon; // This stores the R.drawable.id
    private String highTemp;
    private String lowTemp;
    private String condition;

    public ForecastDay(String dayOfWeek, int weatherIcon, String highTemp, String lowTemp, String condition) {
        this.dayOfWeek = dayOfWeek;
        this.weatherIcon = weatherIcon;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.condition = condition;
    }

    // These MUST match the names used in your ForecastAdapter
    public String getDayOfWeek() { return dayOfWeek; }
    public int getWeatherIcon() { return weatherIcon; } // The Adapter calls this
    public String getHighTemp() { return highTemp; }
    public String getLowTemp() { return lowTemp; }
    public String getCondition() { return condition; }
}