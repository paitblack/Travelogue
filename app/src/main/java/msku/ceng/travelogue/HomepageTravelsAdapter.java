package msku.ceng.travelogue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

// MERT SENGUN
public class HomepageTravelsAdapter extends FirestoreRecyclerAdapter<Travel, HomepageTravelsAdapter.TravelViewHolder> {

    private OnItemClickListener listener;

    public HomepageTravelsAdapter(@NonNull FirestoreRecyclerOptions<Travel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TravelViewHolder holder, int position, @NonNull Travel model) {
        if (model.getTravelName() != null) {
            holder.travelName.setText(model.getTravelName());
        } else {
            holder.travelName.setText("");
        }
    }

    @NonNull
    @Override
    public TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_homepage_travel, parent, false);
        return new TravelViewHolder(view);
    }

    class TravelViewHolder extends RecyclerView.ViewHolder {
        TextView travelName;
        Button viewDetailsButton;

        public TravelViewHolder(@NonNull View itemView) {
            super(itemView);
            travelName = itemView.findViewById(R.id.homepage_item_travel_name);
            viewDetailsButton = itemView.findViewById(R.id.homepage_item_view_details_button);

            viewDetailsButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
