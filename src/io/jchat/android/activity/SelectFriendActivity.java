package io.jchat.android.activity;

import android.os.Bundle;
import com.jmessage.jdemo.R;
import io.jchat.android.controller.SelectFriendController;
import io.jchat.android.view.SelectFriendView;

public class SelectFriendActivity extends BaseActivity {

    private SelectFriendView mView;
    private SelectFriendController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
        mView = (SelectFriendView) findViewById(R.id.select_friend_view);
        float ratioWidth = (float)mWidth / 720;
        float ratioHeight = (float)mHeight / 1280;
        float ratio = Math.min(ratioWidth, ratioHeight);
        mView.initModule(ratio);
        mController = new SelectFriendController(mView, this);
        mView.setListeners(mController);
        mView.setSideBarTouchListener(mController);
        mView.setTextWatcher(mController);
    }
}
