package com.example.meetup.signup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetup.AccountActivity;
import com.example.meetup.Users;
import com.example.meetup.databinding.FragmentSignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpFragment extends Fragment {

    FirebaseAuth mfirebaseAuth;
    FirebaseDatabase firebaseDatabase;
    private FragmentSignUpBinding binding;

    private boolean isSigningUp = false;

    public SignUpFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mfirebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance("https://meetup2-a8e75-default-rtdb.europe-west1.firebasedatabase.app");

        binding.buttonSignUp.setOnClickListener(v -> {

            if (isSigningUp) return;
            isSigningUp = true;
            binding.buttonSignUp.setEnabled(false);

            String email = binding.emailSignUp.getText().toString().trim();
            String password = binding.passwordSignUp.getText().toString().trim();
            String name = binding.nameSingUp.getText().toString().trim();
            String lastname = binding.lastnameSingUp.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || lastname.isEmpty()) {
                Toast.makeText(requireContext(), "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show();
                isSigningUp = false;
                binding.buttonSignUp.setEnabled(true);
                return;
            }

            if (!validateEmail() || !validatePassword()) {
                isSigningUp = false;
                binding.buttonSignUp.setEnabled(true);
                return;
            }

            binding.emailSignUp.setError(null);
            binding.passwordSignUp.setError(null);

            mfirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Users user = new Users(name, lastname, email);

                                firebaseDatabase.getReference()
                                        .child("Users")
                                        .child(mfirebaseAuth.getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                isSigningUp = false;
                                                binding.buttonSignUp.setEnabled(true);

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(requireContext(), "Регистрация выполнена!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getActivity(), AccountActivity.class));
                                                    requireActivity().finish();
                                                } else {
                                                    Toast.makeText(requireContext(), "Не удалось завершить регистрацию", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else {
                                isSigningUp = false;
                                binding.buttonSignUp.setEnabled(true);
                                Toast.makeText(requireContext(), "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    private boolean validateEmail() {
        String emailInput = binding.emailSignUp.getText().toString().trim();

        if (emailInput.isEmpty()) {
            binding.emailSignUp.setError("Введите email");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            binding.emailSignUp.setError("Введите корректный email");
            return false;
        } else {
            binding.emailSignUp.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = binding.passwordSignUp.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            binding.passwordSignUp.setError("Введите пароль");
            return false;
        } else if (passwordInput.length() < 6) {
            binding.passwordSignUp.setError("Длина пароля должна быть больше 6");
            return false;
        } else {
            binding.passwordSignUp.setError(null);
            return true;
        }
    }
}
