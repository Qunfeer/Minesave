package com.qunfeer.minesave.GUI.Main;

import com.qunfeer.minesave.Controller.GitUtil;
import com.qunfeer.minesave.Controller.GitView;
import com.qunfeer.minesave.Controller.OperationGuard;
import com.qunfeer.minesave.Controller.Version;
import com.qunfeer.minesave.Controller.VersionsManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController {


    private Stage mainStage;

    /** 备份/回溯操作进行中的标记，用于阻止窗口在操作期间被关闭 */
    private final AtomicBoolean busy = new AtomicBoolean(false);

    /** 彩蛋随机数与连点计数 */
    private final Random random = new Random();
    private int eggCount = 0;

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        mainStage.setOnCloseRequest(event -> {
            if (busy.get()) {
                event.consume();
                showInfo("备份/回溯正在进行中，为避免存档损坏，请等待操作完成后再关闭程序。");
            }
        });
    }

    @FXML
    private ChoiceBox<Version> versions;

    @FXML
    private Button reset;

    @FXML
    private Button commit;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ChoiceBox<String> saves;

    @FXML
    private ListView<GitView> choiceGit;

    @FXML
    private MenuItem versionManage;

    @FXML
    void initialize() {
        choiceGit.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        System.out.println("FXML初始化完成，控件就绪");
        //初始化versions控件
        versions.getItems().setAll(VersionsManager.versions);
        //监听versions组件
        versions.valueProperty().addListener((obs, oldItem, newItem) -> {
            saves.getItems().clear();
            if (newItem == null) {
                return;
            }
            new Thread(() -> {
                File savePath = new File(newItem.getPath(), "saves");
                File[] files = savePath.listFiles();
                if (files == null) {
                    return;
                }
                List<String> names = new ArrayList<>();
                for (File f : files) {
                    names.add(f.getName());
                }
                Platform.runLater(() -> saves.getItems().setAll(names));
            }).start();
        });

        saves.valueProperty().addListener((obs, oldItem, newItem) -> {
            GitUtil.closeGit();
            choiceGit.getItems().clear();
            reset.setDisable(true);
            commit.setDisable(true);
            if (newItem == null) {
                return;
            }
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            ArrayList<GitView> data = new ArrayList<>();
            new Thread(() -> {
                File gitPath = new File(versions.getValue().getPath(), "/saves/" + newItem);
                if (GitUtil.isGitRepoJGit(gitPath)) {
                    try {
                        GitUtil.git = Git.open(gitPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        GitUtil.git = Git.init().setDirectory(gitPath).call();
                    } catch (GitAPIException e) {
                        throw new RuntimeException(e);
                    }
                }
                //拿到了数据，现在在GitUtil里面，运行GitUtil要多线程


                try {
                    data.addAll(GitUtil.log());
                } catch (GitAPIException ignored) {
                }
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    choiceGit.getItems().addAll(data);
                    commit.setDisable(false);
                });
            }).start();
        });
        choiceGit.getSelectionModel().selectedItemProperty().addListener((obs, oldSelect, newSelect) -> {
            reset.setDisable(newSelect == null);
        });

        // 启动完成后检查是否有上次被中断的操作
        Platform.runLater(this::checkInterruptedOperation);
    }

    @FXML
    protected void onVersionManageButtonClick() {
        if (busy.get()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("version-manage-view.fxml"));
            Parent root = loader.load();
            VersionManageController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setStage(dialogStage);
            dialogStage.initOwner(mainStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("版本管理");
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        refreshVersions();
    }

    private void refreshVersions() {
        versions.getItems().setAll(VersionsManager.versions);
    }

    @FXML
    protected void onResetButtonClick() {
        GitView selected = choiceGit.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String hash = selected.getRc().getName();
        loading();
        new Thread(() -> {
            boolean success;
            try {
                GitUtil.reset(hash);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            boolean finalSuccess = success;
            Platform.runLater(() -> {
                unloading();
                showInfo(finalSuccess ? "回档成功" : "回档失败");
                refreshLog();
            });
        }).start();
    }

    @FXML
    protected void onCommitButtonClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Git提交");
        dialog.setHeaderText("新建一次提交");
        dialog.setContentText("请输入提交备注：");

        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty()) {
            return;
        }
        String message = res.get().isBlank() ? "存档" : res.get().trim();
        loading();
        new Thread(() -> {
            boolean success;
            try {
                GitUtil.commit(message);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            boolean finalSuccess = success;
            Platform.runLater(() -> {
                unloading();
                showInfo(finalSuccess ? "存档成功" : "存档失败");
                refreshLog();
            });
        }).start();
    }

    @FXML
    protected void onEasterEggClick() {
        eggCount++;
        String[] messages = {
                "嘻嘻，被你发现啦！",
                "今天也要记得备份存档哦～",
                "Minesave 永远的神！",
                "这条消息出现的概率是四分之一",
        };
        if (eggCount >= 5) {
            showInfo("恭喜你连点 5 次，解锁隐藏成就：「手速惊人」！\n\n再点可以继续抽彩蛋～");
            eggCount = 0;
        } else {
            showInfo(messages[random.nextInt(messages.length)] + "\n\n（再点 " + (5 - eggCount) + " 次有隐藏惊喜）");
        }
    }

    @FXML
    protected void onAboutClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 Minesave");
        alert.setHeaderText("Minesave - Minecraft 存档备份工具");
        alert.setContentText("版本：1.0-TEST\n\n作者邮箱：qunfeer@163.com\n\n感谢使用！");
        alert.showAndWait();
    }

    /**
     * 启动时检测上次是否有备份/回溯被中断，若有则提示用户是否恢复
     */
    private void checkInterruptedOperation() {
        OperationGuard.InterruptedOperation op = OperationGuard.readInterrupted();
        if (op == null) {
            return;
        }
        // 已读取到信息，先清除标记，避免每次启动重复提示
        OperationGuard.end();

        if ("reset".equals(op.operation) && !op.savePath.isBlank() && !op.ref.isBlank()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("检测到未完成的回溯");
            confirm.setHeaderText("上次回溯操作被中断");
            confirm.setContentText("存档可能处于不一致状态，是否重新执行回溯以恢复？\n\n" + op.savePath);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok;
                    try {
                        GitUtil.recoverReset(new File(op.savePath), op.ref);
                        ok = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        ok = false;
                    }
                    boolean finalOk = ok;
                    Platform.runLater(() -> showInfo(finalOk ? "回溯恢复完成" : "回溯恢复失败，请手动处理"));
                }).start();
            }
        } else if ("commit".equals(op.operation)) {
            showInfo("上次备份操作被中断。备份是原子的，通常不会损坏存档，请检查最近一次备份是否完整。");
        }
    }


    private void loading() {
        busy.set(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        versions.setDisable(true);
        saves.setDisable(true);
        reset.setDisable(true);
        commit.setDisable(true);
        versionManage.setDisable(true);
    }

    private void unloading() {
        busy.set(false);
        progressBar.setProgress(0);
        versions.setDisable(false);
        saves.setDisable(false);
        versionManage.setDisable(false);
        // 恢复需要先选中一条历史记录；备份仅在 git 仓库可用时启用
        reset.setDisable(true);
        commit.setDisable(GitUtil.git == null);
    }

    /**
     * 备份/回溯完成后重新读取当前存档的 git 历史并刷新列表。
     * 只重新读取日志，不关闭/重开 git，避免出现 git 为空却可点击备份的窗口。
     */
    private void refreshLog() {
        new Thread(() -> {
            ArrayList<GitView> data = new ArrayList<>();
            try {
                data.addAll(GitUtil.log());
            } catch (Exception ignored) {
            }
            Platform.runLater(() -> choiceGit.getItems().setAll(data));
        }).start();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
