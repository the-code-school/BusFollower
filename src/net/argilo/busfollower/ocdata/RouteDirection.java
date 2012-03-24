package net.argilo.busfollower.ocdata;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class RouteDirection {
	private static final String TAG = "RouteDirection";

	private String routeNumber;
	private String routeLabel;
	private String direction;
	private String error;
	private String requestProcessingTime;
	private ArrayList<Trip> trips;

	public RouteDirection(XmlPullParser xpp) {
		try {
			while (xpp.next() == XmlPullParser.START_TAG) {
				String tagName = xpp.getName();
				if ("RouteNo".equalsIgnoreCase(tagName)) {
					routeNumber = xpp.nextText();
				} else if ("RouteLabel".equalsIgnoreCase(tagName)) {
					routeLabel = xpp.nextText();
				} else if ("Direction".equalsIgnoreCase(tagName)) {
					direction = xpp.nextText();
				} else if ("Error".equalsIgnoreCase(tagName)) {
					error = xpp.nextText();
				} else if ("RequestProcessingTime".equalsIgnoreCase(tagName)) {
					requestProcessingTime = xpp.nextText();
				} else if ("Trips".equalsIgnoreCase(tagName)) {
					xpp.next();
					xpp.require(XmlPullParser.START_TAG, "", "Trip");
					while (xpp.next() == XmlPullParser.START_TAG) {
						xpp.require(XmlPullParser.START_TAG, "", "node");
						trips.add(new Trip(xpp));
					}
					xpp.require(XmlPullParser.END_TAG, "", "Trip");
					xpp.next();
					xpp.require(XmlPullParser.END_TAG, "", "Trips");
				} else {
					Log.w(TAG, "Unrecognized start tag: " + tagName);
				}
				xpp.require(XmlPullParser.END_TAG, "", tagName);
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
