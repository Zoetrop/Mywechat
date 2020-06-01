package com.example.lc98034.mywechat;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.logging.Handler;
import java.util.logging.LogRecord;


/*
    本例演示了使用手机蓝牙实现会话功能
    实验步骤：
    （1）在两个手机分别安装、运行本应用。如果未打开手机蓝牙，则进入打开蓝牙设置界面；
    （2）在OptionMenu（选项菜单）里，选择“我的好友”，即可直接连接，再进行会话；
    （3）新建好友前，需要先使用选项菜单“设置在线”，即要求对方可见（能被扫描到）。
    然后，单击“搜索好友”按钮。
*/
public class weixinFragment extends Fragment {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private static final int REQUEST_CONNECT_DEVICE = 1;  //请求连接设备
    private static final int REQUEST_ENABLE_BT = 2;
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ChatService mChatService = null;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab01, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        setHasOptionsMenu(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        //创建选项菜单
        toolbar.inflateMenu(R.menu.option_menu);
        //选项菜单监听
        toolbar.setOnMenuItemClickListener(new MyMenuItemClickListener());
        mTitle = view.findViewById(R.id.title_left_text);
        mTitle.setText("蓝牙通信");
        mTitle = view.findViewById(R.id.title_right_text);
        // 得到本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(view.getContext(), "蓝牙不可用", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return view;
        }
        if (!mBluetoothAdapter.isEnabled()) { //若当前设备蓝牙功能未开启
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT); //
        } else {
            if (mChatService == null) {
                setupChat();  //创建会话
            }
        }

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(view.getContext(), "未授权，蓝牙搜索功能将不可用！", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public synchronized void onResume() {
        //synchronized：同步方法实现排队调用
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == ChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        mConversationArrayAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.message;
        mConversationView = view.findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        mOutEditText = view.findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = view.findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView textView = view.findViewById(R.id.edit_text_out);
                String message = textView.getText().toString();
                sendMessage(message);
            }
        });
        //创建服务对象
        mChatService = new ChatService(view.getContext(), mHandler);
        mOutStringBuffer = new StringBuffer("");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
    }
    private void ensureDiscoverable() { //修改本机蓝牙设备的可见性
        //打开手机蓝牙后，能被其它蓝牙设备扫描到的时间不是永久的
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置在300秒内可见（能被扫描）
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
            Toast.makeText(view.getContext(), "已经设置本机蓝牙设备的可见性，对方可搜索了。", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendMessage(String message) {
        if (mChatService.getState() != ChatService.STATE_CONNECTED) {
            Toast.makeText(view.getContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                //软键盘里的回车也能发送消息
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };
    //使用Handler对象在UI主线程与子线程之间传递消息
    private final Handler mHandler = new Handler() {
        @Override
        //消息处理
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("我:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
                            + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getActivity().getApplicationContext(),"链接到 " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    //返回进入好友列表操作后的数回调方法
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }else if(resultCode==Activity.RESULT_CANCELED){
                    Toast.makeText(view.getContext(), "未选择任何好友！", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(view.getContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }
    //内部类，选项菜单的单击事件处理
    private class MyMenuItemClickListener implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.scan:
                    //启动DeviceList这个Activity
                    Intent serverIntent = new Intent(getActivity(), DeviceList.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                    return true;
                case R.id.discoverable:
                    ensureDiscoverable();
                    return true;
                case R.id.back:
                    getActivity().finish();
                    System.exit(0);
                    return true;
            }
            return false;
        }
    }

}
