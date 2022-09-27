package com.tangrun.mdm.boxwindow.core.context;

import com.tangrun.mdm.boxwindow.core.LifecycleOwner;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface JavaFXContext extends LifecycleOwner {
    JavaFXApplicationContext getApplicationContext();

    Stage getStage();

    void popBack();

    Scene startScene( String resourceURI);
}
