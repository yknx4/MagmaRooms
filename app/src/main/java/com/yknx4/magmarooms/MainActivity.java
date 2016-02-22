package com.yknx4.magmarooms;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_ALL = 3;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Toolbar toolbar;
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
    private CoordinatorLayout mCoordinatorLayour;
    private PermissionUtil.PermissionRequestObject mRequestObject;
    private CalendarFetcher mCalendarFetcher;
    private ImageView imgLogo;
    private TextView txtTitle;
    private TextClock txtTime;
    private TextView txtPreviousEvent;
    private TextView txtTitleCurrentEvent;
    private TextView txtEventTitle;
    private ImageView imageView2;
    private TextView textView2;
    private TextView txtNextEvent;
    private TextView txtNextEventTime;
    private TextView txtLateEvent;
    private TextView txtLateEventTime;
    private TextView txt_current_event_time;
    private ImageView icon_current;
    private ImageView icon_next;
    private ImageView icon_late;


    private void showSnackBar(String text){
        Snackbar.make(mCoordinatorLayour,text,Snackbar.LENGTH_LONG).show();
    }
    private void showSnackBar(@StringRes int text){
        showSnackBar(getString(text));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPermissionsForMarshmallow();
        setupObjects();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar();
        setSupportActionBar(toolbar);
        mCoordinatorLayour = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mCalendarFetcher = new CalendarFetcher(mCredential);

    }

    private void setupObjects() {
        imgLogo = (ImageView) findViewById(R.id.img_logo);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTime = (TextClock) findViewById(R.id.txt_time);
        txtPreviousEvent = (TextView) findViewById(R.id.txt_previous_event);
        txtTitleCurrentEvent = (TextView) findViewById(R.id.txt_title_current_event);
        txtEventTitle = (TextView) findViewById(R.id.txt_event_title);
        txt_current_event_time = (TextView) findViewById(R.id.textView2);
        txtNextEvent = (TextView) findViewById(R.id.txt_next_event);
        txtNextEventTime = (TextView) findViewById(R.id.txt_next_event_time);
        txtLateEvent = (TextView) findViewById(R.id.txt_late_event);
        txtLateEventTime = (TextView) findViewById(R.id.txt_late_event_time);
        icon_current = (ImageView) findViewById(R.id.img_time_current);
        icon_late = (ImageView) findViewById(R.id.img_time_late);
        icon_next = (ImageView) findViewById(R.id.img_time_next);
    }

    private void setupPermissionsForMarshmallow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            firstRunTutorial();
            mRequestObject = PermissionUtil.with(this).request(Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.ACCOUNT_MANAGER,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
            ).onAllGranted(
                    new Func() {
                        @Override
                        protected void call() {
                            MainActivity.this.showSnackBar(R.string.txt_permissions_granted);
                            //Happy Path
                        }
                    }).onAnyDenied(
                    new Func() {
                        @Override
                        protected void call() {
                            //Sad Path
                            MainActivity.this.showSnackBar(R.string.txt_permissions_not_granted);
                            finish();
                        }
                    }).ask(REQUEST_CODE_ALL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG,"Refreshing results");
                    refreshResults();
                }
            }, 0, 300000);
        } else {
            showSnackBar("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }

    }

    private void setupToolbar() {



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    showSnackBar("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCalendarFetcher).execute(Constants.MAGMA_CALENDAR_ID);
            } else {
                showSnackBar("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                MainActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */

    private void setFreeBackground(boolean isFree){
        int newColor;
        if(isFree){
            newColor = getResources().getColor(R.color.camo_green);
        }
        else {
            newColor = getResources().getColor(R.color.faded_red);
        }
        toolbar.setBackgroundColor(newColor);
        findViewById(R.id.main_back).setBackgroundColor(newColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(newColor);
        }

    }
    private class MakeRequestTask extends CalendarAsyncTask {


        public MakeRequestTask(CalendarFetcher calendarFetcher) {
            super(calendarFetcher);
        }

        @Override
        protected void onPreExecute() {
//            mOutputText.setText(");
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<Event> output) {

            if(output.size()==0){
                showSnackBar("No events found on the Calendar");
            }

            EventHelper eh = new EventHelper(output);
            Cleaner cl;

            cl = Cleaner.with(eh.getCurrentEvent());

            txtEventTitle.setText(cl.getCurrentEventName());
            txt_current_event_time.setText(cl.getTimeString());
            icon_current.setVisibility(eh.hasCurrent()? View.VISIBLE:View.GONE);


            cl = Cleaner.with(eh.getPreviousEvent());
            txtPreviousEvent.setText(String.format(getString(R.string.txt_previous_event), cl.getEventName()));

            cl = Cleaner.with(eh.getNextEvent());
            txtNextEvent.setText(String.format(getString(R.string.string_coming_next), cl.getEventName()));
            txtNextEventTime.setText(cl.getTimeString());
            icon_next.setVisibility(eh.hasNext()?View.VISIBLE:View.GONE);

            cl = Cleaner.with(eh.getLateEvent());
            txtLateEvent.setText(String.format(getString(R.string.txt_late_today), cl.getEventName()));
            txtLateEventTime.setText(cl.getTimeString());
            icon_late.setVisibility(eh.hasLate()?View.VISIBLE:View.GONE);
            setFreeBackground(!eh.hasCurrent());

            }

        @Override
        protected void onCancelled() {
//            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    showSnackBar("The following error occurred:\n"
                            + mLastError.getMessage());
                    Log.d(TAG,mLastError.getMessage());
                }
            } else {
                showSnackBar("Request cancelled.");
            }
        }



    }
}
