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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultFragment extends Fragment {

    private String OPENAI_API_KEY = "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";

    private TextView resultText;
    private ImageView previewImage;
    private LinearLayout macroRow;
    private Chip proteinChip, carbsChip, fatChip;

    // Feedback UI
    private Button yesButton, noButton, submitFeedbackButton;
    private EditText commentBox;
    private LinearLayout commentContainer;

    // Firebase reference
    private DatabaseReference feedbackRef;

    // Real‑time Database reference
    private DatabaseReference mealsRef;

    // Firestore for meal data
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button saveMealButton;  // new button to log the analyzed meal

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        // --- Initialize UI components ---
        resultText = view.findViewById(R.id.resultText);
        previewImage = view.findViewById(R.id.previewImage);
        macroRow = view.findViewById(R.id.macroRow);
        proteinChip = view.findViewById(R.id.proteinChip);
        carbsChip = view.findViewById(R.id.carbsChip);
        fatChip = view.findViewById(R.id.fatChip);

        yesButton = view.findViewById(R.id.yesButton);
        noButton = view.findViewById(R.id.noButton);
        submitFeedbackButton = view.findViewById(R.id.submitFeedbackButton);
        commentBox = view.findViewById(R.id.commentBox);
        commentContainer = view.findViewById(R.id.commentContainer);

        // --- Firebase setup ---
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");
        mealsRef = FirebaseDatabase.getInstance().getReference("meals");

        // --- Firestore setup for meal logging ---
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        saveMealButton = view.findViewById(R.id.saveMealButton);
        if (saveMealButton != null) {
            saveMealButton.setOnClickListener(v -> saveMealToRealtimeDB());
        }

        // --- Button functionality ---
        yesButton.setOnClickListener(v -> {
            saveFeedback(true, "");
            disableFeedbackButtons();
            Toast.makeText(getContext(), "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
        });

        noButton.setOnClickListener(v -> {
            commentContainer.setVisibility(View.VISIBLE);
        });

        submitFeedbackButton.setOnClickListener(v -> {
            String comment = commentBox.getText().toString().trim();
            if (comment.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a comment before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveFeedback(false, comment);
            disableFeedbackButtons();
            Toast.makeText(getContext(), "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
        });

        // --- Handle passed image ---
        if (getArguments() != null) {
            String imageUriString = getArguments().getString("imageUri");
            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                previewImage.setImageURI(imageUri);
                analyzeImageWithGPT(imageUri);
            }
        }

        return view;
    }

    // --- Disable buttons after feedback submitted ---
    private void disableFeedbackButtons() {
        yesButton.setEnabled(false);
        noButton.setEnabled(false);
        submitFeedbackButton.setEnabled(false);
        commentBox.setEnabled(false);
    }

    // --- Save feedback to Firebase ---
    private void saveFeedback(boolean wasAccurate, String comment) {
        String feedbackId = feedbackRef.push().getKey();
        Feedback feedback = new Feedback(wasAccurate, comment, System.currentTimeMillis());
        if (feedbackId != null) {
            feedbackRef.child(feedbackId).setValue(feedback)
                    .addOnSuccessListener(aVoid -> {
                        // Optional: success toast handled in click logic
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // --- Analyze image with GPT ---
    private void analyzeImageWithGPT(Uri imageUri) {
        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageBytes = stream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                OkHttpClient client = new OkHttpClient();

                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");

                String prompt = "Analyze the food in this image. Provide a calorie and macro estimate. Do not include any intro text, just the analysis. " +
                        "Show how much each analyzed component contributed to the macro and calorie breakdown. " +
                        "Format it like this:\n\n" +
                        "- [Component 1]: ~[Calories], P:[X]g, C:[Y]g, F:[Z]g\n" +
                        "- [Component 2]: ~[Calories], P:[X]g, C:[Y]g, F:[Z]g\n\n" +
                        "Total Estimated Calories: ~[Number]\n\n" +
                        "Total Macros:\n" +
                        "- Protein: [X]g\n" +
                        "- Carbs: [Y]g\n" +
                        "- Fat: [Z]g";

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
                requestBodyJson.put("max_tokens", 150);

                RequestBody body = RequestBody.create(
                        requestBodyJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + OPENAI_API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseString = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseString);
                String output = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                requireActivity().runOnUiThread(() -> {
                    resultText.setText(output);
                    resultText.setAlpha(0f);
                    resultText.animate().alpha(1f).setDuration(600).start();

                    if (output.contains("Protein:") && output.contains("Carbs:") && output.contains("Fat:")) {
                        try {
                            macroRow.setVisibility(View.VISIBLE);
                            String protein = output.split("Protein:")[1].split("g")[0].trim();
                            String carbs = output.split("Carbs:")[1].split("g")[0].trim();
                            String fat = output.split("Fat:")[1].split("g")[0].trim();
                            proteinChip.setText("Protein: " + protein + "g");
                            carbsChip.setText("Carbs: " + carbs + "g");
                            fatChip.setText("Fat: " + fat + "g");
                        } catch (Exception ignored) {}
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        resultText.setText("⚠️ Network error: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        resultText.setText("⚠️ API error: " + e.getMessage()));
            }
        }).start();
    }

    private void saveMealToRealtimeDB() {
        String mealName = "Custom Meal"; // Or extract from resultText / user input
        String output = resultText.getText().toString();

        double calories = 0, protein = 0, carbs = 0, fat = 0;

        try {
            // Extract total calories (same logic as before)
            Matcher caloriesMatcher = Pattern.compile("~(\\d+(?:\\.\\d+)?)\\s*Calories", Pattern.MULTILINE).matcher(output);
            while (caloriesMatcher.find()) {
                calories += Double.parseDouble(caloriesMatcher.group(1));
            }

            // --- New regex patterns to catch both shorthand and full words ---
            Pattern patternP = Pattern.compile("(?:P\\s*[:=]\\s*)(\\d+(?:\\.\\d+)?)\\s*g", Pattern.CASE_INSENSITIVE);
            Pattern patternC = Pattern.compile("(?:C\\s*[:=]\\s*)(\\d+(?:\\.\\d+)?)\\s*g", Pattern.CASE_INSENSITIVE);
            Pattern patternF = Pattern.compile("(?:F\\s*[:=]\\s*)(\\d+(?:\\.\\d+)?)\\s*g", Pattern.CASE_INSENSITIVE);

            Matcher matcherP = patternP.matcher(output);
            Matcher matcherC = patternC.matcher(output);
            Matcher matcherF = patternF.matcher(output);

            // Sum all occurrences for total macros
            while (matcherP.find()) {
                protein += Double.parseDouble(matcherP.group(1));
            }
            while (matcherC.find()) {
                carbs += Double.parseDouble(matcherC.group(1));
            }
            while (matcherF.find()) {
                fat += Double.parseDouble(matcherF.group(1));
            }

            Log.d("Parsed-Macros", "Calories=" + calories + ", Protein=" + protein + ", Carbs=" + carbs + ", Fat=" + fat);

        } catch (Exception e) {
            Log.e("Parser", "Error parsing macros", e);
            Toast.makeText(getContext(), "Could not parse nutrition details.", Toast.LENGTH_SHORT).show();
        }

        Meal meal = new Meal(mealName, calories, protein, carbs, fat, System.currentTimeMillis());

        String mealId = mealsRef.push().getKey(); // generates unique child id
        if (mealId != null) {
            mealsRef.child(mealId).setValue(meal)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Meal saved to Realtime Database!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}

