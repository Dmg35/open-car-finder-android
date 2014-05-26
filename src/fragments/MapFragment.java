package fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.acr.opencarfinder.R;
import org.osmdroid.views.MapView;

/**
 * Created by alan on 5/26/14.
 */
public class MapFragment extends Fragment {

    private static final String TAG = "MapFragmnet";
    private MapView mv;

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
        mv = (MapView) v.findViewById(R.id.mapview);
        return v;

    }
}