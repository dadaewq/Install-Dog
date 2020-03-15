package com.modosa.apkinstaller.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.modosa.apkinstaller.R;
import com.modosa.apkinstaller.util.NotifyUtil;
import com.modosa.apkinstaller.util.PackageInstallerUtil;
import com.modosa.apkinstaller.util.ResultUtil;

import java.io.File;

/**
 * @author dadaewq
 */
public class Installer5Activity extends AbstractInstallerActivity {

    private final Context context = this;
    private String realPath;
    private String uninstallPkgName;

    @Override
    public void startInstall(String getinstallApkPath) {
        Log.d("Start install", getinstallApkPath + "");
        if (getinstallApkPath != null) {
            realPath = getinstallApkPath;
            installApkFile = new File(getinstallApkPath);
            new InstallApkTask().start();
        }
    }

    @Override
    protected void startUninstall(String getUninstallPkgName) {
        uninstallPkgName = getUninstallPkgName;
        new UninstallApkTask().start();
    }

    private class InstallApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            showMyToast0(String.format(getString(R.string.tip_start_install), apkinfo[0]));
            try {
                String result = PackageInstallerUtil.installPackage(context, Uri.fromFile(installApkFile), null);
                if (result == null) {
                    showMyToast0(String.format(getString(R.string.tip_success_install), apkinfo[0]));
                } else {
                    String err = String.format("%s: %s %s | %s | Android %s \n", getString(R.string.installer_device), Build.BRAND, Build.MODEL, ResultUtil.isMiui() ? "MIUI" : " ", Build.VERSION.RELEASE) +
                            String.format(alertDialogMessage + "\n%s", result);
                    copyErr(err);
                    showMyToast1(String.format(getString(R.string.tip_failed_install_witherror), apkinfo[0], result));
                }
                if (show_notification) {
                    Log.e("packagename", apkinfo[1]);
                    if (result == null) {
                        deleteCache();
                        new NotifyUtil(context).sendNotification("5", String.format(getString(R.string.tip_success_install), apkinfo[0]), apkinfo[1]);
                    } else {
                        new NotifyUtil(context).sendNotification("21", String.format(getString(R.string.tip_failed_install), apkinfo[0]), apkinfo[1], realPath, istemp);
                    }
                } else {
                    deleteCache();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class UninstallApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            Log.d("Start uninstall", uninstallPkgName);

            try {
                String result = PackageInstallerUtil.uninstallPackage(context, uninstallPkgName);
                if (result == null) {
                    showMyToast0(String.format(getString(R.string.tip_success_uninstall), uninstallPackageLable));
                } else {
                    copyErr(String.format("%s: %s %s | %s | Android %s \n\n%s\n%s", getString(R.string.installer_device), Build.BRAND, Build.MODEL, ResultUtil.isMiui() ? "MIUI" : " ", Build.VERSION.RELEASE, alertDialogMessage, result));
                    showMyToast1(String.format(getString(R.string.tip_failed_uninstall_witherror), uninstallPackageLable, result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}