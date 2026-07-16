package com.moodlink.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(255, 248, 245));

        TextView title = new TextView(this);
        title.setText("Mood");
        title.setTextColor(Color.rgb(53, 42, 41));
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTypeface(title.getTypeface(), 1);
        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView intro = new TextView(this);
        intro.setText("配对一次，以后直接点桌面小组件，不会打开网页。");
        intro.setTextColor(Color.rgb(138, 119, 116));
        intro.setTextSize(15);
        intro.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams introParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        introParams.setMargins(0, dp(10), 0, dp(28));
        root.addView(intro, introParams);

        EditText url = new EditText(this);
        url.setHint("配对地址");
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        url.setSingleLine(true);
        url.setText(MoodStore.serverUrl(this));
        root.addView(url, fieldParams());

        EditText token = new EditText(this);
        token.setHint("配对密钥");
        token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        token.setSingleLine(true);
        token.setText(MoodStore.token(this));
        LinearLayout.LayoutParams tokenParams = fieldParams();
        tokenParams.setMargins(0, dp(12), 0, dp(18));
        root.addView(token, tokenParams);

        Button save = new Button(this);
        save.setText("保存配对");
        save.setTextSize(17);
        save.setTextColor(Color.WHITE);
        save.setBackgroundColor(Color.rgb(169, 77, 86));
        save.setOnClickListener(v -> {
            String address = url.getText().toString().trim();
            String secret = token.getText().toString().trim();
            if (!address.startsWith("https://") || secret.length() < 16) {
                Toast.makeText(this, "地址或密钥不对，再检查一下", Toast.LENGTH_SHORT).show();
                return;
            }
            MoodStore.saveConnection(this, address, secret);
            MoodStore.setLastStatus(this, "已配对 · 点一下就好");
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            int[] ids = manager.getAppWidgetIds(new ComponentName(this, MoodWidgetProvider.class));
            MoodWidgetProvider.updateWidgets(this, manager, ids);
            Toast.makeText(this, "配对好了", Toast.LENGTH_SHORT).show();
        });
        root.addView(save, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        TextView help = new TextView(this);
        help.setText("然后长按手机桌面 → 小组件 → Mood，把它拖到桌面。");
        help.setTextColor(Color.rgb(138, 119, 116));
        help.setTextSize(14);
        help.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams helpParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        helpParams.setMargins(0, dp(24), 0, 0);
        root.addView(help, helpParams);

        setContentView(root);
    }

    private LinearLayout.LayoutParams fieldParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
    }
}
