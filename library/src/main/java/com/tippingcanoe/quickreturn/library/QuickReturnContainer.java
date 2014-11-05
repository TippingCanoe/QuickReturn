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
	protected View offsetView;
	protected View observedView;
	protected AbsListView.OnScrollListener passThroughListViewOnScrollListener;
	protected GenericOnScrollListener<ObservableScrollView> passThroughScrollViewOnScrollListener;

	protected ArrayList<View> headerViews = new ArrayList<View>();
	protected ArrayList<Boolean> headerViewsShouldQuickReturn = new ArrayList<Boolean>();
	protected ArrayList<Boolean> headerViewsRenderOverList = new ArrayList<Boolean>();
	protected ArrayList<Integer> headerViewHeights = new ArrayList<Integer>();
	protected ArrayList<Boolean> headerViewsPermanentlyHidden = new ArrayList<Boolean>();

	protected ArrayList<View> footerViews = new ArrayList<View>();
	protected ArrayList<Boolean> footerViewsShouldQuickReturn = new ArrayList<Boolean>();
	protected ArrayList<Boolean> footerViewsRenderOverList = new ArrayList<Boolean>();
	protected ArrayList<Integer> footerViewHeights = new ArrayList<Integer>();
	protected ArrayList<Boolean> footerViewsPermanentlyHidden = new ArrayList<Boolean>();

	protected AnimationState animationState = AnimationState.SHOWN;

	protected boolean lastActionWasScrollDown = true;
	protected int runningScrollTally = 0;
	protected Runnable idleRunnable;

	protected RevealListenerType revealListenerType = RevealListenerType.SCROLL;
	protected boolean revealOnIdle = false;
	protected boolean snapToIntent = true;
	protected boolean snapToMidpoint = false;
	protected float parallaxEffect = 0.8f;
	protected int animationTimeOut = 200;
	protected int animationTimeIn = 300;
	protected int idleRevealDelay = 800;
	protected int minDifferenceBeforeHide = 300;
	protected int minDifferenceBeforeShow = 100;

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
	 * Default is false.
	 *
	 * @param revealOnIdle
	 */
	public void setRevealOnIdle ( boolean revealOnIdle ) {
		this.revealOnIdle = revealOnIdle;
	}

	/**
	 * Sets up whether the quick returned views should reveal themselves based on scrolling or animation.
	 *
	 * Default is SCROLL.
	 *
	 * @param revealListenerType
	 */
	public void setRevealListenerType ( RevealListenerType revealListenerType ) {
		this.revealListenerType = revealListenerType;
	}

	/**
	 * Sets up whether the quick returned views should snap/open closed based on the drag direction.
	 *
	 * Default is true.
	 *
	 * Overrides snapToMidpoint.
	 *
	 *
	 * @param snapToIntent
	 */
	public void setSnapToIntent ( boolean snapToIntent ) {
		this.snapToIntent = snapToIntent;
	}

	/**
	 * Sets up whether the quick returned views should snap open/closed based on their midpoint.
	 *
	 * Default is false.
	 *
	 * Is overridden by snapToIntent.
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
	 * Default is 0.8f.
	 *
	 * @param parallaxEffect
	 */
	public void setParallaxEffect ( float parallaxEffect ) {
		this.parallaxEffect = parallaxEffect;
	}

	/**
	 * Sets the delay in ms that must be reached (without further scrolling) before the quick returned views should show.
	 * Used in combination with revealOnIdle.
	 *
	 * Should be set to at least animationTimeOut.
	 *
	 * Default is 800ms;
	 *
	 * @param idleRevealDelay
	 */
	public void setIdleRevealDelay ( int idleRevealDelay ) {
		this.idleRevealDelay = idleRevealDelay;
	}

	/**
	 * Sets the observed scrollable view to a AbsListView implementation.
	 *
	 * @param listView
	 */
	public void setObservedView ( AbsListView listView ) {
		setObservedView(listView, new ScrollYProvider() {
			@Override
			public int getScrollY ( AbsListView listView ) {
				return Helpers.getScrollY(listView);
			}
		});
	}

	/**
	 * Sets the observed scrollable view to a AbsListView implementation. Allows injection of a custom Y offset
	 * provider.
	 *
	 * @param listView
	 * @param scrollYProvider
	 */
	public void setObservedView ( AbsListView listView, ScrollYProvider scrollYProvider ) {
		listView.setOnScrollListener(new AbsOnScrollListenerWrapper(scrollYProvider) {
			@Override
			public void onScroll ( AbsListView absListView, int i, int i2, int i3 ) {
				super.onScroll(absListView, i, i2, i3);

				if (passThroughListViewOnScrollListener != null) {
					passThroughListViewOnScrollListener.onScroll(absListView, i, i2, i3);
				}
			}

			@Override
			public void onScrollChanged ( AbsListView view, int x, int y, int oldX, int oldY ) {
				handleScrollChanged(y, oldY);
			}

			@Override
			public void onScrollStateChanged ( AbsListView listView, int i ) {
				if (passThroughListViewOnScrollListener != null) {
					passThroughListViewOnScrollListener.onScrollStateChanged(listView, i);
				}

				handleScrollStateChanged(i);
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

				handleScrollChanged(y, oldY);
			}

			@Override
			public void onScrollStateChanged ( ObservableScrollView view, int i ) {
				if (passThroughScrollViewOnScrollListener != null) {
					passThroughScrollViewOnScrollListener.onScrollStateChanged(view, i);
				}

				handleScrollStateChanged(i);
			}
		});

		observedView = scrollView;
	}

	/**
	 * Set a view included at the top of your observable area that can be used to offset the content.
	 * An example would be a header view attached to an ListView.
	 *
	 * @param offsetView
	 */
	public void setOffsetView ( View offsetView ) {
		this.offsetView = offsetView;
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
	 * @param rendersOverList
	 * @param permanentlyHidden
	 */
	public void attachHeaderView ( View view, boolean shouldQuickReturn, boolean rendersOverList, boolean permanentlyHidden ) {
		headerViews.add(view);
		headerViewsShouldQuickReturn.add(shouldQuickReturn);
		headerViewsRenderOverList.add(rendersOverList);
		headerViewsPermanentlyHidden.add(permanentlyHidden);
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
			headerViewsRenderOverList.remove(index);
			headerViewsPermanentlyHidden.remove(index);

			setupView();
		}
	}

	/**
	 * Sets whether the specified header view should render above the observed scrollable area.
	 *
	 * @param view
	 * @param shouldRenderAboveList
	 */
	public void setHeaderViewShouldRenderAboveList ( View view, boolean shouldRenderAboveList ) {
		setHeaderViewShouldRenderAboveList(headerViews.indexOf(view), shouldRenderAboveList);
	}

	/**
	 * Sets whether the specified header view should render above the observed scrollable area.
	 *
	 * @param index
	 * @param shouldRenderAboveList
	 */
	public void setHeaderViewShouldRenderAboveList ( int index, boolean shouldRenderAboveList ) {
		if (index >= 0 && index < headerViews.size()) {
			headerViewsRenderOverList.set(index, shouldRenderAboveList);

			setupView();
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

			setupView();
		}
	}

	/**
	 * Attaches a view to be a footer for the observed scrollable area.
	 *
	 * @param view
	 * @param shouldQuickReturn
	 * @param rendersOverList
	 * @param permanentlyHidden
	 */
	public void attachFooterView ( View view, boolean shouldQuickReturn, boolean rendersOverList, boolean permanentlyHidden ) {
		footerViews.add(view);
		footerViewsShouldQuickReturn.add(shouldQuickReturn);
		footerViewsRenderOverList.add(rendersOverList);
		footerViewsPermanentlyHidden.add(permanentlyHidden);
	}

	/**
	 * Detaches a view from the footer of the observed scrollable area.
	 *
	 * @param view
	 */
	public void detachFooterView ( View view ) {
		detachFooterView(footerViews.indexOf(view));
	}

	/**
	 * Detaches a view from the footer of the observed scrollable area.
	 *
	 * @param index
	 */
	public void detachFooterView ( int index ) {
		if (index >= 0 && index < footerViews.size()) {
			footerViews.remove(index);
			footerViewsShouldQuickReturn.remove(index);
			footerViewsRenderOverList.remove(index);
			footerViewsPermanentlyHidden.remove(index);

			setupView();
		}
	}

	/**
	 * Sets whether the specified footer view should render above the observed scrollable area.
	 *
	 * @param view
	 * @param shouldRenderAboveList
	 */
	public void setFooterViewShouldRenderAboveList ( View view, boolean shouldRenderAboveList ) {
		setFooterViewShouldRenderAboveList(footerViews.indexOf(view), shouldRenderAboveList);
	}

	/**
	 * Sets whether the specified footer view should render above the observed scrollable area.
	 *
	 * @param index
	 * @param shouldRenderAboveList
	 */
	public void setFooterViewShouldRenderAboveList ( int index, boolean shouldRenderAboveList ) {
		if (index >= 0 && index < footerViews.size()) {
			footerViewsRenderOverList.set(index, shouldRenderAboveList);

			setupView();
		}
	}

	/**
	 * Sets whether the specified footer view should animate in and out while scrolling the observed scrollable area.
	 *
	 * @param view
	 * @param shouldQuickReturn
	 */
	public void setFooterViewShouldQuickReturn ( View view, boolean shouldQuickReturn ) {
		setFooterViewShouldQuickReturn(footerViews.indexOf(view), shouldQuickReturn);
	}

	/**
	 * Sets whether the specified footer view should animate in and out while scrolling the observed scrollable area.
	 *
	 * @param index
	 * @param shouldQuickReturn
	 */
	public void setFooterViewShouldQuickReturn ( int index, boolean shouldQuickReturn ) {
		if (index >= 0 && index < footerViews.size()) {
			footerViewsShouldQuickReturn.set(index, shouldQuickReturn);

			setupView();
		}
	}

	/**
	 * Sets the time, in ms, that it should take for the quick returned views to animate out when using
	 * RevealListenerType.ANIMATED.
	 *
	 * Default is 200ms.
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
	 * Default is 300ms.
	 *
	 * @param animationTimeIn
	 */
	public void setAnimationTimeIn ( int animationTimeIn ) {
		this.animationTimeIn = animationTimeIn;
	}

	/**
	 * Sets the minimum distance, in pixels, that the list needs to travel before the quick returned views are hidden.
	 * Note that this effect really works best with snapping enabled.
	 *
	 * Default is 300.
	 *
	 * @param minDifferenceBeforeHide
	 */
	public void setMinDifferenceBeforeHide ( int minDifferenceBeforeHide ) {
		this.minDifferenceBeforeHide = minDifferenceBeforeHide;
	}

	/**
	 * Sets the minimum distance, in pixels, that the list needs to travel before the quick returned views are shown.
	 * Note that this effect really works best with snapping enabled.
	 *
	 * Default is 100.
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
	public void showHiddenQuickReturns ( boolean animated ) {
		if (animationState == AnimationState.SHOWN || animationState == AnimationState.HIDDEN) {
			if (animated) {
				ArrayList<Animator> animators = new ArrayList<Animator>();

				int runningPermanentlyHiddenHeaderHeightSum = 0;
				for (int i = 0; i < headerViews.size(); i++) {
					if (headerViewsPermanentlyHidden.get(i)) {
						runningPermanentlyHiddenHeaderHeightSum += headerViewHeights.get(i);
					} else if (headerViewsShouldQuickReturn.get(i)) {
						View view = headerViews.get(i);
						animators.add(Glider.glide(Skill.QuintEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", -1 * runningPermanentlyHiddenHeaderHeightSum)));
					}
				}

				int runningPermanentlyHiddenFooterHeightSum = 0;
				for (int i = 0; i < footerViews.size(); i++) {
					if (footerViewsPermanentlyHidden.get(i)) {
						runningPermanentlyHiddenFooterHeightSum += footerViewHeights.get(i);
					} else if (footerViewsShouldQuickReturn.get(i)) {
						View view = footerViews.get(i);
						animators.add(Glider.glide(Skill.QuintEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", runningPermanentlyHiddenFooterHeightSum)));
					}
				}

				if (animators.size() > 0) {
					animationState = AnimationState.SHOWING;
					AnimatorSet animatorSet = new AnimatorSet();

					animatorSet.playTogether(animators);
					animatorSet.setDuration(animationTimeIn);
					animatorSet.addListener(new AnimationSetTracker() {
						@Override
						public void onAllAnimationsEnded () {
							animationsComplete(AnimationState.SHOWING);
						}
					});

					animatorSet.start();
				}
			} else {
				int runningPermanentlyHiddenHeaderHeightSum = 0;
				for (int i = 0; i < headerViews.size(); i++) {
					if (headerViewsPermanentlyHidden.get(i)) {
						runningPermanentlyHiddenHeaderHeightSum += headerViewHeights.get(i);
					} else if (headerViewsShouldQuickReturn.get(i)) {
						ViewHelper.setTranslationY(headerViews.get(i), -1 * runningPermanentlyHiddenHeaderHeightSum);
					}
				}

				int runningPermanentlyHiddenFooterHeightSum = 0;
				for (int i = 0; i < footerViews.size(); i++) {
					if (footerViewsPermanentlyHidden.get(i)) {
						runningPermanentlyHiddenFooterHeightSum += footerViewHeights.get(i);
					} else if (footerViewsShouldQuickReturn.get(i)) {
						ViewHelper.setTranslationY(footerViews.get(i), runningPermanentlyHiddenFooterHeightSum);
					}
				}
			}
		}
	}

	/**
	 * Hides any shown quick returned views, optionally animating them back into place.
	 *
	 * @param animated
	 */
	public void hideShownQuickReturns ( boolean animated ) {
		if (animationState == AnimationState.SHOWN || animationState == AnimationState.HIDDEN) {
			if (animated) {
				ArrayList<Animator> animators = new ArrayList<Animator>();
				int runningHeaderHeightSum = 0;
				int runningFooterHeightSum = 0;

				for (int i = 0; i < headerViews.size(); i++) {
					if (headerViewsShouldQuickReturn.get(i) || headerViewsPermanentlyHidden.get(i)) {
						View view = headerViews.get(i);
						runningHeaderHeightSum += headerViewHeights.get(i);
						animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", -1 * runningHeaderHeightSum)));
					}
				}

				for (int i = 0; i < footerViews.size(); i++) {
					if (footerViewsShouldQuickReturn.get(i) || footerViewsPermanentlyHidden.get(i)) {
						View view = footerViews.get(i);
						runningFooterHeightSum += footerViewHeights.get(i);
						animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", runningFooterHeightSum)));
					}
				}

				if (animators.size() > 0) {
					animationState = AnimationState.HIDING;

					AnimatorSet animatorSet = new AnimatorSet();
					animatorSet.playTogether(animators);
					animatorSet.setDuration(animationTimeOut);
					animatorSet.addListener(new AnimationSetTracker() {
						@Override
						public void onAllAnimationsEnded () {
							animationsComplete(AnimationState.HIDING);
						}
					});

					animatorSet.start();
				}
			} else {
				int runningHeaderHeightSum = 0;
				for (int i = 0; i < headerViews.size(); i++) {
					if (headerViewsShouldQuickReturn.get(i) || headerViewsPermanentlyHidden.get(i)) {
						runningHeaderHeightSum += headerViewHeights.get(i);
						ViewHelper.setTranslationY(headerViews.get(i), -1 * runningHeaderHeightSum);
					}
				}

				int runningFooterHeightSum = 0;
				for (int i = 0; i < footerViews.size(); i++) {
					if (footerViewsShouldQuickReturn.get(i) || footerViewsPermanentlyHidden.get(i)) {
						runningFooterHeightSum += footerViewHeights.get(i);
						ViewHelper.setTranslationY(footerViews.get(i), runningFooterHeightSum);
					}
				}
			}
		}
	}

	/**
	 * Mark a header view as permanently hidden. These views will not render until the manual show is called.
	 *
	 * @param view
	 * @param animated
	 */
	public void permanentlyHideQuickReturnHeader ( View view, boolean animated ) {
		permanentlyHideQuickReturnHeader(headerViews.indexOf(view), animated);
	}

	/**
	 * Mark a header view as permanently hidden. These views will not render until the manual show is called.
	 *
	 * @param index
	 * @param animated
	 */
	public void permanentlyHideQuickReturnHeader ( int index, boolean animated ) {
		if (index >= 0 && index < headerViews.size() && !headerViewsPermanentlyHidden.get(index)) {
			headerViewsPermanentlyHidden.set(index, true);

			if (animationState == AnimationState.SHOWN || animationState == AnimationState.HIDDEN) {
				if (animated) {
					ArrayList<Animator> animators = new ArrayList<Animator>();
					int runningHeaderHeightSum = 0;

					for (int i = 0; i < headerViews.size(); i++) {
						if (index == i || (headerViewsShouldQuickReturn.get(i) && !headerViewsPermanentlyHidden.get(i))) {
							View view = headerViews.get(i);
							runningHeaderHeightSum += headerViewHeights.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", -1 * runningHeaderHeightSum)));
							} else if (i > index) {
								animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", currentTranslation - headerViewHeights.get(index))));
							}
						}
					}

					if (animators.size() > 0) {
						animationState = AnimationState.HIDING;

						AnimatorSet animatorSet = new AnimatorSet();
						animatorSet.playTogether(animators);
						animatorSet.setDuration(animationTimeOut);
						animatorSet.addListener(new AnimationSetTracker() {
							@Override
							public void onAllAnimationsEnded () {
								animationsComplete(AnimationState.HIDING);
								setupView();
							}
						});

						animatorSet.start();
					}
				} else {
					int runningHeaderHeightSum = 0;
					for (int i = 0; i < headerViews.size(); i++) {
						if (index == i || (headerViewsShouldQuickReturn.get(i) && !headerViewsPermanentlyHidden.get(i))) {
							View view = headerViews.get(i);
							runningHeaderHeightSum += headerViewHeights.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								ViewHelper.setTranslationY(headerViews.get(i), -1 * runningHeaderHeightSum);
							} else if (i > index) {
								ViewHelper.setTranslationY(headerViews.get(i), currentTranslation - headerViewHeights.get(index));
							}
						}
					}

					setupView();
				}
			}
		}
	}

	/**
	 * Remove a permanently hidden quick return header, allowing it to be shown again.
	 *
	 * @param view
	 * @param animated
	 */
	public void showPermanentlyHiddenQuickReturnHeader ( View view, boolean animated ) {
		showPermanentlyHiddenQuickReturnHeader(headerViews.indexOf(view), animated);
	}

	/**
	 * Remove a permanently hidden quick return header, allowing it to be shown again.
	 *
	 * @param index
	 * @param animated
	 */
	public void showPermanentlyHiddenQuickReturnHeader ( int index, boolean animated ) {
		if (index >= 0 && index < headerViews.size() && headerViewsPermanentlyHidden.get(index)) {
			headerViewsPermanentlyHidden.set(index, false);

			if (animationState == AnimationState.SHOWN || animationState == AnimationState.HIDDEN) {
				if (animated) {
					ArrayList<Animator> animators = new ArrayList<Animator>();

					for (int i = 0; i < headerViews.size(); i++) {
						if (index == i || (headerViewsShouldQuickReturn.get(i) && !headerViewsPermanentlyHidden.get(i))) {
							View view = headerViews.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								animators.add(Glider.glide(Skill.QuadEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", 0)));
							} else if (i > index) {
								animators.add(Glider.glide(Skill.QuadEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", currentTranslation + headerViewHeights.get(index))));
							}
						}
					}

					if (animators.size() > 0) {
						animationState = AnimationState.SHOWING;

						AnimatorSet animatorSet = new AnimatorSet();
						animatorSet.playTogether(animators);
						animatorSet.setDuration(animationTimeIn);
						animatorSet.addListener(new AnimationSetTracker() {
							@Override
							public void onAllAnimationsEnded () {
								animationsComplete(AnimationState.SHOWING);
								setupView();
							}
						});

						animatorSet.start();
					}
				} else {
					for (int i = 0; i < headerViews.size(); i++) {
						if (index == i || (headerViewsShouldQuickReturn.get(i) && !headerViewsPermanentlyHidden.get(i))) {
							View view = headerViews.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								ViewHelper.setTranslationY(headerViews.get(i), 0);
							} else if (i > index) {
								ViewHelper.setTranslationY(headerViews.get(i), currentTranslation + headerViewHeights.get(index));
							}
						}
					}

					setupView();
				}
			}
		}
	}

	/**
	 * Mark a footer view as permanently hidden. These views will not render until the manual show is called.
	 *
	 * @param view
	 * @param animated
	 */
	public void permanentlyHideQuickReturnFooter ( View view, boolean animated ) {
		permanentlyHideQuickReturnFooter(footerViews.indexOf(view), animated);
	}

	/**
	 * Mark a footer view as permanently hidden. These views will not render until the manual show is called.
	 *
	 * @param index
	 * @param animated
	 */
	public void permanentlyHideQuickReturnFooter ( int index, boolean animated ) {
		if (index >= 0 && index < footerViews.size() && !footerViewsPermanentlyHidden.get(index)) {
			footerViewsPermanentlyHidden.set(index, true);

			if (animationState == AnimationState.SHOWN || animationState == AnimationState.HIDDEN) {
				if (animated) {
					ArrayList<Animator> animators = new ArrayList<Animator>();
					int runningFooterHeightSum = 0;

					for (int i = 0; i < footerViews.size(); i++) {
						if (index == i || (footerViewsShouldQuickReturn.get(i) && !footerViewsPermanentlyHidden.get(i))) {
							View view = footerViews.get(i);
							runningFooterHeightSum += footerViewHeights.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", runningFooterHeightSum)));
							} else if (i > index) {
								animators.add(Glider.glide(Skill.QuadEaseIn, animationTimeOut, ObjectAnimator.ofFloat(view, "translationY", currentTranslation + footerViewHeights.get(index))));
							}
						}
					}

					if (animators.size() > 0) {
						animationState = AnimationState.HIDING;

						AnimatorSet animatorSet = new AnimatorSet();
						animatorSet.playTogether(animators);
						animatorSet.setDuration(animationTimeOut);
						animatorSet.addListener(new AnimationSetTracker() {
							@Override
							public void onAllAnimationsEnded () {
								animationsComplete(AnimationState.HIDING);
								setupView();
							}
						});

						animatorSet.start();
					}
				} else {
					int runningFooterHeightSum = 0;
					for (int i = 0; i < footerViews.size(); i++) {
						if (index == i || (footerViewsShouldQuickReturn.get(i) && !footerViewsPermanentlyHidden.get(i))) {
							View view = footerViews.get(i);
							runningFooterHeightSum += footerViewHeights.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								ViewHelper.setTranslationY(footerViews.get(i), runningFooterHeightSum);
							} else if (i > index) {
								ViewHelper.setTranslationY(footerViews.get(i), currentTranslation + footerViewHeights.get(index));
							}
						}
					}

					setupView();
				}
			}
		}
	}

	/**
	 * Remove a permanently hidden quick return footer, allowing it to be shown again.
	 *
	 * @param view
	 * @param animated
	 */
	public void showPermanentlyHiddenQuickReturnFooter ( View view, boolean animated ) {
		showPermanentlyHiddenQuickReturnFooter(footerViews.indexOf(view), animated);
	}

	/**
	 * Remove a permanently hidden quick return footer, allowing it to be shown again.
	 *
	 * @param index
	 * @param animated
	 */
	public void showPermanentlyHiddenQuickReturnFooter ( int index, boolean animated ) {
		if (index >= 0 && index < footerViews.size() && footerViewsPermanentlyHidden.get(index)) {
			footerViewsPermanentlyHidden.set(index, false);

			if (animationState == AnimationState.SHOWN || animationState == AnimationState.HIDDEN) {
				if (animated) {
					ArrayList<Animator> animators = new ArrayList<Animator>();

					for (int i = 0; i < footerViews.size(); i++) {
						if (index == i || (footerViewsShouldQuickReturn.get(i) && !footerViewsPermanentlyHidden.get(i))) {
							View view = footerViews.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								animators.add(Glider.glide(Skill.QuadEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", 0)));
							} else if (i > index) {
								animators.add(Glider.glide(Skill.QuadEaseOut, animationTimeIn, ObjectAnimator.ofFloat(view, "translationY", currentTranslation - footerViewHeights.get(index))));
							}
						}
					}

					if (animators.size() > 0) {
						animationState = AnimationState.SHOWING;

						AnimatorSet animatorSet = new AnimatorSet();
						animatorSet.playTogether(animators);
						animatorSet.setDuration(animationTimeIn);
						animatorSet.addListener(new AnimationSetTracker() {
							@Override
							public void onAllAnimationsEnded () {
								animationsComplete(AnimationState.SHOWING);
								setupView();
							}
						});

						animatorSet.start();
					}
				} else {
					for (int i = 0; i < footerViews.size(); i++) {
						if (index == i || (footerViewsShouldQuickReturn.get(i) && !footerViewsPermanentlyHidden.get(i))) {
							View view = footerViews.get(i);
							int currentTranslation = (int) ViewHelper.getTranslationY(view);

							if (i == index) {
								ViewHelper.setTranslationY(footerViews.get(i), 0);
							} else if (i > index) {
								ViewHelper.setTranslationY(footerViews.get(i), currentTranslation - footerViewHeights.get(index));
							}
						}
					}

					setupView();
				}
			}
		}
	}

	@Override
	protected void onLayout ( boolean changed, int l, int t, int r, int b ) {
		if (changed) {
			setupView();
		}

		super.onLayout(changed, l, t, r, b);
	}

	public void setupView () {
		recalculateQuickReturnViewHeights();
		setupMargins();
		hideShownQuickReturns(false);
		showHiddenQuickReturns(false);
	}

	protected void recalculateQuickReturnViewHeights () {
		headerViewHeights.clear();
		for (View view : headerViews) {
			int previousVisibility = view.getVisibility();
			view.setVisibility(VISIBLE);
			headerViewHeights.add(view.getMeasuredHeight());
			view.setVisibility(previousVisibility);
		}

		footerViewHeights.clear();
		for (View view : footerViews) {
			int previousVisibility = view.getVisibility();
			view.setVisibility(VISIBLE);
			footerViewHeights.add(view.getMeasuredHeight());
			view.setVisibility(previousVisibility);
		}
	}

	protected void setupMargins () {
		int runningHeaderHeightSum = 0;
		for (int i = 0; i < headerViews.size(); i++) {
			if (!headerViewsRenderOverList.get(i) && !headerViewsPermanentlyHidden.get(i)) {
				runningHeaderHeightSum += headerViewHeights.get(i);
			}
		}

		if (offsetView != null) {
			offsetView.setMinimumHeight(runningHeaderHeightSum);
		}
	}

	protected void setQuickReturnViewAnimations ( int y, int oldY ) {
		if (y > oldY) {
			hideShownQuickReturns(true);
		} else if (y < oldY) {
			showHiddenQuickReturns(true);
		}
	}

	protected void animationsComplete ( AnimationState fromDirection ) {
		if (fromDirection == AnimationState.SHOWING) {
			animationState = AnimationState.SHOWN;
		} else {
			animationState = AnimationState.HIDDEN;
		}
	}

	protected void setQuickReturnViewTranslations ( int y, int oldY ) {
		if (animationState == AnimationState.HIDDEN || animationState == AnimationState.SHOWN) {

			int runningHeaderHeightSum = 0;
			int runningPermanentlyHiddenHeaderHeightSum = 0;
			float diff = oldY - y;
			for (int i = 0; i < headerViews.size(); i++) {
				if (headerViewsPermanentlyHidden.get(i)) {
					runningPermanentlyHiddenHeaderHeightSum += headerViewHeights.get(i);
					runningHeaderHeightSum += headerViewHeights.get(i);
				} else if (headerViewsShouldQuickReturn.get(i)) {
					View view = headerViews.get(i);
					int height = headerViewHeights.get(i);
					float currentTranslation = ViewHelper.getTranslationY(view);

					runningHeaderHeightSum += height;

					if (runningHeaderHeightSum > 0) {
						diff *= 1.0f + ((float) height / (float) runningHeaderHeightSum);
					}

					setQuickReturnTranslation(view, currentTranslation, diff, -1 * runningHeaderHeightSum, -1 * runningPermanentlyHiddenHeaderHeightSum);
				}
			}

			int runningFooterHeightSum = 0;
			int runningPermanentlyHiddenFooterHeightSum = 0;
			diff = y - oldY;
			for (int i = 0; i < footerViews.size(); i++) {
				if (footerViewsPermanentlyHidden.get(i)) {
					runningPermanentlyHiddenFooterHeightSum += footerViewHeights.get(i);
					runningFooterHeightSum += footerViewHeights.get(i);
				} else if (footerViewsShouldQuickReturn.get(i)) {
					View view = footerViews.get(i);
					int height = footerViewHeights.get(i);
					float currentTranslation = ViewHelper.getTranslationY(view);

					if (runningFooterHeightSum > 0) {
						diff *= 1.0f + ((float) height / (float) runningFooterHeightSum);
					}

					runningFooterHeightSum += height;

					setQuickReturnTranslation(view, currentTranslation, diff, runningPermanentlyHiddenFooterHeightSum, runningFooterHeightSum);
				}
			}
		}
	}

	protected void setQuickReturnTranslation ( View view, float currentTranslation, float diff, int min, int max ) {
		if (animationState != AnimationState.SHOWING ) {
			ViewHelper.setTranslationY(view, Math.min(Math.max(min, currentTranslation + (diff * parallaxEffect)), max));
		}
	}

	protected boolean scrollTallySignificantEnough ( int y, int oldY ) {
		int diff = oldY - y;

		if (diff > 0) {
			// Scrolled up.
			if (runningScrollTally < 0) {
				runningScrollTally = 0;
			}

			runningScrollTally += diff;

			if (runningScrollTally >= minDifferenceBeforeShow) {
				return true;
			}
		} else if (diff < 0) {
			// Scrolled down.
			if (runningScrollTally > 0) {
				runningScrollTally = 0;
			}

			runningScrollTally += diff;

			if (Math.abs(runningScrollTally) >= minDifferenceBeforeHide) {
				return true;
			}
		} else {
			runningScrollTally = 0;
		}

		return false;
	}

	protected void snapQuickReturnsToIntent ( boolean animated ) {
		if (lastActionWasScrollDown) {
			hideShownQuickReturns(animated);
		} else {
			showHiddenQuickReturns(animated);
		}
	}

	protected void snapQuickReturnsToMidpoint ( boolean animated ) {
		int runningPermanentlyHiddenHeaderHeightSum = 0;
		for (int i = 0; i < headerViews.size(); i++) {
			if (headerViewsPermanentlyHidden.get(i)) {
				runningPermanentlyHiddenHeaderHeightSum += headerViewHeights.get(i);
			} else if (headerViewsShouldQuickReturn.get(i)) {
				float height = headerViewHeights.get(i);
				float offset = Math.abs(ViewHelper.getTranslationY(headerViews.get(i)));

				if (offset != runningPermanentlyHiddenHeaderHeightSum) {
					if ((height * 0.75f) > (offset - runningPermanentlyHiddenHeaderHeightSum)) {
						showHiddenQuickReturns(animated);
					} else {
						hideShownQuickReturns(animated);
					}

					return;
				}
			}
		}

		int runningPermanentlyHiddenFooterHeightSum = 0;
		for (int i = 0; i < footerViews.size(); i++) {
			if (footerViewsPermanentlyHidden.get(i)) {
				runningPermanentlyHiddenFooterHeightSum += footerViewHeights.get(i);
			} else if (footerViewsShouldQuickReturn.get(i)) {
				float height = footerViewHeights.get(i);
				float offset = Math.abs(ViewHelper.getTranslationY(footerViews.get(i)));

				if (offset != runningPermanentlyHiddenFooterHeightSum) {
					if ((height * 0.75f) > (offset - runningPermanentlyHiddenFooterHeightSum)) {
						showHiddenQuickReturns(animated);
					} else {
						hideShownQuickReturns(animated);
					}

					return;
				}
			}
		}
	}

	protected void handleScrollStateChanged ( int i ) {
		if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			if (revealListenerType == RevealListenerType.SCROLL) {
				if (snapToIntent) {
					snapQuickReturnsToIntent(true);
				} else if (snapToMidpoint) {
					snapQuickReturnsToMidpoint(true);
				}
			}

			if (revealOnIdle) {
				if (idleRunnable != null) {
					removeCallbacks(idleRunnable);
				}

				idleRunnable = new Runnable() {
					@Override
					public void run () {
						showHiddenQuickReturns(true);
					}
				};

				postDelayed(idleRunnable, idleRevealDelay);
			}
		}
	}

	protected void handleScrollChanged ( int y, int oldY ) {
		if (scrollTallySignificantEnough(y, oldY)) {
			if (revealOnIdle && idleRunnable != null) {
				removeCallbacks(idleRunnable);
			}

			lastActionWasScrollDown = y >= oldY;

			if (revealListenerType == RevealListenerType.SCROLL) {
				setQuickReturnViewTranslations(y, oldY);
			} else if (revealListenerType == RevealListenerType.ANIMATED) {
				setQuickReturnViewAnimations(y, oldY);
			}
		}
	}
}
