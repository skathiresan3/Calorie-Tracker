package com.example.calorietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_WelcomeFragment_to_LoginFragment)
        );

        registerButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_WelcomeFragment_to_RegisterFragment)
        );

        return view;
    }
}