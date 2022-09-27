package com.tangrun.mdm.boxwindow.core.context;

import com.tangrun.mdm.boxwindow.core.LifecycleOwner;
import javafx.stage.Stage;

public interface JavaFXApplicationContext extends LifecycleOwner {

    JavaFXContext startStage(Stage stage);

    void removeStage(Stage stage);

    void exitApp();

}
