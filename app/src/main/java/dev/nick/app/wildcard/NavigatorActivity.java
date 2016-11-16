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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.pinlock.PinLockStub;
import dev.nick.app.wildcard.app.AppCompat;
import dev.nick.app.wildcard.bean.WildPackage;
import dev.nick.app.wildcard.repo.SettingsProvider;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class NavigatorActivity extends TransactionSafeActivity {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101;

    private Logger mLogger;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private WildcardApp mApp;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());
        mApp = (WildcardApp) getApplication();

        setContentView(R.layout.activity_navogator);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });

        showWildPackageList();
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
        PinLockStub.LockInfo info = new PinLockStub.LockInfo(getString(R.string.app_name),
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher));

        PinLockStub stub = new PinLockStub(this, info, new PinLockStub.Listener() {
            @Override
            public void onShown() {
                // None
            }

            @Override
            public void onDismiss() {
                startActivity(new Intent(getApplicationContext(), PackagePickerActivity.class));
            }
        });
        boolean hasPwd = !TextUtils.isEmpty(stub.getStoredPwd());
        if (!hasPwd) {
            stub.lock();
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

        new AsyncTask<Void, Void, List<WildPackage>>() {

            @Override
            protected List<WildPackage> doInBackground(Void... voids) {
                return mApp.getProviderService().read();
            }

            @Override
            protected void onPostExecute(List<WildPackage> wildPackages) {
                super.onPostExecute(wildPackages);
                mAdapter.update(wildPackages);
            }
        }.execute();
    }

    protected void showWildPackageList() {
        mRecyclerView.setHasFixedSize(true);
        boolean useGrid = SettingsProvider.get().gridView(getApplicationContext());
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
        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);
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
            holder.title.setText(item.getName());
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
