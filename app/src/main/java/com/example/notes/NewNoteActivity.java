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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import yuku.ambilwarna.AmbilWarnaDialog;


public class NewNoteActivity extends AppCompatActivity implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

    private TextView toolBarTv;
    private Toolbar toolbar;
    private EditText title, text;
    private ImageButton saveBtn, backBtn;
    private static final String LOG_TAG = "NewNoteActivity";
    private FirebaseFirestore firestore;
    private BottomAppBar bottomAppBar;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private Uri imageUri;
    private RecyclerView recyclerImagesView;

    private String idDocument = null;
    private StorageReference storageReference;
    private AdapterImages adapterImages;
    private String uniqId;

    private final int IMAGE_GALLERY_REQUEST_CODE = 100;
    private final int IMAGE_CAMERA_REQUEST_CODE = 200;
    private final int PERMISSION_CODE = 1000;

    private int noteColor = Color.WHITE;
    private ConstraintLayout rootLayout;
    private int textColor = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);


        rootLayout = findViewById(R.id.rootLayout);

        //Toolbars
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomAppBar = findViewById(R.id.bottom_appbar);
        setSupportActionBar(bottomAppBar);
        bottomAppBar.setOnMenuItemClickListener(this);

        //Firebase
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("imagesNote");

        idDocument = firestore.collection("Users").document(userId).collection("myNotes").document().getId();

        //recycler Images
        recyclerImagesView = findViewById(R.id.recyclerImagesView);
        Query query = firestore.collection("Users").document(userId).collection("myNotes")
                .document(idDocument).collection("images");
        FirestoreRecyclerOptions<ImageModel> optionsRecycler = new FirestoreRecyclerOptions.Builder<ImageModel>()
                .setQuery(query, ImageModel.class)
                .build();
        adapterImages = new AdapterImages(optionsRecycler, getApplicationContext());
        recyclerImagesView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerImagesView.setAdapter(adapterImages);
        //


        title = findViewById(R.id.title);
        text = findViewById(R.id.text);
        saveBtn = findViewById(R.id.save_btn);
        backBtn = findViewById(R.id.back_btn);
        toolBarTv = findViewById(R.id.appbar_text_view);

        saveBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                toolBarTv.setText(s);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_btn:
                String strTitle = title.getText().toString();
                String strText = text.getText().toString();

                if (strText.trim().isEmpty() && strTitle.trim().isEmpty() && adapterImages.getItemCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Пустая заметка удалена", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;
                } else {
                    saveNote(strTitle, strText);
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), "Заметка сохранена", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.back_btn:
                final DocumentReference docRefIdDocumentNote = firestore.collection("Users").document(userId)
                        .collection("myNotes").document(idDocument);
                final StorageReference refImagesNote = FirebaseStorage.getInstance().getReference().child("imagesNote");
                docRefIdDocumentNote.collection("images")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.getDocuments().size() == 0) {
                                    onBackPressed();
                                } else {
                                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                        final String imageId = doc.getString("imageId");

                                        refImagesNote.child(imageId)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        docRefIdDocumentNote.collection("images").document(imageId)
                                                                .delete();
                                                        onBackPressed();
                                                    }
                                                });
                                    }
                                }
                            }
                        });
                break;
        }
    }

    public void saveNote(String strTitle, String strText) {

        Log.d(LOG_TAG, strText + " " + strText);

        Log.d(LOG_TAG, idDocument);
        final Note note = new Note(strTitle, strText, new Timestamp(new Date()), idDocument, noteColor, textColor);

        firestore.collection("Users").document(userId).collection("myNotes")
                .document(idDocument)
                .set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, idDocument);
                        note.setNoteId(idDocument);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LOG_TAG, e.getMessage());
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.bottombar_newnote_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.background_botmenu:

                openBackgroundColorPicker();


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
                title.setTextColor(textColor);
                text.setTextColor(textColor);
            }
        });
        colorPicker.show();
    }

    private void openBackgroundColorPicker() {
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

        Log.d(LOG_TAG, String.valueOf(resultCode));

        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_GALLERY_REQUEST_CODE && data.getData() != null) {
                imageUri = data.getData();
                Log.d(LOG_TAG, imageUri.toString());
                addInDB(imageUri);
            }
            if (requestCode == IMAGE_CAMERA_REQUEST_CODE) {
                Bitmap bitmapCamera = (Bitmap) data.getExtras().get("data");
                uploadImageInDB(bitmapCamera);
            }

        }


    }

    private void uploadImageInDB(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        uniqId = String.valueOf(System.currentTimeMillis());

        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("imagesNote").child(uniqId);
        reference.putBytes(byteArrayOutputStream.toByteArray())
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
                            .document(uniqId).set(imageModel);
                } else {
                    Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
