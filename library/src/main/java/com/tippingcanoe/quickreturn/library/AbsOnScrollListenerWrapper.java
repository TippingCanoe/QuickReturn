package com.tippingcanoe.quickreturn.library;

import android.widget.AbsListView;

public abstract class AbsOnScrollListenerWrapper implements AbsListView.OnScrollListener, GenericOnScrollListener<AbsListView> {
	protected int oldX = 0;
	protected int oldY = 0;

	@Override
	public void onScroll ( AbsListView absListView, int i, int i2, int i3 ) {
		int newX = absListView.getScrollX();
		int newY = Helpers.getScrollY(absListView);

		onScrollChanged(absListView, newX, newY, oldX, oldY);

		oldX = newX;
		oldY = newY;
	}
}
