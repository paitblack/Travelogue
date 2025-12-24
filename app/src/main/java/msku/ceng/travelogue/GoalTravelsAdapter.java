package msku.ceng.travelogue;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GoalTravelsAdapter extends FirestoreRecyclerAdapter<Goal, GoalTravelsAdapter.GoalViewHolder> {

    private OnDataChangedListener onDataChangedListener;

    public interface OnDataChangedListener {
        void onDataChanged(boolean isEmpty);
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.onDataChangedListener = listener;
    }

    public GoalTravelsAdapter(@NonNull FirestoreRecyclerOptions<Goal> options) {
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
    protected void onBindViewHolder(@NonNull GoalViewHolder holder, int position, @NonNull Goal model) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        String goalId = snapshot.getId();
        holder.bind(model, goalId);
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_travel, parent, false);
        return new GoalViewHolder(view);
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        private final TextView travelLocation, travelDate;
        private final ImageButton deleteButton;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            travelLocation = itemView.findViewById(R.id.item_goal_travel_location);
            travelDate = itemView.findViewById(R.id.item_goal_travel_date);
            deleteButton = itemView.findViewById(R.id.item_goal_travel_delete_btn);
        }

        public void bind(Goal goal, String goalId) {
            travelLocation.setText(String.format("%s, %s", goal.getCountry(), goal.getCity()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            travelDate.setText(sdf.format(new Date(goal.getDate())));

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle(R.string.delete_confirmation_title)
                        .setMessage(R.string.delete_goal_confirmation_message)
                        .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                            deleteGoal(goalId);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
        }

        private void deleteGoal(String goalId) {
            getSnapshots().getSnapshot(getAdapterPosition()).getReference().delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(itemView.getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(itemView.getContext(), "Failed to delete goal", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
