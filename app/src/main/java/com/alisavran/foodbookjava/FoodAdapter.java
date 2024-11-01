package com.alisavran.foodbookjava;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alisavran.foodbookjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodHolder> {

    ArrayList<Food> foodArrayList;

    public FoodAdapter(ArrayList<Food> foodArrayList){
        this.foodArrayList = foodArrayList;
    }

    @NonNull
    @Override
    public FoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new FoodHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(foodArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),FoodActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("foodId",foodArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodArrayList.size();
    }

    public class FoodHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public FoodHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
