package dev.nick.app.wildcard;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.scalpel.core.opt.SharedExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.pinlock.PwdSetter;
import dev.nick.app.wildcard.app.AppCompat;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.camera.SpyPinLock;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.app.wildcard.tiles.GuardSwitcher;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class NavigatorActivity extends TransactionSafeActivity implements GuardSwitcher.Callback {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101;

    private Logger mLogger;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private WildcardApp mApp;

    private DisplayMode mDisplayMode;

    private Observer mSettingsObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            applyTheme();
            setLayoutManager();
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());
        mApp = (WildcardApp) getApplication();

        setContentView(R.layout.activity_navogator);

        applyTheme();

        placeFragment(R.id.container, new GuardSwitcher(), null, true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });

        showWildPackageList();

        showLock();
    }

    private void showLock() {
        PinLockStub.LockSettings settings = new PinLockStub.LockSettings(
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher), getPackageName(), getThemeColor()
                , false, false);

        PinLockStub locker = new SpyPinLock(getApplicationContext(), settings, new PinLockStub.Listener() {
            @Override
            public void onShown(PinLockStub.LockSettings settings) {

            }

            @Override
            public void onDismiss(PinLockStub.LockSettings info) {

            }
        });

        if (TextUtils.isEmpty(locker.getStoredPwd())) return;

        boolean mCompatMode = SettingsProvider.get().compatMode(this);
        if (!mCompatMode) {
            locker.lock();
        } else {
            Intent intent = new Intent(this, LockProxyActivity.class);
            intent.putExtra("pkg", getPackageName());
            intent.putExtra("color", getThemeColor());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private boolean hasUsagePermission() {
        return AppCompat.from(this).hasUsagePermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
            if (!hasUsagePermission()) {
                // User denied.
            }
        }
    }

    private void onFabClick() {
        PinLockStub.LockSettings info = new PinLockStub.LockSettings(
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher)
                , null, getThemeColor(), false, false);

        PinLockStub stub = new SpyPinLock(this, info, new PinLockStub.Listener() {
            @Override
            public void onShown(PinLockStub.LockSettings settings) {
                // None
            }

            @Override
            public void onDismiss(PinLockStub.LockSettings lockInfo) {
                startActivity(new Intent(getApplicationContext(), PackagePickerActivity.class));
            }
        });
        boolean hasPwd = !TextUtils.isEmpty(stub.getStoredPwd());
        if (!hasPwd) {
            startActivity(new Intent(getApplicationContext(), PwdSetter.class));
        } else {
            startActivity(new Intent(getApplicationContext(), PackagePickerActivity.class));
        }
    }

    private void showRentation() {
        new AlertDialog.Builder(NavigatorActivity.this)
                .setTitle(R.string.title_get_status)
                .setMessage(R.string.message_get_status)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(
                                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                                MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (!hasUsagePermission()) {
            showRentation();
            return;
        }

        asyncLoadPackages();
    }

    private void asyncLoadPackages() {
        new AsyncTask<Void, Void, List<WildPackage>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                findViewById(R.id.progress).setVisibility(View.VISIBLE);
            }

            @Override
            protected List<WildPackage> doInBackground(Void... voids) {
                return mApp.getProviderService().read();
            }

            @Override
            protected void onPostExecute(List<WildPackage> wildPackages) {
                super.onPostExecute(wildPackages);
                findViewById(R.id.progress).setVisibility(View.GONE);
                mAdapter.update(wildPackages);
            }
        }.executeOnExecutor(SharedExecutor.get().getService());
    }

    protected void showWildPackageList() {
        mRecyclerView.setHasFixedSize(true);
        setLayoutManager();
        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);

        SettingsProvider.get().addObserver(mSettingsObserver);
    }

    private void setLayoutManager() {

        DisplayMode newMode = SettingsProvider.get().gridView(getApplicationContext())
                ? DisplayMode.Grid : DisplayMode.List;

        boolean reset = mDisplayMode == null || newMode != mDisplayMode;
        mDisplayMode = newMode;

        mLogger.debug("Reset layout:" + reset);

        if (reset) {
            boolean useGrid = mDisplayMode == DisplayMode.Grid;
            if (useGrid) {
                mRecyclerView.setLayoutManager(
                        new GridLayoutManager(getApplicationContext(),
                                getResources().getInteger(R.integer.wildlist_num_columns),
                                LinearLayoutManager.VERTICAL,
                                false));
            } else {
                mRecyclerView.setLayoutManager(
                        new LinearLayoutManager(getApplicationContext(),
                                LinearLayoutManager.VERTICAL,
                                false));
            }
        }
    }

    @Override
    public void onEnabled() {
        // Noop
    }

    @Override
    public void onDisabled() {
        // Noop
    }

    private enum DisplayMode {
        List, Grid;
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        ImageView thumbnail;
        View actionBtn;

        TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
            actionBtn = itemView.findViewById(R.id.hint);
            thumbnail = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

        private final List<WildPackage> data;

        Adapter(List<WildPackage> data) {
            this.data = data;
        }

        Adapter() {
            this(new ArrayList<WildPackage>());
        }

        void update(List<WildPackage> data) {

            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();

            StringBuilder sb = new StringBuilder("\n");
            for (WildPackage wildPackage : data) {
                sb.append("<item>");
                sb.append(wildPackage.getPkgName());
                sb.append("</item>");
                sb.append("\n");
            }
            mLogger.verbose(sb.toString());
        }

        void remove(int position) {
            this.data.remove(position);
            notifyItemRemoved(position);
        }

        void clear() {
            this.data.clear();
            notifyDataSetChanged();
        }

        public void add(WildPackage wildPackage, int position) {
            this.data.add(position, wildPackage);
            notifyItemInserted(position);
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_card_item, parent, false);
            return new TwoLinesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, int position) {
            final WildPackage item = data.get(position);
            if (SettingsProvider.get().gridView(getApplicationContext())) {
                holder.title.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.thumbnail.getLayoutParams();
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    holder.thumbnail.setLayoutParams(params);
                }
            } else {
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(item.getName());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.thumbnail.getLayoutParams();
                    params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                    holder.thumbnail.setLayoutParams(params);
                }
            }
            holder.thumbnail.setImageDrawable(item.getIcon());
            holder.actionBtn.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(NavigatorActivity.this, holder.actionBtn);
                    popupMenu.inflate(R.menu.package_item_actions);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_remove:
                                    remove(holder.getAdapterPosition());
                                    onRemove(item);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

        }

        private void onRemove(WildPackage wildPackage) {
            WildcardApp app = (WildcardApp) getApplication();
            app.getProviderService().remove(wildPackage);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
