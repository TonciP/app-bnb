package com.tonci.appbnb.ui.authentication;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tonci.appbnb.databinding.ItemAuthTipBinding;

import java.util.List;

/** Adapter del carrusel (ViewPager2): enlaza cada {@link AuthTip} con su ViewHolder. */
public class AuthTipsAdapter extends RecyclerView.Adapter<AuthTipsAdapter.TipViewHolder> {

    private final List<AuthTip> tips;

    public AuthTipsAdapter(@NonNull List<AuthTip> tips) {
        this.tips = tips;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TipViewHolder(
                ItemAuthTipBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        holder.bind(tips.get(position));
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static final class TipViewHolder extends RecyclerView.ViewHolder {

        private final ItemAuthTipBinding binding;

        TipViewHolder(@NonNull ItemAuthTipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull AuthTip tip) {
            binding.tipIllustration.setImageResource(tip.getIllustration());
            binding.tipText.setText(tip.getText());
        }
    }
}
