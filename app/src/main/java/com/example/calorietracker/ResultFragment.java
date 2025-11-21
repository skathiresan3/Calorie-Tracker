//package com.example.calorietracker;
//
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.google.android.material.chip.Chip;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class ResultFragment extends Fragment {
//
//    private String OPENAI_API_KEY = "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";
//
//    private TextView resultText;
//    private ImageView previewImage;
//    private LinearLayout macroRow;
//    private Chip proteinChip, carbsChip, fatChip;
//
//    // Feedback UI
//    private Button yesButton, noButton, submitFeedbackButton;
//    private EditText commentBox;
//    private LinearLayout commentContainer;
//
//    // Firebase reference
//    private DatabaseReference feedbackRef;
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_result, container, false);
//
//        // --- Initialize UI components ---
//        resultText = view.findViewById(R.id.resultText);
//        previewImage = view.findViewById(R.id.previewImage);
//        macroRow = view.findViewById(R.id.macroRow);
//        proteinChip = view.findViewById(R.id.proteinChip);
//        carbsChip = view.findViewById(R.id.carbsChip);
//        fatChip = view.findViewById(R.id.fatChip);
//
//        yesButton = view.findViewById(R.id.yesButton);
//        noButton = view.findViewById(R.id.noButton);
//        submitFeedbackButton = view.findViewById(R.id.submitFeedbackButton);
//        commentBox = view.findViewById(R.id.commentBox);
//        commentContainer = view.findViewById(R.id.commentContainer);
//
//        // --- Firebase setup ---
//        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");
//
//        // --- Button functionality ---
//        yesButton.setOnClickListener(v -> {
//            saveFeedback(true, "");
//            disableFeedbackButtons();
//            Toast.makeText(getContext(), "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
//        });
//
//        noButton.setOnClickListener(v -> {
//            commentContainer.setVisibility(View.VISIBLE);
//        });
//
//        submitFeedbackButton.setOnClickListener(v -> {
//            String comment = commentBox.getText().toString().trim();
//            if (comment.isEmpty()) {
//                Toast.makeText(getContext(), "Please enter a comment before submitting.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            saveFeedback(false, comment);
//            disableFeedbackButtons();
//            Toast.makeText(getContext(), "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
//        });
//
//        // --- Handle passed image ---
//        if (getArguments() != null) {
//            String imageUriString = getArguments().getString("imageUri");
//            if (imageUriString != null) {
//                Uri imageUri = Uri.parse(imageUriString);
//                previewImage.setImageURI(imageUri);
//                analyzeImageWithGPT(imageUri);
//            }
//        }
//
//        return view;
//    }
//
//    // --- Disable buttons after feedback submitted ---
//    private void disableFeedbackButtons() {
//        yesButton.setEnabled(false);
//        noButton.setEnabled(false);
//        submitFeedbackButton.setEnabled(false);
//        commentBox.setEnabled(false);
//    }
//
//    // --- Save feedback to Firebase ---
//    private void saveFeedback(boolean wasAccurate, String comment) {
//        String feedbackId = feedbackRef.push().getKey();
//        Feedback feedback = new Feedback(wasAccurate, comment, System.currentTimeMillis());
//        if (feedbackId != null) {
//            feedbackRef.child(feedbackId).setValue(feedback)
//                    .addOnSuccessListener(aVoid -> {
//                        // Optional: success toast handled in click logic
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(getContext(), "Failed to save feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        }
//    }
//
//    // --- Analyze image with GPT ---
//    private void analyzeImageWithGPT(Uri imageUri) {
//        new Thread(() -> {
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
//                byte[] imageBytes = stream.toByteArray();
//                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
//
//                OkHttpClient client = new OkHttpClient();
//
//                JSONObject textPart = new JSONObject();
//                textPart.put("type", "text");
//
//                String prompt = "Analyze the food in this image. Provide a calorie and macro estimate. Do not include any intro text, just the analysis. " +
//                        "Show how much each analyzed component contributed to the macro and calorie breakdown. " +
//                        "Format it like this:\n\n" +
//                        "- [Component 1]: ~[Calories], P:[X]g, C:[Y]g, F:[Z]g\n" +
//                        "- [Component 2]: ~[Calories], P:[X]g, C:[Y]g, F:[Z]g\n\n" +
//                        "Total Estimated Calories: ~[Number]\n\n" +
//                        "Total Macros:\n" +
//                        "- Protein: [X]g\n" +
//                        "- Carbs: [Y]g\n" +
//                        "- Fat: [Z]g";
//
//                textPart.put("text", prompt);
//
//                JSONObject imagePart = new JSONObject();
//                imagePart.put("type", "image_url");
//
//                JSONObject imageUrlObject = new JSONObject();
//                imageUrlObject.put("url", "data:image/jpeg;base64," + base64Image);
//                imagePart.put("image_url", imageUrlObject);
//
//                JSONArray contentArray = new JSONArray();
//                contentArray.put(textPart);
//                contentArray.put(imagePart);
//
//                JSONObject message = new JSONObject();
//                message.put("role", "user");
//                message.put("content", contentArray);
//
//                JSONArray messages = new JSONArray();
//                messages.put(message);
//
//                JSONObject requestBodyJson = new JSONObject();
//                requestBodyJson.put("model", "gpt-4o-mini");
//                requestBodyJson.put("messages", messages);
//                requestBodyJson.put("max_tokens", 150);
//
//                RequestBody body = RequestBody.create(
//                        requestBodyJson.toString(),
//                        MediaType.parse("application/json")
//                );
//
//                Request request = new Request.Builder()
//                        .url("https://api.openai.com/v1/chat/completions")
//                        .header("Authorization", "Bearer " + OPENAI_API_KEY)
//                        .post(body)
//                        .build();
//
//                Response response = client.newCall(request).execute();
//
//                if (!response.isSuccessful()) {
//                    throw new IOException("Unexpected code " + response);
//                }
//
//                String responseString = response.body().string();
//                JSONObject jsonResponse = new JSONObject(responseString);
//                String output = jsonResponse
//                        .getJSONArray("choices")
//                        .getJSONObject(0)
//                        .getJSONObject("message")
//                        .getString("content");
//
//                requireActivity().runOnUiThread(() -> {
//                    resultText.setText(output);
//                    resultText.setAlpha(0f);
//                    resultText.animate().alpha(1f).setDuration(600).start();
//
//                    if (output.contains("Protein:") && output.contains("Carbs:") && output.contains("Fat:")) {
//                        try {
//                            macroRow.setVisibility(View.VISIBLE);
//                            String protein = output.split("Protein:")[1].split("g")[0].trim();
//                            String carbs = output.split("Carbs:")[1].split("g")[0].trim();
//                            String fat = output.split("Fat:")[1].split("g")[0].trim();
//                            proteinChip.setText("Protein: " + protein + "g");
//                            carbsChip.setText("Carbs: " + carbs + "g");
//                            fatChip.setText("Fat: " + fat + "g");
//                        } catch (Exception ignored) {}
//                    }
//                });
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                requireActivity().runOnUiThread(() ->
//                        resultText.setText("⚠️ Network error: " + e.getMessage()));
//            } catch (Exception e) {
//                e.printStackTrace();
//                requireActivity().runOnUiThread(() ->
//                        resultText.setText("⚠️ API error: " + e.getMessage()));
//            }
//        }).start();
//    }
//}



//
//
//package com.example.calorietracker;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
//import androidx.navigation.fragment.NavHostFragment;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.imageview.ShapeableImageView;
//import com.google.android.material.slider.Slider;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import android.widget.TextView;
//import java.util.Locale;
//
//public class ResultFragment extends Fragment {
//
//    private Uri imageUri;
//    private NutritionViewModel nutritionViewModel;
//
//    // --- SIMULATED GPT-4 RESPONSE ---
//    // In a real app, this data would come from your API call
//    private final int MEAL_CALORIES = 820;
//    private final int MEAL_PROTEIN = 40;
//    private final int MEAL_CARBS = 50;
//    private final int MEAL_FAT = 15;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Initialize the ViewModel
//        nutritionViewModel = new ViewModelProvider(requireActivity()).get(NutritionViewModel.class);
//
//        if (getArguments() != null) {
//            String uriString = getArguments().getString("imageUri");
//            if (uriString != null) {
//                imageUri = Uri.parse(uriString);
//            }
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_result, container, false);
//
//        // Parent layouts
//        ConstraintLayout mainContent = view.findViewById(R.id.mainContent);
//        ConstraintLayout thankYouLayout = view.findViewById(R.id.thankYouLayout);
//
//        // Find views using the 'mainContent' layout as the parent
//        ShapeableImageView mealImageView = mainContent.findViewById(R.id.mealImageView);
//        MaterialButton likeButton = mainContent.findViewById(R.id.likeButton);
//        MaterialButton dislikeButton = mainContent.findViewById(R.id.dislikeButton);
//
//        // Find the "Analyze Another" button from within the 'thankYouLayout'
//        MaterialButton analyzeAnotherButton = thankYouLayout.findViewById(R.id.analyzeAnotherButton);
//
//        // --- FIND THE NEW TEXTVIEWS ---
//        TextView calorieTextView = mainContent.findViewById(R.id.calorieTextView);
//        TextView proteinTextView = mainContent.findViewById(R.id.proteinTextView);
//        TextView carbsTextView = mainContent.findViewById(R.id.carbsTextView);
//        TextView fatTextView = mainContent.findViewById(R.id.fatTextView);
//
//
//        // --- POPULATE THE UI ---
//        if (imageUri != null) {
//            mealImageView.setImageURI(imageUri);
//        }
//
//        // Set the text for the new macro fields
//        calorieTextView.setText(String.valueOf(MEAL_CALORIES));
//        proteinTextView.setText(String.format(Locale.getDefault(), "%dg", MEAL_PROTEIN));
//        carbsTextView.setText(String.format(Locale.getDefault(), "%dg", MEAL_CARBS));
//        fatTextView.setText(String.format(Locale.getDefault(), "%dg", MEAL_FAT));
//
//
//        // --- EVENT LISTENERS ---
//
//        // Listener for the feedback buttons
//        View.OnClickListener feedbackClickListener = v -> {
//            // Add the meal's nutrition to our ViewModel
//            nutritionViewModel.addMeal(MEAL_CALORIES, MEAL_PROTEIN, MEAL_CARBS, MEAL_FAT);
//
//            // Show the "Thank You" screen (the ScrollView's ID is mainContentScrollView)
//            View mainScrollView = view.findViewById(R.id.mainContentScrollView);
//            if (mainScrollView != null) {
//                mainScrollView.setVisibility(View.GONE);
//            }
//            thankYouLayout.setVisibility(View.VISIBLE);
//        };
//        likeButton.setOnClickListener(feedbackClickListener);
//        dislikeButton.setOnClickListener(feedbackClickListener);
//
//        // Listener for the "Analyze Another Meal" button
//        analyzeAnotherButton.setOnClickListener(v -> {
//            // Navigate back to the home screen
//            NavHostFragment.findNavController(ResultFragment.this).popBackStack();
//        });
//
//        return view;
//    }
//}



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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultFragment extends Fragment {

    private String OPENAI_API_KEY = "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";


    private Uri imageUri;
    private NutritionViewModel nutritionViewModel;

    // UI References (needed for async updates)
    private TextView calorieTextView, proteinTextView, carbsTextView, fatTextView;
    private MaterialButton likeButton, dislikeButton;

    private int fetchedCalories = 0;
    private int fetchedProtein = 0;
    private int fetchedCarbs = 0;
    private int fetchedFat = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nutritionViewModel = new ViewModelProvider(requireActivity()).get(NutritionViewModel.class);

        if (getArguments() != null) {
            String uriString = getArguments().getString("imageUri");
            if (uriString != null) {
                imageUri = Uri.parse(uriString);
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
        MaterialButton analyzeAnotherButton = thankYouLayout.findViewById(R.id.analyzeAnotherButton);

        MaterialButton backToHomeButton = thankYouLayout.findViewById(R.id.backToHomeButton);

        calorieTextView = mainContent.findViewById(R.id.calorieTextView);
        proteinTextView = mainContent.findViewById(R.id.proteinTextView);
        carbsTextView = mainContent.findViewById(R.id.carbsTextView);
        fatTextView = mainContent.findViewById(R.id.fatTextView);

        if (imageUri != null) {
            mealImageView.setImageURI(imageUri);
            analyzeImageWithGPT(imageUri);
        }

//        setButtonsEnabled(false);

        calorieTextView.setText("...");
        proteinTextView.setText("...");
        carbsTextView.setText("...");
        fatTextView.setText("...");

        View.OnClickListener feedbackClickListener = v -> {
            nutritionViewModel.addMeal(fetchedCalories, fetchedProtein, fetchedCarbs, fetchedFat);

            // UI Transition
            if (mainScrollView != null) mainScrollView.setVisibility(View.GONE);
            thankYouLayout.setVisibility(View.VISIBLE);
        };

        likeButton.setOnClickListener(feedbackClickListener);
        dislikeButton.setOnClickListener(feedbackClickListener);

        analyzeAnotherButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ResultFragment.this).popBackStack();
        });

        backToHomeButton.setOnClickListener(v -> {
            // Navigate all the way back to the HomeFragment
            // popUpTo makes sure the back stack is cleared up to HomeFragment
            NavHostFragment.findNavController(ResultFragment.this).popBackStack(R.id.HomeFragment, false);
        });



        return view;
    }

    private void setButtonsEnabled(boolean enabled) {
        if (likeButton != null) likeButton.setEnabled(enabled);
        if (dislikeButton != null) dislikeButton.setEnabled(enabled);
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
                // Prompt tailored to return strict numbers for easier parsing
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

                parseGPTResponse(output);

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

    private void parseGPTResponse(String output) {
        // Regex to find values based on the prompt "KEY: [number]"
        fetchedCalories = extractInt(output, "CALORIES:");
        fetchedProtein = extractInt(output, "PROTEIN:");
        fetchedCarbs = extractInt(output, "CARBS:");
        fetchedFat = extractInt(output, "FAT:");

        // Update UI on Main Thread
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

    private int extractInt(String text, String key) {
        try {
            if (text.contains(key)) {
                String temp = text.split(key)[1].trim();
                String numberStr = temp.split("\\s+")[0];
                numberStr = numberStr.replaceAll("[^0-9]", "");
                return Integer.parseInt(numberStr);
            }
        } catch (Exception e) {
            Log.e("ResultFragment", "Error parsing " + key, e);
        }
        return 0;
    }
}