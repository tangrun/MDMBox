module StudyOnlineTool {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires static lombok;
    requires javafx.graphics;
    requires org.apache.logging.log4j;
    requires java.naming;
    requires commons.exec;
    requires com.h2database;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires jdk.charsets;

    opens com.tangrun.mdm.boxwindow.core to javafx.graphics;
    opens com.tangrun.mdm.boxwindow.core.context to javafx.graphics;
    opens com.tangrun.mdm.boxwindow.controller to javafx.fxml;
    opens com.tangrun.mdm.boxwindow.pojo to com.google.gson;

    exports com.tangrun.mdm.boxwindow;

}