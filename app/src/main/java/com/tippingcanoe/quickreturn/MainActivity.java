package com.tippingcanoe.quickreturn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.*;
import com.tippingcanoe.quickreturn.library.QuickReturnContainer;
import com.tippingcanoe.quickreturn.library.RevealListenerType;

import java.util.Arrays;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu ( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected ( MenuItem item ) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		QuickReturnContainer quickReturn;

		public PlaceholderFragment () {
		}

		@Override
		public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			TextView fixedHeader = (TextView) rootView.findViewById(R.id.fixedHeader);
			TextView fixedFooter = (TextView) rootView.findViewById(R.id.fixedFooter);

			TextView quickHeader = (TextView) rootView.findViewById(R.id.quickReturnHeader);
			TextView quickHeaderTwo = (TextView) rootView.findViewById(R.id.quickReturnHeaderTwo);
			final TextView quickFooter = (TextView) rootView.findViewById(R.id.quickReturnFooter);
			TextView quickFooterTwo = (TextView) rootView.findViewById(R.id.quickReturnFooterTwo);

			ListView listView = (ListView) rootView.findViewById(R.id.listView);

			listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Arrays.asList("lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit", "curabitur", "vel", "hendrerit", "libero", "eleifend", "blandit", "nunc", "ornare", "odio", "ut", "orci", "gravida", "imperdiet", "nullam", "purus", "lacinia", "a", "pretium", "quis", "congue", "praesent", "sagittis", "laoreet", "auctor", "mauris", "non", "velit", "eros", "dictum", "proin", "accumsan", "sapien", "nec", "massa", "volutpat", "venenatis", "sed", "eu", "molestie", "lacus", "quisque", "porttitor", "ligula", "dui", "mollis", "tempus", "at", "magna", "vestibulum", "turpis", "ac", "diam", "tincidunt", "id", "condimentum", "enim", "sodales", "in", "hac", "habitasse", "platea", "dictumst", "aenean", "neque", "fusce", "augue", "leo", "eget", "semper", "mattis", "tortor", "scelerisque", "nulla", "interdum", "tellus", "malesuada", "rhoncus", "porta", "sem", "aliquet", "et", "nam", "suspendisse", "potenti", "vivamus", "luctus", "fringilla", "erat", "donec", "justo", "vehicula", "ultricies", "varius", "ante", "primis", "faucibus", "ultrices", "posuere", "cubilia", "curae", "etiam", "cursus", "aliquam", "quam", "dapibus", "nisl", "feugiat", "egestas", "class", "aptent", "taciti", "sociosqu", "ad", "litora", "torquent", "per", "conubia", "nostra", "inceptos", "himenaeos", "phasellus", "nibh", "pulvinar", "vitae", "urna", "iaculis", "lobortis", "nisi", "viverra", "arcu", "morbi", "pellentesque", "metus", "commodo", "ut", "facilisis", "felis", "tristique", "ullamcorper", "placerat", "aenean", "convallis", "sollicitudin", "integer", "rutrum", "duis", "est", "etiam", "bibendum", "donec", "pharetra", "vulputate", "maecenas", "mi", "fermentum", "consequat", "suscipit", "aliquam", "habitant", "senectus", "netus", "fames", "quisque", "euismod", "curabitur", "lectus", "elementum", "tempor", "risus", "cras")));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick ( AdapterView<?> adapterView, View view, int i, long l ) {
					Toast.makeText(getActivity(), "Showing quick returns", Toast.LENGTH_SHORT).show();
					quickReturn.showHiddenQuickReturns(true);
				}
			});

			quickReturn = (QuickReturnContainer) rootView.findViewById(R.id.quickReturn);

			quickReturn.setObservedView(listView);
			quickReturn.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged ( AbsListView listView, int i ) {
					if (i == SCROLL_STATE_IDLE) {
						Toast.makeText(getActivity(), "Scrolling finished", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onScroll ( AbsListView listView, int i, int i2, int i3 ) {
				}
			});

			quickReturn.attachHeaderView(fixedHeader, false);
			quickReturn.attachHeaderView(quickHeaderTwo, true);
			quickReturn.attachHeaderView(quickHeader, true);

			CheckBox onScrollCheck = (CheckBox) rootView.findViewById(R.id.onScrollCheckbox);
			onScrollCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged ( CompoundButton compoundButton, boolean b ) {
					quickReturn.setRevealListenerType(b ? RevealListenerType.SCROLL : RevealListenerType.ANIMATED);
				}
			});

			CheckBox parallaxCheck = (CheckBox) rootView.findViewById(R.id.parallaxCheckbox);
			parallaxCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged ( CompoundButton compoundButton, boolean b ) {
					quickReturn.setParallaxEffect(b ? 0.8f : 1.0f);
				}
			});

			CheckBox snapCheck = (CheckBox) rootView.findViewById(R.id.snapCheckbox);
			snapCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged ( CompoundButton compoundButton, boolean b ) {
					quickReturn.setSnapToMidpoint(b);
				}
			});

			CheckBox revealCheck = (CheckBox) rootView.findViewById(R.id.revealCheckbox);
			revealCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged ( CompoundButton compoundButton, boolean b ) {
					quickReturn.setRevealOnIdle(b);
				}
			});

			return rootView;
		}
	}
}
