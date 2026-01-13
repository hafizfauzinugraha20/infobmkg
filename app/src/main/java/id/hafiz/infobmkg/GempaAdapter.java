package id.hafiz.infobmkg;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GempaAdapter extends RecyclerView.Adapter<GempaAdapter.ViewHolder> {

    private List<GempaData.Gempa> gempaList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GempaData.Gempa gempa);
    }

    public GempaAdapter(List<GempaData.Gempa> gempaList, OnItemClickListener listener) {
        this.gempaList = gempaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gempa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GempaData.Gempa gempa = gempaList.get(position);

        holder.tvMagnitude.setText(gempa.magnitude);
        holder.tvWilayah.setText(gempa.wilayah);
        holder.tvJam.setText(gempa.jam);
        holder.tvKedalaman.setText(gempa.kedalaman);

        // Styling Logika Warna
        try {
            double mag = Double.parseDouble(gempa.magnitude);
            if (mag >= 5.0) {
                holder.cardMag.setCardBackgroundColor(Color.parseColor("#EF4444")); // Merah
            } else {
                holder.cardMag.setCardBackgroundColor(Color.parseColor("#FB923C")); // Orange
            }
        } catch (NumberFormatException e) {
            holder.cardMag.setCardBackgroundColor(Color.GRAY);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(gempa));
    }

    @Override
    public int getItemCount() {
        return gempaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMagnitude, tvWilayah, tvJam, tvKedalaman;
        CardView cardMag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMagnitude = itemView.findViewById(R.id.tvMagnitude);
            tvWilayah = itemView.findViewById(R.id.tvWilayah);
            tvJam = itemView.findViewById(R.id.tvJam);
            tvKedalaman = itemView.findViewById(R.id.tvKedalaman);
            cardMag = itemView.findViewById(R.id.cardMag);
        }
    }
}