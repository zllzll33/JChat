package io.jchat.android.activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import de.greenrobot.event.EventBus;
import com.jmessage.jdemo.R;
import io.jchat.android.controller.ConversationListController;
import io.jchat.android.controller.MenuItemController;
import io.jchat.android.entity.Event;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.ConversationListView;
import io.jchat.android.view.MenuItemView;

/*
 * 会话列表界面
 */
public class ConversationListFragment extends BaseFragment {

    private static String TAG = ConversationListFragment.class.getSimpleName();
    private View mRootView;
    private ConversationListView mConvListView;
    private ConversationListController mConvListController;
    private PopupWindow mMenuPopWindow;
    private View mMenuView;
    private MenuItemView mMenuItemView;
    private MenuItemController mMenuController;
    private Activity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        JMessageClient.registerEventReceiver(this);
        EventBus.getDefault().register(this);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_conv_list,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mConvListView = new ConversationListView(mRootView, this.getActivity());
        mConvListView.initModule();
        mMenuView = getActivity().getLayoutInflater().inflate(R.layout.drop_down_menu, null);
        mConvListController = new ConversationListController(mConvListView, this, mDensityDpi, mWidth);
        mConvListView.setListener(mConvListController);
        mConvListView.setItemListeners(mConvListController);
        mConvListView.setLongClickListener(mConvListController);
        mMenuPopWindow = new PopupWindow(mMenuView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, true);
        mMenuItemView = new MenuItemView(mMenuView);
        mMenuItemView.initModule();
        mMenuController = new MenuItemController(mMenuItemView, this, mConvListController);
        mMenuItemView.setListeners(mMenuController);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    //显示下拉菜单
    public void showMenuPopWindow() {
        mMenuPopWindow.setTouchable(true);
        mMenuPopWindow.setOutsideTouchable(true);
        mMenuPopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(),
                (Bitmap) null));
        if (mMenuPopWindow.isShowing()) {
            mMenuPopWindow.dismiss();
        } else
            mMenuPopWindow.showAsDropDown(mRootView.findViewById(R.id.create_group_btn), -10, -5);
    }

    /**
     * 在会话列表中接收消息
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        Message msg = event.getMessage();
        Log.d("JMessage", "收到消息：msg = " + msg.toString());
        ConversationType convType = msg.getTargetType();
        if (convType == ConversationType.group) {
            long groupID = ((GroupInfo) msg.getTargetInfo()).getGroupID();
            Conversation conv = JMessageClient.getGroupConversation(groupID);
            if (conv != null && mConvListController != null) {
                mConvListController.refreshConvList(conv);
            }
        } else {
            final UserInfo userInfo = (UserInfo) msg.getTargetInfo();
            final String targetID = userInfo.getUserName();
            final Conversation conv = JMessageClient.getSingleConversation(targetID);
            if (conv != null && mConvListController != null) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果设置了头像
                        if (!TextUtils.isEmpty(userInfo.getAvatar())){
                            //如果本地不存在头像
                            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int status, String desc, Bitmap bitmap) {
                                    if (status == 0) {
                                        mConvListController.getAdapter().notifyDataSetChanged();
                                    } else {
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                }
                            });
                        }
                    }
                });
                mConvListController.refreshConvList(conv);
            }
        }
    }

    /**
     * 收到创建单聊的消息
     *
     * @param event 可以从event中得到targetID
     */
    public void onEventMainThread(Event.StringEvent event) {
        Log.d(TAG, "StringEvent execute");
        String targetID = event.getTargetID();
        Conversation conv = JMessageClient.getSingleConversation(targetID);
        if (conv != null) {
            mConvListController.getAdapter().addNewConversation(conv);
        }
    }

    /**
     * 收到创建群聊的消息
     *
     * @param event 从event中得到groupID
     */
    public void onEventMainThread(Event.LongEvent event) {
        long groupID = event.getGroupID();
        Conversation conv = JMessageClient.getGroupConversation(groupID);
        if (conv != null) {
            mConvListController.getAdapter().addNewConversation(conv);
        }
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
        dismissPopWindow();
        super.onResume();
    }

    public void dismissPopWindow() {
        if (mMenuPopWindow.isShowing()) {
            mMenuPopWindow.dismiss();
        }
    }


    @Override
    public void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    public void StartCreateGroupActivity() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), CreateGroupActivity.class);
        startActivity(intent);
    }

    public void sortConvList() {
        if (mConvListController != null) {
            mConvListController.getAdapter().sortConvList();
        }
    }

}
