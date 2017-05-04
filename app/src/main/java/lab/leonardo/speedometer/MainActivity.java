package lab.leonardo.speedometer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.privacystreams.core.Callback;
import com.github.privacystreams.core.Item;
import com.github.privacystreams.core.MStream;
import com.github.privacystreams.core.UQI;
import com.github.privacystreams.core.purposes.Purpose;
import com.github.privacystreams.location.Geolocation;
import com.github.privacystreams.location.GeolocationOperators;
import com.github.privacystreams.location.LatLng;
import com.github.privacystreams.utils.Globals;

import java.text.DecimalFormat;

/**
 * Created by leonardo on 17/5/4.
 */
public class MainActivity extends Activity {
    private UQI uqi;
    private Purpose purpose;
    private TextView text1,text2,text3;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        text1 = (TextView)  findViewById(R.id.textView1);
        text2 = (TextView)  findViewById(R.id.textView2);
        text3 = (TextView)  findViewById(R.id.textView3);

        uqi = new UQI(MainActivity.this);
        Globals.LocationConfig.useGoogleService = false;
        new MyAsyncTask().execute();

        //testLocation();

    }
    private class MyAsyncTask extends AsyncTask<Object, Object, Object> {
        private double latitude,longtitude;
        private double latitude1,longtitude1;
        private long time;
        @Override
        protected Object doInBackground(Object[] objects) {
            latitude1 = 0;
            longtitude1 = 0;
            while(true) {
                testLocation();
                System.out.println("latitude:" + latitude);
                System.out.println("longtitude:" + longtitude);
                try {
                    Thread.sleep(1000);
                    publishProgress(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Object[] objects)
        {
            long time1 = time;
            time = System.currentTimeMillis();
            long t = (time - time1);
            text1.setText("latitude: "+latitude);
            text2.setText("longtitude: "+longtitude);
            if(longtitude1 == 0 && latitude1 == 0)
            {
                text3.setText("begin" );
            }
            else {
                double radLat1 = rad(latitude);
                double radLat2 = rad(latitude1);
                double a = radLat1 - radLat2;
                double b = rad(longtitude) - rad(longtitude1);
                double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
                s = s * 6378137;
                double tmp = s/t;
                DecimalFormat   df   =   new DecimalFormat("#####0.00");
                text3.setText("speed: "+df.format(tmp)+" m/s");
            }
            longtitude1 =longtitude;
            latitude1 =latitude;
        }
        double rad(double d)
        {
            return d * Math.PI / 180.0;
        }

        public void testLocation() {

            MStream locationStream = uqi.getData(Geolocation.asUpdates(1000, Geolocation.LEVEL_CITY), Purpose.TEST("test"))
                    .setField("distorted_lat_lng", GeolocationOperators.distort(Geolocation.LAT_LNG, 1000))
                    .setField("distortion", GeolocationOperators.distanceBetween(Geolocation.LAT_LNG, "distorted_lat_lng"))
                    .reuse(2);

            locationStream.debug();
            locationStream.forEach("distorted_lat_lng", new Callback<LatLng>() {
                @Override
                protected void onInput(LatLng input) {
                    latitude = input.getLatitude();
                    longtitude = input.getLongitude();
                    //System.out.println("latitude:"+latitude);
                    //System.out.println("longtitude:"+longtitude);
                    //System.out.println(input);
                }
            });
        /*
        try {
            Thread.sleep(10000);
            uqi.stopAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        }
    }



    public void monitorLocationUpdates() {

        double minLat = 40.0, minLng = -180.0, maxLat = 40.1, maxLng = -180.1; // the square region.

        uqi.getData(Geolocation.asUpdates(1000, Geolocation.LEVEL_EXACT), purpose) // get a live stream of location updates, the location granularity is "EXACT".
                .setField("inRegion", GeolocationOperators.inSquare("lat_lng", minLat, minLng, maxLat, maxLng))
                .forEach(new Callback<Item>() {
                    @Override
                    protected void onInput(Item location) { // callback for each item.
                        LatLng latLng = location.getValueByField("lat_lng"); // get the value of "lat_lng" field.
                        Boolean inRegion = location.getValueByField("inRegion"); // get the value of "inRegion" field.
                        Log.d("transfer", "lat=" + latLng.getLatitude() + ", lng=" + latLng.getLongitude());
                        System.out.println("lat=" + latLng.getLatitude() + ", lng=" + latLng.getLongitude());

                        if (inRegion) System.out.println("the location is in the square region.");
                    }
                });
    }
}
