package com.mohitsharma.letschat.Notifications;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA0TnC4dA:APA91bFbvkU4ReYFrNtn2cn0-870bZPm9VoNJRxLe9jvKkcIh9wbAbhyLr9h9OvSgrwQFjdFgIYJoqU8H5aeTaseTRI4oROOo0GnTBEBbrRxXb1qetSoBsAHfLUvfQfr4VGoA1tBrppC"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
