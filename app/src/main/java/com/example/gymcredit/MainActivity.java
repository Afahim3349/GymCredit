package com.example.gymcredit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Place.Field> placeFieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.TYPES);
    PlacesClient placesClient;
    Button wya;
    TextView display, credit;
    private String placeId = "";
    private List<Place.Type> type;
    private int bank = 0;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        requestPermissions();

        wya = (Button) findViewById(R.id.b_wyabb);
        display = (TextView) findViewById(R.id.tv_currentPlace);
        credit = (TextView) findViewById(R.id.tv_credit);

        database = FirebaseDatabase.getInstance();
        mRef = database.getReference("User");

        initPlaces();

        wya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentPlace();
            }
        });
    }

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.INTERNET
//                }, 10);
//                return;
//            }
//        }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 10:
//                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//        }
//    }

    private void getCurrentPlace() {
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFieldList).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<FindCurrentPlaceResponse> placeResponseTask = placesClient.findCurrentPlace(request);
        placeResponseTask.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                FindCurrentPlaceResponse res = task.getResult();
                //Need to compare likelihoods (sort list) so we can get most likely
                if (res != null) {
                    Collections.sort(res.getPlaceLikelihoods(), new Comparator<PlaceLikelihood>() {
                        @Override
                        public int compare(PlaceLikelihood o1, PlaceLikelihood o2) {
                            return Double.compare(o1.getLikelihood(), o2.getLikelihood());
                        }
                    });
                    //post sort, just reverse list to get most likely result
                    Collections.reverse(res.getPlaceLikelihoods());
                    placeId = res.getPlaceLikelihoods().get(0).getPlace().getId();
                    type = res.getPlaceLikelihoods().get(0).getPlace().getTypes();
                    if (type.toString().equals("GYM")) {
                        mRef.child("Credit").setValue(1);
                    } else {
                        Toast.makeText(MainActivity.this, "Failure at comparator", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initPlaces() {
        Places.initialize(MainActivity.this, this.getString(R.string.places_api_key));
        placesClient = Places.createClient(this);
    }
}


