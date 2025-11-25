package com.example.calorietracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList = new ArrayList<>();
    private OnMealClickListener listener;

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
    }

    public void setOnMealClickListener(OnMealClickListener listener) {
        this.listener = listener;
    }

    public void setMeals(List<Meal> meals) {
        this.mealList = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.bind(meal, listener);
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, caloriesText, dateText;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.mealName);
            caloriesText = itemView.findViewById(R.id.mealCalories);
            dateText = itemView.findViewById(R.id.mealDate);
        }

        public void bind(final Meal meal, final OnMealClickListener listener) {
            nameText.setText(meal.getName());
            caloriesText.setText((int) meal.getCalories() + " kcal");

            String date = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(new Date(meal.getTimestamp()));
            dateText.setText(date);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealClick(meal);
                }
            });
        }
    }
}