package com.example.notes.adapters;

import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.EditNoteActivity;
import com.example.notes.R;
import com.example.notes.models.Note;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;


public class AdapterNotes extends FirestoreRecyclerAdapter<Note, AdapterNotes.ViewHolder> {

    public AdapterNotes(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i, @NonNull final Note note) {
        viewHolder.titleTv.setText(note.getTitle().trim());
        viewHolder.contentTv.setText(note.getContent().trim());
        CharSequence dateCharSeq = DateFormat.format("d MMM, H:mm", note.getTimestamp().toDate());
        viewHolder.timeTv.setText(dateCharSeq);

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("noteId", note.getNoteId());
                intent.putExtra("noteColor", note.getNoteColor());
                intent.putExtra("textColor", note.getTextColor());
                v.getContext().startActivity(intent);
            }
        });

        viewHolder.cardView.setCardBackgroundColor(note.getNoteColor());
        viewHolder.titleTv.setTextColor(note.getTextColor());
        viewHolder.contentTv.setTextColor(note.getTextColor());
        viewHolder.timeTv.setTextColor(note.getTextColor());

        if (note.getTitle().isEmpty()) {
            viewHolder.titleTv.setVisibility(View.GONE);
        } else
            viewHolder.titleTv.setVisibility(View.VISIBLE);

        if (note.getContent().isEmpty()) {
            viewHolder.contentTv.setVisibility(View.GONE);
        } else {
            viewHolder.contentTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public Note getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_note_item, parent, false);
        return new ViewHolder(view);
    }

    public void deleteOneItemFromPosition(final int position) {

        final String noteId = getSnapshots().get(position).getNoteId();

        final DocumentReference refNoteId = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("myNotes").document(noteId);

        getSnapshots().getSnapshot(position).getReference().delete();
        refNoteId.collection("images")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                final String idDoc = doc.getId();

                                refNoteId.collection("images")
                                        .document(idDoc)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                String imageId = documentSnapshot.getString("imageId");
                                                FirebaseStorage.getInstance().getReference().child("imagesNote")
                                                        .child(imageId)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                refNoteId.collection("images")
                                                                        .document(idDoc).delete();
                                                            }
                                                        });
                                            }
                                        });
                            }

                        } else {
                            Log.d("AdapterNotes", "query arr size equal 0");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("AdapterNotes", e.getMessage());
                    }
                });

    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv, contentTv, timeTv;
        MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.title);
            contentTv = itemView.findViewById(R.id.content);
            timeTv = itemView.findViewById(R.id.timeNote);
            cardView = itemView.findViewById(R.id.noteCard);
        }
    }

}
