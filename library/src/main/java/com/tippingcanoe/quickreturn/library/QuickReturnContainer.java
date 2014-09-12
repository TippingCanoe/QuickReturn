package com.tippingcanoe.quickreturn.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import com.daimajia.easing.Glider;
import com.daimajia.easing.Skill;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;

public class QuickReturnContainer extends RelativeLayout {
	protected View observedView;
	protected AbsListView.OnScrollListener passThroughListViewOnScrollListener;
	protected GenericOnScrollListener<ObservableScrollView> passThroughScrollViewOnScrollListener;

	protected ArrayList<View> headerViews = new ArrayList<View>();
	protected ArrayList<Boolean> headerViewsShouldQuickReturn = new ArrayList<Boolean>();
	protected ArrayList<Integer> headerViewHeights = new ArrayList<Integer>();

	protected AnimationState animationState = AnimationState.SHOWN;

	protected int runningScrollTally = 0;

	protected RevealListenerType revealListenerType = RevealListenerType.SCROLL;
	protected boolean revealOnIdle = true;
	protected boolean snapToMidpoint = true;
	protected float parallaxEffect = 0.8f;
	protected int animationTimeOut = 200;
	protected int animationTimeIn = 300;
	protected int minDifferenceBeforeHide = 20;
	protected int minDifferenceBeforeShow = 10;

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
	 * In a AbsListView observable, sets whether the quick returned views should reveal themselves when the list is
	 * idle.
	 *
	 * @param revealOnIdle
	 */
	public void setRevealOnIdle ( boolean revealOnIdle ) {
		this.revealOnIdle = revealOnIdle;
	}

	/**
	 * Sets up whether the quick returned views should reveal themselves based on scrolling or animation.
	 *
	 * @param revealListenerType
	 */
	public void setRevealListenerType ( RevealListenerType revealListenerType ) {
		this.revealListenerType = revealListenerType;
	}

	/**
	 * Sets up whether the quick returned views should snap open/closed based on their midpoint.
	 *
	 * @param snapToMidpoint
	 */
	public void setSnapToMidpoint ( boolean snapToMidpoint ) {
		this.snapToMidpoint = snapToMidpoint;
	}

	/**
	 * Sets up how extreme quick returned views should scroll with a parallax effect compared to the observed list. A
	 * value of 0.5f indicates the quick returned views should move at half the speed of the list. A value of 1.0f
	 * indicates no parallax effect. A value of 1.5f indicates the quick returned views should move at 150% the speed of
	 * the list.
	 *
	 * Note that this only applies to RevealListenerType.SCROLL.
	 *
	 * @param parallaxEffect
	 */
	public void setParallaxEffect ( float parallaxEffect ) {
		this.parallaxEffect = parallaxEffect;
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
				if (revealListenerType == RevealListenerType.SCROLL) {
					setHeaderTranslations(y, oldY);
				} else if (revealListenerType == RevealListenerType.ANIMATED ) {
					setHeaderAnimations(y, oldY);
				}
			}

			@Override
			public void onScrollStateChanged ( AbsListView listView, int i ) {
				if (passThroughListViewOnScrollListener != null) {
					passThroughListViewOnScrollListener.onScrollStateChanged(listView, i);
				}

				if (i == SCROLL_STATE_IDLE && revealOnIdle ) {
					revealHiddenQuickReturns(true);
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

	/**
	 * Sets the time, in ms, that it should take for the quick returned views to animate out when using
	 * RevealListenerType.ANIMATED.
	 *
	 * @param animationTimeOut
	 */
	public void setAnimationTimeOut ( int animationTimeOut ) {
		this.animationTimeOut = animationTimeOut;
	}

	/**
	 * Sets the time, in ms, that it should take for the quick returned views to animate in when using
	 * RevealListenerType.ANIMATED.
	 *
	 * @param animationTimeIn
	 */
	public void setAnimationTimeIn ( int animationTimeIn ) {
		this.animationTimeIn = animationTimeIn;
	}

	/**
	 * Sets the minimum distance, in pixels, that the list needs to travel before the quick returned views are hidden.
	 *
	 * @param minDifferenceBeforeHide
	 */
	public void setMinDifferenceBeforeHide ( int minDifferenceBeforeHide ) {
		this.minDifferenceBeforeHide = minDifferenceBeforeHide;
	}

	/**
	 * Sets the minimum distance, in pixels, that the list needs to travel before the quick returned views are shown.
	 *
	 * @param minDifferenceBeforeShow
	 */
	public void setMinDifferenceBeforeShow ( int minDifferenceBeforeShow ) {
		this.minDifferenceBeforeShow = minDifferenceBeforeShow;
	}

	/**
	 * Shows any hidden quick returned views, optionally animating them back into place.
	 *
	 * @param animated
	 */
	public void revealHiddenQuickReturns ( boolean animated ) {
		if (animationState != AnimationState.SHOWING) {
			if (animated) {
				ArrayList<Animator> animators = new ArrayList<Animator>();

				for (int i = 0; i < headerViews.size(); i++) {
					if (headerViewsShouldQuickReturn.get(i)) {
						View view = headerViews.get(i);
						int currentTranslation = (int) ViewHelper.getTranslationY(view);

						if (currentTranslation < 0) {
							animators.add(Glider.glide(Skill.QuintEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", 0)));
						}
					}
				}

				if (animators.size() > 0) {
					animationState = AnimationState.SHOWING;
					AnimatorSet headerAnimatiorSet = new AnimatorSet();

					headerAnimatiorSet.playTogether(animators);
					headerAnimatiorSet.setDuration(animationTimeIn);
					headerAnimatiorSet.addListener(new AnimationSetTracker() {
						@Override
						public void onAllAnimationsEnded () {
							animationState = AnimationState.SHOWN;
						}
					});
					headerAnimatiorSet.start();
				}
			} else {
				for (int i = 0; i < headerViews.size(); i++) {
					if (headerViewsShouldQuickReturn.get(i)) {
						ViewHelper.setTranslationY(headerViews.get(i), 0);
					}
				}
			}
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
			int previousVisiblity = view.getVisibility();
			view.setVisibility(VISIBLE);
			headerViewHeights.add(view.getMeasuredHeight());
			view.setVisibility(previousVisiblity);
		}
	}

	protected void setHeaderAnimations ( int y, int oldY ) {
		if (y > oldY) {
			animateHeadersOut();
		} else if (y < oldY) {
			animateHeadersIn();
		}
	}

	protected void animateHeadersOut () {
		if (headerViews.size() > 0 && animationState == AnimationState.SHOWN) {
			ArrayList<Animator> animators = new ArrayList<Animator>();
			int runningHeaderHeightSum = 0;

			for (int i = 0; i < headerViews.size(); i++) {
				if (headerViewsShouldQuickReturn.get(i)) {
					View view = headerViews.get(i);
					runningHeaderHeightSum += headerViewHeights.get(i);
					animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", 0, -1 * runningHeaderHeightSum)));
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

				headerAnimatiorSet.setDuration(animationTimeOut);
				headerAnimatiorSet.start();
			}
		}
	}

	protected void animateHeadersIn () {
		if (headerViews.size() > 0 && animationState == AnimationState.HIDDEN) {
			ArrayList<Animator> animators = new ArrayList<Animator>();
			int runningHeaderHeightSum = 0;

			for (int i = 0; i < headerViews.size(); i++) {
				if (headerViewsShouldQuickReturn.get(i)) {
					View view = headerViews.get(i);
					runningHeaderHeightSum += headerViewHeights.get(i);
					animators.add(Glider.glide(Skill.QuintEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", -1 * runningHeaderHeightSum, 0)));
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

				headerAnimatiorSet.setDuration(animationTimeIn);
				headerAnimatiorSet.start();
			}
		}
	}

	protected void headerAnimationsStarted ( AnimationState fromDirection ) {
		animationState = fromDirection;
	}

	protected void headerAnimationsComplete ( AnimationState fromDirection ) {
		if (fromDirection == AnimationState.SHOWING) {
			animationState = AnimationState.SHOWN;
		} else {
			animationState = AnimationState.HIDDEN;
		}
	}

	protected void setHeaderTranslations ( int y, int oldY ) {
		int runningHeaderHeightSum = 0;
		float diff = oldY - y;
		for (int i = 0; i < headerViews.size(); i++) {
			if (headerViewsShouldQuickReturn.get(i)) {
				View view = headerViews.get(i);
				int height = headerViewHeights.get(i);
				float currentTranslation = ViewHelper.getTranslationY(view);

				if (runningHeaderHeightSum > 0) {

					diff *= 1.0f + ((float) height / (float) runningHeaderHeightSum);
				}

				runningHeaderHeightSum += height;

				setQuickReturnTranslation(view, currentTranslation, diff, -1 * runningHeaderHeightSum, 0);
			}
		}
	}

	protected void setQuickReturnTranslation ( View view, float currentTranslation, float diff, int min, int max ) {
		if (animationState != AnimationState.SHOWING ) {
			ViewHelper.setTranslationY(view, Math.min(Math.max(min, currentTranslation + (diff * parallaxEffect)), max));
		}
	}
}
