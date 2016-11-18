package dev.nick.app.wildcard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.app.wildcard.service.GuardService;
import dev.nick.logger.LoggerManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        SettingsProvider provider = SettingsProvider.get();
        boolean start = provider.startOnBoot(context);
        LoggerManager.getLogger(getClass()).debug("Boot completed, start on boot:" + start);
        if (start) {
            context.startService(new Intent(context, GuardService.class));
        }
    }
}
