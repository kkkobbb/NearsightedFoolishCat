package com.example.nearsightedfoolishcat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.simpleNfc.NfcIntentManager;

import java.util.EventListener;
import java.util.LinkedList;

/**
 * 起動時にNFCインテントを受け取るように設定する
 */
public class MainActivity extends AppCompatActivity {
    final LinkedList<OnNewNfcIntentListener> onNewNfcIntentListeners = new LinkedList<>();

    private NfcIntentManager nfcIntentManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // インテント受け取り用の初期化
        if (nfcIntentManager == null) {
            nfcIntentManager = new NfcIntentManager(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcIntentManager != null) {
            // インテントを受け取れるようにする
            nfcIntentManager.enable();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // 終了時のみの処理
        if (isFinishing() && nfcIntentManager != null) {
            nfcIntentManager.disable();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        // NFC用のインテントの場合のみ、登録されたリスナーを実行する
        if (!NfcIntentManager.isNfcIntent(intent))
            return;

        // TODO listenerの登録元のFragmentが破棄された場合、どうなる？
        for (OnNewNfcIntentListener listener : onNewNfcIntentListeners) {
            listener.onNewNfcIntent(intent);
        }
    }

    /**
     * NFC用のインテントを受け取った際のイベントを登録する
     * @param listener イベントリスナー
     */
    void addOnNewNfcIntentListener(final OnNewNfcIntentListener listener) {
        onNewNfcIntentListeners.add(listener);
    }

    interface OnNewNfcIntentListener extends EventListener {
        void onNewNfcIntent(final Intent intent);
    }
}
