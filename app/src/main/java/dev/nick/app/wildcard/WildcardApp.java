package dev.nick.app.wildcard;

import android.util.Log;

import dev.nick.app.pinlock.application.VaultApp;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.repo.IProviderService;
import dev.nick.app.wildcard.repo.ProviderService;
import dev.nick.logger.LoggerManager;

public class WildcardApp extends VaultApp {

    private IProviderService<WildPackage> mProviderService;

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerManager.setDebugLevel(Log.VERBOSE);
        LoggerManager.setTagPrefix("WildcardApp");
        mProviderService = new ProviderService(this);
    }

    public IProviderService<WildPackage> getProviderService() {
        return mProviderService;
    }
}
