package com.example.firebasemlkittestingtext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView display;
    private ImageView imageView;
    Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int GALLERY_REQUEST_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button select = findViewById(R.id.img_btn);
        Button find_text_btn = findViewById(R.id.find_text_btn);
        Button choose_img_btn = findViewById(R.id.choose_img_btn);

        display = findViewById(R.id.sample_text);
        imageView = findViewById(R.id.imageView);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();

            }
        });

        find_text_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();

            }
        });

        choose_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create an Intent with action as ACTION_PICK
                Intent intent=new Intent(Intent.ACTION_PICK);
                // Sets the type as image/*. This ensures only components of type image are selected
                intent.setType("image/*");
                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                // Launching the Intent
                startActivityForResult(intent,GALLERY_REQUEST_CODE);
            }
        });


    }

    private void detectTextFromImage() {
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);

        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImgae(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Text Recognition failed!", Toast.LENGTH_SHORT).show();
                Log.d("Error: ", e.getMessage());
            }
        });

    }

    private void displayTextFromImgae(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if(blockList.size() ==0){
            Toast.makeText(this, "No Text Found!", Toast.LENGTH_SHORT).show();
        }
        for(FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()){
            String text = block.getText();
            display.setText(display.getText()+" "+text);
        }

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            display.setText("");
        }else if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            display.setText("");
            Uri selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, selectedImage.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
