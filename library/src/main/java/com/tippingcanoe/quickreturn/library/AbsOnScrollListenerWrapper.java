package com.tippingcanoe.quickreturn.library;

import android.widget.AbsListView;

public abstract class AbsOnScrollListenerWrapper implements GenericAbsOnScrollListenerWrapper {
	ScrollYProvider scrollYProvider;

	int oldX = 0;
	int oldY = 0;

	protected AbsOnScrollListenerWrapper ( ScrollYProvider scrollYProvider ) {
		this.scrollYProvider = scrollYProvider;
	}

	@Override
	public void onScroll ( AbsListView absListView, int i, int i2, int i3 ) {
		int newX = absListView.getScrollX();
		int newY = scrollYProvider.getScrollY(absListView);

		onScrollChanged(absListView, newX, newY, oldX, oldY);

		oldX = newX;
		oldY = newY;
	}
}
