package com.tangrun.mdm.boxwindow.core;

import com.tangrun.mdm.boxwindow.Launcher;
import com.tangrun.mdm.boxwindow.core.context.JavaFXApplicationContext;
import com.tangrun.mdm.boxwindow.core.context.JavaFXContext;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Log4j2
class StageContext implements JavaFXContext, LifecycleEventListener {

    ApplicationContext applicationContext;
    Stage stage;
    Stack<Scene> stack = new Stack<>();
    Map<Scene, SceneContext> sceneContextMap = new HashMap<>();
    BaseListenerLifecycleObserver lifecycleOwner = new BaseListenerLifecycleObserver();

    public StageContext(ApplicationContext applicationContext, Stage stage) {
        this.applicationContext = applicationContext;
        this.stage = stage;
    }

    @Override
    public JavaFXApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public LifecycleObserver getLifecycle() {
        return lifecycleOwner;
    }

    @Override
    public void popBack() {
        int size = stack.size();
        Scene pop = size > 0 ? stack.pop() : null;
        if (pop != null) {
            SceneContext sceneContext = sceneContextMap.remove(pop);
            if (sceneContext != null) {
                dispatchEvent(sceneContext, LifecycleState.OnRelease, LifecycleState.OnClosed);
            }
        }
        if (size > 1) {
            stage.setScene(stack.peek());
        } else {
            stage.setScene(null);
            if (stage.isShowing())
                stage.close();
            else {
                stage.showingProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        stage.close();
                    }
                });
            }
            applicationContext.removeStage(getStage());
        }
    }

    @Override
    public Scene startScene(String resourceURI) {
        FXMLWrapper<Parent> wrapper = loadFXMLWrapper(resourceURI);
        Scene scene = new Scene(wrapper.load);
        stack.add(scene);
        sceneContextMap.put(scene, wrapper.context);
        stage.setScene(scene);
        if (!stage.isShowing()) {
            stage.showingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        wrapper.controller.initialLazy();
                        dispatchEvent(wrapper.context, LifecycleState.OnInit, LifecycleState.OnReady);
                        stage.showingProperty().removeListener(this);
                    }
                }
            });
            stage.show();
        } else{
            wrapper.controller.initialLazy();
            dispatchEvent(wrapper.context, LifecycleState.OnInit, LifecycleState.OnReady);
        }
        return scene;
    }

    private void dispatchEvent(SceneContext context, LifecycleState... states) {
        for (LifecycleState state : states) {
            context.onEvent(state);
        }
    }

    private void dispatchAllEvent(LifecycleState... states) {
        for (SceneContext value : sceneContextMap.values()) {
            dispatchEvent(value, states);
        }
    }

    public <T extends Parent> FXMLWrapper<T> loadFXMLWrapper(String resourceURI) {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource(resourceURI));
        try {
            T load = fxmlLoader.load();
            BaseController controller = null;
            if (fxmlLoader.getController() instanceof BaseController) {
                controller = fxmlLoader.getController();

                Class<?> controllerClass = controller.getClass();
                while (controllerClass != null && controllerClass != BaseController.class) {
                    controllerClass = controllerClass.getSuperclass();
                }
                if (controllerClass == null)
                    throw new RuntimeException("controller is not extend from BaseController");
                SceneContext sceneContext = null;
                for (Field field : controllerClass.getDeclaredFields()) {
                    if (field.getType() == JavaFXContext.class) {
                        field.setAccessible(true);
                        sceneContext = new SceneContext(this);
                        field.set(controller, sceneContext);

                    }
                }
                return new FXMLWrapper<>(load, controller, sceneContext);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    private static class FXMLWrapper<T extends Parent> {
        T load;
        BaseController controller;
        SceneContext context;

        public FXMLWrapper(T load, BaseController controller, SceneContext context) {
            this.load = load;
            this.controller = controller;
            this.context = context;
        }
    }

    @Override
    public void onEvent(LifecycleState state) {
        log.debug("stage event {}",state);
        lifecycleOwner.onEvent(state);
    }

}
