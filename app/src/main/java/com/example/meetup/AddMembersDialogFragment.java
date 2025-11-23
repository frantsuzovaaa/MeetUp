package com.example.meetup;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.meetup.databinding.FragmentAddMembersDialogBinding;


public class AddMembersDialogFragment extends DialogFragment {
    FragmentAddMembersDialogBinding binding;
    private static final int PICK_CONTACT_REQUEST = 123;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 124;
    private EditText Name, Phone;
    private AppCompatImageButton btnAddContact;
    private AppCompatButton btrAddMember;
    private EventsInfoShareViewModel shareViewModel;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddMembersDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    public  interface  onMemberAddedListener{
        void onMemberAdded(Member member);
    }
    private  AddMembersDialogFragment.onMemberAddedListener memberAddedListener;
    public  void setOnMemberAddedListener(AddMembersDialogFragment.onMemberAddedListener listener){
        this.memberAddedListener = listener;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Name = binding.nameMember;
        Phone = binding.number;

        btnAddContact = binding.addContact;
        btrAddMember = binding.addMemberButton;
        RadioButton radioButtonIndividual = binding.radioIndividual;
        RadioButton radioButtonGroup = binding.radioGroup;
        RadioGroup IndividualContent = binding.individualContent;
        EditText GroupContent = binding.groupContent;
        RadioGroup radioGroupType = binding.radioGroupType;
        shareViewModel = new ViewModelProvider(requireActivity()).get(EventsInfoShareViewModel.class);
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {

                if (checkedId == R.id.radioIndividual) {
                    IndividualContent.setVisibility(View.VISIBLE);
                    GroupContent.setVisibility(View.GONE);
                } else if (checkedId == R.id.radioGroup) {
                    IndividualContent.setVisibility(View.GONE);
                    GroupContent.setVisibility(View.VISIBLE);
                }
            }
        });
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkContactPermission();
            }
        });
        btrAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isIndividual = radioButtonIndividual.isChecked();
                boolean isGroup = radioButtonGroup.isChecked();
                binding.number.setError(null);
                binding.number.setBackgroundTintList(null);
                binding.addMemberButton.setEnabled(false);
                binding.groupContent.setError(null);
                if (binding.nameMember.getText().toString().isEmpty()
                        || binding.number.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show();
                    binding.addMemberButton.setEnabled(true);
                    return;
                }
                String number = binding.number.getText().toString();
                if (!isPhoneLengthValid(number)){
                    binding.number.setError("Неправильный телефон");
                    binding.addMemberButton.setEnabled(true);
                    return;
                }

                if (isGroup){
                    try{
                        int size_group = Integer.parseInt(binding.groupContent.getText().toString());
                        if (binding.groupContent.getText().toString().startsWith("0")){
                            binding.groupContent.setError("Количество не может начинаться с 0");
                            return;
                        }
                        else if (size_group<1 || size_group>100){
                            binding.groupContent.setError("max - 100, min = 2 гостя");
                            return;
                        }
                    }
                    catch (NumberFormatException e){
                        Toast.makeText(getActivity(), "Произошла ошибка с проверкой количества гостей", Toast.LENGTH_SHORT).show();
                        binding.addMemberButton.setEnabled(true);
                        return;
                    }

                }
                savedata(isIndividual,isGroup);

            }
        });



    }
    void savedata(boolean Ind, boolean Gr){
        String name = binding.nameMember.getText().toString();
        String number = binding.number.getText().toString();
        String event_id = shareViewModel.getCurrentEventId().getValue();
        if (event_id == null) {
            Toast.makeText(getActivity(), "Ошибка: не выбран ивент", Toast.LENGTH_SHORT).show();
            return;
        }
        int max_useges = 0;
        if (Ind){
            RadioButton radioButtonDis = binding.disposableQR;
            RadioButton radioButtonReuse = binding.reusableQR;
            if (radioButtonDis.isChecked()){
                max_useges = 1;
            }
            if(radioButtonReuse.isChecked()){
                max_useges = -1;
            }

        }
        else if (Gr){
            max_useges = Integer.parseInt(binding.groupContent.getText().toString());
        }
        if (max_useges== 0 ){
            Toast.makeText(getActivity(), "Ошибка: не удалось выбрать тип приглашения", Toast.LENGTH_SHORT).show();
            return;
        }
        Member member = new Member(name, number, event_id, max_useges);
        if (memberAddedListener != null) {
            memberAddedListener.onMemberAdded(member);
            dismiss();
        }

    }
    private boolean isPhoneLengthValid(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");

        return (cleanPhone.startsWith("7") || cleanPhone.startsWith("8"))& cleanPhone.length()==11;
    }
    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        }
    }
    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(getActivity(), "Нужно разрешение для доступа к контактам",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            getContactDetails(contactUri);
        }
    }
    private void getContactDetails(Uri contactUri) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = requireActivity().getContentResolver().query(
                contactUri, projection, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));


                if (phone != null) {
                    phone = phone.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
                }

                Name.setText(name);
                Phone.setText(phone);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Ошибка при выборе контакта", Toast.LENGTH_SHORT).show();
        }
    }

}