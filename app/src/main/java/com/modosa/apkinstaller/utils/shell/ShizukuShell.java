package com.modosa.apkinstaller.utils.shell;

import android.util.Log;

import androidx.annotation.Nullable;

import com.modosa.apkinstaller.utils.IOUtils;
import com.modosa.apkinstaller.utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;

import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuService;

public class ShizukuShell implements Shell {
    private static final String TAG = "ShizukuShell";

    private static ShizukuShell sInstance;

    private ShizukuShell() {
        sInstance = this;
    }

    public static ShizukuShell getInstance() {
        synchronized (ShizukuShell.class) {
            return sInstance != null ? sInstance : new ShizukuShell();
        }
    }

    @Override
    public boolean isAvailable() {
        if (!ShizukuService.pingBinder()) {
            return false;
        }

        try {
            return exec(new Command("echo", "test")).isSuccessful();
        } catch (Exception e) {
            Log.w(TAG, e);
            return false;
        }
    }

    @Override
    public Result exec(Command command) {
        return execInternal(command, null);
    }

    @Override
    public Result exec(Command command, InputStream inputPipe) {
        return execInternal(command, inputPipe);
    }

    private Result execInternal(Command command, @Nullable InputStream inputPipe) {
        StringBuilder stdOutSb = new StringBuilder();
        StringBuilder stdErrSb = new StringBuilder();

        try {
            RemoteProcess process = ShizukuService.newProcess(command.toStringArray(), null, null);

            Thread stdOutD = IOUtils.writeStreamToStringBuilder(stdOutSb, process.getInputStream());
            Thread stdErrD = IOUtils.writeStreamToStringBuilder(stdErrSb, process.getErrorStream());

            if (inputPipe != null) {
                try (OutputStream outputStream = process.getOutputStream(); InputStream inputStream = inputPipe) {
                    IOUtils.copyStream(inputStream, outputStream);
                }
            }

            process.waitFor();
            stdOutD.join();
            stdErrD.join();

            return new Result(command, process.exitValue(), stdOutSb.toString().trim(), stdErrSb.toString().trim());
        } catch (Exception e) {
            Log.w(TAG, "Unable execute command: ");
            Log.w(TAG, e);
            return new Result(command, -1, stdOutSb.toString().trim(), stdErrSb.toString() + "\n\n<!> SAI ShizukuShell Java exception: " + Utils.throwableToString(e));
        }
    }
}
