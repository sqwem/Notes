package com.example.notes.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private TextView usernameTv, userEmailTv;
    private String userId;
    private ImageView userImage;
    private TextInputLayout usernameEt, passwordEt, oldPasswordEt;
    private String updatedUsername, updatedPassword;
    private Button updateBtn, oldPasswordBtn, updatePasswordBtn;

    private final int IMAGE_GALLERY_REQUEST_CODE = 100;
    private final int IMAGE_CAMERA_REQUEST_CODE = 200;
    private final int PERMISSION_CODE = 123;

    private String imageProfileUri = null;
    private ConstraintLayout oldPasswordLayout, rootLayout;

    private ProgressBar progressBar;

    private boolean isCheckOldPassword;

    private final String defaultImageUri = "https://firebasestorage.googleapis.com/v0/b/notes-584c3.appspot.com/o/profileImages%2Fdefaultuser.png?alt=media&token=8228e977-91b2-46a2-8a4a-5df8321f76c8";

    public ProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        oldPasswordLayout = view.findViewById(R.id.oldPasswordLayout);
        rootLayout = view.findViewById(R.id.rootLayout);
        progressBar = view.findViewById(R.id.progressBar);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        usernameTv = view.findViewById(R.id.usernameTv);
        userEmailTv = view.findViewById(R.id.userEmailTv);
        userImage = view.findViewById(R.id.usernameImage);
        usernameEt = view.findViewById(R.id.usernameTextInput);

        oldPasswordEt = view.findViewById(R.id.oldPasswordET);
        updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);
        updateBtn = view.findViewById(R.id.updateData);
        oldPasswordBtn = view.findViewById(R.id.oldPasswordBtn);

        oldPasswordBtn.setOnClickListener(this);
        updateBtn.setOnClickListener(this);
        userImage.setOnClickListener(this);
        updatePasswordBtn.setOnClickListener(this);

        getDataFromDBandSet();

        return view;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ViewGroup viewGroup = (ViewGroup) getView();
        viewGroup.removeAllViewsInLayout();
        View view = onCreateView(getActivity().getLayoutInflater(), viewGroup, null);
        viewGroup.addView(view);

        if (isCheckOldPassword) {
            view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.oldPasswordLayout).setVisibility(View.GONE);
        }
    }

    private void getDataFromDBandSet() {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String usernameDB = documentSnapshot.getString("username");
                        String userEmailDB = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        imageProfileUri = documentSnapshot.getString("profileImageUri");

                        usernameTv.setText(usernameDB);
                        userEmailTv.setText(userEmailDB);
                        usernameEt.getEditText().setText(usernameDB);

                        Picasso.with(getContext())
                                .load(imageProfileUri)
                                .into(userImage);

                    }
                });
    }

    private Boolean validateUsername() {
        if (updatedUsername.isEmpty()) {
            usernameEt.setError("Поле должно быть заполненным");
            return false;
        } else if (updatedUsername.length() <= 3) {
            usernameEt.setError("Слишком короткое имя пользователя");
            return false;
        } else if (updatedUsername.length() > 12) {
            usernameEt.setError("Слишком длинное имя пользователя");
            return false;
        } else {
            usernameEt.setError(null);
            usernameEt.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updateData:
                updatedUsername = usernameEt.getEditText().getText().toString();

                if (!validateUsername()) {
                    return;
                } else {

                    Map<String, Object> userInfoMap = new HashMap<>();
                    userInfoMap.put("username", updatedUsername);

                    FirebaseFirestore.getInstance().collection("Users").document(userId)
                            .update(userInfoMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Данные обновлены", Toast.LENGTH_LONG).show();
                                    getDataFromDBandSet();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_LONG).show();
                                }
                            });

                }

                break;
            case R.id.usernameImage:

                final CharSequence[] items = {"Галерея", "Камера", "Установить фото по умолчанию"};
                new AlertDialog.Builder(getContext())
                        .setTitle("Выберите способ добавления фото")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (items[which].equals("Галерея")) {

                                    Intent galleryIntent = new Intent();
                                    galleryIntent.setType("image/*");
                                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(galleryIntent, IMAGE_GALLERY_REQUEST_CODE);

                                } else if (items[which].equals("Камера")) {

                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                                            || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                            PackageManager.PERMISSION_DENIED) {
                                        String[] permissons = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                        requestPermissions(permissons, PERMISSION_CODE);
                                    } else {
                                        openCamera();
                                    }

                                } else if (items[which].equals("Установить фото по умолчанию")) {
                                    setDefaultProfileImage();
                                }
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                break;
            case R.id.oldPasswordBtn:
                String oldPassword = oldPasswordEt.getEditText().getText().toString();

                if (!validateOldPassword(oldPassword)) {
                    return;
                } else {

                    progressBar.setVisibility(View.VISIBLE);
                    rootLayout.setVisibility(View.GONE);
                    oldPasswordLayout.setVisibility(View.GONE);

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    AuthCredential authCredential = EmailAuthProvider.getCredential(userEmail, oldPassword);

                    firebaseUser.reauthenticate(authCredential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    rootLayout.setVisibility(View.VISIBLE);
                                    oldPasswordLayout.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);

                                    isCheckOldPassword = true;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    oldPasswordLayout.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), "Неверный пароль", Toast.LENGTH_LONG).show();

                                    isCheckOldPassword = false;
                                    return;
                                }
                            });
                }

                break;
            case R.id.updatePasswordBtn:
                openUpdatePasswordDialog();
                break;

        }
    }

    private void setDefaultProfileImage() {
        Map<String, Object> mapImageUri = new HashMap<>();
        mapImageUri.put("profileImageUri", defaultImageUri);

        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(mapImageUri)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseStorage.getInstance().getReference().child("profileImages").child(userId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Picasso.with(getContext())
                                                .load(defaultImageUri)
                                                .into(userImage);

                                        Toast.makeText(getContext(), "Данные обновлены", Toast.LENGTH_LONG).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openUpdatePasswordDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

        final View rootDialogView = getLayoutInflater().inflate(R.layout.update_password_dialog, null);

        passwordEt = rootDialogView.findViewById(R.id.userPasswordTextInput);

        alertBuilder.setView(rootDialogView)
                .setTitle("Изменение пароля")
                .setMessage("Введите новый пароль")
                .setView(rootDialogView)
                .setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatedPassword = passwordEt.getEditText().getText().toString();
                        if (updatedPassword.isEmpty()) {
                            Toast.makeText(getActivity(), "Поле не может быть пустым", Toast.LENGTH_LONG).show();
                        } else if (updatedPassword.length() < 7) {
                            Toast.makeText(getActivity(), "Слишком короткий пароль", Toast.LENGTH_LONG).show();
                        } else {
                            passwordEt.setError(null);
                            passwordEt.setErrorEnabled(false);
                            FirebaseAuth.getInstance().getCurrentUser().updatePassword(updatedPassword)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Пароль изменён", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_LONG).show();
                                        }
                                    });


                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private boolean validateOldPassword(String oldPassword) {
        if (oldPassword.isEmpty()) {
            oldPasswordEt.setError("Поле должно быть заполненным");
            return false;
        } else {
            oldPasswordEt.setError(null);
            oldPasswordEt.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmapCamera = (Bitmap) data.getExtras().get("data");
            uploadImageInDB(bitmapCamera);
        }
        if (requestCode == IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageFromGalleryUri = data.getData();
            uploadImageFromCameraInDB(imageFromGalleryUri);
        }

    }

    private void uploadImageFromCameraInDB(Uri imageFromGalleryUri) {
        final StorageReference imagesRef = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
        imagesRef.putFile(imageFromGalleryUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Picasso.with(getContext())
                            .load(downloadUri)
                            .into(userImage);

                    FirebaseFirestore.getInstance().collection("Users")
                            .document(userId)
                            .update("profileImageUri", downloadUri.toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Данные обновлены", Toast.LENGTH_LONG).show();
                                }
                            });

                } else {
                    Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadImageInDB(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
        reference.putBytes(byteArrayOutputStream.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUri(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getDownloadUri(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        setImageAndUploadToDB(uri);
                    }
                });
    }

    private void setImageAndUploadToDB(Uri uri) {
        Picasso.with(getContext())
                .load(uri)
                .into(userImage);

        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .update("profileImageUri", uri.toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Данные обновлены", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
