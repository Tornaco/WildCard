package dev.nick.app.wildcard.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.util.List;

public abstract class AppCompat {

    public static AppCompat from(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return new Impl22(context);
        }
        return new Impl(context);
    }

    public abstract boolean hasUsagePermission();

    public abstract String getTopPackage();

    private static class Impl extends AppCompat {

        private Context context;

        Impl(Context context) {
            this.context = context;
        }

        @Override
        public boolean hasUsagePermission() {
            return true;
        }

        @Override
        public String getTopPackage() {
            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                ComponentName componentName = tasks.get(0).topActivity;
                if (componentName != null) {
                    return componentName.getPackageName();
                }
            }
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private static class Impl22 extends AppCompat {

        private Context context;

        Impl22(Context context) {
            this.context = context;
        }

        @Override
        public boolean hasUsagePermission() {
            AppOpsManager appOps = (AppOpsManager)
                    context.getSystemService(Context.APP_OPS_SERVICE);
            int mode;
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        }

        @Override
        public String getTopPackage() {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);

                String topActivity = "";
                if (!stats.isEmpty()) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                    return topActivity;
                }
            }
            return null;
        }
    }

}
