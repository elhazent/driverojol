package com.elhazent.picodiploma.online.network;


import com.elhazent.picodiploma.online.model.ResponseBooking;
import com.elhazent.picodiploma.online.model.ResponseDetailDriver;
import com.elhazent.picodiploma.online.model.ResponseHistory;
import com.elhazent.picodiploma.online.model.ResponseLoginRegis;
import com.elhazent.picodiploma.online.model.ResponseWaitingDriver;
import com.elhazent.picodiploma.online.model.ResponseWaypoint;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestApi {
    //    //todo 2 set endpoint di api.php
//
//    //    //endpoint untuk register
    @FormUrlEncoded
    @POST("daftar")
    Call<ResponseLoginRegis> registerUser(
            @Field("nama") String strnama,
            @Field("phone") String strphone,
            @Field("email") String stremail,
            @Field("password") String strpassword
    );

    @FormUrlEncoded
    @POST("login")
    Call<ResponseLoginRegis> loginUser(
            @Field("device") String DEVICE,
            @Field("f_password") String password,
            @Field("f_email") String stremail
    );
//
    @FormUrlEncoded
    @POST("checkBooking")
    Call<ResponseWaitingDriver> cekStatusOrder(
            @Field("idbooking") String idbooking

    );

    @FormUrlEncoded
    @POST("get_driver")
    Call<ResponseDetailDriver> detailDriver(
            @Field("f_iddriver") String f_iddriver

    );
    @FormUrlEncoded
    @POST("get_booking")
    Call<ResponseHistory> getDataHistory(
            @Field("f_token") String token,
            @Field("f_device") String device,
            @Field("status") String status,
            @Field("f_idUser") String iduser
    );

    @FormUrlEncoded
    @POST("cancel_booking")
    Call<ResponseWaitingDriver> cancelBooking(
            @Field("idbooking") String idbooking,
            @Field("f_device") String device,
            @Field("f_token") String token
    );

    @FormUrlEncoded
    @POST("insert_booking")
    Call<ResponseBooking> insertBooking(
            @Field("f_device") String DEVICE,
            @Field("f_token") String token,
            @Field("f_jarak") Float jarak,
            @Field("f_idUser") String iduser,
            @Field("f_latAwal") String latwaal,
            @Field("f_lngAwal") String lonawal,
            @Field("f_awal") String awal,
            @Field("f_latAkhir") String latakhir,
            @Field("f_lngAkhir") String lonakhir,
            @Field("f_akhir") String akhir,
            @Field("f_catatan") String catatan
    );

    @GET("json")
    Call<ResponseWaypoint> setRute(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String key
    );
}