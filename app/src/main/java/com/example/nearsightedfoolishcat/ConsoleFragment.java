package com.example.nearsightedfoolishcat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.util.Log;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * 操作用の画面
 */
public class ConsoleFragment extends Fragment {
    /** ファイル名の共通部分 */
    private static final String SAVE_BASE_NAME = "_nfc";
    /** パーミッション許可確認用 */
    private static final int REQUEST_PERMISSION = 1;

    /** 表示するview */
    private View view;

    /** 保存先ファイル */
    private File saveFile;

    private State state = State.RECV;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        // 移動先ディレクトリを決める
        final File envPictDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // 保存先ファイル名を決める
        final StringBuilder filename = new StringBuilder(SAVE_BASE_NAME);
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        filename.append("_").append(f.format(cal.getTime()));
        saveFile = new File(envPictDir, filename.toString());

        // 表示用のviewの生成
        view = inflater.inflate(R.layout.console, container, false);

        // activityにインテントを受け取った際のイベントリスナーを登録する
        final Activity activity = Objects.requireNonNull(getActivity());
        Intent intent = activity.getIntent();
        onNewIntent(intent);

        if (activity instanceof MainActivity) {
            final MainActivity mainActivity = (MainActivity) activity;
            final ConsoleFragment self = this;
            mainActivity.addOnNewIntentListener(new MainActivity.OnNewIntentListener() {
                @Override
                public void onNewIntent(Intent intent) {
                    self.onNewIntent(intent);
                }
            });
        }

        return view;
    }

    /**
     * インテント受け取り処理
     * (MainActivityのイベントに登録する用)
     * @param intent 受け取ったインテント
     */
    void onNewIntent(Intent intent) {
        final String action = Objects.requireNonNull(intent.getAction());

        // NFCのインテントの場合、タグを操作する
        if (NfcController.isNfcIntent(intent)) {
            if (state == State.RECV) {
                // 受信処理
                final String NfcInfo = action + "\n\n" + NfcController.getNfcInfo(intent);
                final TextView textViewRecv = view.findViewById(R.id.textViewRecv);
                textViewRecv.setText(NfcInfo);
            }

            if (state == State.SEND) {
                // 送信処理
                final EditText editTextSend = view.findViewById(R.id.editTextSend);
                SpannableStringBuilder sb = (SpannableStringBuilder) editTextSend.getText();
                String text = sb.toString();
                boolean sent = NfcNdefWriter.sendText(intent, text);
                if (!sent) {
                    show("送信に失敗しました");
                }
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 状態変更用のスピナー
        final List<String> spinnerNumberList = new ArrayList<>();
        spinnerNumberList.add(getResources().getString(State.RECV.getId()));
        spinnerNumberList.add(getResources().getString(State.SEND.getId()));
        final Spinner spinnerNumber = view.findViewById(R.id.spinnerNumber);
        final ArrayAdapter<String> adapterSpinnerNumber = new ArrayAdapter<>(
                spinnerNumber.getContext(), R.layout.spinner_item, spinnerNumberList);
        adapterSpinnerNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumber.setAdapter(adapterSpinnerNumber);
        spinnerNumber.setSelection(0, false);
        spinnerNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 状態を変更する
                switch (position) {
                    case 0:
                        state = State.RECV;
                        break;
                    case 1:
                        state = State.SEND;
                        break;
                }
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

        initScreen();
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
            case R.id.menu_copy_path:
                copySavePath();
                return true;
            case R.id.menu_show_save_file:
                showSaveFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 簡易版メッセージ表示 （とりあえずtoast）
     * @param msg 表示するメッセージ
     */
    private void show(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void showShort(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 画面を初期配置にする
     */
    private void initScreen() {
        // TODO 初期化処理
    }

    /**
     * 可能ならば、ファイルを読み込む
     *  (パーミッション確認あり)
     * @return 成否
     */
    private boolean loadIfPossible() {
        if (!hasPermissionSTORAGE()) {
            requestPermissionSTORAGE();
            return false;
        }

        // TODO 読み込み処理

        return true;
    }

    /**
     * 可能ならば、ファイル保存する
     *  (パーミッション確認あり)
     *  (ディレクトリ作成あり)
     * @return 成否
     */
    private boolean saveIfPossible() {
        if (!hasPermissionSTORAGE()) {
            requestPermissionSTORAGE();
            return false;
        }

        final File savePath = getSavePath();
        final File saveDir = savePath.getParentFile();
        if (!saveDir.exists()) {
            if(!saveDir.mkdir()) {
                show("出力先ディレクトリの作成に失敗しました");
                return false;
            }
        }

        writeTo(savePath);

        return true;
    }

    /**
     * 保存するファイルのフルパスを返す
     * @return フルパス
     */
    private File getSavePath() {
        return saveFile;
    }

    /**
     * 絵をファイルに書き込む
     * @param outputPath 出力先
     */
    private void writeTo(File outputPath) {
        // TODO ファイル出力処理
    }

    /**
     * 書き込み許可を持っているか
     * @return 書き込み許可がある場合、真
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")  // 常に!で確認しているが、戻り値を反対にするとややこしいため
    private boolean hasPermissionSTORAGE() {
        final int permission = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 外部ストレージ書き込み許可をユーザに求める
     */
    private void requestPermissionSTORAGE() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO パーミッション取得時の動作
                    Log.d("permission", "追加");
                } else {
                    // パーミッションが得られなかった場合、メッセージ表示
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // 拒否された場合のメッセージ（権限についての補足）
                        show("画像ファイル保存のために権限が必要です");
                    } else {
                        // 永続的に拒否された場合のメッセージ（状況説明のみ）
                        show("ストレージにアクセスできません");
                    }
                }
                break;
        }
    }

    /**
     * クリップボードに保存先ディレクトリパスをコピーする
     */
    private void copySavePath() {
        final File saveDir = getSavePath().getParentFile();
        final ClipData.Item item = new ClipData.Item(saveDir.getPath());
        final String[] mimeType = new String[1];
        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
        final ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
        final ClipboardManager cm = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);
        show(saveDir.getPath());
    }

    /**
     * 保存ファイル名を表示する
     */
    private void showSaveFile() {
        show(saveFile.getName());
    }

    enum State {
        /** 読み込み */
        RECV(R.string.spinner_recv),
        /** 書き込み */
        SEND(R.string.spinner_send);

        private final int id;

        State(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }
    }
}
