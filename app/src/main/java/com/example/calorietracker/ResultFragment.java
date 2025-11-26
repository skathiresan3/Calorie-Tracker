package com.example.calorietracker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultFragment extends Fragment {
    private String OPENAI_API_KEY = "";
    private Uri imageUri;
    private NutritionViewModel nutritionViewModel;

    private TextView calorieTextView, proteinTextView, carbsTextView, fatTextView;
    private MaterialButton likeButton, dislikeButton, saveMealButton;
    private EditText commentInput;
    private EditText mealNameInput;

    private int fetchedCalories = 0;
    private int fetchedProtein = 0;
    private int fetchedCarbs = 0;
    private int fetchedFat = 0;
    private String currentMealId;

    private boolean isHistoryView = false;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nutritionViewModel = new ViewModelProvider(requireActivity()).get(NutritionViewModel.class);
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            String uriString = getArguments().getString("imageUri");
            if (uriString != null) {
                imageUri = Uri.parse(uriString);
            }

            isHistoryView = getArguments().getBoolean("isHistoryView", false);

            if (isHistoryView) {
                fetchedCalories = getArguments().getInt("calories");
                fetchedProtein = getArguments().getInt("protein");
                fetchedCarbs = getArguments().getInt("carbs");
                fetchedFat = getArguments().getInt("fat");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        ConstraintLayout mainContent = view.findViewById(R.id.mainContent);
        ConstraintLayout thankYouLayout = view.findViewById(R.id.thankYouLayout);
        View mainScrollView = view.findViewById(R.id.mainContentScrollView);

        ShapeableImageView mealImageView = mainContent.findViewById(R.id.mealImageView);
        likeButton = mainContent.findViewById(R.id.likeButton);
        dislikeButton = mainContent.findViewById(R.id.dislikeButton);
        saveMealButton = mainContent.findViewById(R.id.saveMealButton);

        commentInput = mainContent.findViewById(R.id.commentInput);
        mealNameInput = mainContent.findViewById(R.id.mealNameInput);

        MaterialButton analyzeAnotherButton = thankYouLayout.findViewById(R.id.analyzeAnotherButton);
        MaterialButton backToHomeButton = thankYouLayout.findViewById(R.id.backToHomeButton);

        calorieTextView = mainContent.findViewById(R.id.calorieTextView);
        proteinTextView = mainContent.findViewById(R.id.proteinTextView);
        carbsTextView = mainContent.findViewById(R.id.carbsTextView);
        fatTextView = mainContent.findViewById(R.id.fatTextView);

        if (isHistoryView) {
            mealImageView.setImageResource(R.drawable.ic_launcher_foreground); // Use a generic icon

            calorieTextView.setText(String.valueOf(fetchedCalories));
            proteinTextView.setText(fetchedProtein + "g");
            carbsTextView.setText(fetchedCarbs + "g");
            fatTextView.setText(fetchedFat + "g");

            if (getArguments() != null) {
                mealNameInput.setText(getArguments().getString("mealName", ""));
            }

            saveMealButton.setText("Log This Meal Again");
            setButtonsEnabled(true);

        } else {
            if (imageUri != null) {
                mealImageView.setImageURI(imageUri);
                analyzeImageWithGPT(imageUri);
            }
            calorieTextView.setText("...");
            proteinTextView.setText("...");
            carbsTextView.setText("...");
            fatTextView.setText("...");
            setButtonsEnabled(false);
        }

        saveMealButton.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;

            if (fetchedCalories > 0) {

                if (isHistoryView) {
                    // Log as NEW meal for today
                    String name = mealNameInput.getText().toString();
                    if (name.isEmpty()) name = "Quick Add Meal";

                    DatabaseReference userMealsRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(currentUser.getUid()).child("meals");

                    Meal meal = new Meal(name, fetchedCalories, fetchedProtein, fetchedCarbs, fetchedFat, System.currentTimeMillis());
                    userMealsRef.push().setValue(meal)
                            .addOnSuccessListener(a -> Toast.makeText(getContext(), "Logged Again!", Toast.LENGTH_SHORT).show());

                } else {
                    if (currentMealId != null) {
                        String customName = mealNameInput.getText().toString().trim();
                        if (!customName.isEmpty()) {
                            DatabaseReference userMealsRef = FirebaseDatabase.getInstance()
                                    .getReference("users").child(currentUser.getUid()).child("meals");
                            userMealsRef.child(currentMealId).child("name").setValue(customName);
                        }
                        Toast.makeText(getContext(), "Meal Updated!", Toast.LENGTH_SHORT).show();
                    }
                }
                saveMealButton.setEnabled(false);
            }
        });

        View.OnClickListener feedbackClickListener = v -> {
            boolean isLike = (v.getId() == R.id.likeButton);
            saveFeedbackToFirebase(isLike);
            if (mainScrollView != null) mainScrollView.setVisibility(View.GONE);
            thankYouLayout.setVisibility(View.VISIBLE);
        };

        likeButton.setOnClickListener(feedbackClickListener);
        dislikeButton.setOnClickListener(feedbackClickListener);

        analyzeAnotherButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ResultFragment.this).popBackStack();
        });

        backToHomeButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ResultFragment.this).popBackStack(R.id.HomeFragment, false);
        });

        return view;
    }


    private void analyzeImageWithGPT(Uri imageUri) {
        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] imageBytes = stream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                String prompt = "Analyze the food in this image. Provide a strict estimate for Calories, Protein, Carbs, and Fat. " +
                        "Return ONLY the numbers in this exact format:\n" +
                        "CALORIES: [number]\n" +
                        "PROTEIN: [number]\n" +
                        "CARBS: [number]\n" +
                        "FAT: [number]\n" +
                        "Do not include units (g) or range symbols (~) in the output numbers.";
                textPart.put("text", prompt);

                JSONObject imagePart = new JSONObject();
                imagePart.put("type", "image_url");
                JSONObject imageUrlObject = new JSONObject();
                imageUrlObject.put("url", "data:image/jpeg;base64," + base64Image);
                imagePart.put("image_url", imageUrlObject);

                JSONArray contentArray = new JSONArray();
                contentArray.put(textPart);
                contentArray.put(imagePart);

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", contentArray);

                JSONArray messages = new JSONArray();
                messages.put(message);

                JSONObject requestBodyJson = new JSONObject();
                requestBodyJson.put("model", "gpt-4o-mini");
                requestBodyJson.put("messages", messages);
                requestBodyJson.put("max_tokens", 100);

                RequestBody body = RequestBody.create(
                        requestBodyJson.toString(),
                        MediaType.parse("application/json")
                );

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + OPENAI_API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseString = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseString);
                String output = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                parseAndAutoSave(output);

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Analysis Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        }).start();
    }

    private void parseAndAutoSave(String output) {
        int c = extractInt(output, "CALORIES:");
        int p = extractInt(output, "PROTEIN:");
        int carbs = extractInt(output, "CARBS:");
        int f = extractInt(output, "FAT:");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference userMealsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("meals");

        Meal meal = new Meal("Scanned Meal", c, p, carbs, f, System.currentTimeMillis());

        DatabaseReference newRef = userMealsRef.push();
        currentMealId = newRef.getKey();

        newRef.setValue(meal).addOnSuccessListener(aVoid -> {
            fetchDataFromFirebaseAndUpdateUI(newRef);
        });
    }

    private void fetchDataFromFirebaseAndUpdateUI(DatabaseReference ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Meal savedMeal = snapshot.getValue(Meal.class);
                if (savedMeal != null) {
                    fetchedCalories = (int) savedMeal.getCalories();
                    fetchedProtein = (int) savedMeal.getProtein();
                    fetchedCarbs = (int) savedMeal.getCarbs();
                    fetchedFat = (int) savedMeal.getFat();

                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            calorieTextView.setText(String.valueOf(fetchedCalories));
                            proteinTextView.setText(fetchedProtein + "g");
                            carbsTextView.setText(fetchedCarbs + "g");
                            fatTextView.setText(fetchedFat + "g");
                            setButtonsEnabled(true);
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveFeedbackToFirebase(boolean isLike) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to save feedback.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String userComment = commentInput.getText().toString().trim();
        String finalComment = userComment.isEmpty() ? (isLike ? "Liked" : "Disliked") : userComment;

        DatabaseReference userFeedbackRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("feedback");

        Feedback feedback = new Feedback(isLike, finalComment, System.currentTimeMillis());

        userFeedbackRef.push().setValue(feedback)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "User feedback saved"))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save feedback", Toast.LENGTH_SHORT).show());
    }

    private int extractInt(String text, String key) {
        try {
            if (text.contains(key)) {
                String temp = text.split(key)[1].trim();
                String numberStr = temp.split("\\s+")[0];
                numberStr = numberStr.replaceAll("[^0-9]", "");
                return Integer.parseInt(numberStr);
            }
        } catch (Exception e) {}
        return 0;
    }

    private void setButtonsEnabled(boolean enabled) {
        if (likeButton != null) likeButton.setEnabled(enabled);
        if (dislikeButton != null) dislikeButton.setEnabled(enabled);
        if (saveMealButton != null) saveMealButton.setEnabled(enabled);
        if (commentInput != null) commentInput.setEnabled(enabled);
        if (mealNameInput != null) mealNameInput.setEnabled(enabled);
    }
}