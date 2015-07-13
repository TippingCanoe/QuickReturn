package com.tippingcanoe.quickreturn.library;

import android.support.v7.widget.RecyclerView;

public abstract class RecyclerViewOnScrollListenerWrapper extends RecyclerView.OnScrollListener implements GenericOnScrollListener<RecyclerView> {
	int runningX = 0;
	int runningY = 0;

	@Override
	public void onScrolled ( RecyclerView recyclerView, int dx, int dy ) {
		super.onScrolled(recyclerView, dx, dy);

		onScrollChanged(recyclerView, runningX + dx, runningY + dy, runningX, runningY);

		runningX += dx;
		runningY += dy;
	}
}
