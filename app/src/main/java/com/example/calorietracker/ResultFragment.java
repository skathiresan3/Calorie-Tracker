package com.example.calorietracker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;

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

    private String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;

    private TextView resultText;
    private ImageView previewImage;
    private LinearLayout macroRow;
    private Chip proteinChip, carbsChip, fatChip;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        // UI refs
        resultText = view.findViewById(R.id.resultText);
        previewImage = view.findViewById(R.id.previewImage);
        macroRow = view.findViewById(R.id.macroRow);
        proteinChip = view.findViewById(R.id.proteinChip);
        carbsChip = view.findViewById(R.id.carbsChip);
        fatChip = view.findViewById(R.id.fatChip);

        // Image from previous screen
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

    private void analyzeImageWithGPT(Uri imageUri) {
        new Thread(() -> {
            try {
                // Convert image to Base64
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageBytes = stream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                OkHttpClient client = new OkHttpClient();

                // Build message
                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                String prompt =
                        "Analyze the food in this image and provide a calorie and macro estimate. " +
                                "Do not include any introductory text or disclaimers. " +
                                "Format it like this:\n\n" +
                                "Analyzed Components:\n" +
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
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseString = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseString);
                String output = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                requireActivity().runOnUiThread(() -> {
                    // Fade in result
                    resultText.setText(output);
                    resultText.setAlpha(0f);
                    resultText.animate().alpha(1f).setDuration(600).start();

                    // Try to populate chips
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
}
