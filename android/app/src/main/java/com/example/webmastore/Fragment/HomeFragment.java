package com.example.webmastore.Fragment;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webmastore.Activities.ProductActivity;
import com.example.webmastore.Model.Products;
import com.example.webmastore.R;
import com.example.webmastore.ViewHolder.ProductViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paulrybitskyi.persistentsearchview.PersistentSearchView;
import com.paulrybitskyi.persistentsearchview.listeners.OnSearchConfirmedListener;
import com.squareup.picasso.Picasso;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewBags, recyclerViewFans,
            recyclerViewHats, recyclerViewHoodies, recyclerViewLanyards, recyclerViewMasks,
            recyclerViewShirt, recyclerViewSweaters;
    private ScrollView scrollView;
    private PersistentSearchView persistentSearchView;
    private String searchInput = "";

    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference productReference, listReference;
    private boolean isMember;
    private TextView bagsHeader, fansHeader, hatsHeader, hoodiesHeader, lanyardsHeader, masksHeader, shirtsHeader, sweatersHeader;
    private FirestoreRecyclerAdapter<Products, ProductViewHolder> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager4 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager5 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager6 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager7 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager8 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);


        recyclerViewBags = (RecyclerView) v.findViewById(R.id.recycler_view_bags);
        recyclerViewFans = (RecyclerView) v.findViewById(R.id.recycler_view_fans);
        recyclerViewHats = (RecyclerView) v.findViewById(R.id.recycler_view_hats);
        recyclerViewHoodies = (RecyclerView) v.findViewById(R.id.recycler_view_hoodies);
        recyclerViewLanyards = (RecyclerView) v.findViewById(R.id.recycler_view_lanyards);
        recyclerViewMasks = (RecyclerView) v.findViewById(R.id.recycler_view_masks);
        recyclerViewShirt = (RecyclerView) v.findViewById(R.id.recycler_view_shirt);
        recyclerViewSweaters = (RecyclerView) v.findViewById(R.id.recycler_view_sweaters);

        recyclerViewBags.setLayoutManager(layoutManager);
        recyclerViewFans.setLayoutManager(layoutManager2);
        recyclerViewHats.setLayoutManager(layoutManager3);
        recyclerViewHoodies.setLayoutManager(layoutManager4);
        recyclerViewLanyards.setLayoutManager(layoutManager5);
        recyclerViewMasks.setLayoutManager(layoutManager6);
        recyclerViewShirt.setLayoutManager(layoutManager7);
        recyclerViewSweaters.setLayoutManager(layoutManager8);

        bagsHeader = v.findViewById(R.id.category_bags);
        fansHeader = v.findViewById(R.id.category_fans);
        hatsHeader = v.findViewById(R.id.category_hats);
        hoodiesHeader = v.findViewById(R.id.category_hoodies);
        lanyardsHeader = v.findViewById(R.id.category_lanyards);
        masksHeader = v.findViewById(R.id.category_masks);
        shirtsHeader = v.findViewById(R.id.category_shirts);
        sweatersHeader = v.findViewById(R.id.category_sweaters);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        productReference = store.collection("Products");
        listReference = store.collection("Carts").document(auth.getUid()).collection("List");
        persistentSearchView = v.findViewById(R.id.persistentSearchView);

        scrollView = v.findViewById(R.id.scrollView_home);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY + 75 <= oldScrollY || scrollY == 0) {
                        ;
                        persistentSearchView.setVisibility(View.VISIBLE);
                    } else if (scrollY > oldScrollY) {
                        persistentSearchView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        persistentSearchView.setOnClearInputBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchInput = "";
                onStart();
            }
        });

        persistentSearchView.setOnLeftBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchInput = "";
                onStart();
            }
        });

        persistentSearchView.setOnSearchConfirmedListener(new OnSearchConfirmedListener() {
            @Override
            public void onSearchConfirmed(PersistentSearchView searchView, String query) {
                // Handle a search confirmation. This is the place where you'd
                // want to perform a search against your data provider.

                searchView.collapse();
                searchInput = query;
                onStart();
            }

        });

        isUserMember();

        // Inflate the layout for this fragment
        return v;
    }

    public void onStart() {
        super.onStart();

        FirestoreRecyclerAdapter<Products, ProductViewHolder> adapter;
        adapter = populateRecyclerView("bags");
        recyclerViewBags.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("fans");
        recyclerViewFans.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("hats");
        recyclerViewHats.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("hoodies");
        recyclerViewHoodies.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("lanyards");
        recyclerViewLanyards.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("masks");
        recyclerViewMasks.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("shirt");
        recyclerViewShirt.setAdapter(adapter);
        adapter.startListening();

        adapter = populateRecyclerView("sweaters");
        recyclerViewSweaters.setAdapter(adapter);
        adapter.startListening();

    }

    private FirestoreRecyclerAdapter<Products, ProductViewHolder> populateRecyclerView(String category) {
        FirestoreRecyclerOptions<Products> options;

        options =
                new FirestoreRecyclerOptions.Builder<Products>()
                        .setQuery(productReference.whereEqualTo("category", category).orderBy("name", Query.Direction.ASCENDING), Products.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Products, ProductViewHolder>(options) {
                    private boolean allGone = true;
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull final Products model) {
                        if (model.getName().toLowerCase().contains(searchInput.toLowerCase())) {
                            holder.itemView.setVisibility(View.VISIBLE);
                            allGone = false;

                            if (category.equals("lanyards")) {
                                Log.d("its lanyard time", "Lanyards");
                            }
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }

                        switch (category) {
                            case "bags":
                                if (allGone) {
                                    bagsHeader.setVisibility(View.GONE);
                                } else {
                                    bagsHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "fans":
                                if (allGone) {
                                    fansHeader.setVisibility(View.GONE);
                                } else {
                                    fansHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "hats":
                                if (allGone) {
                                    hatsHeader.setVisibility(View.GONE);
                                } else {
                                    hatsHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "hoodies":
                                if (allGone) {
                                    hoodiesHeader.setVisibility(View.GONE);
                                } else {
                                    hoodiesHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "lanyards":
                                if (allGone) {
                                    lanyardsHeader.setVisibility(View.GONE);
                                    Log.d("its lanyard time", "Lanyards22222");
                                } else {
                                    lanyardsHeader.setVisibility(View.VISIBLE);
                                    Log.d("its lanyard time", "Lanyards33333");
                                }
                                break;
                            case "masks":
                                if (allGone) {
                                    masksHeader.setVisibility(View.GONE);
                                } else {
                                    masksHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "shirt":
                                if (allGone) {
                                    Log.d("TAGSAYONARA", "onBindViewHolder: " + model.getName());
                                    shirtsHeader.setVisibility(View.GONE);
                                } else {
                                    shirtsHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "sweaters":
                                if (allGone) {
                                    sweatersHeader.setVisibility(View.GONE);
                                } else {
                                    sweatersHeader.setVisibility(View.VISIBLE);
                                }
                                break;
                        }


                        holder.txtProductName.setText(model.getName());

                        if (isMember) {
                            holder.txtProductPrice.setText("₱" + model.getPriceMember());
                        } else {
                            holder.txtProductPrice.setText("₱" + model.getPriceNonMember());
                        }

                        // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                        listReference.whereEqualTo("ProductID", model.getID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                int cumulativeSlots = 0;
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        cumulativeSlots += document.getLong("Quantity");
                                    }
                                    if (model.getStocks() - cumulativeSlots < 1) {
                                        holder.linearLayout.setAlpha(0.25f);
                                    } else {
                                        holder.linearLayout.setAlpha(1f);
                                    }
                                }
                            }
                        });


                        storageReference.child("products/" + model.getID() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.imageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), ProductActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("ProductID", model.getID());
                                bundle.putBoolean("isMember", isMember);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                    }


                    @NonNull
                    @Override
                    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);
                        ProductViewHolder holder = new ProductViewHolder(view);
                        return holder;
                    }
                };
        return adapter;
    }

    private void isUserMember() {
        final DocumentReference docRef = store.collection("Users").document(auth.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    isMember = snapshot.getBoolean("isMember");
                    Log.d(TAG, "isMember" + isMember);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}