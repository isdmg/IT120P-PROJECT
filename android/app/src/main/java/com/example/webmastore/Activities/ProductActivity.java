package com.example.webmastore.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.webmastore.Fragment.HomeFragment;
import com.example.webmastore.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProductActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private StorageReference storageReference;

    private CollectionReference productReference, listReference;
    private ElegantNumberButton btnQuantity;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private Bundle bundle;
    private String ID, ListID, category;
    private TextView productName, productPrice, productStocks, productCategory, productDetails;
    private boolean isMember, isEdit;
    private ImageView imgProduct, btnBorder;
    private ChipGroup sizeGroup;
    private int originalPrice;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        listReference = store.collection("Carts").document(auth.getUid())
                .collection("List");
        productReference = store.collection("Products");
        btnAddToCart = findViewById(R.id.button_add_to_cart);
        btnQuantity = findViewById(R.id.button_quantity);
        btnBack = findViewById(R.id.backToPrevious);

        progressDialog = new ProgressDialog(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bundle = getIntent().getExtras();
        ID = bundle.getString("ProductID");
        isMember = bundle.getBoolean("isMember");

        if (bundle.getBoolean("Edit")) {
            ListID = bundle.getString("ListID");
            isEdit = true;
            btnAddToCart.setText("Save");
        } else {
            isEdit = false;
        }

        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        productStocks = findViewById(R.id.product_stocks);
        productCategory = findViewById(R.id.category_text);
        btnAddToCart = findViewById(R.id.button_add_to_cart);
        productDetails = findViewById(R.id.product_details);
        imgProduct = findViewById(R.id.details_image);
        btnBorder = findViewById(R.id.numberborder);
        sizeGroup = findViewById(R.id.chipgroup_size);
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });

        sizeGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                Chip checkedChip = findViewById(sizeGroup.getCheckedChipId());
                String size = null;

                if (checkedChip != null) {
                    size = checkedChip.getText().toString();
                    Log.d(TAG, "onCheckedChanged: " + size);
                    Log.d(TAG, "onCheckedChanged: " + originalPrice);
                    Log.d(TAG, "onCheckedChanged: " + productPrice.getText().toString());

                    switch (size) {
                        case "XS":
                            productPrice.setText("₱" + String.valueOf(originalPrice - (60 * 2)));
                            break;
                        case "S":
                            productPrice.setText("₱" + String.valueOf(originalPrice - (60 * 1)));
                            break;
                        case "M":
                            productPrice.setText("₱" + String.valueOf(originalPrice));
                            break;
                        case "L":
                            productPrice.setText("₱" + String.valueOf(originalPrice + (60 * 1)));
                            break;
                        case "XL":
                            productPrice.setText("₱" + String.valueOf(originalPrice + (60 * 2)));
                            break;
                    }
                }
            }
        });

        getProductDetails();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final DocumentReference docRef = store.collection("Products").document(ID);
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

                    category = snapshot.getString("category");
                    Log.d("Category", category);

                    if (category.equals("shirt") || category.equals("sweaters") || category.equals("hoodies")) {
                        if (isMember) {
                            originalPrice = snapshot.getLong("priceMember").intValue();
                        } else {
                            originalPrice = snapshot.getLong("priceNonMember").intValue();
                        }
                        sizeGroup.setVisibility(View.VISIBLE);
                        Chip chip = findViewById(R.id.chip_m);
                        chip.setChecked(true);
                    } else {
                        sizeGroup.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void getProductDetails() {
        // Get product document
        final DocumentReference docRef = store.collection("Products").document(ID);
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
                            String size = null;
                            if (task.isSuccessful()) {
                                // Loop through same product
                                for (DocumentSnapshot document : task.getResult()) {
                                    cumulativeSlots += document.getLong("Quantity");
                                    size = document.getString("Size");
                                }
                                if (snapshot.getLong("Stocks") - cumulativeSlots < 1) {
                                    // Out of stock
                                    if (!isEdit) {
                                        btnQuantity.setVisibility(View.GONE);
                                        btnAddToCart.setVisibility(View.GONE);
                                        btnBorder.setVisibility(View.GONE);

                                        if (category.equals("shirt") || category.equals("sweaters") || category.equals("hoodies")) {
                                            sizeGroup.setVisibility(View.GONE);
                                        }
                                    }
                                } else {
                                    // Still available
                                    btnQuantity.setVisibility(View.VISIBLE);
                                    btnAddToCart.setVisibility(View.VISIBLE);
                                    btnBorder.setVisibility(View.VISIBLE);

                                    if (category.equals("shirt") || category.equals("sweaters") || category.equals("hoodies")) {
                                        sizeGroup.setVisibility(View.VISIBLE);
                                    }
                                }

                                if (!isEdit) {
                                    productStocks.setText("Available Stocks: " + (snapshot.getLong("Stocks") - cumulativeSlots));
                                    btnQuantity.setRange(1, snapshot.getLong("Stocks").intValue() - cumulativeSlots);
                                } else {
                                    productStocks.setText("Available Stocks: " + (snapshot.getLong("Stocks")));
                                    btnQuantity.setRange(1, snapshot.getLong("Stocks").intValue());


                                    // Show selected size
                                    // Check if null
                                    if (size != null) {
                                        for (int i = 0; i < sizeGroup.getChildCount(); i++) {
                                            Chip chip = (Chip) sizeGroup.getChildAt(i);
                                            if (chip.getText().equals(size)) {
                                                chip.setChecked(true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });

                    // Set product details
                    productName.setText(snapshot.getString("name"));
                    productCategory.setText(snapshot.getString("category").substring(0, 1).toUpperCase() + snapshot.getString("category").substring(1));
                    productDetails.setText(snapshot.getString("description"));

                    if (isMember) {
                        productPrice.setText("₱" + snapshot.get("priceMember"));
                    } else {
                        productPrice.setText("₱" + snapshot.get("priceNonMember"));
                    }

                    int quantity = bundle.getInt("Quantity");
                    if (quantity != 0) {
                        btnQuantity.setNumber(String.valueOf(quantity));
                    }

                    storageReference.child("products/" + snapshot.get("ID") + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(imgProduct);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Log.d(TAG, "Current data: null");
                }

            }
        });
    }

    private void addToCart() {
        progressDialog.setTitle("Cart");
        progressDialog.setMessage("Adding to cart...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Chip checkedChip = findViewById(sizeGroup.getCheckedChipId());
        String size = null;

        // Check if empty category for tops, alert user to select size
        if ((category.equals("shirt") || category.equals("sweaters") || category.equals("hoodies")) && checkedChip == null) {
            Toast.makeText(ProductActivity.this, "Please select a size", Toast.LENGTH_SHORT).show();
            return;
        }

        // If not null, get size
        if (checkedChip != null) {
            size = checkedChip.getText().toString();
        }

        String randomKey;
        randomKey = UUID.randomUUID().toString();

        Map<String, Object> cartInfo = new HashMap<>();
        cartInfo.put("ListID", randomKey);
        cartInfo.put("ProductID", ID);
        cartInfo.put("ProductName", productName.getText().toString());
        cartInfo.put("ProductPrice", Integer.parseInt(productPrice.getText().toString().substring(1)));
        cartInfo.put("Quantity", Integer.parseInt(btnQuantity.getNumber()));

        if (size != null) {
            cartInfo.put("Size", size);
        }

        listReference
                .whereEqualTo("ListID", ListID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String documentID = null;
                        boolean documentExists = false;
                        if (task.isSuccessful()) {
                            // TODO: Test this block of code

                            // Check if the product is on a list
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                documentID = document.getId();
                                documentExists = document.exists();
                                Log.d("documentId: ", documentID);
                                Log.d("documentExists", String.valueOf(documentExists));
                            }
                            if (documentExists) {
                                // Update quantity
                                listReference.document(documentID).update("Quantity", Integer.parseInt(btnQuantity.getNumber()));

                                // Update size
                                if (checkedChip != null) {
                                    listReference.document(documentID).update("Size", checkedChip.getText().toString());
                                }

                                Toast.makeText(ProductActivity.this, "Item details saved to cart", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                onBackPressed();
                            } else {
                                // Create new list
                                listReference.document(randomKey)
                                        .set(cartInfo)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(ProductActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                onBackPressed();
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}