package com.example.webmastore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.webmastore.Model.Orders;
import com.example.webmastore.R;
import com.example.webmastore.ViewHolder.OrderViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference collectionReference;
    private FirestoreRecyclerAdapter<Orders, OrderViewHolder> adapter;
    private ImageButton btnBack;
    private ImageView noItemsImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(OrderActivity.this, RecyclerView.VERTICAL, false);

        recyclerViewOrders = findViewById(R.id.recycler_view_orders);
        recyclerViewOrders.setLayoutManager(layoutManager);
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();

        collectionReference = store.collection("Orders");

        noItemsImage = findViewById(R.id.no_orders);

        btnBack = findViewById(R.id.backToPrevious);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirestoreRecyclerOptions<Orders> options =
                new FirestoreRecyclerOptions.Builder<Orders>()
                        .setQuery(collectionReference.whereEqualTo("accountRef", auth.getUid()).orderBy("Timestamp", Query.Direction.DESCENDING), Orders.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Orders, OrderViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull final Orders model) {
                        holder.txtOrderId.setText("Order # " + "\n" + model.getOrderId());

                        SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
                        String saveCurrentDate = currentDate.format(model.getTimestamp().toDate());

                        holder.txtProductDate.setText("Order Placed: " + saveCurrentDate);

                        holder.txtProductStatus.setText(model.getOrderStatus());

                        holder.orderDetail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(OrderActivity.this, OrderDetailsActivity.class);
                                intent.putExtra("orderId", model.getOrderId());
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public int getItemCount() {
                        return getSnapshots().size();
                    }

                    @Override
                    public void onDataChanged() {
                        adapter.notifyDataSetChanged();
                        if (getItemCount() == 0) {
                            noItemsImage.setVisibility(View.VISIBLE);
                        }

                        else {
                            noItemsImage.setVisibility(View.GONE);
                        }
                    }

                    @NonNull
                    @Override
                    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_items_layout, parent, false);
                        OrderViewHolder holder = new OrderViewHolder(view);
                        return holder;
                    }
                };
        recyclerViewOrders.setAdapter(adapter);
        adapter.startListening();
    }
}