package com.tangrun.mdm.boxwindow.core;

import com.tangrun.mdm.boxwindow.core.context.JavaFXApplicationContext;
import com.tangrun.mdm.boxwindow.core.context.JavaFXContext;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@Log4j2
class SceneContext implements JavaFXContext, LifecycleEventListener {

    StageContext context;
    BaseListenerLifecycleObserver lifecycleOwner = new BaseListenerLifecycleObserver();

    public SceneContext(StageContext context) {
        this.context = context;
    }

    @Override
    public JavaFXApplicationContext getApplicationContext() {
        return context.getApplicationContext();
    }

    @Override
    public Stage getStage() {
        return context.getStage();
    }

    @Override
    public LifecycleObserver getLifecycle() {
        return lifecycleOwner;
    }

    @Override
    public void popBack() {
         context.popBack();
    }

    @Override
    public Scene startScene(String resourceURI) {
       return context.startScene( resourceURI);
    }


    @Override
    public void onEvent(LifecycleState state) {
        log.debug("scene event {}",state);
        lifecycleOwner.onEvent(state);
    }
}
