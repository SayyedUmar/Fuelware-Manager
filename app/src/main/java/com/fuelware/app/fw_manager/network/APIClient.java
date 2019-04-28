package com.fuelware.app.fw_manager.network;

import com.fuelware.app.fw_manager.BuildConfig;
import com.fuelware.app.fw_manager.Const.AppConst;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;

    public static Retrofit getRetroClient(String url) {

        if (retrofit == null) {
            synchronized (APIClient.class) {
                if (retrofit == null) {
                    retrofit = getClient(AppConst.ROOT_URL);
                }
            }
        } else if (!retrofit.baseUrl().toString().equalsIgnoreCase(url)) {
            synchronized (APIClient.class) {
                MLog.e("old URL", retrofit.baseUrl().toString());
                MLog.e("newUrl URL", url);
                retrofit = getClient(url);
            }
        }
        return retrofit;
    }

//    private static OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//
//
//            Request.Builder ongoing = chain.request().newBuilder();
//
//            //Log.d("printauthkeys","****   "+Idea_Urban.getInstance().get_Authkeyvalues());
//
//            ongoing.addHeader("auth_key","201905Awgvv9gYkN75aa34b9c");
//            ongoing.addHeader("Content-Type", "application/xml");
//
//            return chain.proceed(ongoing.build());
//        }
//    }).build();


    private static Retrofit getClient(String url) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
//                .cache(new Cache(new File(App.getContext().getCacheDir(), "http"), 1024 * 1024 * 10))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            okHttpBuilder.addInterceptor(loggingInterceptor);
        }

        return new Retrofit.Builder()
                .client(okHttpBuilder.build()).baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static FuelwareAPI getApiService(String url) {
        return getRetroClient(url).create(FuelwareAPI.class);
    }

    public static FuelwareAPI getApiService() {
        return getApiService(AppConst.ROOT_URL);
    }
}


