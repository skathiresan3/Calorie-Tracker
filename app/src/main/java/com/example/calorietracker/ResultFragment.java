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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.calorietracker.BuildConfig;
import android.util.Log;


public class ResultFragment extends Fragment {

    String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;

    private TextView resultText;
    private ImageView previewImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        resultText = view.findViewById(R.id.resultText);
        previewImage = view.findViewById(R.id.previewImage);

        OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;

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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageBytes = stream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                OkHttpClient client = new OkHttpClient();

                // 🧠 Build the JSON request
                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                textPart.put("text", "Analyze this image and output a text output describing the estimated calories and macros for this image.");

                JSONObject imagePart = new JSONObject();
                imagePart.put("type", "image_url");

                // ✅ Correct field name for OpenAI Vision API
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
                requestBodyJson.put("max_tokens", 300);

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

                requireActivity().runOnUiThread(() -> resultText.setText(output));

            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> resultText.setText("⚠️ Network error: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> resultText.setText("⚠️ API error: " + e.getMessage()));
            }
        }).start();
    }

}
