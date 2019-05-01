package com.fuelware.app.fw_manager.services;


import com.fuelware.app.fw_manager.network.FuelwareAPI;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyService<T, R>{

//    static Map<String, MyServiceListerner> uniqueIDList = new HashMap<>();

    private MyService(FuelwareAPI fuelwareInterface) {

    }

    public static  <T extends MyServiceListerner, R> void CallAPI2(Observable<R> observable, final T call) {

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<R>() {

                    @Override
                    public void onNext(R response) {
                        if (call != null)
                            call.onNext(response);
                    }

                    @Override
                    public void onComplete() {
                        if (call != null)
                            call.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (call != null)
                            call.onError(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (call != null)
                            call.onSubscribe(d);
                    }
                });

    }
    public static  <T extends MyServiceListerner, R> void CallAPI (Observable<R> observable, final T call) {

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<R>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (call != null)
                            call.onSubscribe(d);
                    }

                    @Override
                    public void onNext(R response) {
                        if (call != null)
                            call.onNext(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (call != null)
                            call.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        if (call != null)
                            call.onComplete();
                    }
                });

    }


}


