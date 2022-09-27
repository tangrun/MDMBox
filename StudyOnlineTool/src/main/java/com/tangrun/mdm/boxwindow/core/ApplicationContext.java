package com.tangrun.mdm.boxwindow.core;

import com.tangrun.mdm.boxwindow.core.context.JavaFXApplicationContext;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
class ApplicationContext implements JavaFXApplicationContext ,LifecycleEventListener {

    BaseListenerLifecycleObserver lifecycleOwner = new BaseListenerLifecycleObserver();
    LinkedHashMap<Stage, StageContext> contexts = new LinkedHashMap<>(1);

    public StageContext startStage(Stage stage) {
        StageContext stageContext = contexts.get(stage);
        if (stageContext != null) return stageContext;
        StageContext context = new StageContext(this, stage);
        contexts.put(stage, context);
        context.onEvent(LifecycleState.OnInit);
        context.onEvent(LifecycleState.OnReady);
        return context;
    }


    public void removeStage(Stage stage) {
        StageContext stageContext = contexts.remove(stage);
        if (stageContext != null) {
            stageContext.onEvent(LifecycleState.OnRelease);
            stageContext.onEvent(LifecycleState.OnClosed);
        }
    }

    @Override
    public void exitApp() {
        Platform.exit();
    }

    @Override
    public LifecycleObserver getLifecycle() {
        return lifecycleOwner;
    }

    @Override
    public void onEvent(LifecycleState state) {
        log.debug("application event {}",state);
        lifecycleOwner.onEvent(state);
    }
}
