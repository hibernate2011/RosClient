package net.xxhong.rosclient.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.xxhong.rosclient.R;
import com.unnamed.b.atv.model.TreeNode;

/**TreeView holder for DetailActivity
 * Created by xxhong on 16-11-24.
 */

public class TreeViewHolder extends TreeNode.BaseNodeViewHolder<TreeViewHolder.TreeItem> {
    ImageView ivIcon;

    public String jsonData = "";

    public TreeViewHolder(Context context) { super(context); }

    @Override
    public View createNodeView(TreeNode node, final TreeItem item) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_tree_item, null, false);
        TextView tvName = (TextView) view.findViewById(R.id.tv_param_name);
        tvName.setText(item.name);
        TextView tvType = (TextView) view.findViewById(R.id.tv_param_type);
        tvType.setText(item.type);
        final EditText etValue = (EditText) view.findViewById(R.id.et_value);

        if(item.isLeafNode) {
            //show EditText and set listener
            etValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    if("string".equalsIgnoreCase(item.type))  //add quotes for string type
                        jsonData = "\"" + item.name + "\":\"" + etValue.getText() + "\"";
                    else
                        jsonData = "\"" + item.name + "\":" + etValue.getText();
                }
            });

            //Set default value by item type
            if("string".equalsIgnoreCase(item.type))  //add quotes for string type
                jsonData = "\"" + item.name + "\":\"" + etValue.getText() + "\"";
            else if("bool".equalsIgnoreCase(item.type)) {
                jsonData = "\"" + item.name + "\":" + false;
            } else
                jsonData = "\"" + item.name + "\":" + 0;

        } else {
            etValue.setVisibility(View.GONE);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            ivIcon.setVisibility(View.VISIBLE);
            jsonData = item.name;
        }
        return view;
    }

    @Override
    public void toggle(boolean active) {
        if(ivIcon != null)
            ivIcon.setImageResource(active ? R.drawable.node_p : R.drawable.node_d);
    }

    public static class TreeItem {
        public String name;
        public String type;
        public boolean isLeafNode;

        public TreeItem(String name, String type, boolean isLeafNode) {
            this.name = name;
            this.type = type;
            this.isLeafNode = isLeafNode;
        }
    }
}
