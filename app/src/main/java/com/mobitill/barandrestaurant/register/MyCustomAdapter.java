package com.mobitill.barandrestaurant.register;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobitill.barandrestaurant.R;
import com.mobitill.barandrestaurant.data.product.models.Product;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by andronicus on 5/19/2017.
 */

public class MyCustomAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<Product> data;
    private LayoutInflater layoutInflater;

    public MyCustomAdapter(Context context, List<Product> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        View view =  layoutInflater.inflate(R.layout.product_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //Products entered should go here e.g Tusker, Balozi and the likes
        Product product = data.get(position);
        holder.bindView(product);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}