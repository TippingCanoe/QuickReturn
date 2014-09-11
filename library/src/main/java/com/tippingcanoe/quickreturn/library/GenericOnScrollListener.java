package com.tippingcanoe.quickreturn.library;

public interface GenericOnScrollListener<T> {
	public void onScrollChanged ( T view, int x, int y, int oldX, int oldY );
}
