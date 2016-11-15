package dev.nick.app.wildcard.repo;

import android.support.annotation.NonNull;

import java.util.List;

import dev.nick.app.wildcard.bean.WildPackage;

public interface IProviderService<T> {
    void add(@NonNull WildPackage wildPackage);

    void remove(@NonNull WildPackage wildPackage);

    List<T> read();

    void observe(@NonNull Observer observer);

    interface Observer {
        void onChange();
    }
}
