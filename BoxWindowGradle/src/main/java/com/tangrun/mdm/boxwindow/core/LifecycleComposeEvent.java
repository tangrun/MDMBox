package com.tangrun.mdm.boxwindow.core;

import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class LifecycleComposeEvent implements LifecycleOwner {

    private final BaseListenerLifecycleObserver lifecycleObserver = new BaseListenerLifecycleObserver();

    public void addSource(LifecycleObserver lifecycleObserver, LifecycleState... states) {
        lifecycleObserver.addEvent(new LifecycleEventListener() {
            @Override
            public void onEvent(LifecycleState state) {
                if (states == null || states.length ==0) {
                    LifecycleComposeEvent.this.lifecycleObserver.onEvent(state);
                } else {
                    for (LifecycleState lifecycleState : states) {
                        if (lifecycleState == state) {
                            LifecycleComposeEvent.this.lifecycleObserver.onEvent(state);
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public LifecycleObserver getLifecycle() {
        return lifecycleObserver;
    }
}
