package com.tangrun.mdm.boxwindow.core;

public interface LifecycleObserver {

    LifecycleState getState();

    void addEvent(LifecycleEventListener lifecycleEventListener);

    void removeEvent(LifecycleEventListener lifecycleEventListener);
}
