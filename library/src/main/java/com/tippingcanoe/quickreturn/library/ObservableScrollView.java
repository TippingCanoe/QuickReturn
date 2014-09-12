package com.tippingcanoe.quickreturn.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
	protected GenericOnScrollListener<ObservableScrollView> onScrollListener;
	protected boolean isScrolling;
	protected boolean isTouching;
	protected Runnable scrollingRunnable;

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

		if (Math.abs(oldt - t) > 0) {
			if (scrollingRunnable != null) {
				removeCallbacks(scrollingRunnable);
			}

			scrollingRunnable = new Runnable() {
				public void run () {
					if (isScrolling && !isTouching) {
						if (onScrollListener != null) {
							onScrollListener.onScrollStateChanged(ObservableScrollView.this, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
						}
					}

					isScrolling = false;
					scrollingRunnable = null;
				}
			};

			postDelayed(scrollingRunnable, 200);
		}

		if (onScrollListener != null) {
			onScrollListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}

	@Override
	public boolean onTouchEvent ( MotionEvent ev ) {
		int action = ev.getAction();

		if (action == MotionEvent.ACTION_MOVE) {
			isTouching = true;
			isScrolling = true;
		} else if (action == MotionEvent.ACTION_UP) {
			if (isTouching && !isScrolling) {
				if (onScrollListener != null) {
					onScrollListener.onScrollStateChanged(this, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
				}
			}

			isTouching = false;
		}

		return super.onTouchEvent(ev);
	}
}
