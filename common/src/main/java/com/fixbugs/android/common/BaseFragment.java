package com.fixbugs.android.common;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    public static final int RESULT_OK = -1;

    public void toast(Object content) {
        if (content instanceof String) {
            Toast.makeText(getActivity(), (CharSequence) content, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), this.getString((Integer) content), Toast.LENGTH_SHORT)
              .show();
        }
    }

    public Context getContext() {
        return getActivity();
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected Fragment getFragment() {
        return this;
    }
}
