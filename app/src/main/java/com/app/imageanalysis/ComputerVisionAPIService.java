package com.app.imageanalysis;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ComputerVisionAPIService {

    @POST("analyze?visualFeatures=Categories&language=en&model-version=latest")
    Call<Void> getCategories(
            @Header("Ocp-Apim-Subscription-Key") String key,
            @Body RequestBody image
    );
}
