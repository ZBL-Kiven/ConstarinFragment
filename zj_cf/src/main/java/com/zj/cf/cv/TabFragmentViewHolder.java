package com.zj.cf.cv;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

public final class TabFragmentViewHolder extends RecyclerView.ViewHolder {

    private TabFragmentViewHolder(@NonNull FrameLayout container) {
        super(container);
    }

    @NonNull static TabFragmentViewHolder create(@NonNull ViewGroup parent) {
        FrameLayout container = new FrameLayout(parent.getContext());
        container.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        container.setId(ViewCompat.generateViewId());
        container.setSaveEnabled(false);
        return new TabFragmentViewHolder(container);
    }

    @NonNull FrameLayout getContainer() {
        return (FrameLayout) itemView;
    }
}