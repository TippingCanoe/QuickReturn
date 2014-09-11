package com.tippingcanoe.quickreturn.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
	protected GenericOnScrollListener<ObservableScrollView> onScrollListener;

	public ObservableScrollView ( Context context ) {
		super(context);
	}

	public ObservableScrollView ( Context context, AttributeSet attrs ) {
		super(context, attrs);
	}

	public ObservableScrollView ( Context context, AttributeSet attrs, int defStyle ) {
		super(context, attrs, defStyle);
	}

	public void setOnScrollListener ( GenericOnScrollListener<ObservableScrollView> onScrollListener ) {
		this.onScrollListener = onScrollListener;
	}

	@Override
	protected void onScrollChanged ( int l, int t, int oldl, int oldt ) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (onScrollListener != null) {
			onScrollListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}
}
