package com.elhazent.picodiploma.online;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elhazent.picodiploma.online.model.DataItem;

import java.util.List;


class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.Holder> {

    List<DataItem> dataHistory;
    FragmentActivity context;


    public CustomRecyclerAdapter(List<DataItem> dataHistory, FragmentActivity activity) {
        this.dataHistory = dataHistory;
        this.context = activity;

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View view = LayoutInflater.from(context).inflate(R.layout.custom_recyclerview, viewGroup,false);
       return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.texttgl.setText(dataHistory.get(position).getBookingTanggal());
        holder.txtawal.setText(dataHistory.get(position).getBookingFrom());
        holder.txtakhir.setText(dataHistory.get(position).getBookingTujuan());
        holder.txtharga.setText(dataHistory.get(position).getBookingBiayaUser());
    }

    @Override
    public int getItemCount() {
        return dataHistory.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView texttgl;
        TextView txtawal;
        TextView txtakhir;
        TextView txtharga;

        public Holder(@NonNull View itemView) {
            super(itemView);
            texttgl = itemView.findViewById(R.id.texttgl);
            txtawal = itemView.findViewById(R.id.txtawal);
            txtakhir = itemView.findViewById(R.id.txtakhir);
            txtharga = itemView.findViewById(R.id.txtharga);
        }
    }
}
