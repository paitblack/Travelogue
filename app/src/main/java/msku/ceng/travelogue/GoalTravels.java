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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class GoalTravels extends Fragment implements GoalTravelsAdapter.OnDataChangedListener {

    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private GoalTravelsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public GoalTravels(){
        super(R.layout.goaltravels);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goaltravels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.goal_travels_recycler_view);
        emptyTextView = view.findViewById(R.id.empty_view_goal_travels);
        setupRecyclerView();

        FloatingActionButton fab = view.findViewById(R.id.fab_add_goal);
        ImageButton backButton = view.findViewById(R.id.goal_travels_back);
        NavController navController = Navigation.findNavController(view);

        fab.setOnClickListener(v -> navController.navigate(R.id.action_goalTravels_to_addGoal));
        backButton.setOnClickListener(v -> navController.popBackStack());

        // Listen for the result from AddGoalFragment
        getParentFragmentManager().setFragmentResultListener("add_goal_result", getViewLifecycleOwner(), (requestKey, bundle) -> {
            boolean success = bundle.getBoolean("goal_added_success", false);
            if (success) {
                Snackbar.make(view, "Goal Added Successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        if (mAuth.getCurrentUser() == null) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        Query query = db.collection("goals")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Goal> options = new FirestoreRecyclerOptions.Builder<Goal>()
                .setQuery(query, Goal.class)
                .build();

        adapter = new GoalTravelsAdapter(options);
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
