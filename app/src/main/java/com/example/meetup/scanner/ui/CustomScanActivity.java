package com.example.meetup.scanner.ui;

import com.example.meetup.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

public class CustomScanActivity extends CaptureActivity {

    private DecoratedBarcodeView barcodeView;
    private int currentCameraId = 0;

    @Override
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_custom_scan);

        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        ExtendedFloatingActionButton btnSwitch = findViewById(R.id.btnSwitchCamera);

        btnSwitch.setOnClickListener(v -> switchCamera());

        return barcodeView;
    }

    private void switchCamera() {
        currentCameraId = (currentCameraId == 0) ? 1 : 0;

        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(currentCameraId);
        barcodeView.getBarcodeView().setCameraSettings(settings);

        barcodeView.pause();
        barcodeView.resume();

        getSharedPreferences("scanner_prefs", MODE_PRIVATE)
                .edit()
                .putInt("camera_id", currentCameraId)
                .apply();
    }
}
