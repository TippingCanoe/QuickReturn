package com.tippingcanoe.quickreturn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.tippingcanoe.quickreturn.library.QuickReturnContainer;
import com.tippingcanoe.quickreturn.library.RevealListenerType;

import java.util.Arrays;
import java.util.List;

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

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView label;

		public ViewHolder ( View itemView ) {
			super(itemView);
			label = (TextView) itemView.findViewById(android.R.id.text1);
		}

		public TextView getLabel () {
			return label;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		QuickReturnContainer quickReturn;
		private boolean twoHeadersHidden = true;

		public PlaceholderFragment () {
		}

		@Override
		public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			TextView fixedHeader = (TextView) rootView.findViewById(R.id.fixedHeader);
			TextView fixedFooter = (TextView) rootView.findViewById(R.id.fixedFooter);

			TextView quickHeader = (TextView) rootView.findViewById(R.id.quickReturnHeader);
			final TextView quickHeaderTwo = (TextView) rootView.findViewById(R.id.quickReturnHeaderTwo);
			final TextView quickFooter = (TextView) rootView.findViewById(R.id.quickReturnFooter);
			final TextView quickFooterTwo = (TextView) rootView.findViewById(R.id.quickReturnFooterTwo);

			FrameLayout offsetView = new FrameLayout(getActivity());

			RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.listView);
			listView.setLayoutManager(new LinearLayoutManager(getActivity()));
			listView.setHasFixedSize(true);

			//listView.addHeaderView(offsetView);

			final List<String> strings = Arrays.asList("lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit", "curabitur", "vel", "hendrerit", "libero", "eleifend", "blandit", "nunc", "ornare", "odio", "ut", "orci", "gravida", "imperdiet", "nullam", "purus", "lacinia", "a", "pretium", "quis", "congue", "praesent", "sagittis", "laoreet", "auctor", "mauris", "non", "velit", "eros", "dictum", "proin", "accumsan", "sapien", "nec", "massa", "volutpat", "venenatis", "sed", "eu", "molestie", "lacus", "quisque", "porttitor", "ligula", "dui", "mollis", "tempus", "at", "magna", "vestibulum", "turpis", "ac", "diam", "tincidunt", "id", "condimentum", "enim", "sodales", "in", "hac", "habitasse", "platea", "dictumst", "aenean", "neque", "fusce", "augue", "leo", "eget", "semper", "mattis", "tortor", "scelerisque", "nulla", "interdum", "tellus", "malesuada", "rhoncus", "porta", "sem", "aliquet", "et", "nam", "suspendisse", "potenti", "vivamus", "luctus", "fringilla", "erat", "donec", "justo", "vehicula", "ultricies", "varius", "ante", "primis", "faucibus", "ultrices", "posuere", "cubilia", "curae", "etiam", "cursus", "aliquam", "quam", "dapibus", "nisl", "feugiat", "egestas", "class", "aptent", "taciti", "sociosqu", "ad", "litora", "torquent", "per", "conubia", "nostra", "inceptos", "himenaeos", "phasellus", "nibh", "pulvinar", "vitae", "urna", "iaculis", "lobortis", "nisi", "viverra", "arcu", "morbi", "pellentesque", "metus", "commodo", "ut", "facilisis", "felis", "tristique", "ullamcorper", "placerat", "aenean", "convallis", "sollicitudin", "integer", "rutrum", "duis", "est", "etiam", "bibendum", "donec", "pharetra", "vulputate", "maecenas", "mi", "fermentum", "consequat", "suscipit", "aliquam", "habitant", "senectus", "netus", "fames", "quisque", "euismod", "curabitur", "lectus", "elementum", "tempor", "risus", "cras");

			listView.setAdapter(new RecyclerView.Adapter() {
				@Override
				public RecyclerView.ViewHolder onCreateViewHolder ( ViewGroup parent, int viewType ) {
					return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
				}

				@Override
				public void onBindViewHolder ( RecyclerView.ViewHolder holder, int position ) {
					((ViewHolder) holder).getLabel().setText(strings.get(position));
				}

				@Override
				public int getItemCount () {
					return strings.size();
				}
			});

			quickReturn = (QuickReturnContainer) rootView.findViewById(R.id.quickReturn);

			quickReturn.setObservedView(listView);
			quickReturn.setOffsetView(offsetView);

			quickReturn.attachHeaderView(fixedHeader, false, false, false);
			quickReturn.attachHeaderView(quickHeaderTwo, false, false, true);
			quickReturn.attachHeaderView(quickHeader, true, true, false);

			quickReturn.attachFooterView(fixedFooter, false, false, false);
			quickReturn.attachFooterView(quickFooterTwo, true, true, false);
			quickReturn.attachFooterView(quickFooter, true, true, false);


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
					quickReturn.setSnapToIntent(b);
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
