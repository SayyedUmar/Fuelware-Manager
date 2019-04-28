package com.fuelware.app.fw_manager.network;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {

    public static final String ADD_ACTION = "ADD";
    public static final String EDIT_ACTION = "EDIT";
    public static final String DELETE_ACTION = "DELETE";

    private RxBus() {}

    private static class SingletonHelper{
        private static final RxBus INSTANCE = new RxBus();
    }

    public static RxBus getInstance(){
        return RxBus.SingletonHelper.INSTANCE;
    }

    private final Subject<Map<Object, Object>> bus = PublishSubject.create();

    public void publish(Map<Object, Object> event) {
        bus.onNext(event);
    }

    public Observable<Map<Object, Object>> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }




    //
    private final Subject<Object> anotherBus = PublishSubject.create();
    private final Subject<Object> anotherBus2 = PublishSubject.create();
    public void publish1 (final Object event) {
        anotherBus.onNext(event);
    }
    public void publish2 (final Object event) {
        anotherBus2.onNext(event);
    }

    public Observable<Object> toObservable1() {
        return anotherBus;
    }

    public Observable<Object> toObservable2() {
        return anotherBus2;
    }

    public boolean hasObservers1() {
        return anotherBus.hasObservers();
    }
}
