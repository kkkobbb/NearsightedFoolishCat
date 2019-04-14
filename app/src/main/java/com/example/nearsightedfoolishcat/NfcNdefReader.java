package com.example.nearsightedfoolishcat;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * NDEF形式の読み込み
 */
class NfcNdefReader {

    /**
     * NDEFのMessage構造の読み込み
     * (動作確認用)
     * @param intent NDEF形式のタグ読み込み時のインテント
     * @return Message情報
     */
    static String getMessageInfo(final Intent intent) {
        final Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (parcelables == null) {
            return "no message";
        }

        final StringBuilder info = new StringBuilder();
        for (Parcelable parcelable : parcelables) {
            NdefMessage ndefMessage = (NdefMessage) parcelable;
            info.append("message\n");  // 1しかないはず

            final int byteLength = ndefMessage.getByteArrayLength();
            info.append("  byte length: ").append(byteLength).append("\n");

            final NdefRecord[] ndefRecords = ndefMessage.getRecords();
            for (int i = 0; i < ndefRecords.length; i++) {
                final NdefRecord record = ndefRecords[i];
                info.append("  record[").append(i).append("]\n");

                final short tnf = record.getTnf();
                info.append("    TNF: ").append(getTnfName(tnf)).append("\n");

                final String type = new String(record.getType());
                info.append("    Type: ").append(type).append("\n");

                final byte[] rawId = record.getId();
                final StringBuilder id = new StringBuilder();
                for (byte b : rawId) {
                    id.append(String.format("%02X ", b));
                }
                info.append("    ID: ").append(id).append("\n");

                final String mime = record.toMimeType();
                info.append("    MIME: ").append(mime).append("\n");

                final byte[] payload = record.getPayload();
                StringBuilder payloadHex = new StringBuilder();
                for (int j = 0; j < payload.length; j++) {
                    if (j % 16 == 0) {
                        payloadHex.append("\n      ");
                    }
                    final byte b = payload[j];
                    payloadHex.append(String.format("%02X ", b));
                }
                info.append("    Payload hex: ").append(payloadHex).append("\n");
                info.append("    Payload language code: ")
                        .append(getLanguageCodeAtPayload(payload)).append("\n");
                info.append("    Payload text: \n")
                        .append(getTextAtPayload(payload)).append("\n");
            }
        }

        return info.toString();
    }

    /**
     * payloadのRTD text形式を読んで言語コードを返す
     * @param payload 生データ
     * @return 言語コード
     */
    static private @NonNull String getLanguageCodeAtPayload(final byte[] payload) {
        final int languageCodeLength = payload[0] & 0x3F;
        return new String(payload, 1, languageCodeLength, StandardCharsets.US_ASCII);
    }

    /**
     * payloadのRTD text形式を読んで本文を返す
     * @param payload 生データ
     * @return 本文
     */
    static private @NonNull String getTextAtPayload(final byte[] payload) {
        // payloadの先頭数byteに文字コードと言語を表す値が格納されている

        // 文字コード識別
        final boolean isUtf8 = (payload[0] & 0x80) == 0;
        final Charset charset;
        if (isUtf8) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = StandardCharsets.UTF_16;
        }

        final int languageCodeLength = payload[0] & 0x3F;
        // 先頭1バイト＋言語コード長以外が本文となる
        final int textLength = payload.length - 1 - languageCodeLength;
        return new String(payload, 1 + languageCodeLength, textLength, charset);
    }

    /**
     * TNFの値からその意味を表す文字列を返す
     * @param tnf TNFの値
     * @return TNFを表す文字列
     */
    static private @NonNull String getTnfName(final short tnf) {
        final String name;
        switch (tnf) {
            case NdefRecord.TNF_EMPTY:
                name = "TNF_EMPTY";
                break;
            case NdefRecord.TNF_WELL_KNOWN:
                name = "TNF_WELL_KNOWN";
                break;
            case NdefRecord.TNF_MIME_MEDIA:
                name = "TNF_MIME_MEDIA";
                break;
            case NdefRecord.TNF_ABSOLUTE_URI:
                name = "TNF_ABSOLUTE_URI";
                break;
            case NdefRecord.TNF_EXTERNAL_TYPE:
                name = "TNF_EXTERNAL_TYPE";
                break;
            case NdefRecord.TNF_UNKNOWN:
                name = "TNF_UNKNOWN";
                break;
            case NdefRecord.TNF_UNCHANGED:
                name = "TNF_UNCHANGED";
                break;
            default:
                name = "";
        }

        return name;
    }

    /*
     * RTD Text フォーマット
     *   NDEF形式での文字列格納 payload部分 (TNF:well-known Type:T の場合)
     *
     * 1byte目
     *   1bit目  Encode
     *     文字コードの指定
     *     0: utf-8
     *     1: utf-16
     *   2bit目  RFU (Reserved for Future Use)
     *     予約
     *   残り 6bit  Language Code Length
     *     次バイト以降で、言語を表すコードの文字数
     * 2byte目以降 (Language Code Length分)
     *   言語コード
     *     例: en ja 等
     * 残り
     *   実際の文字列
     *
     *  TODO 公式のフォーマットは？
     *  参考
     *  http://bs-nfc.blogspot.com/2012/08/rtd-text.html
     */
}
