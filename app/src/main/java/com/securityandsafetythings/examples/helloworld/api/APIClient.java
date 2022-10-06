package com.securityandsafetythings.examples.helloworld.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {


        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.7.100")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        return retrofit;
    }

}