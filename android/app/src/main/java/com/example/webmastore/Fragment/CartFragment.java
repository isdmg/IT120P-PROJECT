package com.example.webmastore.Fragment;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webmastore.Activities.CheckoutActivity;
import com.example.webmastore.Activities.ProductActivity;
import com.example.webmastore.Model.Cart;
import com.example.webmastore.R;
import com.example.webmastore.ViewHolder.CartViewHolder;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private StorageReference storageReference;

    private long overTotalPrice = 0;
    private CollectionReference listReference, productReference;
    private FirestoreRecyclerAdapter<Cart, CartViewHolder> adapter;
    private Button btnCheckout;
    private ImageView noItemsImage;
    private HashMap<String, Object> updateListInfo;
    private CardView cardCartTotal, cardCartItems;
    private TextView grandTotal, subtext1, subtext2;
    private boolean isMember;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);

        updateListInfo = new HashMap<>();

        recyclerViewCart = (RecyclerView) v.findViewById(R.id.recycler_view_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
                                              @Override
                                              public void onLayoutCompleted(RecyclerView.State state) {
                                                  super.onLayoutCompleted(state);
                                                  validateCart();
                                              }
                                          }
        );

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        listReference = store.collection("Carts").document(auth.getUid()).collection("List");
        btnCheckout = v.findViewById(R.id.button_checkout);
        noItemsImage = v.findViewById(R.id.no_items_cart);
        cardCartTotal = v.findViewById(R.id.cardview_cart_total);
        cardCartItems = v.findViewById(R.id.cardview_cart_products);
        grandTotal = v.findViewById(R.id.cart_delivery_and_total_fee);
        subtext1 = v.findViewById(R.id.cart_subtext);
        subtext2 = v.findViewById(R.id.cart_subtext2);

        btnCheckout.setVisibility(View.GONE);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkout();
            }
        });

        isUserMember();

        // Inflate the layout for this fragment
        return v;
    }

    public void onStart() {
        super.onStart();
        FirestoreRecyclerOptions<Cart> options =
                new FirestoreRecyclerOptions.Builder<Cart>()
                        .setQuery(listReference, Cart.class) // TODO: Add query order
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Cart, CartViewHolder>(options) {
                    @Override
                    public int getItemCount() {
                        return getSnapshots().size();
                    }

                    @Override
                    public void onDataChanged() {
                        adapter.notifyDataSetChanged();
                        validateCart();
                        overTotalPrice = 0;
                        if (getItemCount() == 0) {
                            btnCheckout.setVisibility(View.GONE);
                            cardCartTotal.setVisibility(View.GONE);
                            cardCartItems.setVisibility(View.GONE);
                            subtext1.setVisibility(View.GONE);
                            grandTotal.setVisibility(View.GONE);

                            subtext2.setVisibility(View.VISIBLE);
                            noItemsImage.setVisibility(View.VISIBLE);

                        } else {
                            btnCheckout.setVisibility(View.VISIBLE);
                            cardCartTotal.setVisibility(View.VISIBLE);
                            cardCartItems.setVisibility(View.VISIBLE);
                            subtext1.setVisibility(View.VISIBLE);
                            grandTotal.setVisibility(View.VISIBLE);

                            subtext2.setVisibility(View.GONE);
                            noItemsImage.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                        holder.txtProductQuantity.setText("Quantity: " + Integer.toString(model.getQuantity()));
                        holder.txtProductPrice.setText("Price: ₱" + ((Integer.valueOf(model.getProductPrice()))) * Integer.valueOf(model.getQuantity()));

                        if (model.getSize() != null) {
                            holder.txtProductName.setText(model.getProductName() + " (" + model.getSize() + ")");
                        } else {
                            holder.txtProductName.setText(model.getProductName());
                        }

                        updateListInfo.put(model.getProductID(), model.getListID());
                        long productPrice = model.getProductPrice() * model.getQuantity();
                        overTotalPrice = overTotalPrice + productPrice;
                        grandTotal.setText("Grand Total: ₱" + String.valueOf(overTotalPrice));

                        storageReference.child("products/" + model.getProductID() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.imgProduct);
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
                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Edit",
                                                "Remove"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Cart Options: ");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0) {
                                            Intent intent = new Intent(getActivity(), ProductActivity.class);
                                            Bundle bundle = new Bundle();

                                            bundle.putString("ProductID", model.getProductID());
                                            bundle.putString("ListID", model.getListID());
                                            bundle.putBoolean("Edit", true);
                                            bundle.putInt("Quantity", model.getQuantity());
                                            bundle.putBoolean("isMember", isMember);
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                        } else {
                                            listReference.document(model.getListID()).delete();
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                        CartViewHolder holder = new CartViewHolder(view);
                        return holder;
                    }
                };
        recyclerViewCart.setAdapter(adapter);
        adapter.startListening();
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

    private void validateCart() {
        for (Map.Entry<String, Object> entry : updateListInfo.entrySet()) {
            final DocumentReference docRef = store.collection("Products").document(entry.getKey());
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


                        listReference.whereEqualTo("ProductID", snapshot.getString("ID")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                int cumulativeSlots = 0;
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        cumulativeSlots += document.getLong("Quantity");
                                    }

                                    long productSlots = snapshot.getLong("Stocks");

                                    if (productSlots == 0) {
                                        // Delete cart list
                                        listReference.document(String.valueOf(entry.getValue())).delete();

                                    } else if (productSlots < cumulativeSlots) {
                                        // Adjust slots if someone else made a purchase
                                        listReference.document(String.valueOf(entry.getValue())).update("Quantity", snapshot.getLong("Slots"));
                                    }
                                }
                            }
                        });
                    } else {
                        // Delete cart list.
                        listReference.document(String.valueOf(entry.getValue())).delete();

                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }
    }

    private void checkout() {
        Intent intent = new Intent(getActivity(), CheckoutActivity.class);
        intent.putExtra("totalAmount", overTotalPrice);
        startActivity(intent);
    }
}