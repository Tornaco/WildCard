package dev.nick.app.pinlock.utils;

import java.util.ArrayList;

public class Lists {
    public static <T extends Object> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <T extends Object> ArrayList<T> newArrayList(int size) {
        return new ArrayList<T>(size);
    }
}
