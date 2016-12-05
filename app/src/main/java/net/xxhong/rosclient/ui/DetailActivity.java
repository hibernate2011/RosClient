package net.xxhong.rosclient.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jilk.ros.rosapi.message.TypeDef;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.xxhong.rosclient.R;
import net.xxhong.rosclient.RCApplication;
import net.xxhong.rosclient.entity.PublishEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class DetailActivity extends Activity {

    private static final String TAG = "DetailActivity";
    ROSBridgeClient client;
    @Bind(R.id.tv_type_name)
    TextView tvTitle;
    @Bind(R.id.tv_log)
    TextView tvLog;
    @Bind(R.id.btn_topic_sub)
    Button btnSubTopic;
    @Bind(R.id.btn_call)
    Button btnCall;
    @Bind(R.id.ll_param_layout)
    LinearLayout paramContainer;

    String detailType;
    String detailName;
    TypeDef[] typeDef;

    private boolean isSubscribe = false;

    private Timer timer;
    public  boolean moving = false;
    public  float linearX = 0;
    public  float angularZ = 0;

    TreeNode root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);

        tvLog.setMovementMethod(new ScrollingMovementMethod());
        client = ((RCApplication)getApplication()).getRosClient();
        detailType= getIntent().getStringExtra("type");
        detailName= getIntent().getStringExtra("name");
        tvTitle.setText(detailType + ":" + detailName);

        try {
            if(detailType.equalsIgnoreCase("topic")) {
                typeDef = client.getTopicMessageList(detailName);
            } else if(detailType.equalsIgnoreCase("service")) {
                btnSubTopic.setVisibility(View.GONE);
                btnCall.setText("Call");
                typeDef = client.getServiceRequestList(detailName);
            }

            root = TreeNode.root();
            genParamTree(root, typeDef[0]);

            AndroidTreeView tView = new AndroidTreeView(this, root);
            paramContainer.addView(tView.getView());

            tView.expandAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(detailName.equals("/cmd_vel")) {
            processMoveTopic();
        }
    }

    private void genParamTree(TreeNode parent, TypeDef type) {
        for (int i = 0; i < type.fieldtypes.length; i++) {
            if(type.fieldtypes[i].contains("/")) {//Not basic type
                for (TypeDef t : typeDef) {
                    //set child for all fields
                    if (t.type.equals(type.fieldtypes[i])) {
                        TreeNode node = new TreeNode(new TreeViewHolder.TreeItem(type.fieldnames[i],type.fieldtypes[i],false)).setViewHolder(new TreeViewHolder(this));

                        genParamTree(node, t);
                        parent.addChild(node);
                        break;
                    }
                }
            } else {//Basic type
                TreeNode node = new TreeNode(new TreeViewHolder.TreeItem(type.fieldnames[i],type.fieldtypes[i],true)).setViewHolder(new TreeViewHolder(this));
                parent.addChild(node);
            }
        }
    }

    private String getTreeData(TreeNode parent) {
        String json = "";
        List<TreeNode> nodeList = parent.getChildren();
        for (int i = 0; i < nodeList.size(); i++) {
            TreeNode node = nodeList.get(i);
            if(node.isLeaf()) {//leaf node is basic type
                String data = ((TreeViewHolder)node.getViewHolder()).jsonData;
                json += data + ",";
            } else {
                String data = getTreeData(node);
                json +="\"" + ((TreeViewHolder)node.getViewHolder()).jsonData +  "\":{" + data + "},";
            }
        }
        if(!TextUtils.isEmpty(json))
            json = json.substring(0,json.length() - 1);
        return json;
    }

    @OnClick({R.id.btn_call,R.id.btn_topic_sub})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_topic_sub:
                if(isSubscribe) {
                    client.send("{\"op\":\"unsubscribe\",\"topic\":\"" + detailName + "\"}");
                    btnSubTopic.setText("Subscribe");
                } else {
                    client.send("{\"op\":\"subscribe\",\"topic\":\"" + detailName + "\"}");
                    btnSubTopic.setText("Unsubscribe");
                }
                isSubscribe = !isSubscribe;
                break;
            case R.id.btn_call:
                String data = getTreeData(root);

                String msg = "";
                if(detailType.equalsIgnoreCase("topic")) {
                    msg = "{\"op\":\"publish\",\"topic\":\"" + detailName + "\",\"msg\":{"+data+"}}";
                    client.send(msg);
                } else if(detailType.equalsIgnoreCase("service")) {
                    msg = "{\"op\":\"call_service\",\"service\":\"" + detailName + "\",\"args\":["+data+"]}";
                    client.send(msg);
                }

                tvLog.setText(tvLog.getText() + "Publish msg:" + msg + "\n");
                Log.d(TAG,"send msg:" + msg);
                break;
        }
    }

    //Receive data from ROS server, send from ROSBridgeWebSocketClient onMessage()
    public void onEvent(final PublishEvent event) {
        if("/map".equals(event.name)) {
            parseMapTopic(event);
            return;
        }

        //show data on TextView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(tvLog.getText().length() > 2000) {
                    tvLog.setText("");
                }

                tvLog.setText(tvLog.getText() + "\ninfo:  " + event.msg + "\n");

                int offset=tvLog.getLineCount()*tvLog.getLineHeight();
                if(offset>tvLog.getHeight()){
                    tvLog.scrollTo(0,offset-tvLog.getHeight());
                }
            }
        });
        Log.d(TAG, event.msg);
    }

    //Add TouchListener on log TextView
    private void processMoveTopic() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                if (moving) {
                    client.send("{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + linearX + ",\"y\":0,\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + angularZ + "}}}");
                    Log.d(TAG,"send cmd_vel msg:x:" + linearX + " z:" + angularZ);
                }
            }
        };

        timer.schedule(timerTask, 1000, 600);
        tvLog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moving = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();
                        int width = view.getWidth();
                        int height = view.getHeight();
//                        float mx = (x - (width / 2f)) / (width / 2f);
//                        float my = (height / 2f - y) / (height / 2f);
//                        double argular = Math.atan2(my, mx);
//                        argular = argular / (2 * Math.PI);
//                        linearX = my;
//                        angularZ = - (float) argular;

                        float mx = (x - (width / 2f));
                        float my = (height / 2f - y);
                        if(Math.abs(mx) > Math.abs(my)) {
                            angularZ = - mx / (width / 2f) / 0.5f;//Max 0.5
                        } else {
                            linearX = my / (height / 2f) / 0.5f;//Max 0.5
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        moving = false;
                        linearX = 0;
                        angularZ = 0;
                        break;
                }
                return true;
            }
        });
    }

    public void parseMapTopic(PublishEvent event) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(event.msg);
            JSONArray dataArray = (JSONArray)jsonObj.get("data");
            JSONObject jsonInfo = (JSONObject)jsonObj.get("info");
            int width = (int)(long)jsonInfo.get("width");
            int height = (int)(long)jsonInfo.get("height");

            final Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);

            int len = dataArray.size();
            int x, y, d, p;//底色黑色
            for(int i = 0; i< len; i++) {
                x = i % width;
                y = i / width;
                d = (int)(long)dataArray.get(i);
                if(d == -1) {
                    bitmap.setPixel(x, y, Color.rgb(0x59, 0x59, 0x59));
                } else {
                    p = 0x59 + (int)((0xB1 - 0x59) * d / 100f);
                    bitmap.setPixel(x, y, Color.rgb(p,p,p));
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvLog.setBackground(new BitmapDrawable(getResources(),bitmap));
                    tvLog.setText(tvLog.getText() + "Received map,set to background\n");
                }
            });

            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        client.send("{\"op\":\"unsubscribe\",\"topic\":\"" + detailName + "\"}");
        btnSubTopic.setText("Subscribe");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
