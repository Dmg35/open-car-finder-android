package activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.acr.opencarfinder.R;
import fragments.HomeFragment;

/**
 * @author Alan Ruvalcaba
 */
public class OpenCarFinderActivity extends Activity {
    private static final String TAG = "OpenCarFinderActivity";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d(TAG, "onCreate called");

        this.setContentView(R.layout.activity_open_car_finder);

        if (null == bundle) {

            Log.d(TAG, "Creating HomeFragment");

            this.getFragmentManager().beginTransaction().add(R.id.frame_layout, new HomeFragment(), "HomeFragment").commit();
        }

    }

}

