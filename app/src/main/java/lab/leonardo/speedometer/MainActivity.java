package lab.leonardo.speedometer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.github.privacystreams.core.Callback;
import com.github.privacystreams.core.UQI;
import com.github.privacystreams.core.purposes.Purpose;
import com.github.privacystreams.location.Geolocation;
import com.github.privacystreams.utils.Globals;

/**
 * Created by leonardo on 17/5/4.
 */
public class MainActivity extends Activity {
    private UQI uqi;
    private TextView text1;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        text1 = (TextView)  findViewById(R.id.textView1);

        uqi = new UQI(MainActivity.this);
        Globals.LocationConfig.useGoogleService = false;
        new MyAsyncTask().execute();
    }
    private class MyAsyncTask extends AsyncTask<Object, Object, Object> {

        private float speed;
        @Override
        protected Object doInBackground(Object[] objects) {
            uqi.getData(Geolocation.asUpdates(500, Geolocation.LEVEL_CITY), Purpose.TEST("test"))
            .forEach("speed", new Callback<Float>() {
                @Override
                protected void onInput(Float input) {
                    speed = input;
                    System.out.println("speed:" + input.toString());
                    publishProgress(100);
                }
            });
            return null;
        }
        @Override
        protected void onProgressUpdate(Object[] objects) {
            text1.setText("speed: "+speed+ " m/s");
        }
    }

}
