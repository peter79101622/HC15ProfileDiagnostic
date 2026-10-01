package com.peter.hc15profilediag;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(38, 94, 156);
    private static final int RED = Color.rgb(190, 42, 42);
    private static final int GREEN = Color.rgb(49, 92, 74);
    private static final int DARK = Color.rgb(28, 31, 29);
    private static final int MUTED = Color.rgb(92, 99, 95);
    private static final int CARD = Color.rgb(255, 255, 255);
    private static final String HC_SETTINGS_ACTION = "android.health.connect.action.HEALTH_HOME_SETTINGS";

    private LinearLayout statusBox;
    private String lastReport = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        refreshDiagnostics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (statusBox != null) refreshDiagnostics();
    }

    private View buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(246, 247, 245));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(root);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(titleRow, new LinearLayout.LayoutParams(-1, -2));

        TextView title = text("HC15 Profile Diagnostic", 25, DARK, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, -2, 1f);
        titleRow.addView(title, titleLp);

        TextView author = text("作者：Peter", 13, MUTED, false);
        titleRow.addView(author);

        TextView subtitle = text("Android 14+ / 15 工作設定檔 Health Connect 診斷｜V0.1.1", 14, MUTED, false);
        subtitle.setPadding(0, dp(4), 0, dp(18));
        root.addView(subtitle);

        TextView section = text("診斷狀態", 18, DARK, true);
        root.addView(section);

        statusBox = new LinearLayout(this);
        statusBox.setOrientation(LinearLayout.VERTICAL);
        statusBox.setPadding(dp(16), dp(14), dp(16), dp(14));
        android.graphics.drawable.GradientDrawable cardBg = new android.graphics.drawable.GradientDrawable();
        cardBg.setColor(CARD);
        cardBg.setCornerRadius(dp(14));
        statusBox.setBackground(cardBg);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.topMargin = dp(10);
        cardLp.bottomMargin = dp(18);
        root.addView(statusBox, cardLp);

        Button refresh = button("重新檢測");
        refresh.setOnClickListener(v -> refreshDiagnostics());
        root.addView(refresh, buttonLp());

        Button openHc = button("開啟 Health Connect");
        openHc.setOnClickListener(v -> openHealthConnect());
        root.addView(openHc, buttonLp());

        Button copy = button("複製診斷結果");
        copy.setOnClickListener(v -> copyReport());
        root.addView(copy, buttonLp());

        TextView note = text(
                "用途：比較主空間與 Shelter / Work Profile 是否能取得 Android Framework 的 HealthConnectManager。這個測試 App 不需要 LSPosed。",
                13, MUTED, false);
        note.setPadding(0, dp(12), 0, 0);
        root.addView(note);

        return scroll;
    }

    private void refreshDiagnostics() {
        statusBox.removeAllViews();
        StringBuilder report = new StringBuilder();

        int uid = Process.myUid();
        int userId = uid / 100000;
        UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);

        boolean isManagedProfile = false;
        boolean isProfile = false;
        try {
            if (um != null) {
                isManagedProfile = um.isManagedProfile();
                if (Build.VERSION.SDK_INT >= 30) isProfile = um.isProfile();
            }
        } catch (Throwable ignored) {}

        add("Android", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")", BLUE);
        add("裝置", Build.MANUFACTURER + " " + Build.MODEL, DARK);
        add("UID / User", uid + " / " + userId, DARK);
        add("Profile", isProfile ? "是" : "否", isProfile ? RED : BLUE);
        add("Managed Profile", isManagedProfile ? "是" : "否", isManagedProfile ? RED : BLUE);

        report.append("HC15 Profile Diagnostic V0.1.1\n");
        report.append("Author: Peter\n");
        report.append("Android: ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        report.append("UID/User: ").append(uid).append("/").append(userId).append("\n");
        report.append("isProfile: ").append(isProfile).append("\n");
        report.append("isManagedProfile: ").append(isManagedProfile).append("\n");

        String hcClass = "android.health.connect.HealthConnectManager";
        Object hcService = null;
        boolean classExists = false;
        String serviceError = null;
        try {
            Class<?> clazz = Class.forName(hcClass);
            classExists = true;
            hcService = getSystemService(clazz);
        } catch (Throwable t) {
            serviceError = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
        }

        add("HealthConnectManager 類別", classExists ? "存在" : "不存在", classExists ? BLUE : RED);
        if (hcService != null) {
            add("HealthConnectManager Service", "AVAILABLE", BLUE);
        } else {
            add("HealthConnectManager Service", "NULL", RED);
        }
        if (serviceError != null) add("Framework 檢查錯誤", serviceError, RED);

        report.append("HealthConnectManager class: ").append(classExists).append("\n");
        report.append("HealthConnectManager service: ").append(hcService != null ? "AVAILABLE" : "NULL").append("\n");
        if (serviceError != null) report.append("HealthConnectManager error: ").append(serviceError).append("\n");

        ComponentName controller = resolveHealthConnectController();
        add("Health Connect Controller", controller != null ? controller.flattenToShortString() : "無法解析", controller != null ? BLUE : RED);
        report.append("Health Connect controller: ").append(controller != null ? controller.flattenToShortString() : "UNRESOLVED").append("\n");

        PackageState hcControllerPkg = getPackageState("com.google.android.healthconnect.controller");
        PackageState hcDataPkg = getPackageState("com.google.android.apps.healthdata");
        add("controller 套件", hcControllerPkg.label(), hcControllerPkg.installed ? DARK : RED);
        add("healthdata 套件", hcDataPkg.label(), hcDataPkg.installed ? DARK : RED);
        report.append("com.google.android.healthconnect.controller: ").append(hcControllerPkg.label()).append("\n");
        report.append("com.google.android.apps.healthdata: ").append(hcDataPkg.label()).append("\n");

        String verdict;
        int verdictColor;
        if (Build.VERSION.SDK_INT >= 34 && isProfile && hcService == null) {
            verdict = "工作設定檔內 Framework Health Connect Service 不可用";
            verdictColor = RED;
        } else if (Build.VERSION.SDK_INT >= 34 && hcService != null) {
            verdict = "Framework Health Connect Service 可取得";
            verdictColor = BLUE;
        } else {
            verdict = "需要依裝置狀況進一步判讀";
            verdictColor = GREEN;
        }
        add("判讀", verdict, verdictColor);
        report.append("Verdict: ").append(verdict).append("\n");

        lastReport = report.toString();
    }

    private ComponentName resolveHealthConnectController() {
        try {
            Intent i = new Intent(HC_SETTINGS_ACTION);
            android.content.pm.ResolveInfo ri = getPackageManager().resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);
            if (ri != null && ri.activityInfo != null) {
                return new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private void openHealthConnect() {
        try {
            Intent i = new Intent(HC_SETTINGS_ACTION);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            } else {
                Toast.makeText(this, "此 Profile 無法解析 Health Connect 設定入口", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable t) {
            Toast.makeText(this, "開啟失敗：" + t.getClass().getSimpleName() + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyReport() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("HC15 Profile Diagnostic", lastReport));
            Toast.makeText(this, "已複製診斷結果", Toast.LENGTH_SHORT).show();
        }
    }

    private PackageState getPackageState(String packageName) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
            String version = "?";
            try {
                version = getPackageManager().getPackageInfo(packageName, 0).versionName;
            } catch (Throwable ignored) {}
            return new PackageState(true, ai.enabled, version);
        } catch (PackageManager.NameNotFoundException e) {
            return new PackageState(false, false, "-");
        }
    }

    private void add(String name, String value, int valueColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(5), 0, dp(5));
        TextView n = text(name, 13, MUTED, false);
        TextView v = text(value, 15, valueColor, true);
        row.addView(n);
        row.addView(v);
        statusBox.addView(row);
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return v;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setAllCaps(false);
        return b;
    }

    private LinearLayout.LayoutParams buttonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.bottomMargin = dp(10);
        return lp;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static class PackageState {
        final boolean installed;
        final boolean enabled;
        final String version;

        PackageState(boolean installed, boolean enabled, String version) {
            this.installed = installed;
            this.enabled = enabled;
            this.version = version;
        }

        String label() {
            if (!installed) return "未安裝";
            return "已安裝 / " + (enabled ? "enabled" : "disabled") + " / " + version;
        }
    }
}
