package fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;
import com.acr.opencarfinder.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

/**
 * Created by Alan Ruvalcaba on 5/26/14.
 */
public class MapFragment extends Fragment implements View.OnClickListener, LocationListener {

    private static final String TAG = "MapFragment";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private final Handler mHandler = new Handler();
    private MapView mv;
    private IMapController mapController;
    private Marker myMarker;
    private Marker carMarker;
    private LocationManager locationManager;
    private String provider;
    private Location myLocation;
    private RoadManager roadManager = new OSRMRoadManager();
    private Polyline myRoadOverlay;

    //Update markerindicating user's location on map
    private Runnable update = new Runnable() {
        @Override
        public void run() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (null == myMarker)
                        myMarker = new Marker(mv);

                    GeoPoint myGeoPoint = new GeoPoint(myLocation.getLatitude(), myLocation.getLongitude());
                    myMarker.setPosition(myGeoPoint);
                    mapController.setCenter(myMarker.getPosition());
                }
            }).start();

            mv.invalidate();
        }


    };

    //Update route on map
    private Runnable routeUpdate = new Runnable() {
        @Override
        public void run() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                    waypoints.add(myMarker.getPosition());
                    waypoints.add(carMarker.getPosition());

                    //Acquire path using user's location and parked car location
                    Road road = roadManager.getRoad(waypoints);

                    myRoadOverlay = RoadManager.buildRoadOverlay(road, getActivity());


                    if (myRoadOverlay.isVisible())
                        mv.getOverlays().remove(myRoadOverlay);

                    mv.getOverlays().add(myRoadOverlay);


                    myMarker.setPosition(myRoadOverlay.getPoints().get(0));
                    carMarker.setPosition(myRoadOverlay.getPoints().get(myRoadOverlay.getNumberOfPoints() - 1));
                }
            }).start();

            mv.invalidate();
        }


    };

    @Override
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate called");
        super.onCreate(bundle);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view,
                             Bundle bundle) {
        Log.d(TAG, "onCreateView called");
        super.onCreateView(inflater, view, bundle);

        View v = inflater.inflate(R.layout.fragment_map, view, false);


        MapBoxTileSource.retrieveMapBoxMapId(this.getActivity().getApplicationContext());
        MapBoxTileSource mapBoxTileSource = new MapBoxTileSource();

        Log.d(TAG, MapBoxTileSource.getMapBoxMapId());

        this.mv = (MapView) v.findViewById(R.id.mapview);
        this.mv.setMultiTouchControls(true);
        this.mv.setTileSource(mapBoxTileSource);
        this.mapController = mv.getController();

        v.findViewById(R.id.zoom_me_button).setOnClickListener(this);
        v.findViewById(R.id.zoom_car_button).setOnClickListener(this);
        v.findViewById(R.id.zoom_route_button).setOnClickListener(this);
        return v;

    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        Log.d(TAG, "onActivityCreated called");
        super.onActivityCreated(bundle);

        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Define the criteria how to select the location provider -> use
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        Location location = locationManager.getLastKnownLocation(provider);

        if (null != location)
            onLocationChanged(location);

        locationManager.requestLocationUpdates(provider, 60000, 3, this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        GeoPoint carGeoPoint = new GeoPoint(Double.longBitsToDouble(settings.getLong(LAT, 0)), Double.longBitsToDouble(settings.getLong(LNG, 0)));

        this.carMarker = new Marker(this.mv);
        this.myMarker = new Marker(this.mv);

        this.myMarker.setIcon(getResources().getDrawable(R.drawable.my_location));
        this.carMarker.setIcon(getResources().getDrawable(R.drawable.drop_pin));

        this.myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        this.carMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        this.carMarker.setPosition(carGeoPoint);

        this.mapController.setZoom(16);

        this.mv.getOverlays().add(this.carMarker);
        this.mv.getOverlays().add(this.myMarker);

        this.mv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        this.mv.invalidate();

    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.zoom_me_button:


                if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    this.showSettingsAlert();
                    break;
                }

                if (null != myLocation)
                    this.mHandler.post(this.update);

                else
                    Toast.makeText(this.getActivity(), "Locking in your position...", Toast.LENGTH_LONG);

                break;
            case R.id.zoom_car_button:
                this.mapController.setCenter(this.carMarker.getPosition());
                break;
            case R.id.zoom_route_button:

                this.mHandler.post(this.routeUpdate);
        }

    }

    /* Request updates at startup */
    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 60000, 3, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (null != location) {
            this.myLocation = location;
            this.mHandler.post(this.update);
        }
        return;

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
}