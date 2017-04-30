package com.example.snowiot.snowiotsimple;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class PlowedDrivewayPhotoRating extends AppCompatActivity {

    private TextView mPhotoUploadedBy;
    private ImageView mPlowedDrivewayPhoto;
    private RatingBar mRateJob;


    private StorageReference mDrivewayPhotoFolder = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plowed_driveway_photo_rating);

       final SharedPreferences sharedPreferences = getSharedPreferences("com.example.snowiot.snowiotsimple", MODE_PRIVATE);

        final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mUserRef = mRootRef.child("users/" + ((GlobalVariables) getApplication()).getUserUID()); //dynamic reference to requesthandle)

            mPlowedDrivewayPhoto = (ImageView) findViewById(R.id.drivewayFinishedPhoto);
        mPhotoUploadedBy = (TextView) findViewById(R.id.uploadedBy);
        mRateJob = (RatingBar) findViewById(R.id.snowPlowRating);

        mPhotoUploadedBy.setText("Service provided by: " + sharedPreferences.getString("lastJobDoneBy", "null"));

        mRateJob.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                mRootRef.child("users/" + ((GlobalVariables) getApplication()).getUserUID() + "/requesthandle/pendingSnowPlowRating").setValue("null");                 //Reset flag
                mRootRef.child("users/" + sharedPreferences.getString("jobTakenByUID", "null") + "/ratings/" + ((GlobalVariables) getApplication()).getUserUID()).setValue(rating);
                mUserRef.child("requesthandle/prompt").setValue(0);
                finish();

            }
        });
            downloadPlowedDrivewayPhoto();

        }

    public void downloadPlowedDrivewayPhoto(){

        String appUserUID = ((GlobalVariables) this.getApplication()).getUserUID();

        mDrivewayPhotoFolder.child("users/" + appUserUID + "/drivewayfinished.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.with(getApplicationContext()).load(uri).into(mPlowedDrivewayPhoto);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Download failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
