package com.tippingcanoe.quickreturn.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import com.daimajia.easing.Glider;
import com.daimajia.easing.Skill;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;

public class QuickReturnContainer extends RelativeLayout {
	protected final int ANIMATION_TIME_OUT = 200;
	protected final int ANIMATION_TIME_IN = 300;

	protected View observedView;
	protected AbsListView.OnScrollListener passThroughListViewOnScrollListener;
	protected GenericOnScrollListener<ObservableScrollView> passThroughScrollViewOnScrollListener;

	protected ArrayList<View> headerViews = new ArrayList<View>();
	protected ArrayList<Boolean> headerViewsShouldQuickReturn = new ArrayList<Boolean>();
	protected ArrayList<Integer> headerViewHeights = new ArrayList<Integer>();

	protected AnimationState headerAnimationState = AnimationState.SHOWN;

	public QuickReturnContainer ( Context context ) {
		super(context);
	}

	public QuickReturnContainer ( Context context, AttributeSet attrs ) {
		super(context, attrs);
	}

	public QuickReturnContainer ( Context context, AttributeSet attrs, int defStyle ) {
		super(context, attrs, defStyle);
	}

	/**
	 * Sets the observed scrollable view to a AbsListView implementation.
	 *
	 * @param listView
	 */
	public void setObservedView ( AbsListView listView ) {
		listView.setOnScrollListener(new AbsOnScrollListenerWrapper() {
			@Override
			public void onScroll ( AbsListView absListView, int i, int i2, int i3 ) {
				super.onScroll(absListView, i, i2, i3);

				if (passThroughListViewOnScrollListener != null) {
					passThroughListViewOnScrollListener.onScroll(absListView, i, i2, i3);
				}
			}

			@Override
			public void onScrollChanged ( AbsListView view, int x, int y, int oldX, int oldY ) {
				if (y > oldY) {
					animateHeadersOut();
				} else if (y < oldY) {
					animateHeadersIn();
				}
			}

			@Override
			public void onScrollStateChanged ( AbsListView listView, int i ) {
				if (passThroughListViewOnScrollListener != null) {
					passThroughListViewOnScrollListener.onScrollStateChanged(listView, i);
				}
			}
		});

		observedView = listView;
	}

	/**
	 * Sets the observed scrollable view to observe from an extension on ScrollView available in this library.
	 *
	 * @param scrollView
	 */
	public void setObservedView ( ObservableScrollView scrollView ) {
		scrollView.setOnScrollListener(new GenericOnScrollListener<ObservableScrollView>() {
			@Override
			public void onScrollChanged ( ObservableScrollView view, int x, int y, int oldX, int oldY ) {
				if (passThroughScrollViewOnScrollListener != null) {
					passThroughScrollViewOnScrollListener.onScrollChanged(view, x, y, oldX, oldY);
				}
			}
		});

		observedView = scrollView;
	}

	/**
	 * Set the OnScrollListener for the attached AbsListView. Note that this library consumes the normal
	 * OnScrollListener, so you need to use this passthrough.
	 *
	 * @param onScrollListener
	 */
	public void setOnScrollListener ( AbsListView.OnScrollListener onScrollListener ) {
		this.passThroughListViewOnScrollListener = onScrollListener;
	}

	/**
	 * Set the OnScrollListener for the attached ScrollView. Note that this library consumes the normal
	 * OnScrollListener, so you need to use this passthrough.
	 *
	 * @param onScrollListener
	 */
	public void setOnScrollListener ( GenericOnScrollListener<ObservableScrollView> onScrollListener ) {
		this.passThroughScrollViewOnScrollListener = onScrollListener;
	}

	/**
	 * Attaches a view to be a header for the observed scrollable area.
	 *
	 * @param view
	 * @param shouldQuickReturn
	 */
	public void attachHeaderView ( View view, boolean shouldQuickReturn ) {
		headerViews.add(view);
		headerViewsShouldQuickReturn.add(shouldQuickReturn);
	}

	/**
	 * Detaches a view from the header of the observed scrollable area.
	 *
	 * @param view
	 */
	public void detachHeaderView ( View view ) {
		detachHeaderView(headerViews.indexOf(view));
	}

	/**
	 * Detaches a view from the header of the observed scrollable area.
	 *
	 * @param index
	 */
	public void detachHeaderView ( int index ) {
		if (index >= 0 && index < headerViews.size()) {
			headerViews.remove(index);
			headerViewsShouldQuickReturn.remove(index);
			updateHeaderOffsets();
		}
	}

	/**
	 * Sets whether the specified header view should animate in and out while scrolling the observed scrollable area.
	 *
	 * @param view
	 * @param shouldQuickReturn
	 */
	public void setHeaderViewShouldQuickReturn ( View view, boolean shouldQuickReturn ) {
		setHeaderViewShouldQuickReturn(headerViews.indexOf(view), shouldQuickReturn);
	}

	/**
	 * Sets whether the specified header view should animate in and out while scrolling the observed scrollable area.
	 *
	 * @param index
	 * @param shouldQuickReturn
	 */
	public void setHeaderViewShouldQuickReturn ( int index, boolean shouldQuickReturn ) {
		if (index >= 0 && index < headerViews.size()) {
			headerViewsShouldQuickReturn.set(index, shouldQuickReturn);
		}
	}

	@Override
	protected void onLayout ( boolean changed, int l, int t, int r, int b ) {
		if (changed) {
			recalculateHeaderHeights();
		}

		super.onLayout(changed, l, t, r, b);
	}

	protected void recalculateHeaderHeights () {
		headerViewHeights.clear();
		for (View view : headerViews) {
			headerViewHeights.add(view.getMeasuredHeight());
		}

		updateHeaderOffsets();
	}

	protected void updateHeaderOffsets () {
		int runningHeaderHeightSum = 0;

		for (int i = 0; i < headerViews.size(); i++) {
			View view = headerViews.get(i);
			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
			if (layoutParams == null) {
				layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			}

			layoutParams.addRule(ALIGN_PARENT_TOP, 1);

			layoutParams.topMargin = runningHeaderHeightSum;
			runningHeaderHeightSum += headerViewHeights.get(i);

			view.setLayoutParams(layoutParams);
		}
	}

	protected void animateHeadersOut () {
		if (headerViews.size() > 0 && headerAnimationState == AnimationState.SHOWN) {
			ArrayList<Animator> animators = new ArrayList<Animator>();

			for (int i = 0; i < headerViews.size(); i++) {
				if (headerViewsShouldQuickReturn.get(i)) {
					View view = headerViews.get(i);
					animators.add(Glider.glide(Skill.QuadEaseIn, ANIMATION_TIME_OUT, ObjectAnimator.ofFloat(view, "translationY", 0, -1 * (headerViewHeights.get(i)))));
				}
			}

			if (animators.size() > 0) {
				headerAnimationsStarted(AnimationState.HIDING);

				AnimatorSet headerAnimatiorSet = new AnimatorSet();

				headerAnimatiorSet.playTogether(animators);
				headerAnimatiorSet.addListener(new AnimationSetTracker() {
					@Override
					public void onAllAnimationsEnded () {
						headerAnimationsComplete(AnimationState.HIDING);
					}
				});

				headerAnimatiorSet.setDuration(ANIMATION_TIME_OUT);
				headerAnimatiorSet.start();
			}
		}
	}

	protected void animateHeadersIn () {
		if (headerViews.size() > 0 && headerAnimationState == AnimationState.HIDDEN) {
			ArrayList<Animator> animators = new ArrayList<Animator>();

			for (int i = 0; i < headerViews.size(); i++) {
				if (headerViewsShouldQuickReturn.get(i)) {
					View view = headerViews.get(i);
					animators.add(Glider.glide(Skill.BackEaseOut, ANIMATION_TIME_IN, ObjectAnimator.ofFloat(view, "translationY", -1 * (headerViewHeights.get(i)), 0)));
				}
			}

			if (animators.size() > 0) {
				headerAnimationsStarted(AnimationState.SHOWING);

				AnimatorSet headerAnimatiorSet = new AnimatorSet();

				headerAnimatiorSet.playTogether(animators);
				headerAnimatiorSet.addListener(new AnimationSetTracker() {
					@Override
					public void onAllAnimationsEnded () {
						headerAnimationsComplete(AnimationState.SHOWING);
					}
				});

				headerAnimatiorSet.setDuration(ANIMATION_TIME_IN);
				headerAnimatiorSet.start();
			}
		}
	}

	protected void headerAnimationsStarted ( AnimationState fromDirection ) {
		headerAnimationState = fromDirection;
	}

	protected void headerAnimationsComplete ( AnimationState fromDirection ) {
		if (fromDirection == AnimationState.SHOWING) {
			headerAnimationState = AnimationState.SHOWN;
		} else {
			headerAnimationState = AnimationState.HIDDEN;
		}
	}
}
