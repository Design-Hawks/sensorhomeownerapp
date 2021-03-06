package com.example.snowiot.snowiotsimple;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class UserContactedWindow extends AppCompatActivity {


    /**
     * Area dedicated to notification experiment
     */

    NotificationManager notificationManager;

    boolean notificationActive = false;

    int notificationID = 1;


    /**
     * End of notification test
     */

    private Button mAcceptService, mDeclineService, mNotificationTest;
    private TextView mContacterName, mContactorRating;
    private ImageView mContactorPhoto;
    private Driveways holdContacterInfo;

    private StorageReference mDrivewayPhotoFolder = FirebaseStorage.getInstance().getReference();

    String contactorUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_contacted_window);

        mAcceptService = (Button) findViewById(R.id.hireContacter);
        mDeclineService = (Button) findViewById(R.id.declineContacter);
//        mNotificationTest = (Button) findViewById(R.id.notifytest);
        mContacterName = (TextView) findViewById(R.id.contacterName);
        mContactorRating = (TextView) findViewById(R.id.contactorRating);
        mContactorPhoto = (ImageView) findViewById(R.id.snowPlowPhoto);

        final DatabaseReference mUserHandleRef, mContactorInfoRef, mRootRef, mContactorHandleRef, mSnowPlowRating;

        mRootRef = FirebaseDatabase.getInstance().getReference();

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE);

        final DatabaseReference mUserInfoRef = mRootRef.child("driveways/" + ((GlobalVariables) this.getApplication()).getUserUID());
//        mUserHandleRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://snowtotals-68015.firebaseio.com/users/" + ((GlobalVariables) this.getApplication()).getUserUID() + "/requesthandle");
//        mContactorHandleRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://snowtotals-68015.firebaseio.com/users/" + ((GlobalVariables) this.getApplication()).getContacterUID() + "/requesthandle");
//        mContactorInfoRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://snowtotals-68015.firebaseio.com/driveways/" + ((GlobalVariables) this.getApplication()).getContacterUID());
        mUserHandleRef = mRootRef.child("users/" + ((GlobalVariables) getApplication()).getUserUID() + "/requesthandle");
        mContactorHandleRef = mRootRef.child("users/" + ((GlobalVariables) this.getApplication()).getContacterUID() + "/requesthandle");
        mContactorInfoRef = mRootRef.child("driveways/" + ((GlobalVariables) this.getApplication()).getContacterUID());
        mSnowPlowRating = mRootRef.child("users/" + ((GlobalVariables) this.getApplication()).getContacterUID() + "/ratings");

        mSnowPlowRating.addListenerForSingleValueEvent(new ValueEventListener() {

            float f = 0.0f;
            int count = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot dataSnapshot1 : dataSnapshots) {

                    f = f + dataSnapshot1.getValue(Float.class);
                    count = count + 1;
                }

                mContactorRating.setText("Snowplow rating: " + Float.toString(f/count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mContactorInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holdContacterInfo = dataSnapshot.getValue(Driveways.class);

                contactorUID = dataSnapshot.getKey();

                mContacterName.setText("Provider: " + holdContacterInfo.getName());
                mContacterName.setTextSize(18);
                mContacterName.setTextColor(Color.BLACK);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAcceptService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserInfoRef.child("status").setValue(3);                             //Turn user marker on map blue
                mUserHandleRef.child("prompt").setValue(0);             //If user accepts offer then reset prompt that triggers notification
                mUserHandleRef.child("jobAssignedToUID").setValue(((GlobalVariables) getApplication()).getContacterUID());
                mContactorHandleRef.child("jobDeliveredToUID").setValue(((GlobalVariables) getApplication()).getUserUID());

                SharedPreferences.Editor editor = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE).edit();
                editor.putString("jobTakenBy", holdContacterInfo.getName());
                editor.putString("jobTakenByUID", contactorUID);
                editor.commit();
                finish();
            }
        });


        mDeclineService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserHandleRef.child("prompt").setValue(0);             //If user declines offer then reset prompt that triggers notification
                mUserHandleRef.child("jobAssignedToUID").setValue("null");
                mContactorHandleRef.child("jobDeliveredToUID").setValue("null");
                finish();
            }
        });


        downloadSnowPlowPhoto();

    }


    public void downloadSnowPlowPhoto() {

        String userUID = ((GlobalVariables) this.getApplication()).getContacterUID();

        mDrivewayPhotoFolder.child("users/" + userUID + "/profiledriveway.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.with(getApplicationContext()).load(uri).into(mContactorPhoto);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });


    }

}
