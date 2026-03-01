package com.example.meetup.scanner.util;

import com.example.meetup.members.model.Member;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.example.meetup.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrGenerator {

    public static Bitmap generateQR(
            Context context,
            Member member,
            String memberId,
            String eventId
    ) {
        try {
            String qrData = createQRData(member, memberId, eventId);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap qrBitmap = encoder.encodeBitmap(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    600,
                    600
            );


            Bitmap logo = BitmapFactory.decodeResource(
                    context.getResources(),
                    R.drawable.logo_in_qr
            );


            Bitmap roundLogo = makeCircularBitmap(logo);

            int logoSize = qrBitmap.getWidth() / 6;
            Bitmap scaledLogo = Bitmap.createScaledBitmap(
                    roundLogo,
                    logoSize,
                    logoSize,
                    true
            );

            return overlayBitmap(qrBitmap, scaledLogo);

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap overlayBitmap(Bitmap qr, Bitmap logo) {
        Bitmap combined = qr.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(combined);

        int left = (qr.getWidth() - logo.getWidth()) / 2;
        int top = (qr.getHeight() - logo.getHeight()) / 2;

        canvas.drawBitmap(logo, left, top, null);
        return combined;
    }

    private static String createQRData(Member member, String memberId, String eventId) {
        return String.format(
                "MEETUP:%s:%s:%s:%s",
                eventId,
                memberId,
                member.getNumber(),
                member.getName()
        );
    }
    public static Bitmap makeCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Rect src = new Rect(
                (bitmap.getWidth() - size) / 2,
                (bitmap.getHeight() - size) / 2,
                (bitmap.getWidth() + size) / 2,
                (bitmap.getHeight() + size) / 2
        );

        Rect dst = new Rect(0, 0, size, size);
        canvas.drawBitmap(bitmap, src, dst, paint);

        return output;
    }

}
