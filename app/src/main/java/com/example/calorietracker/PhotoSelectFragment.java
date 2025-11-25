package com.example.calorietracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;

public class PhotoSelectFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;


    public PhotoSelectFragment() {
        // Required empty public constructor
    }

    public static PhotoSelectFragment newInstance(String param1, String param2) {
        PhotoSelectFragment fragment = new PhotoSelectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_select, container, false);

        MaterialButton uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setOnClickListener(v -> openGallery());


        return view;
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            Bundle bundle = new Bundle();
            bundle.putString("imageUri", imageUri.toString());

            NavHostFragment.findNavController(PhotoSelectFragment.this)
                    .navigate(R.id.action_photoSelectFragment_to_resultFragment, bundle);
        }
    }
}