package com.tippingcanoe.quickreturn.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
	protected GenericOnScrollListener<ObservableScrollView> onScrollListener;
	protected boolean isTouching;
	protected boolean isScrolling;
	protected int touchSlop;

	public ObservableScrollView ( Context context ) {
		super(context);
		setup();
	}

	public ObservableScrollView ( Context context, AttributeSet attrs ) {
		super(context, attrs);
		setup();
	}

	public ObservableScrollView ( Context context, AttributeSet attrs, int defStyle ) {
		super(context, attrs, defStyle);
		setup();
	}

	protected void setup () {
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	public void setOnScrollListener ( GenericOnScrollListener<ObservableScrollView> onScrollListener ) {
		this.onScrollListener = onScrollListener;
	}

	@Override
	protected void onScrollChanged ( int l, int t, int oldl, int oldt ) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollListener != null) {
			onScrollListener.onScrollChanged(this, l, t, oldl, oldt);

			if (Math.abs(t - oldt) <= touchSlop || t <= 0 || t >= getMeasuredHeight()) {
				isScrolling = false;

				if (!isTouching) {
					onScrollListener.onScrollStateChanged(this, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
				}
			} else {
				isScrolling = true;
			}
		}
	}

	@Override
	public boolean onTouchEvent ( MotionEvent ev ) {
		if (onScrollListener != null) {
			switch (ev.getActionMasked()) {
				case MotionEvent.ACTION_SCROLL:
				case MotionEvent.ACTION_MOVE:
					onScrollListener.onScrollStateChanged(this, AbsListView.OnScrollListener.SCROLL_STATE_FLING);
					isTouching = isScrolling = true;

					break;
				case MotionEvent.ACTION_DOWN:
					onScrollListener.onScrollStateChanged(this, AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
					isTouching = true;

					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					isTouching = false;
					if (!isScrolling) {
						onScrollListener.onScrollStateChanged(this, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
					}

					break;
			}
		}

		return super.onTouchEvent(ev);
	}
}
