package com.fixbugs.android.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        AppManager.getAppManager().addActivity(this);
    }

    public void back(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }

    protected Activity getActivity() {
        return this;
    }

    public void toast(Object content) {
        if (content instanceof String) {
            Toast.makeText(this, (CharSequence) content, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, this.getString((Integer) content), Toast.LENGTH_SHORT).show();
        }
    }

    protected void exit() {
        AppManager.getAppManager().AppExit();
    }

    /**
     * 打开软键盘
     */
    protected void openKeybord(EditText editText) {
        InputMethodManager imm =
          (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     */
    protected void closeKeybord(EditText editText) {
        InputMethodManager imm =
          (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
