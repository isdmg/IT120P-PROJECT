package com.example.webmastore.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.webmastore.Interface.ItemClickListener;
import com.example.webmastore.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public LinearLayout linearLayout; // TODO: Change variable name?
    public TextView txtProductName,  txtProductPrice;
    public ImageView imageView;
    public ItemClickListener listener;
    public ProductViewHolder(View itemView)
    {
        super(itemView);
        linearLayout = (LinearLayout) itemView.findViewById(R.id.product_frame);
        imageView = (ImageView) itemView.findViewById(R.id.product_image);
        txtProductName = (TextView) itemView.findViewById(R.id.product_name);
        txtProductPrice = (TextView) itemView.findViewById(R.id.product_price);
    }

    public void setItemClickListener(ItemClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View view)
    {
        listener.onClick(view, getAdapterPosition(), false);
    }
}
