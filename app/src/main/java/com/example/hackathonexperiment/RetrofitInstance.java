package com.example.hackathonexperiment;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    public static Retrofit retrofit = new Retrofit.Builder().
            baseUrl("").addConverterFactory(GsonConverterFactory.create()).build();
}
