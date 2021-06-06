package com.ztftrue.tool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SystemUtils {


    public void returnHome(Context context) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home, context);
    }

    public void startApplication(ResolveInfo info, Context context) {
        //该应用的包名
        String pkg = info.activityInfo.packageName;
        //应用的主activity类
        String cls = info.activityInfo.name;
        ComponentName component = new ComponentName(pkg, cls);
        Intent intent = new Intent();
        intent.setComponent(component);
        startActivity(intent, context);
    }

    public static String startCommand(String command) throws InterruptedException, IOException {
        Process p = new ProcessBuilder("su").start();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream())); BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            writer.write(command);
            writer.write("\n");
            writer.write("exit\n");
            writer.flush();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            p.waitFor();
            return sb.toString();
        }
    }

    private void startActivity(Intent intent, Context context) {
        context.startActivity(intent);
    }


}
