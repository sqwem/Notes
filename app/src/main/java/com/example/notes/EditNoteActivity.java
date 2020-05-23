package com.example.notes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.adapters.AdapterImages;
import com.example.notes.models.ImageModel;
import com.example.notes.models.Note;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;


import yuku.ambilwarna.AmbilWarnaDialog;


public class EditNoteActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private TextView titleAppBarTv;
    private Toolbar toolbar;
    private Intent intent;
    private EditText titleEt, contentEt;
    private final String LOG_TAG = "Edit note";
    private ImageButton saveBtn, backBtn;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String titleExtra, contentExtra;
    private String noteId;
    private String userId;
    private BottomAppBar bottomAppBar;

    private AdapterImages adapterImages;
    private RecyclerView recyclerImagesView;
    private String idDocument;

    private final int IMAGE_GALLERY_REQUEST_CODE = 123;
    private final int IMAGE_CAMERA_REQUEST_CODE = 256;
    private final int PERMISSION_CODE = 1000;
    private Uri imageUri;
    private String uniqId;

    private int noteColor;
    private ConstraintLayout rootLayout;
    private int textColor = Color.BLACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        rootLayout = findViewById(R.id.rootLayout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        titleAppBarTv = findViewById(R.id.appbar_text_view);

        bottomAppBar = findViewById(R.id.bottom_appbar);
        setSupportActionBar(bottomAppBar);
        bottomAppBar.setOnMenuItemClickListener(this);

        titleEt = findViewById(R.id.title);
        contentEt = findViewById(R.id.text);
        saveBtn = findViewById(R.id.save_btn);
        backBtn = findViewById(R.id.back_btn);

        saveBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();

        getDataFromRecyclerItem();
        rootLayout.setBackgroundColor(noteColor);
        titleEt.setTextColor(textColor);
        contentEt.setTextColor(textColor);

        getWindow().setStatusBarColor(noteColor);
        getWindow().setNavigationBarColor(noteColor);

        idDocument = firestore.collection("Users").document(userId).collection("myNotes").document(noteId).getId();
        recyclerImagesView = findViewById(R.id.recyclerImagesView);

        Log.d(LOG_TAG, idDocument);

        Query query = firestore.collection("Users").document(userId).collection("myNotes")
                .document(noteId).collection("images");

        FirestoreRecyclerOptions<ImageModel> optionsRecycler = new FirestoreRecyclerOptions.Builder<ImageModel>()
                .setQuery(query, ImageModel.class)
                .build();
        adapterImages = new AdapterImages(optionsRecycler, getApplicationContext());
        recyclerImagesView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerImagesView.setAdapter(adapterImages);

        titleEt.setText(titleExtra);
        titleAppBarTv.setText(titleExtra);
        contentEt.setText(contentExtra);

        titleEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleAppBarTv.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterImages.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterImages.stopListening();
    }

    private void getDataFromRecyclerItem() {
        intent = getIntent();
        titleExtra = intent.getStringExtra("title");
        contentExtra = intent.getStringExtra("content");
        noteId = intent.getStringExtra("noteId");
        noteColor = intent.getIntExtra("noteColor", Color.WHITE);
        textColor = intent.getIntExtra("textColor", Color.BLACK);
    }

    @Override
    public void onClick(View v) {
        final String editTitle = titleEt.getText().toString();
        final String editContent = contentEt.getText().toString();
        switch (v.getId()) {
            case R.id.save_btn:

                final Note note = new Note(editTitle, editContent, new Timestamp(new Date()), noteId, noteColor, textColor);

                Log.d(LOG_TAG, noteId);

                firestore.collection("Users").document(userId).collection("myNotes")
                        .document(noteId).collection("images")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.size() == 0 && editTitle.isEmpty() && editContent.isEmpty()) {
                                    new AlertDialog.Builder(EditNoteActivity.this)
                                            .setTitle("Пустая заметка")
                                            .setMessage("Вы действительно хотите удалить пустую заметку?")
                                            .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    firestore.collection("Users").document(userId)
                                                            .collection("myNotes").document(noteId).delete();
                                                    onBackPressed();
                                                    Toast.makeText(getApplicationContext(), "Заметка удалена", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .setNegativeButton("Отмена", null)
                                            .show();
                                } else {
                                    editNote(note);
                                    onBackPressed();
                                }
                            }
                        });

                break;

            case R.id.back_btn:
                onBackPressed();
                break;
        }
    }

    private void editNote(Note note) {
        firestore.collection("Users").document(userId).collection("myNotes")
                .document(noteId).set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Заметка изменена", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Не удалось сохранить изменения", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.bottombar_editnote_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy_botmenu:
                String strEditTitle = titleEt.getText().toString();
                final String strEditContent = contentEt.getText().toString();
                final String copyNoteId = firestore.collection("Users").document(userId)
                        .collection("myNotes")
                        .document().getId();

                final Note note = new Note(strEditTitle, strEditContent, new Timestamp(new Date()), copyNoteId, noteColor, textColor);

                firestore.collection("Users").document(userId)
                        .collection("myNotes")
                        .document(copyNoteId)
                        .set(note);
                Toast.makeText(getApplicationContext(), "Заметка скопирована", Toast.LENGTH_LONG).show();

                break;

            case R.id.image_botmenu:

                final CharSequence[] items = {"Галерея", "Камера"};
                new AlertDialog.Builder(this)
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

                                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                                            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                            PackageManager.PERMISSION_DENIED) {
                                        String[] permissons = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                        requestPermissions(permissons, PERMISSION_CODE);
                                    } else {
                                        openCamera();
                                    }

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
            case R.id.delete_botmenu:

                final DocumentReference refNoteId = FirebaseFirestore.getInstance().collection("Users").document(userId)
                        .collection("myNotes").document(noteId);

                refNoteId.collection("images")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    final String imagesDocId = doc.getId();

                                    refNoteId.collection("images")
                                            .document(imagesDocId)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                    String imageId = documentSnapshot.getString("imageId");
                                                    FirebaseStorage.getInstance().getReference().child("imagesNote").child(imageId)
                                                            .delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    refNoteId.collection("images")
                                                                            .document(imagesDocId)
                                                                            .delete();
                                                                }
                                                            });
                                                }
                                            });

                                }
                                refNoteId.delete();

                                Toast.makeText(getApplicationContext(), "Заметка удалена", Toast.LENGTH_LONG).show();
                                onBackPressed();


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                            }
                        });


                break;
            case R.id.background_botmenu:

                openColorPicker();

                break;
            case R.id.textColor_botmenu:

                openTextColorPicker();

                break;
        }
        return true;
    }

    private void openTextColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, noteColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                textColor = color;
                titleEt.setTextColor(textColor);
                contentEt.setTextColor(textColor);
            }
        });
        colorPicker.show();
    }

    private void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, noteColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                noteColor = color;
                rootLayout.setBackgroundColor(noteColor);
                getWindow().setStatusBarColor(noteColor);
                getWindow().setNavigationBarColor(noteColor);
            }
        });
        colorPicker.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, IMAGE_CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG, String.valueOf(requestCode));

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data.getData() != null) {
            imageUri = data.getData();
            Log.d(LOG_TAG, imageUri.toString());
            addInDB(imageUri);
        }
        if (requestCode == IMAGE_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmapCamera = (Bitmap) data.getExtras().get("data");
            uploadImageInDB(bitmapCamera);
        }
    }

    private void uploadImageInDB(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        uniqId = String.valueOf(System.currentTimeMillis());

        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("imagesNote").child(uniqId);
        reference.putBytes(bytes)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        ImageModel imageModel = new ImageModel(uri.toString(), uniqId);

                                        firestore.collection("Users").document(userId)
                                                .collection("myNotes").document(idDocument).collection("images")
                                                .document(uniqId).set(imageModel);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addInDB(final Uri uri) {

        uniqId = String.valueOf(System.currentTimeMillis());

        final StorageReference imagesRef = FirebaseStorage.getInstance().getReference().child("imagesNote").child(uniqId);
        imagesRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                    Log.e(LOG_TAG, "then: " + downloadUri.toString());

                    ImageModel imageModel = new ImageModel(downloadUri.toString(), uniqId);

                    firestore.collection("Users").document(userId)
                            .collection("myNotes").document(idDocument).collection("images")
                            .document(uniqId)
                            .set(imageModel);

                } else {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
