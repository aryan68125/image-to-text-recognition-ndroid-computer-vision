package com.aditya.textrecognitionfromimage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class MainActivity extends AppCompatActivity {

    ImageView image_view;
    TextView text_display;
    Button capture_image_button;
    Button detect_text_image_button;

    //this method handles the image capture using host device camera
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image_view  = findViewById(R.id.image_view);
        text_display = findViewById(R.id.text_display);
        capture_image_button = findViewById(R.id.capture_image_button);
        detect_text_image_button = findViewById(R.id.detect_text_image_button);

        //handling capture image button
        //this button is responsible for opening the camera app in of the host device
        capture_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    //this method handles the image capture using host device camera
    private void dispatchTakePictureIntent() {
        //allow the user to capture image using phone's camera application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    //this method will get the image thumbnail from the camera application
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image_view.setImageBitmap(imageBitmap);

            //Now here text recognition from image will come
            //step 1. To create a FirebaseVisionImage object from a Bitmap object:
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
            //step 2. get an instance of firebaseVision
            FirebaseVision firebaseVision = FirebaseVision.getInstance();
            //step 3. Create an instance of firebaseVisionTextRecognizer
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
            //step 4. create a task to process an image
            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
            //step 5. if the task is sucessfull then display the text in the image else display error message

            //task if success ful
            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                //the text which is extracted will be available in this argument of onSuccess
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                     //now we can finally get the text from the firebaseVisionText
                    String text = firebaseVisionText.getText();
                    text_display.setText(text);
                }
            });

            //task on failure
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                }
            });
        }
    }
}