package com.app.imageanalysis;

import static retrofit2.converter.gson.GsonConverterFactory.create;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.app.imageanalysis.BuildConfig;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    Button CameraButton;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraButton = (Button) findViewById(R.id.CameraButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        String ha = BuildConfig.API_KEY;

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    takePicture();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void takePicture() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher  = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()== RESULT_OK) {
                Bundle extras = result.getData().getExtras();
                Bitmap imgBitmap = (Bitmap) extras.get("data");


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                String imageEncoded = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    imageEncoded = Base64.getEncoder().encodeToString(b);
                }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .addConverterFactory(create(
                                new GsonBuilder().serializeNulls().create()
                        ))
                        .build();

                //ComputerVisionAPIService pokemonApiService = retrofit.create(PokemonAPIService.class);
                //Call call = pokemonApiService.getPokemons();

                System.out.println(imageEncoded);
                imageView.setImageBitmap(imgBitmap);


                /* -- BASE64 -> BITMAP. --
                Bitmap image = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    byte[] imageDecoded = Base64.getDecoder().decode(imageEncoded);
                    image = BitmapFactory.decodeByteArray(imageDecoded, 0, imageDecoded.length);
                }
                imageView.setImageBitmap(image);
                */
            }
        }
    });
}