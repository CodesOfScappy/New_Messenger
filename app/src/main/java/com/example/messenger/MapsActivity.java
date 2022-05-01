package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MapsActivity extends AppCompatActivity {

    //initialize Variable
    EditText etsource, etdestination;
    Button btntrack, btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Assign variable
        etsource = findViewById(R.id.et_source);
        etdestination = findViewById(R.id.et_destination);
        btntrack = findViewById(R.id.btn_track);
        btnback = findViewById(R.id.btn_back);



        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        btntrack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                //get Value from edit text
                String sSource = etsource.getText().toString().trim();
                String sDestination = etdestination.getText().toString().trim();

                // check conditions
                if (sSource.equals("") && sDestination.equals(""))
                {
                    // When both value blank
                    Toast.makeText(getApplicationContext()
                            , "Enter both location", Toast.LENGTH_SHORT).show();
                } else {
                    // When both value fill
                    //Display Track
                    DisplayTrack(sSource, sDestination);
                }


            }
        });

    }

    private void DisplayTrack(String sSource, String sDestination)
    {
        // if the device does not have a map installed, then redirect to play store
        try{
            // when google maps installed
            // initialize uri
            Uri uri = Uri.parse("http://www.google.co.in/maps/dir/" + sSource + "/"
                    + sDestination);

            // initialize intent whit a action view
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            // set package
            intent.setPackage("com.google.android.apps.maps");
            // Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Start activity
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            //When google maps is not installed
            // Initialize uri
            Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            // Initialize intent whit action view
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            // Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Start activity
            startActivity(intent);
        }
    }
}
