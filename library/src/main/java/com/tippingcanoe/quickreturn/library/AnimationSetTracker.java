package com.tippingcanoe.quickreturn.library;

import com.nineoldandroids.animation.Animator;

public abstract class AnimationSetTracker implements Animator.AnimatorListener {
	int runningAnimations;

	public AnimationSetTracker () {
		runningAnimations = 0;
	}

	@Override
	public void onAnimationStart ( Animator animation ) {
		runningAnimations++;
	}

	@Override
	public void onAnimationEnd ( Animator animation ) {
		runningAnimations--;

		if (runningAnimations <= 0) {
			runningAnimations = 0;
			onAllAnimationsEnded();
		}
	}

	@Override
	public void onAnimationCancel ( Animator animation ) {

	}

	@Override
	public void onAnimationRepeat ( Animator animation ) {

	}

	/**
	 * Called when all animations in the set ended.
	 */
	abstract public void onAllAnimationsEnded ();
}
