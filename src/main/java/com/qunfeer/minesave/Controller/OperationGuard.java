package com.qunfeer.minesave.Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 备份/回溯操作的崩溃防护标记。
 * <p>
 * 在 git 操作开始前写入一个标记文件，操作结束后删除。正常情况下 finally 一定会执行删除，
 * 所以只有进程在操作中途被强制杀死（断电、任务管理器强杀等）时，标记才会残留。
 * 下次启动时通过 {@link #readInterrupted()} 检测到未完成的操作，从而提示恢复。
 */
public final class OperationGuard {

    private static final Path MARKER = Path.of("operation.lock");

    private OperationGuard() {
    }

    /**
     * 写入操作标记。
     * @param operation 操作类型：commit / reset
     * @param savePath  存档 git 仓库的绝对路径（用于恢复时定位）
     * @param ref       回溯目标 ref（commit 时可为空）
     */
    public static void begin(String operation, String savePath, String ref) {
        try {
            String refValue = ref == null ? "" : ref;
            Files.writeString(MARKER, operation + "\n" + savePath + "\n" + refValue, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // 写标记失败不影响主流程
        }
    }

    /** 清除操作标记（操作正常完成时调用） */
    public static void end() {
        try {
            Files.deleteIfExists(MARKER);
        } catch (IOException ignored) {
        }
    }

    /**
     * 读取上次中断操作的信息。
     * @return 中断操作信息，若没有残留标记则返回 null
     */
    public static InterruptedOperation readInterrupted() {
        if (!Files.exists(MARKER)) {
            return null;
        }
        try {
            String[] lines = Files.readString(MARKER, StandardCharsets.UTF_8).split("\n", -1);
            String operation = lines.length > 0 ? lines[0].trim() : "";
            String savePath = lines.length > 1 ? lines[1].trim() : "";
            String ref = lines.length > 2 ? lines[2].trim() : "";
            if (operation.isEmpty()) {
                return null;
            }
            return new InterruptedOperation(operation, savePath, ref);
        } catch (IOException e) {
            return null;
        }
    }

    /** 中断操作的描述信息 */
    public static final class InterruptedOperation {
        public final String operation;
        public final String savePath;
        public final String ref;

        InterruptedOperation(String operation, String savePath, String ref) {
            this.operation = operation;
            this.savePath = savePath;
            this.ref = ref;
        }
    }
}
