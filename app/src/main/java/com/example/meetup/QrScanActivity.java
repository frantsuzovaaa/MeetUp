package com.example.meetup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorKt;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Arrays;

public class QrScanActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    FirebaseDatabase firebaseDatabase;
    String event_id;
    private static final String PREFS_NAME = "scanner_prefs";
    private static final String KEY_CAMERA_ID = "camera_id";

    private int currentCameraId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String eventId;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firebaseDatabase = FirebaseDatabase.getInstance("https://meetup2-a8e75-default-rtdb.europe-west1.firebasedatabase.app");

        showDialogEditText();


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                initQRCodeScanner();
            }
        } else {
            initQRCodeScanner();
        }
    }
    private void initQRCodeScanner() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentCameraId = prefs.getInt(KEY_CAMERA_ID, 0);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.setPrompt("Наведите камеру на QR-код");
        integrator.setCaptureActivity(CustomScanActivity.class);

        integrator.setCameraId(currentCameraId);

        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initQRCodeScanner();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
                finish();
            } else  {
                showInformation(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void showInformation(String contents) {
        try {
            if (contents == null || contents.trim().isEmpty()) {
                Toast.makeText(this, "Пустой QR код", Toast.LENGTH_LONG).show();
                initQRCodeScanner();
                return;
            }
            String[] data = contents.split(":");

            if (data.length < 3) {
                Toast.makeText(this, "Неверный формат QR кода", Toast.LENGTH_LONG).show();
                initQRCodeScanner();
                return;
            }

            String member_id = data[2];
            String event_id_qr = data[1];

            if (event_id == null) {
                Toast.makeText(this, "Сначала введите кодовое слово", Toast.LENGTH_LONG).show();
                showDialogEditText();
                return;
            }

            if (!event_id_qr.equals(event_id)) {
                Toast.makeText(this, "QR код не для этого мероприятия", Toast.LENGTH_LONG).show();
                initQRCodeScanner();
                return;
            }


            firebaseDatabase.getReference("Members")
                    .child(event_id_qr)
                    .child(member_id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                Log.d("QR_DEBUG", "Участник не найден: " + member_id);
                                Toast.makeText(QrScanActivity.this, "Участник не найден", Toast.LENGTH_LONG).show();
                                initQRCodeScanner();
                                return;
                            }

                            Member member = snapshot.getValue(Member.class);
                            if (member == null) {
                                Toast.makeText(QrScanActivity.this, "Ошибка чтения данных участника", Toast.LENGTH_LONG).show();
                                initQRCodeScanner();
                                return;
                            }

                            Log.d("QR_DEBUG", "Найден участник: " + member.getName());
                            processMemberScan(member, member_id, event_id_qr);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("QR_DEBUG", "Ошибка базы данных: " + error.getMessage());
                            Toast.makeText(QrScanActivity.this, "Ошибка поиска участника", Toast.LENGTH_LONG).show();
                            initQRCodeScanner();
                        }
                    });

        } catch (Exception e) {
            Log.e("QR_DEBUG", "Ошибка обработки QR: " + e.getMessage());
            Toast.makeText(this, "Ошибка обработки QR кода", Toast.LENGTH_LONG).show();
            initQRCodeScanner();
        }

}

private void processMemberScan(Member member, String memberId, String eventId) {
    if (member.getMaxUsages() == 0) {
        showDialogInformation("Проход запрещен.\nКоличество возможных посещений - 0.");
        recordScanHistory(member, memberId, eventId, false);
    } else if (member.getMaxUsages() < 0) {
        showDialogInformation("Проход разрешен.\nКоличество возможных посещений неограничено");
        recordScanHistory(member, memberId, eventId, true);
    } else if (member.getMaxUsages() > 0) {
        int newMaxUsages = member.getMaxUsages() - 1;

        firebaseDatabase.getReference("Members")
                .child(eventId)
                .child(memberId)
                .child("maxUsages")
                .setValue(newMaxUsages)
                .addOnSuccessListener(aVoid -> {
                    String message = "✅ Проход разрешен!\nОсталось посещений: " + newMaxUsages;
                    showDialogInformation(message);
                    recordScanHistory(member, memberId, eventId, true);
                })
                .addOnFailureListener(e -> {
                    showDialogInformation("❌ Ошибка обновления данных");
                    recordScanHistory(member, memberId, eventId, false);
                });
    }
}

    private void recordScanHistory(Member member, String memberId, String eventIdQr, boolean b) {
        ScanRecord scanRecord = new ScanRecord(
                eventIdQr,
                memberId,
                member.getMaxUsages(), b);


        firebaseDatabase.getReference("ScanHistory")
                .child(eventIdQr)
                .push()
                .setValue(scanRecord)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Scan", "Запись сохранена в историю");
                })
                .addOnFailureListener(e -> {
                    Log.e("Scan", "Ошибка записи истории: " + e.getMessage());
                });
    }

    private void showDialogInformation(String message){
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(50, 20, 50, 10);

    int color = getResources().getColor(R.color.dark_pink);
    TextView messageText = new TextView(this);
    messageText.setText(message);
    messageText.setTextColor(color);
    messageText.setTextSize(20);
    messageText.setPadding(0, 0, 0, 20);

    layout.addView(messageText);

    AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Ok", null)
            .setCancelable(false)
            .create();

    dialog.setOnShowListener(dialogInterface -> {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.dark_pink));

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initQRCodeScanner();
            }
        });
    });

    dialog.show();


}

private void showDialogEditText() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        int color = getResources().getColor(R.color.dark_pink);
        TextView messageText = new TextView(this);
        messageText.setText("Введите кодовое слово для подтверждения:");
        messageText.setTextSize(20);
        messageText.setPadding(0, 0, 0, 20);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Введите кодовое слово");
        input.setTextColor(color);
        input.setHintTextColor(getResources().getColor(R.color.color_hint));
        input.setBackgroundResource(R.drawable.edittext_normal);

        final TextView errorText = new TextView(this);
        errorText.setTextColor(Color.RED);
        errorText.setTextSize(16);
        errorText.setVisibility(View.GONE);
        errorText.setPadding(10, 5, 0, 0);
        layout.addView(messageText);
        layout.addView(input);
        layout.addView(errorText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton("Проверить", null)
                .setNegativeButton("Отмена", null)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.dark_pink));
            negativeButton.setTextColor(getResources().getColor(R.color.dark_pink));

            positiveButton.setOnClickListener(v -> {
                String userText = input.getText().toString().trim();

                if (userText.isEmpty()) {
                    showError(input, errorText, "Введите кодовое слово!");
                    return;
                }

                firebaseDatabase.getReference("Events")
                        .orderByChild("codeWord")
                        .equalTo(userText)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                                        event_id = eventSnapshot.getKey(); // ← ВОТ ТУТ ИСПРАВИТЬ!
                                        Log.d("QR_DEBUG", "Найден event_id: " + event_id);
                                        break;
                                    }
                                    hideError(input, errorText);
                                    processSuccess();
                                    dialog.dismiss();
                                    checkCameraPermission();
                                } else {
                                    showError(input, errorText, "Неверное кодовое слово!");
                                    input.setText("");
                                    input.requestFocus();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                showError(input, errorText, "Ошибка проверки!");
                            }
                        });
            });

            negativeButton.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        });

        dialog.show();

        input.postDelayed(() -> {
            input.requestFocus();
        }, 200);
    }


    private void showError(EditText input, TextView errorText, String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        input.setBackgroundResource(R.drawable.edittext_error);
        input.startAnimation(shakeError());
    }

    private Animation shakeError() {
        Animation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(50);
        shake.setInterpolator(new CycleInterpolator(5));
        return shake;
    }
    private void hideError(EditText input, TextView errorText) {
        errorText.setVisibility(View.GONE);
        input.setBackgroundResource(R.drawable.edittext_normal);
    }
    private void processSuccess() {
        Toast.makeText(this, "✅ Доступ разрешен!", Toast.LENGTH_LONG).show();
    }
}

