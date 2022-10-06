package com.securityandsafetythings.examples.helloworld.api;


import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIInterface {

    @POST("/")
    void createUser(@Body String rawCOde);
}