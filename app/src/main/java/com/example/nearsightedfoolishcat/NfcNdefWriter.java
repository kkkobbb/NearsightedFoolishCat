package com.example.nearsightedfoolishcat;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import java.io.IOException;

/**
 * NDEF形式の書き込み
 */
class NfcNdefWriter {

    /**
     * タグに文字列を書き込む
     * (動作確認用)
     * @param intent NDEF形式のタグ読み込み時のインテント
     * @param text 書き込む文字列
     * @return 成否
     */
    static boolean sendText(final Intent intent, final String text) {
        // Ndefメッセージの生成
        final NdefRecord record = NdefRecord.createTextRecord(null, text);
        /* 等価なNdefRecord生成
        NdefRecord record = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
                null, text.getBytes());
        */

        final NdefMessage msg = new NdefMessage(new NdefRecord[] {record});

        return send(intent, msg);
    }

    /**
     * タグにAARを書き込む
     * (動作確認用)
     * @param intent NDEF形式のタグ読み込み時のインテント
     * @param pkgName AARとして書き込むパッケージ名
     * @return 成否
     */
    static boolean sendAAR(final Intent intent, final String pkgName) {
        final NdefRecord record = NdefRecord.createApplicationRecord(pkgName);
        final NdefMessage msg = new NdefMessage(record);

        /*
         * AAR: Android Application Record
         *      NdefMessageにAARのレコードが含まれている場合、
         *      AARに記載されたアプリが優先されて起動する
         *        tnf=TNF_EXTERNAL_TYPE のレコード
         *        NdefMessageの末尾に追加が推奨？
         */

        return send(intent, msg);
    }

    /**
     * タグにメッセージを書き込む
     * @param intent NDEF形式のタグ読み込み時のインテント
     * @param msg 書き込むメッセージ
     * @return 成否
     */
    private static boolean send(final Intent intent, NdefMessage msg) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        boolean sent = false;
        try (final Ndef ndef = Ndef.get(tag)) {
            ndef.connect();
            ndef.writeNdefMessage(msg);
            sent = true;
        } catch (IOException | FormatException e) {
            e.printStackTrace();
        }

        return sent;
    }
}
