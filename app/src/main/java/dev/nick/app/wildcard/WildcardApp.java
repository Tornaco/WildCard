package dev.nick.app.wildcard;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.repo.IProviderService;
import dev.nick.app.wildcard.repo.ProviderService;
import dev.nick.app.wildcard.service.GuardService;
import dev.nick.logger.LoggerManager;

public class WildcardApp extends Application {

    private IProviderService<WildPackage> mProviderService;

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerManager.setDebugLevel(Log.DEBUG);
        LoggerManager.setTagPrefix("WildcardApp");
        mProviderService = new ProviderService(this);
        mProviderService.observe(new IProviderService.Observer() {
            @Override
            public void onChange() {
                LoggerManager.getLogger(getClass()).funcEnter();
            }
        });
        startService(new Intent(this, GuardService.class));
    }

    public IProviderService<WildPackage> getProviderService() {
        return mProviderService;
    }
}
