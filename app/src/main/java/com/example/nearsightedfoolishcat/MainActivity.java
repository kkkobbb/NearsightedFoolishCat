package com.example.nearsightedfoolishcat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.EventListener;
import java.util.LinkedList;

/**
 * タグを検出すると自動で起動する
 */
public class MainActivity extends AppCompatActivity {
    final LinkedList<OnNewIntentListener> onNewIntentListeners = new LinkedList<>();

    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private String[][] techLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // アダプタ取得
        adapter =  ((NfcManager)getSystemService(Context.NFC_SERVICE)).getDefaultAdapter();

        // NFC用インテント通知の設定
        pendingIntent = NfcController.createPendingIntent(this);
        // NFC用インテントフィルタ
        intentFilters = NfcController.createIntentFilters();
        // NFC用 tech list
        techLists = NfcController.createTechList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null) {
            // Resume以後でないと例外が発生する
            // 起動時のみインテントを受け取る
            adapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // 終了時のみ
        if (isFinishing() && adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO listenerの登録元のFragmentが破棄された場合、どうなる？
        // 登録されたリスナーを実行する
        for (OnNewIntentListener listener : onNewIntentListeners) {
            listener.onNewIntent(intent);
        }
    }

    /**
     * インテントを受け取った際のイベントを登録する
     * @param listener イベントリスナー
     */
    void addOnNewIntentListener(OnNewIntentListener listener) {
        onNewIntentListeners.add(listener);
    }

    interface OnNewIntentListener extends EventListener {
        void onNewIntent(Intent intent);
    }
}
