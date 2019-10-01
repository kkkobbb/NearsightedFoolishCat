package com.example.nearsightedfoolishcat;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.support.annotation.NonNull;

/**
 * NFCの内容を表示する。
 * NFC A と NFC F に対応している。
 * NDEF形式の場合、さらに詳細を表示する
 */
class NfcController {

    /**
     * NFCの情報を文字列で返す
     * (動作確認用)
     * @param intent 受信したNFCのインテント
     * @return タグ情報
     */
    static @NonNull String getNfcInfo(final Intent intent) {
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
            } else if (tech.equals(Ndef.class.getName())) {
                hasNdef = true;
            } else if (tech.equals(NfcF.class.getName())) {
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
    private static @NonNull String getNfcInfoNfcA(final Tag tag) {
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
    private static @NonNull String getNfcInfoNdef(final Tag tag) {
        // NDEF形式へ変換
        final Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            return "";
        }

        final StringBuilder info = new StringBuilder();

        // NDEFメッセージサイズ取得
        final int maxSize = ndef.getMaxSize();
        info.append("\nMaxSize: ").append(maxSize);

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
    private static @NonNull String getNfcInfoNfcF(final Tag tag) {
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
