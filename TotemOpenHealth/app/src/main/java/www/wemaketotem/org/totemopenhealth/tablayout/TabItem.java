package www.wemaketotem.org.totemopenhealth.tablayout;

import android.support.v4.app.Fragment;

/**
 * Tab item of the viewpager
 */
public class TabItem {

    private String mTitle;
    private Fragment mFragment;

    public TabItem(String title, Fragment fragment) {
        mFragment = fragment;
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public Fragment getFragment() {
        return mFragment;
    }
}
