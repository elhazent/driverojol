package com.elhazent.picodiploma.online;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elhazent.picodiploma.online.helper.DirectionMapsV2;
import com.elhazent.picodiploma.online.helper.GPSTracker;
import com.elhazent.picodiploma.online.helper.HeroHelper;
import com.elhazent.picodiploma.online.helper.MyContants;
import com.elhazent.picodiploma.online.helper.SessionManager;
import com.elhazent.picodiploma.online.model.Distance;
import com.elhazent.picodiploma.online.model.Duration;
import com.elhazent.picodiploma.online.model.LegsItem;
import com.elhazent.picodiploma.online.model.ResponseBooking;
import com.elhazent.picodiploma.online.model.ResponseWaypoint;
import com.elhazent.picodiploma.online.model.RoutesItem;
import com.elhazent.picodiploma.online.network.InitRetrofit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoRideActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.imgpick)
    ImageView imgpick;
    @BindView(R.id.lokasiawal)
    TextView lokasiawal;
    @BindView(R.id.lokasitujuan)
    TextView lokasitujuan;
    @BindView(R.id.edtcatatan)
    EditText edtcatatan;
    @BindView(R.id.txtharga)
    TextView txtharga;
    @BindView(R.id.txtjarak)
    TextView txtjarak;
    @BindView(R.id.txtdurasi)
    TextView txtdurasi;
    @BindView(R.id.requestorder)
    Button requestorder;
    @BindView(R.id.rootlayout)
    RelativeLayout rootlayout;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private GPSTracker gpsTracker;
    private double latawal;
    private double lonawal;
    private String namelocation;
    private List<RoutesItem> dataMap;
    private List<LegsItem> legs;
    private Distance jarak;
    private Duration durasi;
    private SessionManager session;
    double latakhir;
    double lonakhir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FirebaseApp.initializeApp(this);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cekStatusGps();
    }

    private void cekStatusGps() {
        // cek sttus gps aktif atau tidak
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps already enabled", Toast.LENGTH_SHORT).show();
            //     finish();
        }
        // Todo Location Already on  ... end
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            //menampilkan popup untuk mengaktifkan gps
            enableLoc();
        }
    }

    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(GoRideActivity.this, MyContants.REQUEST_LOCATION);

                                finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gpsTracker = new GPSTracker(this);
        getMyLocation();
    }

    //    todo get posisi kita
    private void getMyLocation() {
        if (gpsTracker.canGetLocation()) {
            latawal = gpsTracker.getLatitude();
            lonawal = gpsTracker.getLongitude();
            namelocation = posisiku(latawal, lonawal);
            lokasiawal.setText(namelocation);
            //buat objek untuk mengatur tampilan map
            LatLng lokasiku = new LatLng(latawal, lonawal);
            mMap.addMarker(new MarkerOptions().position(lokasiku).title(namelocation))
                    .setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiku, 17));
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private String posisiku(double latawal, double lonawal) {
        namelocation = null;
        Geocoder geocoder = new Geocoder(GoRideActivity.this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(latawal, lonawal, 1);
            if (list != null && list.size() > 0) {
                namelocation = list.get(0).getAddressLine(0) + "" + list.get(0).getCountryName();

                //fetch data from addresses
            } else {
                Toast.makeText(GoRideActivity.this, "kosong", Toast.LENGTH_SHORT).show();
                //display Toast message
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return namelocation;
    }

    @OnClick({R.id.imgpick, R.id.lokasiawal, R.id.lokasitujuan, R.id.requestorder})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgpick:
                break;
            case R.id.lokasiawal:
                setLokasi(MyContants.LOKASIAWAL);
                break;
            case R.id.lokasitujuan:
                setLokasi(MyContants.LOKASITUJUAN);
                break;
            case R.id.requestorder:
                prosesRequest();
                break;
        }
    }

    private void prosesRequest() {
        session = new SessionManager(this);
        String devicestr = HeroHelper.getDeviceUUID(this);
        String tokenstr = session.getToken();
        String iduserstr = session.getIdUser();
        String ltawalstr = String.valueOf(latawal);
        String loawalstr = String.valueOf(lonawal);
        String ltakhirstr = String.valueOf(latakhir);
        String loakhirstr = String.valueOf(lonakhir);
        String awalstr = lokasiawal.getText().toString();
        String akhirstr = lokasitujuan.getText().toString();
        Float jarakstr = Float.parseFloat(HeroHelper.removeLastChar(txtjarak.getText().toString()));
        String catatanstr = edtcatatan.getText().toString();

        InitRetrofit.getInstance().insertBooking(devicestr, tokenstr, jarakstr, iduserstr, ltawalstr, loawalstr, awalstr,ltakhirstr,loakhirstr, akhirstr, catatanstr)
                .enqueue(new Callback<ResponseBooking>() {
                    @Override
                    public void onResponse(Call<ResponseBooking> call, Response<ResponseBooking> response) {
                        if (response.isSuccessful()){
                            String result = response.body().getResult();
                            String msg = response.body().getMsg();
                            if (result.equals("true")){
                                Toast.makeText(GoRideActivity.this, msg, Toast.LENGTH_SHORT).show();
                                int id_booking = response.body().getIdBooking();
                                Intent i = new Intent(GoRideActivity.this,WaitingDriverActivity.class);
                                i.putExtra(MyContants.IDBOOKING,id_booking);
                                startActivity(i);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBooking> call, Throwable t) {

                    }
                });
    }

    private void setLokasi(int lokasi) {
        AutocompleteFilter filter = new AutocompleteFilter.Builder().
                setCountry("ID")
                .build();

        Intent i = null;
        try {
            i = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(filter)
                    .build(GoRideActivity.this);
            startActivityForResult(i, lokasi);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MyContants.LOKASIAWAL && resultCode == RESULT_OK) {
            Place p = PlaceAutocomplete.getPlace(this, data);
            latawal = p.getLatLng().latitude;
            lonawal = p.getLatLng().longitude;
            LatLng awal = new LatLng(latawal, lonawal);
            mMap.clear();
            namelocation = p.getAddress().toString();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(awal, 17));
            lokasiawal.setText(namelocation);
        } else if (requestCode == MyContants.LOKASITUJUAN && resultCode == RESULT_OK) {
            Place p = PlaceAutocomplete.getPlace(this, data);
            latakhir = p.getLatLng().latitude;
            lonakhir = p.getLatLng().longitude;
            LatLng akhir = new LatLng(latakhir, lonakhir);
            mMap.clear();
            namelocation = p.getAddress().toString();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(akhir, 17));
            lokasitujuan.setText(namelocation);
            aksesRute();
        }
    }

    private void aksesRute() {
        String key = getString(R.string.google_maps_key);
        InitRetrofit.getInstanceGoogle().setRute(
                lokasiawal.getText().toString(),
                lokasitujuan.getText().toString(),
                key
        ).enqueue(new Callback<ResponseWaypoint>() {
            @Override
            public void onResponse(Call<ResponseWaypoint> call, Response<ResponseWaypoint> response) {
                if (response.isSuccessful()) {
                    String status = response.body().getStatus();
                    if (status.equals("OK")) {
                        dataMap = response.body().getRoutes();
                        legs = dataMap.get(0).getLegs();
                        jarak = legs.get(0).getDistance();
                        durasi = legs.get(0).getDuration();
                        double harga = Double.parseDouble(
                                HeroHelper.removeLastChar(jarak.getText())) * 10000;
                        txtjarak.setText(jarak.getText());
                        txtharga.setText(String.valueOf(harga));
                        txtdurasi.setText(durasi.getText());
                        DirectionMapsV2 mapsV2 = new DirectionMapsV2(GoRideActivity.this);
                        String points = dataMap.get(0).getOverviewPolyline().getPoints();
                        mapsV2.gambarRoute(mMap, points);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseWaypoint> call, Throwable t) {
                Toast.makeText(GoRideActivity.this, "gagal" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
