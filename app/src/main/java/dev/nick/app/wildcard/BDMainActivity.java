package dev.nick.app.wildcard;

import android.Manifest;
import android.view.ViewGroup;

import com.baidu.appx.BDBannerAd;

import dev.nick.logger.LoggerManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class BDMainActivity extends NavigatorActivity {

    private static final String SDK_APP_KEY = "9m0I6MNpaFaKljHlUtGboGDrst2RbPZL";
    private static final String SDK_BANNER_AD_ID = "27SteCArhTBDP6o48NEcG4tI";

    private BDBannerAd bannerview;

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    public void showBanner() {
        if (null == bannerview) {
            bannerview = new BDBannerAd(this, SDK_APP_KEY, SDK_BANNER_AD_ID);
            bannerview.setAdSize(BDBannerAd.SIZE_FLEXIBLE);
            bannerview.setAdListener(new BDBannerAd.BannerAdListener() {
                @Override
                public void onAdvertisementDataDidLoadSuccess() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementDataDidLoadFailure() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementViewDidShow() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementViewDidClick() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementViewWillStartNewIntent() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }
            });
            ViewGroup container = (ViewGroup) findViewById(R.id.adview_container);
            container.addView(bannerview);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BDMainActivityPermissionsDispatcher.showBannerWithCheck(BDMainActivity.this);
    }
}
