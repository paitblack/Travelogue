package msku.ceng.travelogue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class YourTravels extends Fragment implements TravelAdapter.OnDataChangedListener {

    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private TravelAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public YourTravels(){
        super(R.layout.yourtravels);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.yourtravels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.your_travels_recycler_view);
        emptyTextView = view.findViewById(R.id.your_travels_empty_text);
        setupRecyclerView();

        FloatingActionButton fab = view.findViewById(R.id.yourTravels_addFAB);
        ImageButton backButton = view.findViewById(R.id.yourTravels_back);
        NavController navController = Navigation.findNavController(view);

        fab.setOnClickListener(v -> navController.navigate(R.id.action_yourTravels_to_addTravels));
        backButton.setOnClickListener(v -> navController.popBackStack());
    }

    private void setupRecyclerView() {
        if (mAuth.getCurrentUser() == null) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        Query query = db.collection("travels")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Travel> options = new FirestoreRecyclerOptions.Builder<Travel>()
                .setQuery(query, Travel.class)
                .build();

        adapter = new TravelAdapter(options);
        adapter.setOnDataChangedListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDataChanged(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
