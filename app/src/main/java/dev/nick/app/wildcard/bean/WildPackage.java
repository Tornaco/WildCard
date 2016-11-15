package dev.nick.app.wildcard.bean;

import android.graphics.drawable.Drawable;

public class WildPackage {

    private int id;
    private String pkgName;


    private int accessTimes;
    private long lastAccessTime;

    private String name;
    private Drawable icon;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public int getAccessTimes() {
        return accessTimes;
    }

    public void setAccessTimes(int accessTimes) {
        this.accessTimes = accessTimes;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public String toString() {
        return "WildPackage{" +
                "id=" + id +
                ", pkgName='" + pkgName + '\'' +
                ", accessTimes=" + accessTimes +
                ", lastAccessTime=" + lastAccessTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WildPackage that = (WildPackage) o;

        return pkgName.equals(that.pkgName);
    }

    @Override
    public int hashCode() {
        return pkgName.hashCode();
    }
}
