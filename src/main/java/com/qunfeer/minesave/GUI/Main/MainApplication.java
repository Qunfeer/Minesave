package com.qunfeer.minesave.GUI.Main;

import com.qunfeer.minesave.Controller.GitUtil;
import com.qunfeer.minesave.Controller.VersionsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 550);
        MainController controller = fxmlLoader.getController();
        controller.setMainStage(stage);
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(450);
        stage.setMaxWidth(600);
        stage.setMaxHeight(650);
        stage.setTitle("Minesave");
        stage.setResizable(false);
        VersionsManager.run();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        GitUtil.closeGit();
        VersionsManager.close();
        super.stop();
    }
}
