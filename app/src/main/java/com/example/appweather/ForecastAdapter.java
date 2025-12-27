package com.example.appweather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private final List<ForecastDay> forecastList;

    public ForecastAdapter(List<ForecastDay> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forecast_item, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastDay currentDay = forecastList.get(position);

        holder.dayOfWeekTextView.setText(currentDay.getDayOfWeek());
        holder.forecastCondition.setText(currentDay.getCondition());
        holder.highTempTextView.setText(currentDay.getHighTemp());
        holder.lowTempTextView.setText(currentDay.getLowTemp());

        // This line pulls the integer ID (like R.drawable.clear_sky)
        // from the ForecastDay object and puts it in the ImageView.
        holder.forecastIcon.setImageResource(currentDay.getWeatherIcon());
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    public static class ForecastViewHolder extends RecyclerView.ViewHolder {
        public TextView dayOfWeekTextView;
        public ImageView forecastIcon;
        public TextView forecastCondition;
        public TextView highTempTextView;
        public TextView lowTempTextView;

        public ForecastViewHolder(View itemView) {
            super(itemView);
            dayOfWeekTextView = itemView.findViewById(R.id.dayOfWeekTextView);
            forecastIcon = itemView.findViewById(R.id.forecastIcon);
            forecastCondition = itemView.findViewById(R.id.forecastCondition);
            highTempTextView = itemView.findViewById(R.id.highTempTextView);
            lowTempTextView = itemView.findViewById(R.id.lowTempTextView);
        }
    }
}