package com.example.snowiot.snowiotsimple;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    TextView mBaseHeight, mSnowThresholdTextDisplay;
    Button mRecalibrate, mRequestService, mSetSnowThreshold;
    Switch mToggleDisplayActualDepth, mEnableSnowWarnings, mShowUserOnMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mBaseHeight = (TextView) findViewById(R.id.baseheight);
        mSnowThresholdTextDisplay = (TextView) findViewById(R.id.snowThresholdValueDisplay);
        mRecalibrate = (Button) findViewById(R.id.recalibrate);
        mRequestService = (Button) findViewById(R.id.requestService);
        mSetSnowThreshold = (Button) findViewById(R.id.setSnowThreshold);
        mToggleDisplayActualDepth = (Switch) findViewById(R.id.snowdepthswitch);
        mEnableSnowWarnings = (Switch) findViewById(R.id.enableSnowWarning);
        mShowUserOnMap = (Switch) findViewById(R.id.showUserOnMap);

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE);
        mToggleDisplayActualDepth.setChecked(sharedPreferences.getBoolean("actualDepthSwitchPressed", true));
        mEnableSnowWarnings.setChecked(sharedPreferences.getBoolean("enableSnowWarningPressed", true));
        mShowUserOnMap.setChecked(sharedPreferences.getBoolean("enableServicesPressed", true));

        //Base Height Value Display
//        DatabaseReference mUserRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://snowtotals-68015.firebaseio.com/users/" + ((GlobalVariables) this.getApplication()).getUserUID());     //Do not place this outside of onStart or onCreate functions as the global variable crashes the app
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mUserRootRef = mRootRef.child("users/" + ((GlobalVariables) this.getApplication()).getUserUID());
        DatabaseReference mUserDrivewayRef = mRootRef.child("driveways/" + ((GlobalVariables) this.getApplication()).getUserUID());
        DatabaseReference mCalibrationValue = mUserRootRef.child("fixedHeight");

        final AlertDialog.Builder mEnableServicesAlert = new AlertDialog.Builder(this);

        mCalibrationValue.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float baseHeight = dataSnapshot.getValue(Float.class);
                mBaseHeight.setText(String.valueOf(baseHeight));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Switch for user to choose whether initial height measured from sensor will be deducted from height read, yielding height of snow rather than what sensor sees
        final DatabaseReference mDeductBaseHeight = mUserRootRef.child("deductBaseHeight");

        mToggleDisplayActualDepth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean actualDepthSwitchPressed) {

                if (actualDepthSwitchPressed) {
                    mDeductBaseHeight.setValue(1);
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                    editor.putBoolean("actualDepthSwitchPressed", true);
                    editor.commit();
                } else {
                    mDeductBaseHeight.setValue(0);
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                    editor.putBoolean("actualDepthSwitchPressed", false);
                    editor.commit();
                }

            }
        });

        final DatabaseReference mSnowWarningEnable = mUserRootRef.child("snowwarning/enableWarning");
        mEnableSnowWarnings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enableSnowWarningPressed) {

                if (enableSnowWarningPressed) {
                    mSnowWarningEnable.setValue(1);
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                    editor.putBoolean("enableSnowWarningPressed", true);
                    editor.commit();
                } else {
                    mSnowWarningEnable.setValue(0);
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                    editor.putBoolean("enableSnowWarningPressed", false);
                    editor.commit();
                }

            }
        });


        final DatabaseReference mAllowSnowPlowRequests = mUserDrivewayRef.child("status");
        mShowUserOnMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean enableServicesPressed) {

                if (enableServicesPressed) {

                    mEnableServicesAlert.setMessage("Warning: By enabling services snow plows will be able to track the status of your sensors. Proceed?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mAllowSnowPlowRequests.setValue(1);
                                    SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                                    editor.putBoolean("enableServicesPressed", true);
                                    editor.commit();
                                    dialog.dismiss();

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mShowUserOnMap.setChecked(false);
                                    dialog.dismiss();

                                }
                            })

                            .create();
                    mEnableServicesAlert.show();


                } else {
                    mAllowSnowPlowRequests.setValue(0);
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                    editor.putBoolean("enableServicesPressed", false);
                    editor.commit();
                }

            }
        });

        mRequestService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mEnableServicesAlert.setMessage("Are you sure that you want to request snow removal services?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mAllowSnowPlowRequests.setValue(2);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        })

                        .create();
                mEnableServicesAlert.show();
            }
        });

        final DatabaseReference mSnowThresholdDisplay = mUserRootRef.child("snowwarning/snowThreshold");
        mSnowThresholdDisplay.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSnowThresholdTextDisplay.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSetSnowThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = (LayoutInflater.from(Settings.this)).inflate(R.layout.setsnowthresholddialog, null);
                AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(Settings.this);
                AlertDialogBuilder.setView(view);
                final EditText mSnowThreshold = (EditText) view.findViewById(R.id.snowThresholdValue);

                AlertDialogBuilder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserRootRef.child("snowwarning/snowThreshold").setValue(Double.valueOf(String.valueOf(mSnowThreshold.getText())));
                        dialog.dismiss();
                    }
                });
                Dialog dialog = AlertDialogBuilder.create();
                dialog.show();
            }
        });


        //Set flag for microcontroller to get a new initial value and store on database
        final DatabaseReference mRecalibrateFlag = mUserRootRef.child("setNewBaseHeight");

        mRecalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecalibrateFlag.setValue(1);
            }
        });

    }

    protected void onStart() {
        super.onStart();


    }
}
