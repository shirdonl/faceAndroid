package com.wis.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wis.R;
import com.wis.activity.ManageActivity;
import com.wis.bean.Person;
import com.wis.util.ImageUtils;

import java.util.List;

/**
 * Created by ybbz on 16/8/28.
 */
public class ImageAdapter extends BaseAdapter {

    private ManageActivity context;
    private List<Person> list;
    private LayoutInflater inflater;

    public ImageAdapter(ManageActivity context, List<Person> list) {
        this.context = context;
        this.list = list;
        inflater = context.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.item_layout);
            holder.textView = (TextView) convertView.findViewById(R.id.list_text);
            holder.imageView = (ImageView) convertView.findViewById(R.id.list_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Person person = list.get(position);
        holder.textView.setText(person.name);
        holder.imageView.setImageBitmap(ImageUtils.BytesToBitmap(person.image));
        // 点击事件
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "姓名：" + person.name, Toast.LENGTH_SHORT).show();
            }
        });
        // 长按事件
        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("确定删除该记录？")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 删除记录，更新UI
                                        context.deletePerson(person);
                                    }
                                })
                        .setNegativeButton("取消", null).show();

                return false;
            }
        });
        return convertView;
    }

    static class ViewHolder {
        LinearLayout itemLayout;
        TextView textView;
        ImageView imageView;
    }

}
