package com.qunfeer.minesave.Controller;


import com.qunfeer.minesave.exception.NoValueException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//操作git的工具类
public class GitUtil{
    private GitUtil() {
    }
    //每次调用需要检查这个git是否更新；volatile 保证多线程读写可见性，避免操作中途读到 null
    static public volatile Git git;

    /**
     * 提交当前的存档，用于存档
     * @param message 提交时的用户备注提示
     * @throws GitAPIException git操作发生异常抛出
     */
    public static void commit(String message) throws GitAPIException{
        // 先取到本地引用，后续都用这个局部变量，避免并发时静态字段被置空导致 NPE
        Git g = git;
        if (g == null) throw new NoValueException();
        OperationGuard.begin("commit", g.getRepository().getWorkTree().getAbsolutePath(), "");
        try {
            g.add().addFilepattern(".").call();
            g.commit().setMessage(message).call();
        } finally {
            OperationGuard.end();
        }
    }

    /**
     * 用于回档，给出GUI选定档的哈希头就可以回档
     * @param ref 哈希头，快照的唯一标识
     * @throws GitAPIException git操作发生异常时抛出
     */
    public static void reset(String ref) throws GitAPIException {
        Git g = git;
        if (g == null) throw new NoValueException();
        OperationGuard.begin("reset", g.getRepository().getWorkTree().getAbsolutePath(), ref);
        try {
            g.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
        } finally {
            OperationGuard.end();
        }
    }

    /**
     * 恢复被中断的回溯：重新执行 reset --hard 到指定 ref（幂等，重复执行安全）。
     * @param repoPath 存档 git 仓库路径
     * @param ref 回溯目标哈希头
     */
    public static void recoverReset(File repoPath, String ref) throws IOException, GitAPIException {
        OperationGuard.begin("reset", repoPath.getAbsolutePath(), ref);
        try (Git g = Git.open(repoPath)) {
            g.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
        } finally {
            OperationGuard.end();
        }
    }

    /**
     * 关闭当前打开的 git 仓库，用于切换存档或退出程序时清理
     */
    public static synchronized void closeGit() {
        if (git != null) {
            git.close();
            git = null;
        }
    }

    /**
     * 返回当前文件夹的git信息
     * @return ArrayList<GitView> 返回一个GItView的数组类
     * @throws GitAPIException  none
     */
    public static ArrayList<GitView> log() throws GitAPIException {
        Git g = git;
        if (g == null) throw new NoValueException();
        ArrayList<GitView> result = new ArrayList<>();
        for (RevCommit rc : g.log().call()){
            result.add(new GitView(rc));
        }
        return result;
    }

    /**
     * 判断是否有git仓库
     * @param dir 目录
     * @return boolean
     */
    public static boolean isGitRepoJGit(File dir){
        try {
            Repository repo = new FileRepositoryBuilder()
                    .setWorkTree(dir)
                    .readEnvironment()
                    .build();
            boolean valid = repo.getObjectDatabase().exists();
            repo.close();
            return valid;
        }catch (Exception e){
            return false;
        }
    }
}
