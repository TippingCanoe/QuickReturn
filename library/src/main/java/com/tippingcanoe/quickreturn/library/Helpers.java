package com.tippingcanoe.quickreturn.library;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Dictionary;
import java.util.Hashtable;

public class Helpers {
	protected static Dictionary<Integer, Integer> listViewItemHeightCache = new Hashtable<Integer, Integer>();

	public static int getScrollY ( ListView listView ) {
		View c = listView.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = listView.getFirstVisiblePosition();
		int top = c.getTop();

		return -top + firstVisiblePosition * c.getHeight();
	}

	public static int getScrollY ( AbsListView listView ) {
		View c = listView.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = listView.getFirstVisiblePosition();
		int scrollY = -(c.getTop());

		listViewItemHeightCache.put(listView.getFirstVisiblePosition(), c.getHeight());

		if (scrollY < 0) {
			scrollY = 0;
		}

		for (int i = 0; i < firstVisiblePosition; ++i) {
			if (listViewItemHeightCache.get(i) != null) {
				scrollY += listViewItemHeightCache.get(i);
			}
		}

		return scrollY;
	}
}
