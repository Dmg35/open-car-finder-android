package fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.acr.opencarfinder.R;

/**
 * Created by Alan Ruvalcaba on 5/26/14.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, LocationListener {

    private static final String TAG = "HomeFragment";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private final Handler mHandler = new Handler();
    boolean parked = false;

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            View v = getView();

            //If car is parked, then change park button to inactive state.
            if (parked) {
                Button parkButton = (Button) v.findViewById(R.id.park_button);
                parkButton.setBackgroundColor(Color.parseColor("#151515"));
                parkButton.setText(R.string.park);
                parkButton.postInvalidate();

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                settings.edit().remove(LAT).apply();
                settings.edit().remove(LNG).apply();
                parked = false;

                return;
            }


            //Else, park button is in inactive state and changes to active
            Button parkButton = (Button) v.findViewById(R.id.park_button);
            parkButton.setBackgroundColor(Color.parseColor("#8CC63E"));
            parkButton.setText(R.string.parked);
            parkButton.postInvalidate();
            parked = true;

            return;

        }
    };
    private LocationManager locationManager;
    private String provider;

    @Override
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate called");
        super.onCreate(bundle);

        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view,
                             Bundle bundle) {
        Log.d(TAG, "onCreateView called");
        super.onCreateView(inflater, view, bundle);

        View v = v = inflater.inflate(R.layout.fragment_home, view, false);

        v.findViewById(R.id.park_button).setOnClickListener(this);
        v.findViewById(R.id.find_button).setOnClickListener(this);

        return v;

    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        Log.d(TAG, "onActivityCreated called");
        super.onActivityCreated(bundle);

        //
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        if (settings.contains(LAT) && settings.contains(LNG)) {
            mHandler.post(this.update);
        }
    }

    //Request updates at startup
    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 60000, 3, this);
    }

    //Remove the locationlistener updates when Activity is paused
    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (null != location && !parked) {

            //Update stored coordinates of parked car
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            settings.edit().putLong(LAT, Double.doubleToLongBits(location.getLatitude())).apply();
            settings.edit().putLong(LNG, Double.doubleToLongBits(location.getLongitude())).apply();
        }

    }

    @Override
    public void onProviderEnabled(String provider) {
        return;
    }

    @Override
    public void onProviderDisabled(String provider) {
        return;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        return;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.find_button:

                if (!parked)
                    return;

                this.getFragmentManager().beginTransaction().addToBackStack(null).add(R.id.frame_layout, new MapFragment()).hide(getFragmentManager().findFragmentByTag("HomeFragment")).commit();
                break;

            case R.id.park_button:


                if (parked) {
                    mHandler.post(this.update);

                    if (!parked)
                        locationManager.requestLocationUpdates(provider, 60000, 3, this);
                    else
                        locationManager.removeUpdates(this);
                    break;
                } else {
                    if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        this.showSettingsAlert();
                        break;
                    }


                    mHandler.post(this.update);

                    if (!parked)
                        locationManager.requestLocationUpdates(provider, 60000, 3, this);
                    else
                        locationManager.removeUpdates(this);
                    break;
                }
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Location Settings");

        // Setting Dialog Message
        alertDialog.setMessage("Location services not enabled.\nDo you want to enable location services?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}


