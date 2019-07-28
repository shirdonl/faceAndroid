package com.wis.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.wis.R;
import com.wis.adapter.ImageAdapter;
import com.wis.bean.Person;
import com.wis.db.DBManager;

import java.util.ArrayList;
import java.util.List;

public class ManageActivity extends Activity {

    private DBManager dbManager;
    private ListView listView;
    private List<Person> personList;
    private ImageAdapter imageAdapter;
    private ActionBar actionBar;

    private Handler mHandler;
    private final static int DELETE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initListView();
        // 删除时，更新UI
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DELETE:
                        personList.clear();
                        personList.addAll(dbManager.query());
                        imageAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initListView() {
        listView = (ListView) findViewById(R.id.list_view);
        personList = new ArrayList<Person>();

        //初始化DBManager
        dbManager = new DBManager(this);
        personList.addAll(dbManager.query());

        imageAdapter = new ImageAdapter(this, personList);
        listView.setAdapter(imageAdapter);
    }

    public void deletePerson(Person person) {
        dbManager.deletePerson(person);
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        Message msg = new Message();
        msg.what = DELETE;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.face_add:
                //添加人脸
                Intent intent = new Intent(ManageActivity.this, AddActivity.class);
                startActivity(intent);
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放DB
        dbManager.closeDB();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
