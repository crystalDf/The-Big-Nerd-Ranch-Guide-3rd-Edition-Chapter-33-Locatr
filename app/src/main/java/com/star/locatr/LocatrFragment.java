package com.star.locatr;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LocatrFragment extends Fragment {

    private static final String TAG = "LocatrFragment";

    private static final int REQUEST_CODE = 0;

    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private GoogleApiClient mGoogleApiClient;

    public static LocatrFragment newInstance() {
        return new LocatrFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().supportInvalidateOptionsMenu();
                        Log.i(TAG, "Connected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i(TAG, "Connection Failed");
                    }
                })
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_locatr, container, false);

        mImageView = (ImageView) view.findViewById(R.id.image);

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_progress_bar);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().supportInvalidateOptionsMenu();

        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_locatr, menu);

        MenuItem menuItem = menu.findItem(R.id.action_locate);
        menuItem.setEnabled(mGoogleApiClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                findImage();
                showProgressBar(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findImage() {

        List<String> permissionList = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(),
                    permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE);
        } else {
            doFindImage();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getActivity(), "Some permission denied. ",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    doFindImage();
                }
                break;
            default:
                break;
        }

    }

    private void doFindImage() {

        try {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setNumUpdates(1);
            locationRequest.setInterval(0);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "Got a fix: " + location);
                            new SearchTask().execute(location);
                        }
                    });
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    private class SearchTask extends AsyncTask<Location, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Location... params) {

            return new FlickrFetchr().searchPhotos(params[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {

            if (!items.isEmpty()) {
                bindGalleryItem(items.get(0));
            }

            showProgressBar(false);
        }
    }

    private void showProgressBar(boolean isShown) {
        if (isShown) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void bindGalleryItem(GalleryItem galleryItem) {
        Picasso.with(getActivity())
                .load(galleryItem.getUrl())
                .placeholder(R.drawable.emma)
                .into(mImageView);
    }
}
