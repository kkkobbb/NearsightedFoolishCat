package com.example.nearsightedfoolishcat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.support.annotation.NonNull;

import java.util.Objects;

class NfcController {

    /**
     * NFC用のインテント通知の設定を生成する
     * @param activity インテントを受け取るアクティビティ
     * @return インテント通知の設定
     */
    static PendingIntent createPendingIntent(Activity activity) {
        return PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    /**
     * NFC用のインテントを受け取るためのフィルタを生成する
     * @return インテントのフィルタ
     */
    static IntentFilter[] createIntentFilters() {
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        return new IntentFilter[] {ndefDetected, techDetected, tagDetected};
    }

    /**
     * NFC用のインテントを受け取るためのフィルタの詳細
     * @return フィルタの詳細
     */
    static String[][] createTechList() {
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
     * NFCの情報を文字列で返す
     * (動作確認用)
     * @param intent 受信インテント
     * @return タグ情報
     */
    static @NonNull String getNfcInfo(Intent intent) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        final StringBuilder info = new StringBuilder();

        // IDm 取得
        final byte[] idm = tag.getId();  // never null
        final StringBuilder idmInfo = new StringBuilder();
        for (byte b : idm) {
            idmInfo.append(String.format("%02X", b));
        }
        info.append("IDm: ").append(idmInfo);

        // NFCの種類の情報取得
        final String[] techList = tag.getTechList();
        if (techList == null) {
            return info.toString();
        }

        // techList 取得
        // 形式の確認
        boolean hasNdef = false;
        boolean hasNfcA = false;
        boolean hasNfcF = false;
        info.append("\ntechList:");
        for (String tech : techList) {
            if (tech.equals(NfcA.class.getName())) {
                hasNfcA = true;
            }
            if (tech.equals(Ndef.class.getName())) {
                hasNdef = true;
            }
            if (tech.equals(NfcF.class.getName())) {
                hasNfcF = true;
            }
            info.append("\n  ").append(tech);
        }

        if (hasNfcA) {
            final String infoNfcA = getNfcInfoNfcA(tag);
            info.append("\n* NfcA").append(infoNfcA);
        }

        if (hasNfcF) {
            final String infoNfcF = getNfcInfoNfcF(tag);
            info.append("\n* NfcF").append(infoNfcF);
        }

        if (hasNdef) {
            final String infoNdef = getNfcInfoNdef(tag);
            info.append("\n* NDEF ").append(infoNdef);
            // NdefMessage内の情報
            final String messageInfo = NfcNdefReader.getMessageInfo(intent);
            info.append("\nMessages: \n").append(messageInfo);
        }

        return info.toString();
    }

    /**
     * NFC typeA の表示
     * (動作確認用)
     * @param tag NFC typeA のタグ
     * @return タグ情報
     */
    private static @NonNull String getNfcInfoNfcA(Tag tag) {
        // NFC A形式への変換
        final NfcA nfcA = NfcA.get(tag);
        if (nfcA == null) {
            return "";
        }

        final StringBuilder info = new StringBuilder();

        // 送信可能な最大バイト数
        final int maxTransceiveLength = nfcA.getMaxTransceiveLength();
        info.append("\nMax Transceive Length: ").append(maxTransceiveLength);

        // ATQA 取得
        final byte[] atqa = nfcA.getAtqa();
        if (atqa != null) {
            final StringBuilder atqaInfo = new StringBuilder();
            for (byte b : atqa) {
                atqaInfo.insert(0, String.format("%02X", b));
            }
            info.append("\nATQA: ").append(atqaInfo);
        }

        // SAK 取得
        final short sak = nfcA.getSak();
        info.append(String.format("\nSAK: %02X", sak));

        return info.toString();
    }

    /**
     * NDEF形式の表示
     * (動作確認用)
     * @param tag NDEF形式のタグ
     * @return タグ情報
     */
    private static @NonNull String getNfcInfoNdef(Tag tag) {
        // NDEF形式へ変換
        final Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            return "";
        }

        final StringBuilder info = new StringBuilder();

        // NDEFメッセージサイズ取得
        final int maxSize = ndef.getMaxSize();
        info.append("\nMaxSize: ").append(Integer.toString(maxSize));

        // タグtype取得
        final String tagType = ndef.getType();
        info.append("\ntag type: ").append(tagType);

        // 書き込み可能か
        final boolean isWritable = ndef.isWritable();
        info.append("\nisWritable: ").append(isWritable);

        // 読み取り専用にできるか
        final boolean canMakeReadOnly = ndef.canMakeReadOnly();
        info.append("\ncanMakeReadOnly: ").append(canMakeReadOnly);

        return info.toString();
    }

    /**
     * NFC typeF の表示 (Felicaの場合)
     * (動作確認用)
     * @param tag NFC typeF のタグ
     * @return タグ情報
     */
    private static @NonNull String getNfcInfoNfcF(Tag tag) {
        // NfcF 形式への変換
        final NfcF nfcF = NfcF.get(tag);
        if (nfcF == null) {
            return "";
        }

        final StringBuilder info = new StringBuilder();

        final byte[] manufacturer = nfcF.getManufacturer();
        final StringBuilder infoM = new StringBuilder();
        for (byte b : manufacturer) {
            infoM.append(String.format("%02X ", b));
        }
        info.append("\nManufacturer: ").append(infoM);

        final int maxTransceiveLength = nfcF.getMaxTransceiveLength();
        info.append("\nMax Transceive Length: ").append(maxTransceiveLength);

        final byte[] systemCode = nfcF.getSystemCode();
        final StringBuilder infoSC = new StringBuilder();
        for (byte b : systemCode) {
            infoSC.append(String.format("%02X ", b));
        }
        info.append("\nSystem Code: ").append(infoSC);

        return info.toString();
    }

}
