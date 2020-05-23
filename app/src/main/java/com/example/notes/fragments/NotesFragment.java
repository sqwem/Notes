package com.example.notes.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.example.notes.adapters.AdapterNotes;
import com.example.notes.NewNoteActivity;
import com.example.notes.models.Note;
import com.example.notes.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class NotesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdapterNotes adapterNotes;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private FloatingActionButton floatingActionButton;
    private LinearLayout layoutHint;

    final String LOG_TAG = "NotesLog";

    private ListenerRegistration listenerRegistration;

    public NotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notes, container, false);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        layoutHint = view.findViewById(R.id.layoutHint);

        floatingActionButton = view.findViewById(R.id.floatingActBtn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newNoteIntent = new Intent(getActivity(), NewNoteActivity.class);
                startActivity(newNoteIntent);
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);

        Query query = firestore.collection("Users").document(currentUserId)
                .collection("myNotes").orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> optionsRecycler = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapterNotes = new AdapterNotes(optionsRecycler);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapterNotes);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapterNotes.deleteOneItemFromPosition(viewHolder.getAdapterPosition());
                Toast.makeText(getContext(), "Заметка удалена", Toast.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterNotes.startListening();
        listenerRegistration = firestore.collection("Users").document(currentUserId).collection("myNotes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d(LOG_TAG, e.getMessage());
                        }
                        if (queryDocumentSnapshots != null) {
                            if (queryDocumentSnapshots.getDocuments().size() == 0) {
                                layoutHint.setVisibility(View.VISIBLE);
                            } else {
                                layoutHint.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterNotes.stopListening();
        listenerRegistration.remove();
    }

}
