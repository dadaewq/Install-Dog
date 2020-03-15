package com.modosa.apkinstaller.activity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.catchingnow.delegatedscopeclient.DSMClient;
import com.modosa.apkinstaller.R;
import com.modosa.apkinstaller.util.NotifyUtil;

import java.io.File;


/**
 * @author dadaewq
 */
public class Installer2Activity extends AbstractInstallerActivity {
    private final boolean ltsdk26 = Build.VERSION.SDK_INT < Build.VERSION_CODES.O;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (ltsdk26) {
            deleteCache();
        }
    }

    @Override
    public void startInstall(String apkPath) {
        if (ltsdk26) {
            showMyToast1(R.string.tip_ltsdk26);
            finish();
        } else {
            Log.d("Start install", apkPath + "");
            if (apkPath != null) {
                installApkFile = new File(apkPath);
                String authority = getPackageName() + ".FILE_PROVIDER";
                Uri installuri = FileProvider.getUriForFile(getApplicationContext(), authority, installApkFile);
                new Thread(() -> {
                    showMyToast0(String.format(getString(R.string.tip_start_install), apkinfo[0]));
                    try {
                        DSMClient.installApp(this, installuri, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (show_notification) {
                            Log.e("packagename", apkinfo[1]);
                            new NotifyUtil(this).sendNotification("2", String.format(getString(R.string.tip_install_over), apkinfo[0]), apkinfo[1], apkPath, istemp);
                        } else {
                            deleteCache();
                        }
                    }
                    showMyToast1(R.string.tip_install_end);

                    finish();
                }).start();
            } else {
                showMyToast0(R.string.tip_failed_read);
                finish();
            }
        }
    }

    @Override
    protected void startUninstall(String pkgName) {
        if (ltsdk26) {
            showMyToast1(R.string.tip_ltsdk26);
            finish();
        } else {
            Log.d("Start uninstall", pkgName);
            new Thread(() -> {
                showMyToast0(String.format(getString(R.string.tip_start_uninstall), uninstallPackageLable));
                try {
                    DSMClient.uninstallApp(this, pkgName);
                } catch (Exception e) {
                    showMyToast1(e.toString());
                }
                //Todo show result
            }).start();
        }
    }

}