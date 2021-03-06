package com.example.nearsightedfoolishcat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simpleNfc.SimpleNfc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 操作用の画面
 */
public class ConsoleFragment extends Fragment {
    /** 表示するview */
    private View view;

    private State state = State.RECV;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        // 表示用のviewの生成
        view = inflater.inflate(R.layout.console, container, false);

        // activityにNFC用のインテントを受け取った際のイベントリスナーを登録する
        final Activity activity = Objects.requireNonNull(getActivity());
        if (activity instanceof MainActivity) {
            final MainActivity mainActivity = (MainActivity) activity;
            final ConsoleFragment self = this;
            mainActivity.addOnNewNfcIntentListener(new MainActivity.OnNewNfcIntentListener() {
                @Override
                public void onNewNfcIntent(final Intent intent) {
                    self.onNewNfcIntent(intent);
                }
            });
        }

        return view;
    }

    /**
     * NFC用のインテント受け取り処理
     * (MainActivityのイベントに登録する用)
     * @param intent 受け取ったNFC用インテント
     */
    void onNewNfcIntent(final Intent intent) {
        final String action = Objects.requireNonNull(intent.getAction());

        if (state == State.RECV) {
            // 受信処理
            final String NfcInfo = action + "\n\n" + SimpleNfc.getNfcInfo(intent);
            final TextView textViewRecv = view.findViewById(R.id.textViewRecv);
            textViewRecv.setText(NfcInfo);
        }

        if (state == State.SEND) {
            // 送信処理
            final EditText editTextSend = view.findViewById(R.id.editTextSend);
            final SpannableStringBuilder sb = (SpannableStringBuilder) editTextSend.getText();
            final String text = sb.toString();
            final boolean sent = SimpleNfc.setNfcText(intent, text);
            if (!sent) {
                show("送信に失敗しました");
            }
        }

        if (state == State.SEND_AAR) {
            // AAR情報送信処理
            final EditText editTextSend = view.findViewById(R.id.editTextSend);
            final SpannableStringBuilder sb = (SpannableStringBuilder) editTextSend.getText();
            final String pkgName = sb.toString();
            final boolean sent = SimpleNfc.setNfcAAR(intent, pkgName);
            if (!sent) {
                show("送信に失敗しました");
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 状態変更用のスピナー
        final List<String> spinnerItemList = new ArrayList<>();
        for (State st : State.values()) {
            spinnerItemList.add(getResources().getString(st.getId()));
        }
        final Spinner spinnerOperation = view.findViewById(R.id.spinnerOperation);
        final ArrayAdapter<String> adapterSpinnerOperation = new ArrayAdapter<>(
                spinnerOperation.getContext(), R.layout.spinner_item, spinnerItemList);
        adapterSpinnerOperation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOperation.setAdapter(adapterSpinnerOperation);
        spinnerOperation.setSelection(0, false);
        spinnerOperation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 状態を変更する
                State[] states = State.values();
                if (states.length <= position) {
                    return;
                }
                state = states[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Button button1 = view.findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO ボタン押下時の動作
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_none:
                show("なにもしない");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 簡易版メッセージ表示 （とりあえずtoast）
     * @param msg 表示するメッセージ
     */
    private void show(final String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    enum State {
        /** 読み込み */
        RECV(R.string.spinner_recv),
        /** 書き込み */
        SEND(R.string.spinner_send),
        /** AAR 書き込み */
        SEND_AAR(R.string.spinner_send_aar);

        private final int id;

        State(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }
    }
}
