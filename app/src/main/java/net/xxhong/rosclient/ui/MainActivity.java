package net.xxhong.rosclient.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;

import net.xxhong.rosclient.R;
import net.xxhong.rosclient.RCApplication;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Bind(R.id.et_ip)
    EditText etIP;
    @Bind(R.id.et_port)
    EditText etPort;

    ROSBridgeClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    private void connect(String ip, String port) {
        client = new ROSBridgeClient("ws://" + ip + ":" + port);
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication)getApplication()).setRosClient(client);
                showTip("Connect ROS success");
                Log.d(TAG,"Connect ROS success");
                startActivity(new Intent(MainActivity.this,NodesActivity.class));
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                showTip("ROS disconnect");
                Log.d(TAG,"ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                showTip("ROS communication error");
                Log.d(TAG,"ROS communication error");
            }
        });
    }

    private void showTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, tip,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick({R.id.tv_ros,R.id.btn_connect})
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tv_ros:
                break;
            case R.id.btn_connect:
                String ip = etIP.getText().toString();
                String port = etPort.getText().toString();
                connect(ip, port);
                break;
            default:break;
        }
    }
}
