package com.example.webmastore.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.webmastore.Interface.ItemClickListener;
import com.example.webmastore.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtOrderId,txtProductStatus,txtProductDate;
    private ItemClickListener itemClickListener;
    public ImageView orderDetail;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtProductStatus = itemView.findViewById(R.id.order_status);
        txtProductDate = itemView.findViewById(R.id.order_date);
        orderDetail = itemView.findViewById(R.id.order_detail);
    }


    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
