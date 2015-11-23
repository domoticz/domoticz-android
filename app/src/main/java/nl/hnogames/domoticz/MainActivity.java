package nl.hnogames.domoticz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import nl.hnogames.domoticz.Adapters.NavigationAdapter;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class MainActivity extends AppCompatActivity {

    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;
    private String[] fragments;
    private String TAG = MainActivity.class.getSimpleName();
    private SharedPrefUtil mSharedPrefs;

    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private NavigationAdapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager

    private Menu menu;
    private int selectedFragment = 0;
    private SearchView searchViewAction;

    private final int iWelcomeResultCode = 885;
    private final int iSettingsResultCode = 995;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPrefs = new SharedPrefUtil(this);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            buildScreen();
        }
    }

    private void buildScreen()
    {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            addDrawerItems();
            addFragment();
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    /* Called when the second activity's finished */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null && resultCode == RESULT_OK) {
            switch(requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (!res.getBoolean("RESULT", false))
                        this.finish();
                    else {
                        buildScreen();
                    }
                    break;
                case iSettingsResultCode:
                    Fragment f =getVisibleFragment();
                    if (f instanceof DomoticzFragment) {
                        ((DomoticzFragment)f).refreshFragment();
                    }
                    else if (f instanceof DomoticzCardFragment)
                        ((DomoticzCardFragment)f).refreshFragment();{
                    }

                    updateDrawerItems();
                    break;

            }
        }
    }

    private void addFragment() {
        int screenIndex = mSharedPrefs.getStartupScreenIndex();
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        selectedFragment = screenIndex;
        tx.replace(R.id.main, Fragment.instantiate(MainActivity.this, getResources().getStringArray(R.array.drawer_fragments)[screenIndex]));
        tx.commitAllowingStateLoss();
    }

    private void updateDrawerItems() {
        String[] drawerActions = mSharedPrefs.getNavigationActions();
        fragments =mSharedPrefs.getNavigationFragments();
        int ICONS[] = mSharedPrefs.getNavigationIcons();
        mAdapter.updateData(drawerActions, ICONS);
    }

    /**
     * Adds the items to the drawer and registers a click listener on the items
     */
    private void addDrawerItems() {
        String[] drawerActions = mSharedPrefs.getNavigationActions();
        fragments =mSharedPrefs.getNavigationFragments();
        int ICONS[] = mSharedPrefs.getNavigationIcons();

        String NAME = getString(R.string.app_name_domoticz);
        String EMAIL = getString(R.string.domoticz_url);
        int PROFILE = R.drawable.ic_launcher;

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new NavigationAdapter(drawerActions, ICONS, NAME, EMAIL, PROFILE, this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    try {
                        searchViewAction.setQuery("", false);
                        searchViewAction.clearFocus();
                    } catch (Exception e) {}

                    try {
                        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                        selectedFragment = recyclerView.getChildPosition(child) - 1;
                        tx.replace(R.id.main, Fragment.instantiate(MainActivity.this, fragments[recyclerView.getChildPosition(child) - 1]));
                        tx.commitAllowingStateLoss();
                    } catch (Exception e) { }

                    invalidateOptionsMenu();
                    mDrawer.closeDrawer(Gravity.LEFT);

                    return true;
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        setupDrawer();
    }

    /**
     * Sets the drawer with listeners for open and closed
     */
    private void setupDrawer() {
        // final CharSequence currentTitle = getSupportActionBar().getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a mDrawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                try {
                    searchViewAction.clearFocus();
                } catch (Exception e) {
                }

                //getSupportActionBar().setTitle(R.string.drawer_navigation_title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a mDrawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(currentTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true); // hamburger menu icon
        mDrawer.setDrawerListener(mDrawerToggle); // attach hamburger menu icon to drawer
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public Fragment getVisibleFragment() {
        try {
            FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }

            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment f = getVisibleFragment();
        if (!(f instanceof DomoticzFragment)) {
            getMenuInflater().inflate(R.menu.menu_simple, menu);

        } else {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);

            MenuItem searchMenuItem = menu.findItem(R.id.search);
            searchViewAction = (SearchView) MenuItemCompat
                    .getActionView(searchMenuItem);

            searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Fragment n = getVisibleFragment();

                    if (n instanceof DomoticzFragment) {
                        ((DomoticzFragment) n).Filter(newText);
                    }
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }


    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    startActivityForResult(new Intent(this, SettingsActivity.class), this.iSettingsResultCode);
                    return true;
            }

            // Activate the navigation drawer toggle
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        catch(Exception ex)
        {}

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }
}