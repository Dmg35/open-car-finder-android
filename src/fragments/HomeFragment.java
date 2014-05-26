package fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.acr.opencarfinder.R;
import listeners.ButtonListener;

/**
 * Created by Alan Ruvalcaba on 5/26/14.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragmnet";


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

        View v = v = inflater.inflate(R.layout.fragment_home, view, false);

        v.findViewById(R.id.park_button).setOnClickListener(new ButtonListener());
        v.findViewById(R.id.find_button).setOnClickListener(new ButtonListener(this.getFragmentManager()));

        return v;

    }
}
