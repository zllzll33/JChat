package io.jchat.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import com.jmessage.jdemo.R;
import io.jchat.android.tools.HandleResponseCode;


/**
 * Created by Ken on 2015/2/13.
 */
public class SelectAreaActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mCommitBtn;
    private EditText mEditAreaEt;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_area);
        mContext = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.title_tv);
        mCommitBtn = (Button) findViewById(R.id.commit_btn);
        mEditAreaEt = (EditText) findViewById(R.id.edit_area_et);
        mEditAreaEt.setHint(getIntent().getStringExtra("OldRegion"));
        mTitle.setText(this.getString(R.string.input_location_title));
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String region = mEditAreaEt.getText().toString().trim();
                if (TextUtils.isEmpty(region)) {
                    Toast.makeText(mContext, mContext.getString(R.string.input_area_error), Toast.LENGTH_SHORT).show();
                } else {
                    final ProgressDialog dialog = new ProgressDialog(mContext);
                    dialog.setMessage(mContext.getString(R.string.modifying_hint));
                    dialog.show();
                    UserInfo myUserInfo = JMessageClient.getMyInfo();
                    myUserInfo.setRegion(region);
                    JMessageClient.updateMyInfo(UserInfo.Field.region, myUserInfo, new BasicCallback() {
                        @Override
                        public void gotResult(final int status, final String desc) {
                            dialog.dismiss();
                            if (status == 0) {
                                Toast.makeText(mContext, mContext.getString(R.string.modify_success_toast),
                                        Toast.LENGTH_SHORT).show();
                                Toast.makeText(mContext, mContext.getString(R.string.modify_success_toast),
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.putExtra("region", region);
                                setResult(1, intent);
                                finish();
                            } else {
                                dismissSoftInput();
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void dismissSoftInput() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
