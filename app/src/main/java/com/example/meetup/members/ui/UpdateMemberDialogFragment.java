package com.example.meetup.members.ui;

import com.example.meetup.members.model.Member;
import com.example.meetup.R;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

public class UpdateMemberDialogFragment extends DialogFragment {

    private static final String ARG_MEMBER = "arg_member";

    public interface OnMemberUpdatedListener {
        void onMemberUpdated(Member updatedMember);
    }

    private OnMemberUpdatedListener listener;
    private Member member;

    public static UpdateMemberDialogFragment newInstance(Member member) {
        UpdateMemberDialogFragment fragment = new UpdateMemberDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEMBER, member);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnMemberUpdatedListener(OnMemberUpdatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            member = (Member) getArguments().getSerializable(ARG_MEMBER);
        }

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_update_member_dialog, null, false);


        EditText editName = view.findViewById(R.id.name_member);
        EditText editNumber = view.findViewById(R.id.number);
        EditText editMaxGroup = view.findViewById(R.id.groupContent);

        RadioButton radioIndividual = view.findViewById(R.id.radioIndividual);
        RadioButton radioGroup = view.findViewById(R.id.radioGroup);
        RadioGroup radioGroupType = view.findViewById(R.id.radioGroupType);
        RadioGroup individualContent = view.findViewById(R.id.individualContent);
        RadioButton radioDisposable = view.findViewById(R.id.disposableQR);
        RadioButton radioReusable = view.findViewById(R.id.reusableQR);

        AppCompatButton updateMemberBtn = view.findViewById(R.id.UpdateMemberButton);

        if (member != null) {
            editName.setText(member.getName());
            editNumber.setText(member.getNumber());

            int maxUsages = member.getMaxUsages();
            if (maxUsages == -1) {
                radioIndividual.setChecked(true);
                individualContent.setVisibility(View.VISIBLE);
                editMaxGroup.setVisibility(View.GONE);
                radioReusable.setChecked(true);
            } else if (maxUsages == 1) {
                radioIndividual.setChecked(true);
                individualContent.setVisibility(View.VISIBLE);
                editMaxGroup.setVisibility(View.GONE);
                radioDisposable.setChecked(true);
            } else if (maxUsages > 1) {
                radioGroup.setChecked(true);
                individualContent.setVisibility(View.GONE);
                editMaxGroup.setVisibility(View.VISIBLE);
                editMaxGroup.setText(String.valueOf(maxUsages));
            } else {
                radioIndividual.setChecked(true);
                individualContent.setVisibility(View.VISIBLE);
                editMaxGroup.setVisibility(View.GONE);
                radioDisposable.setChecked(true);
            }
        }

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioIndividual) {
                individualContent.setVisibility(View.VISIBLE);
                editMaxGroup.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioGroup) {
                individualContent.setVisibility(View.GONE);
                editMaxGroup.setVisibility(View.VISIBLE);
            }
        });


        updateMemberBtn.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String number = editNumber.getText().toString().trim();
            String groupSizeStr = editMaxGroup.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                editName.setError("Введите имя");
                return;
            }
            if (TextUtils.isEmpty(number)) {
                editNumber.setError("Введите номер телефона");
                return;
            }
            if (!isPhoneLengthValid(number)) {
                editNumber.setError("Неправильный телефон");
                return;
            }

            boolean isIndividual = radioIndividual.isChecked();
            boolean isGroup = radioGroup.isChecked();

            int maxUsages = 0;

            if (isIndividual) {
                if (radioDisposable.isChecked()) {
                    maxUsages = 1;
                } else if (radioReusable.isChecked()) {
                    maxUsages = -1;
                } else {
                    Toast.makeText(requireContext(),
                            "Выберите тип приглашения (одноразовый / многоразовый)",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (isGroup) {
                if (TextUtils.isEmpty(groupSizeStr)) {
                    editMaxGroup.setError("Введите количество гостей");
                    return;
                }
                try {
                    if (groupSizeStr.startsWith("0")) {
                        editMaxGroup.setError("Количество не может начинаться с 0");
                        return;
                    }
                    int sizeGroup = Integer.parseInt(groupSizeStr);
                    if (sizeGroup < 1 || sizeGroup > 100) {
                        editMaxGroup.setError("max - 100, min = 2 гостя");
                        return;
                    }
                    maxUsages = sizeGroup;
                } catch (NumberFormatException e) {
                    editMaxGroup.setError("Некорректное число");
                    return;
                }
            }

            if (maxUsages == 0) {
                Toast.makeText(requireContext(),
                        "Ошибка: выберите тип приглашения",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Member updated = new Member(name, number, maxUsages);

            if (listener != null) {
                listener.onMemberUpdated(updated);
            }

            dismiss();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        return dialog;
    }
    private boolean isPhoneLengthValid(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return (cleanPhone.startsWith("7") || cleanPhone.startsWith("8")) && cleanPhone.length() == 11;
    }
}
