module LicenseGenerator {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires static lombok;
    requires com.google.gson;

    opens com.tangrun.mdm.licensegenerator to javafx.graphics,javafx.fxml,com.google.gson;
    exports com.tangrun.mdm.licensegenerator;
}