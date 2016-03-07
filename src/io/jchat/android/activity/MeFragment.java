package io.jchat.android.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.File;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import com.jmessage.jdemo.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.controller.MeController;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.FileHelper;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.SharePreferenceManager;
import io.jchat.android.view.MeView;

public class MeFragment extends BaseFragment {

    private static final String TAG = MeFragment.class.getSimpleName();

    private View mRootView;
    private MeView mMeView;
    private MeController mMeController;
    private Context mContext;
    private String mPath;
    private boolean mIsShowAvatar = false;
    private boolean mIsGetAvatar = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_me,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mMeView = (MeView) mRootView.findViewById(R.id.me_view);
        mMeView.initModule(mDensity, mWidth);
        mMeController = new MeController(mMeView, this, mWidth);
        mMeView.setListeners(mMeController);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        if (!mIsShowAvatar) {
            UserInfo myInfo = JMessageClient.getMyInfo();
            if (!TextUtils.isEmpty(myInfo.getAvatar())) {
                myInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            mMeView.showPhoto(bitmap);
                            mIsShowAvatar = true;
                        } else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
            mMeView.showNickName(myInfo.getNickname());
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //退出登录
    public void Logout() {
        // TODO Auto-generated method stub
        final Intent intent = new Intent();
        UserInfo info = JMessageClient.getMyInfo();
        if (null != info) {
            intent.putExtra("userName", info.getUserName());
            File file = info.getAvatarFile();
            if (file != null && file.isFile()) {
                intent.putExtra("avatarFilePath", file.getAbsolutePath());
            } else {
                String path = FileHelper.getUserAvatarPath(info.getUserName());
                file = new File(path);
                if (file.exists()) {
                    intent.putExtra("avatarFilePath", file.getAbsolutePath());
                }
            }
            SharePreferenceManager.setCachedUsername(info.getUserName());
            SharePreferenceManager.setCachedAvatarPath(file.getAbsolutePath());
            JMessageClient.logout();
            intent.setClass(mContext, ReloginActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "user info is null!");
        }
    }

    public void StartSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), SettingActivity.class);
        startActivity(intent);
    }

    public void startMeInfoActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), MeInfoActivity.class);
        startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_ME_INFO);
    }

    public void cancelNotification() {
        NotificationManager manager = (NotificationManager) this.getActivity().getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    //照相
    public void takePhoto() {
        if (FileHelper.isSdCardExist()) {
            mPath = FileHelper.createAvatarPath(JMessageClient.getMyInfo().getUserName());
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPath)));
            try {
                getActivity().startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_TAKE_PHOTO);
            } catch (ActivityNotFoundException anf) {
                Toast.makeText(this.getActivity(), mContext.getString(R.string.camera_not_prepared), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPhotoPath() {
        return mPath;
    }

    //选择本地图片
    public void selectImageFromLocal() {
        if (FileHelper.isSdCardExist()) {
            Intent intent;
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
            getActivity().startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_SELECT_PICTURE);
        } else {
            Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        }

    }

    public void loadUserAvatar(String path) {
        if (null != mMeView) {
            mIsGetAvatar = true;
            mMeView.showPhoto(path);
        }
    }

    //预览头像
    public void startBrowserAvatar() {
        final UserInfo myInfo = JMessageClient.getMyInfo();
        //如果本地保存了图片，直接加载，否则下载
        if (mIsGetAvatar) {
            String path = FileHelper.getUserAvatarPath(myInfo.getUserName());
            File file = new File(path);
            if (file.exists()) {
                Intent intent = new Intent();
                intent.putExtra("browserAvatar", true);
                intent.putExtra("avatarPath", path);
                intent.setClass(mContext, BrowserViewPagerActivity.class);
                startActivity(intent);
            } else if (!TextUtils.isEmpty(myInfo.getAvatar())) {
                getBigAvatar(myInfo);
            }
        } else if (!TextUtils.isEmpty(myInfo.getAvatar())) {
            getBigAvatar(myInfo);
        }
    }

    private void getBigAvatar(final UserInfo myInfo) {
        final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                mContext.getString(R.string.loading));
        dialog.show();
        myInfo.getBigAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int status, String desc, Bitmap bitmap) {
                if (status == 0) {
                    mIsGetAvatar = true;
                    String path = BitmapLoader.saveBitmapToLocal(bitmap, mContext, myInfo.getUserName());
                    Intent intent = new Intent();
                    intent.putExtra("browserAvatar", true);
                    intent.putExtra("avatarPath", path);
                    intent.setClass(mContext, BrowserViewPagerActivity.class);
                    startActivity(intent);
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
                dialog.dismiss();
            }
        });
    }

    public void refreshNickname(String newName) {
        mMeView.showNickName(newName);
    }
}
