package com.qunfeer.minesave.Controller;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;


/**
 * 在程序运行调用此类
 */
public class VersionsManager{


    //用数组存取所有的版本文件
    public static final ArrayList<Version> versions = new ArrayList<>();
    private static final File file = new File("versions.dat");

    //先从文件导入，导入时验证文件夹是否存在
    static {
        System.out.println("VersionsManager加载成功");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                Object obj = ois.readObject();
                if (obj instanceof ArrayList) {
                    for (Object item : (ArrayList<?>) obj) {
                        if (item instanceof Version && ((Version) item).getPath().exists()) {
                            versions.add((Version) item);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("读取versions.dat失败: " + e.getMessage());
            }
        }
    }

    /**
     * 运行此函数激活static块中的逻辑
     */
    public static void run(){
        System.out.println("数据导入完成");
    }


    /**
     * 添加一个存档文件夹
     * @param dir 文件夹路径
     * @param name 显示名称
     * @return true 添加成功；false 该文件夹已存在
     */
    public static boolean add(@NotNull File dir, String name){
        Version version = new Version(dir, name);
        if (versions.contains(version)) {
            return false;
        }
        versions.add(version);
        return true;
    }

    /**
     * 从管理列表中移除一个存档文件夹（仅移除记录，不删除磁盘文件）
     * @param version 要移除的版本
     */
    public static void remove(@NotNull Version version){
        versions.remove(version);
    }

    /**
     * 这里是关闭程序的时候必须调用的函数，用于把内存中的versions回写到硬盘中，以保存数据
     *
     * @throws IOException IO异常
     */
    public static void close() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(versions);
        }
        System.out.println("version.dat文件已经写入");
    }
}
