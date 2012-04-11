package net.argilo.busfollower.ocdata;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.android.maps.GeoPoint;

import android.util.Log;

public class Trip implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "Trip";
	
	private String destination = null;
	private String startTime = null;
	private String adjustedScheduleTime = null;
	private float adjustmentAge = Float.NaN;
	private boolean lastTripOfSchedule = false;
	private BusType busType = new BusType("");
	private float gpsSpeed = Float.NaN;
	private int latitude = Integer.MIN_VALUE;
	private int longitude = Integer.MIN_VALUE;
	
	// Needed to get the request processing time.
	private RouteDirection routeDirection;
	
	public Trip(XmlPullParser xpp, RouteDirection routeDirection) throws XmlPullParserException, IOException {
		this.routeDirection = routeDirection;
		
		while (xpp.next() == XmlPullParser.START_TAG) {
			String tagName = xpp.getName();
			if ("TripDestination".equalsIgnoreCase(tagName)) {
				destination = xpp.nextText();
			} else if ("TripStartTime".equalsIgnoreCase(tagName)) {
				startTime = xpp.nextText();
			} else if ("AdjustedScheduleTime".equalsIgnoreCase(tagName)) {
				adjustedScheduleTime = xpp.nextText();
			} else if ("AdjustmentAge".equalsIgnoreCase(tagName)) {
				try {
					adjustmentAge = Float.parseFloat(xpp.nextText());
				} catch (Exception e) {
					Log.w(TAG, "Couldn't parse AdjustmentAge.");
				}
			} else if ("LastTripOfSchedule".equalsIgnoreCase(tagName)) {
				lastTripOfSchedule = "true".equalsIgnoreCase(xpp.nextText());
			} else if ("BusType".equalsIgnoreCase(tagName)) {
				busType = new BusType(xpp.nextText());
			} else if ("GPSSpeed".equalsIgnoreCase(tagName)) {
				try {
					gpsSpeed = Float.parseFloat(xpp.nextText());
				} catch (Exception e) {
					// Ignore.
				}
			} else if ("Latitude".equalsIgnoreCase(tagName)) {
				try {
					latitude = Util.latStringToMicroDegrees(xpp.nextText());
				} catch (NumberFormatException e) {
					// Ignore.
				}
			} else if ("Longitude".equalsIgnoreCase(tagName)) {
				try {
					longitude = Util.lonStringToMicroDegrees(xpp.nextText());
				} catch (NumberFormatException e) {
					// Ignore.
				}
			} else {
				Log.w(TAG, "Unrecognized start tag: " + tagName);
			}
			xpp.require(XmlPullParser.END_TAG, null, tagName);
		}
	}
	
	public String getDestination() {
		return destination;
	}
	
	public Date getStartTime() {
		// Start time is measured from "noon minus 12h" (effectively midnight, except for days
		// on which daylight savings time changes occur) at the beginning of the service date.

		// Start with the request processing time (or the current time, if it's unavailable),
		// which should be within a few hours of the start time.
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Toronto"));
		if (routeDirection.getRequestProcessingTime() != null) {
			calendar.setTime(routeDirection.getRequestProcessingTime());
		}
		
		int colonIndex = startTime.indexOf(":");
		int hours = Integer.parseInt(startTime.substring(0, colonIndex));
		int minutes = Integer.parseInt(startTime.substring(colonIndex + 1));
		
		// Subtracting the start time should put us within a few hours of the beginning of
		// the service date.
		calendar.add(Calendar.HOUR, -hours);
		calendar.add(Calendar.MINUTE, -minutes);
		
		// Now scan forward until we get to noon.
		while (calendar.get(Calendar.HOUR_OF_DAY) != 12) {
			calendar.add(Calendar.HOUR, 1);
		}
		calendar.set(Calendar.MINUTE, 0);
		
		// Subtract twelve hours.
		calendar.add(Calendar.HOUR, -12);
		
		// Add in the start time.
		calendar.add(Calendar.HOUR, hours);
		calendar.add(Calendar.MINUTE, minutes);
		
		// Set the seconds and milliseconds to zero.
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar.getTime();
	}
	
	public Date getAdjustedScheduleTime() {
		try {
			Calendar calendar = Calendar.getInstance();
			if (routeDirection.getRequestProcessingTime() != null) {
				calendar.setTime(routeDirection.getRequestProcessingTime());
			}
			calendar.add(Calendar.MINUTE, Integer.parseInt(adjustedScheduleTime));
			return calendar.getTime();
		} catch (NumberFormatException e) {
			Log.w(TAG, "Couldn't parse AdjustedScheduleTime: " + adjustedScheduleTime);
			return null;
		}
	}
	
	public float getAdjustmentAge() {
		return adjustmentAge;
	}
	
	public boolean isEstimated() {
		return (adjustmentAge >= 0);
	}
	
	public boolean isLastTrip() {
		return lastTripOfSchedule;
	}
	
	public BusType getBusType() {
		return busType;
	}
	
	public float getGpsSpeed() {
		return gpsSpeed;
	}
	
	public GeoPoint getGeoPoint() {
		if (latitude == Integer.MIN_VALUE || longitude == Integer.MIN_VALUE) {
			return null;
		} else {
			return new GeoPoint(latitude, longitude);
		}
	}
	
	public RouteDirection getRouteDirection() {
		return routeDirection;
	}
}
