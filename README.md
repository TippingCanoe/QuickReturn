# QuickReturn

## Introduction

This library is based on the QuickReturn UI pattern [described by Roman Nurik](https://plus.google.com/u/0/+RomanNurik/posts/1Sb549FvpJt).
It adds the ability for `AbsListView`s and `ScrollView`s to have multiple attached headers and footers that will
disappear and reappear as the user scrolls through the associated content.

A demo application included with this repository shows an example of this functionality;

![demonstration](https://raw2.github.com/TippingCanoe/QuickReturn/master/demo.gif)

## Installing

1. Add the repository to your `build.gradle` file;

	``` groovy
	repositories {
		mavenCentral()
    	maven {
        	url 'https://raw.github.com/TippingCanoe/QuickReturn/master/maven/'
    	}
	}
	```
	
2. And add the dependency;

	``` groovy
	dependencies {
		compile 'com.tippingcanoe.quickreturn:library:1.0.0@aar'
	}
	```

## Creating layout

The `com.tippingcanoe.quickreturn.library.QuickReturnContainer` view is the core of this project. Simply add one to
your view, and add an `AbsListView` or `ScrollView` and any number of views you wish to act as quick returned headers
and footers as children.

``` xml
<com.tippingcanoe.quickreturn.library.QuickReturnContainer
	android:id="@+id/quickReturn"
	android:layout_width="match_parent"
	android:layout_height="wrap_content">

	<ListView
		android:id="@+id/listView"
		android:layout_width="match_parent"
		android:layout_height="match_parent" />
		
	<TextView
        android:id="@+id/quickReturnHeader"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
        
</com.tippingcanoe.quickreturn.library.QuickReturnContainer>
```

**Hint:** `QuickReturnContainer` extends `RelativeLayout`, so you may find it easier to design and layout your headers
and footers before switching to the custom view.

**Hint:** If you're needing to use a `ScrollView`, this library provides a subclass called `ObservableScrollView` that
will allow the `QuickReturnContainer` to listen for the necessary callbacks.

## Setting up views

Once you've got your layout setup, you'll need to find your views;

``` java
ListView listView = (ListView) rootView.findViewById(R.id.listView);
TextView quickHeader = (TextView) rootView.findViewById(R.id.quickReturnHeader);
QuickReturnContainer quickReturn = (QuickReturnContainer) rootView.findViewById(R.id.quickReturn);
```

And create a layout to act as an offset for any headers you might add so that they don't overlap the content in your
observable view.

If you're observing a `ListView`, this can take the form of a header added to that view;

``` java
FrameLayout offsetView = new FrameLayout(getActivity());
listView.addHeaderView(offsetView);
```

For a `ScrollView`, you can just add an empty `FrameLayout` as a child at the top of that view, for example.

Don't worry about setting the height of the offset view, the library will take care of that for you and update it
as necessary. Once you're done, you'll need to tell the `QuickReturnContainer` about that view;

``` java
quickReturn.setOffsetView(offsetView);
```

And tell it which `AbsListView` or `ObservableScrollView` to observe for scroll events;

``` java
quickReturn.setObservedView(listView);
```

This action will consume the `setOnScrollListener` for an `AbsListView`. If you require listening for these
same callbacks, a passthrough is provided on the `QuickReturnContainer`;

``` java
quickReturn.setOnScrollListener(new AbsListView.OnScrollListener() { ...
```

A custom callback passthrough is also provided for the `ObservableScrollView`, if you wish to listen for those events;

``` java
quickReturn.setOnScrollListener(new GenericOnScrollListener<ObservableScrollView>() { ...
```

Now all that's left to do is add the header and footer views you wish to have managed by the `QuickReturnContainer`;

``` java
quickReturn.attachHeaderView(quickHeader, true, false);
```

It's important to call `.attachHeaderView` for each view in the top down order - starting with the header closest to the
top of the `ObservableScrollView` and working down. Similarly, for `.attachFooterView`, you'll need to work bottom up -
starting from the footer closest to the bottom and working up.

## Customizing

This library provides plenty of customization touch points to ensure the effect is right for your application. See the
`:app` project in this repository for examples, or see any of the public methods in `QuickReturnContainer`

## Contact

Love it? Hate it? Want to make changes to it? Contact me at [@iainconnor](http://www.twitter.com/iainconnor) or
[iainconnor@gmail.com](mailto:iainconnor@gmail.com).