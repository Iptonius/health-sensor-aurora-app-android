package www.wemaketotem.org.totemopenhealth.tablayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import www.wemaketotem.org.totemopenhealth.R;

/**
 * Fragment of the realTime page.
 * Has currently no function
 */
public class FragmentRealTime extends Fragment {

    public static FragmentRealTime newInstance() {
        return new FragmentRealTime();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_realtime,container,false);
    }
}
