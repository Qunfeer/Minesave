module com.qunfeer.minesave {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.eclipse.jgit;
    requires org.jetbrains.annotations;

    opens com.qunfeer.minesave to javafx.fxml;
    exports com.qunfeer.minesave;
    exports com.qunfeer.minesave.GUI.Main;
    opens com.qunfeer.minesave.GUI.Main to javafx.fxml;
}