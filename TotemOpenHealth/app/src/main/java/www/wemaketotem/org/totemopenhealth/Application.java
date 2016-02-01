package www.wemaketotem.org.totemopenhealth;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import www.wemaketotem.org.totemopenhealth.tablayout.FragmentDevice;
import www.wemaketotem.org.totemopenhealth.tablayout.FragmentRealTime;
import www.wemaketotem.org.totemopenhealth.tablayout.FragmentScan;
import www.wemaketotem.org.totemopenhealth.tablayout.SectionsPagerAdapter;
import www.wemaketotem.org.totemopenhealth.tablayout.TabItem;

/**
 * Main application of the app
 */
public class Application extends AppCompatActivity implements Observer{

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpPager();
    }

    /**
     * creates the view pager and sets the adapter.
     */
    private void setUpPager() {
        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager(), getTabs());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Creates the tabs of the pager
     * @return Array list with al the tab items
     */
    private ArrayList<TabItem> getTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();
        FragmentScan fragmentScan = FragmentScan.newInstance();
        FragmentRealTime fragmentRealTime = FragmentRealTime.newInstance();
        FragmentDevice fragmentDevice = FragmentDevice.newInstance();

        fragmentScan.registerObserver(this);
        fragmentScan.registerObserver(fragmentDevice);

        tabItems.add(new TabItem("Scan", fragmentScan));
        tabItems.add(new TabItem("Realtime", fragmentRealTime));
        tabItems.add(new TabItem("Device", fragmentDevice));
        return tabItems;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Go to the device tab on connect
     */
    @Override
    public void deviceConnected() {
        mViewPager.setCurrentItem(2);
    }


    /**
     * Go to the scan tab on disconnect
     */
    @Override
    public void deviceDisconnected() {
        Log.d("main", "show first tab");
        mViewPager.setCurrentItem(0);
    }
}
