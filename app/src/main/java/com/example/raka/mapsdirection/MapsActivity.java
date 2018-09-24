package com.example.raka.mapsdirection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raka.mapsdirection.model.Distance;
import com.example.raka.mapsdirection.model.LegsItem;
import com.example.raka.mapsdirection.model.ResponseRoute;
import com.example.raka.mapsdirection.network.ConfigRetrofit;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.tv_pickUp)
    TextView tvPickUp;
    @BindView(R.id.tv_destination)
    TextView tvDestination;
    @BindView(R.id.tvDistance)
    TextView tvDistance;
    @BindView(R.id.infoPanel)
    LinearLayout infoPanel;
    private GoogleMap mMap;
    //variable constant untuk input location awal dan location akhir untuk place auto complete
    public static final int locAwal = 1;
    public static final String TAG = "TAG";
    public static final int locAkhir = 2;
    int REQUEST_CODE;
    //variable dari latlng location awal dan akhir
    LatLng initialLat, endLat = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "Need permission to access location", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }
        }else{
            Toast.makeText(this, "Accessing google map ", Toast.LENGTH_SHORT).show();
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

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-6.1953083, 106.7948503);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // for ActivityCompat#requestPermissions for more details.
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            return;
        }
        mMap.setPadding(10,180,10,10);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

    }

    @OnClick({R.id.tv_pickUp, R.id.tv_destination})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_pickUp:
                pickUpLocation(locAwal);
                break;
            case R.id.tv_destination:
                pickUpLocation(locAkhir);
                break;
        }
    }

    private void pickUpLocation(int type){
            REQUEST_CODE = type;
        AutocompleteFilter filter = new AutocompleteFilter.Builder().setCountry("ID").build();
        try{
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(filter).build(this);
            startActivityForResult(intent, REQUEST_CODE);
        }catch (GooglePlayServicesNotAvailableException e){
            Log.d(TAG, e.getMessage());
        }catch (GooglePlayServicesRepairableException e){
            Log.d(TAG, "pickUpLoc:"+e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //get location from auto complete
        if (resultCode == RESULT_OK){

            Place place = PlaceAutocomplete.getPlace(this,data);
            // cek data location is empty or not
            if (place.isDataValid()){

                Log.d(TAG, place.toString());
                String placeAddress =  place.getAddress().toString();
                LatLng placeLatLang = place.getLatLng();
                switch (REQUEST_CODE){
                    //get initial loc and latlang
                    case locAwal:
                        tvPickUp.setText(placeAddress);
                        initialLat = place.getLatLng();
                        break;
                        // get end loc and latlang
                    case locAkhir:
                        tvDestination.setText(placeAddress);
                        endLat = place.getLatLng();
                        break;
                }
                if (initialLat != null && endLat != null){
                    actionRoute(placeLatLang, REQUEST_CODE);
                }
            }else {
                Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void actionRoute(LatLng placeLatLang, int request_code) {
        // tampung ke var untuk ambil lat long dari loc awal dan akhir
        final String start = initialLat.latitude+","+initialLat.longitude;
        String end = endLat.latitude+","+endLat.longitude;
        mMap.clear();
        ConfigRetrofit.getInstance().route(start,end,BuildConfig.URL_MAP).enqueue(new Callback<ResponseRoute>() {
            @Override
            public void onResponse(Call<ResponseRoute> call, Response<ResponseRoute> response) {
                if (response.isSuccessful()){
                    if (response.body() != null) {
                        //get json data dari response route dengan cara response.body()
                        ResponseRoute dataRoute = response.body();
                        //get array legs

                        LegsItem dataLegs = dataRoute.getRoutes().get(0).getLegs().get(0);
                        //get polyline
                        String polylinePoint = dataRoute.getRoutes().get(0).getOverviewPolyline().getPoints();

                        //buat grais polyline dan lebarnya dan color polylinenya
                        List<LatLng> decodePath = PolyUtil.decode(polylinePoint);
                        mMap.addPolyline(new PolylineOptions().addAll(decodePath).width(8f)
                                .color(Color.argb(225, 56, 167, 250)));
                        mMap.addMarker(new MarkerOptions().position(initialLat).title("Start location"));

                        mMap.addMarker(new MarkerOptions().position(endLat).title("End location"));

                        Distance distance = dataLegs.getDistance();
                        distance.getValue();
                        tvDistance.setText(distance.getText());
                        // buat layarnya supaya ditengah koordinat
                        LatLngBounds.Builder llang = new LatLngBounds.Builder();
                        llang.include(initialLat);
                        llang.include(endLat);
                        //bound dari coordinatnya
                        LatLngBounds bounds = llang.build();
                        int width = getResources().getDisplayMetrics().widthPixels;
                        int height = getResources().getDisplayMetrics().heightPixels;
                        int paddingMap = (int) (width * 0.2);

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, paddingMap);
                        mMap.animateCamera(cameraUpdate);
                        infoPanel.setVisibility(View.VISIBLE);
                        mMap.setPadding(10, 180, 10, 180);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseRoute> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
