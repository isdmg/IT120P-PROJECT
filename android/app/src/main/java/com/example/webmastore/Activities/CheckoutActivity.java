package com.example.webmastore.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webmastore.Interface.JavaMailAPI;
import com.example.webmastore.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CheckoutActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private long subtotalText;
    private TextView subtotal;
    private String orderId = UUID.randomUUID().toString();
    private Map<String, Object> detailsInfo = new HashMap<>();
    private Spinner paymentMethod;
    private Button btnCheckout;
    private ImageView proofOfPayment;
    private Uri imageUri;

    private ProgressDialog progressDialog;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private StorageReference storageReference;
    private Map<String, Object> extraInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        subtotal = (TextView) findViewById(R.id.text_subtotal);

        subtotalText = getIntent().getLongExtra("totalAmount", 0);
        subtotal.setText("Total Price: ₱" + String.valueOf(+subtotalText));

        paymentMethod = findViewById(R.id.orderform_paymentmethod);
        proofOfPayment = findViewById(R.id.proof_of_payment);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.payment_method_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethod.setAdapter(adapter);
        paymentMethod.setOnItemSelectedListener(this);

        extraInfo = new HashMap<>();


        btnCheckout = findViewById(R.id.orderFormSubmit_btn);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            imageUri = result.getData().getData();
                            Picasso.get().load(imageUri).into(proofOfPayment);
                        }
                    }
                });

        proofOfPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryForResult();
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOrderEntry(orderId, paymentMethod.getSelectedItem().toString());
            }

        });

    }

    private void openGalleryForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

    private void createOrderEntry(String randomKey, String paymentMethod) {
        progressDialog.setTitle("Checkout");
        progressDialog.setMessage("Saving details...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        store.collection("Users").document(auth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        detailsInfo.put("Email", auth.getCurrentUser().getEmail());
                        detailsInfo.put("Name", documentSnapshot.getString("name"));
                        detailsInfo.put("Phone", "+63" + documentSnapshot.getString("phone").substring(3));

                        extraInfo.put("accountRef", auth.getUid());
                        extraInfo.put("OrderId", randomKey);
                        extraInfo.put("OrderStatus", "Pending");
                        extraInfo.put("TotalAmount", getIntent().getLongExtra("totalAmount", 0));
                        extraInfo.put("Timestamp", Timestamp.now());
                        extraInfo.put("PaymentMethod", paymentMethod);

                        if (imageUri != null) {
                            storageReference.child("purchaseProof/" + randomKey + ".jpg")
                                    .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            storageReference.child("purchaseProof/" + randomKey + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    extraInfo.put("ProofRef", uri.toString());
                                                    DocumentReference df1 = store.collection("Orders").document(randomKey);
                                                    df1.set(extraInfo);

                                                    df1.collection("Details").document(auth.getUid())
                                                            .set(detailsInfo);

                                                    addProducts(randomKey);
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CheckoutActivity.this, "Uploading image failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {

                            if (paymentMethod.equals("GCash")) {
                                Toast.makeText(CheckoutActivity.this, "Please upload proof of payment", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                DocumentReference df1 = store.collection("Orders").document(randomKey);
                                df1.set(extraInfo);

                                df1.collection("Details").document(auth.getUid())
                                        .set(detailsInfo);

                                addProducts(randomKey);
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CheckoutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addProducts(String randomKey) {
        CollectionReference collectionReference = store.collection("Carts").document(auth.getUid()).collection("List");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Map<String, Object> orderInfo = new HashMap<>();

                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        store.collection("Products").document(document.getString("ProductID"))
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        int slots = documentSnapshot.getLong("Stocks").intValue();
                                        int newSlots = slots - document.getLong("Quantity").intValue();
                                        store.collection("Products").document(document.getString("ProductID")).update("Stocks", newSlots);

                                        String category = documentSnapshot.getString("category");
                                        Log.d("category wry", category);
                                        if ((category.equals("shirt") || category.equals("sweaters") || category.equals("hoodies"))) {
                                            Log.d("category wry ok", category);
                                            orderInfo.put("Size", document.getString("Size"));
                                            orderInfo.put("ListID", document.getString("ListID"));
                                            orderInfo.put("ProductID", document.getString("ProductID"));
                                            orderInfo.put("ProductName", document.getString("ProductName"));
                                            orderInfo.put("ProductPrice", document.getLong("ProductPrice"));
                                            orderInfo.put("Quantity", document.getLong("Quantity"));
                                        } else {
                                            orderInfo.put("ListID", document.getString("ListID"));
                                            orderInfo.put("ProductID", document.getString("ProductID"));
                                            orderInfo.put("ProductName", document.getString("ProductName"));
                                            orderInfo.put("ProductPrice", document.getLong("ProductPrice"));
                                            orderInfo.put("Quantity", document.getLong("Quantity"));
                                        }

                                        collectionReference.document(document.getId()).delete();
                                        DocumentReference df1 = store.collection("Orders").document(randomKey);
                                        CollectionReference df2 = df1.collection("Products");
                                        df2.add(orderInfo);

                                        sendMail();
                                        progressDialog.dismiss();

                                        Intent intent = new Intent(CheckoutActivity.this, AfterOrderSplash.class);
                                        startActivity(intent);
                                    }
                                });
                    }
                } else {
                    Log.d(CheckoutActivity.class.getSimpleName(), "Error getting documents: ", task.getException());
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void sendMail() {
        String mail = "webmastoreguild.it120p@gmail.com";
        String subject = "Payment Form";
        String message = "Customer Name: " + detailsInfo.get("Name") + '\n' +
                "Phone Number: " + detailsInfo.get("Phone") + "\n" +
                "Order Number: " + orderId + "\n" +
                "Payment Method: " + paymentMethod.getSelectedItem().toString();

        if (paymentMethod.getSelectedItem().toString().equals("GCash")) {
            message = message + "\n" + "Proof of Payment: " + extraInfo.get("ProofRef");
        }

        //Send Mail
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mail, subject, message);
        javaMailAPI.execute();
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (paymentMethod.getSelectedItem().toString().equals("GCash")) {
            proofOfPayment.setVisibility(View.VISIBLE);
        } else {
            proofOfPayment.setVisibility(View.GONE);
            proofOfPayment.setImageResource(R.drawable.baseline_image_search_24);
            imageUri = null;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}