package com.example.nearsightedfoolishcat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

import java.util.Objects;

/**
 * NFC用のインテント受け取りの管理
 *
 * アプリ起動時のみ受け取るようにする
 */
class NfcIntentManager {
    final private Activity activity;
    final private NfcAdapter adapter;
    final private PendingIntent pendingIntent;
    final private IntentFilter[] intentFilters;
    final private String[][] techLists;

    NfcIntentManager(Activity activity)
    {
        this.activity = activity;

        adapter = ((NfcManager)this.activity.getSystemService(Context.NFC_SERVICE)).getDefaultAdapter();
        pendingIntent = createPendingIntent(activity);
        intentFilters = createIntentFilters();
        techLists = createTechList();
    }

    /**
     * インテントを受け取れるようにする
     * (Resume以降でないと例外が発生する)
     */
    void enable()
    {
        if (adapter == null) {
            return;
        }
        adapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists);
    }

    /**
     * インテントの受け取りを無効にする
     */
    void disable()
    {
        if (adapter == null) {
            return;
        }
        adapter.disableForegroundDispatch(activity);
    }

    /**
     * NFCのインテントの場合、真を返す
     * @param intent 確認するインテント
     * @return 真偽値
     */
    static boolean isNfcIntent(Intent intent) {
        final String action = Objects.requireNonNull(intent.getAction());
        switch (action) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
            case NfcAdapter.ACTION_TECH_DISCOVERED:
            case NfcAdapter.ACTION_TAG_DISCOVERED:
                return true;
            default:
                return false;
        }
    }

    /**
     * NFC用のインテント通知の設定を生成する
     * @param activity インテントを受け取るアクティビティ
     * @return インテント通知の設定
     */
    private PendingIntent createPendingIntent(Activity activity) {
        return PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    /**
     * NFC用のインテントを受け取るためのフィルタを生成する
     * @return インテントのフィルタ
     */
    private IntentFilter[] createIntentFilters() {
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        return new IntentFilter[] {ndefDetected, techDetected, tagDetected};
    }

    /**
     * NFC用のインテントを受け取るためのフィルタの詳細
     * @return フィルタの詳細
     */
    private String[][] createTechList() {
        return new String[][] {
                {NfcA.class.getName()},
                {NfcB.class.getName()},
                {NfcF.class.getName()},
                {NfcV.class.getName()},
                {Ndef.class.getName()},
                {NdefFormatable.class.getName()},
                {MifareClassic.class.getName()},
                {MifareUltralight.class.getName()}
        };
    }
}
