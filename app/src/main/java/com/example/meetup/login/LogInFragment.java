package com.example.meetup.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.meetup.AccountActivity;
import com.example.meetup.QrScanActivity;
import com.example.meetup.R;
import com.example.meetup.Users;
import com.example.meetup.databinding.FragmentLogInBinding;
import com.example.meetup.signup.SignUpFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LogInFragment extends Fragment {

    private FragmentLogInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                            Toast.makeText(requireContext(),
                                    "Вход через Google отменён",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account);
                            }
                        } catch (ApiException e) {
                            Log.w("LogInFragment", "Google sign in failed", e);
                            Toast.makeText(requireContext(),
                                    "Ошибка входа через Google",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance("https://meetup2-a8e75-default-rtdb.europe-west1.firebasedatabase.app");

        initGoogleSignIn();

        AppCompatButton scannerButton = binding.scanner;
        AppCompatButton signUpButton = binding.buttonSignUp;
        AppCompatButton logInButton = binding.buttonLogIn;
        SignInButton googleButton = view.findViewById(R.id.btnGoogleSignIn);

        scannerButton.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), QrScanActivity.class))
        );

        signUpButton.setOnClickListener(v -> {
            SignUpFragment registerFragment = new SignUpFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, registerFragment)
                    .addToBackStack("login")
                    .commit();
        });

        logInButton.setOnClickListener(v -> {
            String email = binding.emailLogIn.getText().toString().trim();
            String password = binding.passwordLogIn.getText().toString().trim();

            if (!validateEmail() || !validatePassword()) {
                return;
            }

            binding.emailLogIn.setError(null);
            binding.passwordLogIn.setError(null);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Вход выполнен!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), AccountActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Неверный email или пароль",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        googleButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Toast.makeText(requireContext(),
                    "GoogleSignInClient = null (initGoogleSignIn не вызван или упал)",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        createUserIfNotExists(user);
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка: пользователь не найден после авторизации",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Ошибка Firebase: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createUserIfNotExists(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();

        firebaseDatabase.getReference()
                .child("Users")
                .child(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            String email = firebaseUser.getEmail();
                            String displayName = firebaseUser.getDisplayName();

                            String name = "";
                            String lastname = "";

                            if (displayName != null && !displayName.isEmpty()) {
                                String[] parts = displayName.split(" ");
                                name = parts[0];
                                if (parts.length > 1) {
                                    lastname = parts[1];
                                }
                            }

                            Users user = new Users(name, lastname, email);

                            firebaseDatabase.getReference()
                                    .child("Users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            Toast.makeText(requireContext(),
                                                    "Вход выполнен!",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(requireActivity(), AccountActivity.class));
                                            requireActivity().finish();
                                        } else {
                                            Toast.makeText(requireContext(),
                                                    "Не удалось сохранить профиль пользователя",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(requireContext(),
                                    "Вход выполнен!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(requireActivity(), AccountActivity.class));
                            requireActivity().finish();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка чтения данных пользователя",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateEmail() {
        String emailInput = binding.emailLogIn.getText().toString().trim();

        if (emailInput.isEmpty()) {
            binding.emailLogIn.setError("Введите email");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            binding.emailLogIn.setError("Введите корректный email");
            return false;
        } else {
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
            binding.passwordLogIn.setError(null);
            return true;
        }
    }
}
