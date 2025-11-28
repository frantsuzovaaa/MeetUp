package com.example.meetup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentMembersBinding;
import com.example.meetup.databinding.FragmentQrDialogBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class QrDialogFragment extends DialogFragment {
    private FragmentQrDialogBinding binding;
    private Bitmap qrCode;
    private static final String ARG_MEMBER = "member";
    private static final String ARG_MEMBER_ID = "member_id";
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQrDialogBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return binding.getRoot();
    }
    public static QrDialogFragment newInstance(Member member, String memberId) {
        QrDialogFragment fragment = new QrDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEMBER, member);
        args.putString(ARG_MEMBER_ID, memberId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Member member = (Member) getArguments().getSerializable(ARG_MEMBER);
        String memberId = getArguments().getString(ARG_MEMBER_ID);


        if (member != null) {
            qrCode = MyQrGeneration.generateQR(member, memberId);
            binding.qrImage.setImageBitmap(qrCode);
            binding.memberName.setText(member.getName());

        }

        binding.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File imagesFolder = new File(requireContext().getCacheDir(), "images");
                    imagesFolder.mkdirs();
                    File file = new File(imagesFolder, "qr_code_" + memberId+ ".png");
                    FileOutputStream stream = new FileOutputStream(file);
                    qrCode.compress(Bitmap.CompressFormat.PNG,100, stream);
                    stream.flush();
                    stream.close();

                    Uri contentUri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".fileprovider", file);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "QR-код участника: " + member.getName());
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(shareIntent, "Поделиться QR-кодом"));

                }
                catch (Exception e) {
                    Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }



            }
        });
    }

}