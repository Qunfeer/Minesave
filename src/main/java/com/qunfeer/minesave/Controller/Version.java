package com.qunfeer.minesave.Controller;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;


public class Version implements Serializable {
    private static final long serialVersionUID = 1L;

    public Version(File path, String name){
        this.path = path;
        this.name = name;
    }

    private File path;
    private String name;

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    // 以 path 作为唯一标识，用于去重与列表选中匹配
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return Objects.equals(path, version.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
