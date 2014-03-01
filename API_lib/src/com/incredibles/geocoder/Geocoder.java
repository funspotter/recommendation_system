package com.incredibles.geocoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

public class Geocoder {

	public static class MyLatLng {
		public double lat;
		public double lon;
		
		public MyLatLng(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		
		@Override
		public String toString() {
			return "lat = " + lat + " lon = " + lon;
		}
	}
	
	private final Map<String, MyLatLng> cinemaLocationMap;
	private final com.google.code.geocoder.Geocoder googleGeocoder;
	
	public Geocoder() {
		cinemaLocationMap = new HashMap<String, Geocoder.MyLatLng>();
		cinemaLocationMap.put("Cinema City Alle", 			new MyLatLng(47.474302, 19.048723));
		cinemaLocationMap.put("Cinema City Alba", 			new MyLatLng(47.190806, 18.406002));
		cinemaLocationMap.put("Cinema City Aréna", 			new MyLatLng(47.499141, 19.087497));
		cinemaLocationMap.put("Cinema City Aréna (IMAX)", 	new MyLatLng(47.499141, 19.087497));
		cinemaLocationMap.put("Cinema City Aréna (VIP)", 	new MyLatLng(47.499141, 19.087497));
		cinemaLocationMap.put("Cinema City Balaton", 		new MyLatLng(47.095896, 17.919492));
		cinemaLocationMap.put("Cinema City Campona", 		new MyLatLng(47.40662, 19.015412));
		cinemaLocationMap.put("Cinema City Debrecen", 		new MyLatLng(47.534762, 21.625575));
		cinemaLocationMap.put("Cinema City Duna Pláza", 	new MyLatLng(47.549479, 19.072774));
		cinemaLocationMap.put("Cinema City Gyõr", 			new MyLatLng(47.67004, 17.65086));
		cinemaLocationMap.put("Cinema City Kaposvár", 		new MyLatLng(46.356081, 17.78478));
		cinemaLocationMap.put("Cinema City Miskolc", 		new MyLatLng(48.104938, 20.788983));
		cinemaLocationMap.put("Cinema City Nyíregyháza", 	new MyLatLng(47.955732, 21.732832));
		cinemaLocationMap.put("Cinema City Mammut", 		new MyLatLng(47.508367, 19.026297));
		cinemaLocationMap.put("Cinema City MOM Park", 		new MyLatLng(47.490543, 19.023961));
		cinemaLocationMap.put("Cinema City Pécs", 			new MyLatLng(46.049549, 18.210223));
		cinemaLocationMap.put("Cinema City Savaria", 		new MyLatLng(47.221871, 16.617011));
		cinemaLocationMap.put("Cinema City Sopron", 		new MyLatLng(47.697733, 16.579074));
		cinemaLocationMap.put("Cinema City Szeged", 		new MyLatLng(46.265942, 20.129108));
		cinemaLocationMap.put("Cinema City Szolnok", 		new MyLatLng(47.177418, 20.191839));
		cinemaLocationMap.put("Cinema City Westend", 		new MyLatLng(47.514128, 19.059556));
		cinemaLocationMap.put("Cinema City Zalaegerszeg", 	new MyLatLng(46.847351, 16.852101));
		
		googleGeocoder = new com.google.code.geocoder.Geocoder();
	}
	
	/**
	 * Geocode an address - works only for cinema city names
	 * 
	 * @param address The address that should be geocoded
	 * @return The coordinates of the address
	 */
	public MyLatLng geocode(String address) {
		MyLatLng ret = cinemaLocationMap.get(address);
		if (ret == null) {
			if (!address.contains("Magyarország")) {
				address = address.concat(", Magyarország");
			}
			GeocoderRequest request = 
					new GeocoderRequestBuilder()
						.setAddress(address)
						.setRegion("hu")
						.setLanguage("hu")
						.getGeocoderRequest();
			GeocodeResponse response = googleGeocoder.geocode(request);
			List<GeocoderResult> resultList = response.getResults();
			if (resultList != null && resultList.size() > 0) {
				GeocoderResult result = resultList.get(0);
//				System.out.println("addres " + result.getFormattedAddress());
				LatLng location = result.getGeometry().getLocation();
				ret = new MyLatLng(location.getLat().doubleValue(), location.getLng().doubleValue());
			}
		}
		return ret;
	}
}
