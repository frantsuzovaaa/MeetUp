package com.example.meetup;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MyQrGeneration {
    public static Bitmap generateQR(Member member, String id_member){
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try{
            String qrData = createQRData(member, id_member);

            Bitmap qrCode = barcodeEncoder.encodeBitmap(
                    qrData, BarcodeFormat.QR_CODE, 250,250);

            return qrCode;
        }
        catch (WriterException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String createQRData(Member member, String id_member) {
        return String.format("MEETUP:%s:%s:%s",
                member.getEventId(),
                id_member,
                member.getNumber(),
                member.getName()
        );
    }

}
