package msku.ceng.travelogue;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// SULEYMAN EMRE PARLAK
public class TravelAdapter extends FirestoreRecyclerAdapter<Travel, TravelAdapter.TravelViewHolder> {

    private OnDataChangedListener onDataChangedListener;

    public interface OnDataChangedListener {
        void onDataChanged(boolean isEmpty);
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.onDataChangedListener = listener;
    }

    public TravelAdapter(@NonNull FirestoreRecyclerOptions<Travel> options) {
        super(options);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (onDataChangedListener != null) {
            onDataChangedListener.onDataChanged(getItemCount() == 0);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull TravelViewHolder holder, int position, @NonNull Travel model) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        String travelId = snapshot.getId();
        holder.bind(model, travelId);
    }

    @NonNull
    @Override
    public TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel, parent, false);
        return new TravelViewHolder(view);
    }

    class TravelViewHolder extends RecyclerView.ViewHolder {
        private final TextView travelName, travelLocation, travelDate, viewDetailsButton;
        private final ImageButton deleteButton, editButton;

        public TravelViewHolder(@NonNull View itemView) {
            super(itemView);
            travelName = itemView.findViewById(R.id.item_travel_name);
            travelLocation = itemView.findViewById(R.id.item_travel_location);
            travelDate = itemView.findViewById(R.id.item_travel_date);
            viewDetailsButton = itemView.findViewById(R.id.item_travel_view_details);
            deleteButton = itemView.findViewById(R.id.item_travel_delete_btn);
            editButton = itemView.findViewById(R.id.item_travel_edit_btn);
        }

        public void bind(Travel travel, String travelId) {
            travelName.setText(travel.getTravelName());
            travelLocation.setText(String.format("%s, %s", travel.getCountry(), travel.getCity()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            travelDate.setText(sdf.format(new Date(travel.getDate())));

            viewDetailsButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("travelId", travelId);
                Navigation.findNavController(v).navigate(R.id.action_yourTravels_to_travelDetail, bundle);
            });

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle(R.string.delete_confirmation_title)
                        .setMessage(R.string.delete_confirmation_message)
                        .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                            deleteTravel(travel, travelId);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });

            editButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("travelId", travelId);
                Navigation.findNavController(v).navigate(R.id.action_yourTravels_to_editTravel, bundle);
            });
        }

        private void deleteTravel(Travel travel, String travelId) {
            if (travel.getPhotoUrls() != null && !travel.getPhotoUrls().isEmpty()) {
                for (String photoUrl : travel.getPhotoUrls()) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                    photoRef.delete().addOnFailureListener(e -> {
                        System.err.println("Failed to delete photo: " + photoUrl + " with error: " + e.getMessage());
                    });
                }
            }

            getSnapshots().getSnapshot(getAdapterPosition()).getReference().delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(itemView.getContext(), "Travel deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(itemView.getContext(), "Failed to delete travel", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
