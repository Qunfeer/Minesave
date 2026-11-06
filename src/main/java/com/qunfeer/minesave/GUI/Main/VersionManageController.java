package com.qunfeer.minesave.GUI.Main;

import com.qunfeer.minesave.Controller.Version;
import com.qunfeer.minesave.Controller.VersionsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * 版本（存档文件夹）管理弹窗：用一个 ListView 展示已管理的文件夹，支持添加与删除。
 * 数据变更直接落到 {@link VersionsManager}，本类只负责 UI 交互与刷新。
 */
public class VersionManageController {

    @FXML
    private ListView<Version> versionList;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    void initialize() {
        // 列表里同时展示名称与路径，方便用户区分
        versionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Version item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + "    " + item.getPath());
                }
            }
        });
        refreshList();
    }

    @FXML
    void onAddButtonClick() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择存档根目录");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = chooser.showDialog(stage);
        if (dir == null) {
            return;
        }
        if (!VersionsManager.add(dir, dir.getName())) {
            showInfo("该文件夹已在管理列表中");
            return;
        }
        refreshList();
    }

    @FXML
    void onDeleteButtonClick() {
        Version selected = versionList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("请先在列表中选择要删除的文件夹");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("删除");
        confirm.setHeaderText("确定要移除该存档文件夹吗？");
        confirm.setContentText(selected.getName() + "\n" + selected.getPath() + "\n\n仅从管理列表移除，不会删除磁盘文件。");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            VersionsManager.remove(selected);
            refreshList();
        }
    }

    private void refreshList() {
        versionList.getItems().setAll(VersionsManager.versions);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
