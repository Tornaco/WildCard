package dev.nick.app.wildcard.bean;

public class WildPackage {

    private int id;
    private String pkgName;

    @Override
    public String toString() {
        return "WildPackage{" +
                "id=" + id +
                ", pkgName='" + pkgName + '\'' +
                ", accessTimes=" + accessTimes +
                ", lastAccessTime=" + lastAccessTime +
                '}';
    }

    private int accessTimes;
    private long lastAccessTime;

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
}
