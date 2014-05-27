package fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.acr.opencarfinder.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Created by alan on 5/26/14.
 */
public class MapFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MapFragmnet";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private MapView mv;
    private IMapController mapController;

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
        this.mv.setTileSource(mapBoxTileSource);
        this.mapController = mv.getController();

        v.findViewById(R.id.find_me).setOnClickListener(this);
        v.findViewById(R.id.map_zoom_in).setOnClickListener(this);
        v.findViewById(R.id.map_zoom_out).setOnClickListener(this);
        return v;

    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        Log.d(TAG, "onActivityCreated called");
        super.onActivityCreated(bundle);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        GeoPoint carGeoPoint = new GeoPoint(Double.longBitsToDouble(settings.getLong(LAT, 0)), Double.longBitsToDouble(settings.getLong(LNG, 0)));

        Marker carMarker = new Marker(this.mv);
        carMarker.setPosition(carGeoPoint);
        carMarker.setTitle("My car");

        this.mv.getOverlays().add(carMarker);
        this.mv.invalidate();

        mapController.setZoom(16);
        mapController.setCenter(carGeoPoint);


    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.map_zoom_in:
                this.mapController.setZoom(19);
                break;
            case R.id.map_zoom_out:
                this.mapController.setZoom(12);
                break;
        }

    }
}