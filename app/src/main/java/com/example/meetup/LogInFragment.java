package com.example.meetup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentLogInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LogInFragment extends Fragment {
    private FragmentLogInBinding binding;
    private FirebaseAuth mFirebaseAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatButton scannerButton = binding.scanner;
        Button signUpButton = binding.buttonSignUp;
        Button logInButton = binding.buttonLogIn;
        mFirebaseAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(v -> {
            SignUpFragment registerFragment = new SignUpFragment();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, registerFragment)
                    .addToBackStack("login")
                    .commit();
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailLogIn.getText().toString().trim();
                String password = binding.passwordLogIn.getText().toString().trim();

                if (!validateEmail() || !validatePassword()) {
                    return;
                }
                binding.emailLogIn.setError(null);
                binding.passwordLogIn.setError(null);
                mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Вход выполнен!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), AccountActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });


        scannerButton.setOnClickListener(v ->
                Log.d("DEBUG", "onCreate: clicked on button scanner")
        );
    }
    private boolean validateEmail() {

        String emailInput = binding.emailLogIn.getText().toString().trim();

        if (emailInput.isEmpty()) {
            binding.emailLogIn.setError("Введите email");
            return false;
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            binding.emailLogIn.setError("Введите корректный email");
            return false;
        }
        else {
            binding.emailLogIn.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = binding.passwordLogIn.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            binding.passwordLogIn.setError("Введите пароль");
            return false;
        } else if (passwordInput.length() < 6) {
            binding.passwordLogIn.setError("Длина пароля должна быть больше 6");
            return false;
        } else {
            binding.passwordLogIn.setError("");
            return true;
        }
    }
}
