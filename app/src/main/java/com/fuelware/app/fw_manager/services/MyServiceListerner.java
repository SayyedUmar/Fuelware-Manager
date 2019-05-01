package com.fuelware.app.fw_manager.services;


import io.reactivex.disposables.Disposable;

public interface MyServiceListerner<T> {
    public void onNext(T t);

    public void onError(Throwable e);

//    void onError(ResponseBody e);

    public void onComplete();

    default void onSubscribe(Disposable d) {}

}
