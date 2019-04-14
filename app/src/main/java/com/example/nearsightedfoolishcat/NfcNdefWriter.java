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
        // Ndefメッセージの書き込み
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        final Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            return false;
        }

        // Ndefメッセージの生成
        /*
        NdefRecord record = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
                null, text.getBytes());
        */
        final NdefRecord record = NdefRecord.createTextRecord(null, text);
        final NdefMessage msg = new NdefMessage(new NdefRecord[] {record});

        boolean sent = false;
        try {
            ndef.connect();
            ndef.writeNdefMessage(msg);
            ndef.close();
            sent = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        return sent;
    }
}
