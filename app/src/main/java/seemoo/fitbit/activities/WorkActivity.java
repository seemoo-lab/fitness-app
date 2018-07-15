package seemoo.fitbit.activities;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import seemoo.fitbit.R;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * The working menu.
 */
public class WorkActivity extends RequestPermissionsActivity {

    private final String TAG = this.getClass().getSimpleName();

    private boolean backClosesAppToastShown = false;
    private MainFragment mainFragment;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    /**
     * {@inheritDoc}
     * Initializes several objects and connects to the device.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        mainFragment = new MainFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.work_activity_fragment_frame,
                mainFragment).commit();

        requestPermissionsLocation();

        Toolbar toolbar = (Toolbar) findViewById(R.id.work_activity_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        backClosesAppToastShown = false;

                        switch (menuItem.getItemId()) {
                            case R.id.nav_information:
                                navigationView.getMenu().getItem(0).setChecked(true);
                                mainFragment.buttonCollectBasicInformation();
                                break;
                            case R.id.nav_alarms:
                                mainFragment.buttonAlarms();
                                break;
                            case R.id.nav_online:
                                mainFragment.buttonOnline();
                                break;
                            case R.id.nav_dump:
                                mainFragment.buttonDump();
                                break;
                            case R.id.nav_set_date:
                                mainFragment.buttonSetDate();
                                break;
                            case R.id.nav_live_mode:
                                mainFragment.buttonLiveMode();
                                break;
                            case R.id.nav_devices:
                                mainFragment.buttonDevices();
                                break;
                            case R.id.nav_reconnect:
                                mainFragment.connect();
                                break;
                        }
                        return true;
                    }
                });
        navigationView.getMenu().getItem(0).setChecked(true);

        setFinishOnTouchOutside(true);

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.textView_device)).
                setText(((BluetoothDevice) getIntent().getExtras().get("device")).getName());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.textView_connection_status)).setText(R.string.connected);

    }

    /**
     * {@inheritDoc}
     * Closes bluetooth gatt.
     */
    @Override
    public void onBackPressed() {
        /*super.onBackPressed();
        if (commands != null) {
            commands.close();
        }*/
        if (navigationView.getMenu().getItem(0).isChecked()) {
            if (!backClosesAppToastShown) {
                backClosesAppToastShown = true;
                Toast.makeText(this, R.string.back_closes_app_message, LENGTH_SHORT).show();
            } else {
                this.finishAffinity();
            }
        } else {
            mainFragment.buttonCollectBasicInformation();
        }
    }

    /**
     * {@inheritDoc}
     * Loads the saved settings from internal storage.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_workactivity, menu);
        for (int i = 0; i < menu.size() - 1; i++) { //load settings
            MenuItem item = menu.getItem(i);
            SharedPreferences settings = getSharedPreferences("" + item.getTitle(), MODE_PRIVATE);
            item.setChecked(settings.getBoolean((String) item.getTitle(), false));
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * Lets the user choose the external directory and stores settings internally.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
            default:
                mainFragment.handleOnOptionsItemSelected(item);
        }
        return true;

    }

}

