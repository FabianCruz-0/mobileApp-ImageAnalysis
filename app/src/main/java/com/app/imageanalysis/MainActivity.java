package com.app.imageanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button CameraButton;
    ImageView imageView;
    TextView textViewResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy gfgPolicy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(gfgPolicy);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraButton = findViewById(R.id.CameraButton);
        imageView = findViewById(R.id.imageView);
        textViewResultado = findViewById(R.id.textViewResultado);

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

                String subscriptionKey = BuildConfig.API_KEY;
                String endpoint = BuildConfig.BASE_URL;

                ComputerVisionClient compVisClient = Authenticate(subscriptionKey, endpoint);

                try {
                    List<VisualFeatureTypes> featuresToExtractFromRemoteImage = new ArrayList<>();
                    featuresToExtractFromRemoteImage.add(VisualFeatureTypes.TAGS);
                    ImageAnalysis analysis = compVisClient.computerVision().analyzeImageInStream().withImage(b)
                            .withVisualFeatures(featuresToExtractFromRemoteImage).withLanguage("es").execute();

                    ImageTag primerTag = analysis.tags().get(0);
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    float seguridad = (float) (primerTag.confidence()*100);
                    float seguridadFixed = Float.parseFloat(df.format(seguridad));

                    if(imgBitmap.getWidth()>imgBitmap.getHeight())
                    {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap imagenRotada =  Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.getWidth(), imgBitmap.getHeight(), matrix, true);
                        imageView.setImageBitmap(imagenRotada);
                    }
                    else {
                        imageView.setImageBitmap(imgBitmap);
                    }
                    textViewResultado.setText("OBJETO: "+primerTag.name()+ " SEGURIDAD: "+seguridadFixed);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }


                /*
                String imageEncoded = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imageEncoded = Base64.getEncoder().encodeToString(b);
                }
                 -- BASE64 -> BITMAP. --
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

    private ComputerVisionClient Authenticate(String subscriptionKey, String endpoint) {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }
}