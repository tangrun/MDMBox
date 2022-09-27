package com.tangrun.mdm.boxwindow.core;

import java.util.*;

public class BaseListenerLifecycleObserver implements LifecycleObserver, LifecycleEventListener {
    private LifecycleState state = LifecycleState.None;
    private final LinkedHashMap<LifecycleEventListener, LifecycleWrapperListener> lifecycleEvents = new LinkedHashMap<>(2);

    @Override
    public void onEvent(LifecycleState state) {
        if (this.state == state)return;
        this.state = state;
        for (LifecycleWrapperListener wrapper : lifecycleEvents.values()) {
            wrapper.onEvent(this.state);
        }
    }

    @Override
    public LifecycleState getState() {
        return state;
    }

    @Override
    public void addEvent(LifecycleEventListener lifecycleEventListener) {
        if (lifecycleEvents.containsKey(lifecycleEventListener)) return;
        LifecycleWrapperListener wrapper = new LifecycleWrapperListener(lifecycleEventListener);
        lifecycleEvents.put(lifecycleEventListener, wrapper);
        wrapper.onEvent(state);
    }

    @Override
    public void removeEvent(LifecycleEventListener lifecycleEventListener) {
        lifecycleEvents.remove(lifecycleEventListener);
    }

    private static class LifecycleWrapperListener implements LifecycleEventListener {
        LifecycleState state = LifecycleState.None;
        LifecycleEventListener lifecycleEventListener;

        public LifecycleWrapperListener(LifecycleEventListener lifecycleEventListener) {
            this.lifecycleEventListener = lifecycleEventListener;
        }

        public void onEvent(LifecycleState state) {
            if (this.state == state) return;
            this.state = state;
            lifecycleEventListener.onEvent(state);
        }

    }
}
