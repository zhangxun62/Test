package app.yongjiasoftware.com.videoplayer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import app.yongjiasoftware.com.videoplayer.R;
import app.yongjiasoftware.com.videoplayer.bean.MedieModel;
import app.yongjiasoftware.com.videoplayer.service.PlayerService;

/**
 * @Title MusicPlayerActivity
 * @Description:
 * @Author: alvin
 * @Date: 2016/6/1.10:10
 * @E-mail: 49467306@qq.com
 */
public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = MusicPlayerActivity.class.getSimpleName();
    private PlayerService.MyBinder mBinder;
    private ArrayList<MedieModel> mList;
    private int mIndex;
    private ImageButton previousImageButton, nextImageButton, playImageButton;
    private SeekBar mSeekBar;
    private TextView leftTextView, rightTextView;
    private PlayReceiver mReceiver;
    private ListView mListView;
    private MusicAdapter mAdapter;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "连接成功");
            mBinder = (PlayerService.MyBinder) service;
            mBinder.starPlay();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initDatas();
        initReceiver();
        initViews();

    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.mediaplayer.play");
        intentFilter.addAction("android.mediaplayer.secondSeekBar");
        intentFilter.addAction("android.mediaplayer.stop");
        intentFilter.addAction("android.mediaplayer.updateProgress");

        mReceiver = new PlayReceiver();
        registerReceiver(mReceiver, intentFilter);


    }

    private void initDatas() {
        if (getIntent() != null) {
            mList = getIntent().getParcelableArrayListExtra("Music");
            mIndex = getIntent().getIntExtra("index", 0);
        }
        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra("Music", mList);
        intent.putExtra("index", mIndex);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        previousImageButton = (ImageButton) findViewById(R.id.id_btn_previous);
        previousImageButton.setOnClickListener(this);
        playImageButton = (ImageButton) findViewById(R.id.id_btn_play);
        playImageButton.setOnClickListener(this);
        nextImageButton = (ImageButton) findViewById(R.id.id_btn_next);
        nextImageButton.setOnClickListener(this);

        mSeekBar = (SeekBar) findViewById(R.id.id_seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);

        leftTextView = (TextView) findViewById(R.id.id_tv_leftTime);
        rightTextView = (TextView) findViewById(R.id.id_tv_rightTime);

        mListView = (ListView) findViewById(R.id.id_listView);
        mAdapter = new MusicAdapter();

        MedieModel medieModel = mList.get(mIndex);
        medieModel.setPlaying(true);
        mListView.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_previous:
                if (mBinder != null) {
//                    mIndex = mBinder.toPrevious();
                    mList.get(mIndex).setPlaying(false);
                    Log.i(TAG, "mIndex:" + mIndex);
                    mList.get(mBinder.toPrevious()).setPlaying(true);
                    mIndex = mBinder.getIndex();

                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.id_btn_play:
                if (mBinder != null) {
                    if (mBinder.isPlaying()) {
                        mBinder.pausePlay();
                        playImageButton.setBackgroundResource(R.drawable.play);
                    } else {
                        mBinder.toPlay();
                        playImageButton.setBackgroundResource(R.drawable.pause);
                    }
                }
                break;
            case R.id.id_btn_next:
                if (mBinder != null) {
//                    mIndex = mBinder.toNext();
                    mList.get(mIndex).setPlaying(false);
                    mList.get(mBinder.toNext()).setPlaying(true);
                    mIndex = mBinder.getIndex();
                    mAdapter.notifyDataSetChanged();
                    Log.i(TAG, "播放音乐下一首！");
                }
                break;
        }
    }

    // 将ms转换为分秒时间显示函数
    public String ShowTime(int time) {
        // 将ms转换为s
        time /= 1000;
        // 求分
        int minute = time / 60;
        // 求秒
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mBinder != null) {
            mBinder.setSeekBar(seekBar.getProgress());
        }
    }

    class PlayReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("android.mediaplayer.play")) {
                if (mBinder != null) {
                    Log.i(TAG, "接受到广播");
                    rightTextView.setText(ShowTime(mBinder.getDuration()));
                    mSeekBar.setMax(mBinder.getDuration());
                    mIndex = mBinder.getIndex();
                    playImageButton.setBackgroundResource(R.drawable.pause);

                }

            } else if (intent.getAction().equals("android.mediaplayer.stop")) {
                playImageButton.setBackgroundResource(R.drawable.play);
                mSeekBar.setProgress(0);
                mSeekBar.setSecondaryProgress(0);
                leftTextView.setText("");
                mList.get(mBinder.getIndex()).setPlaying(false);
                mList.get(mBinder.toNext()).setPlaying(true);
                mAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals("android.mediaplayer.secondSeekBar")) {
                int second = intent.getIntExtra("secondSeekBar", -1);
                mSeekBar.setSecondaryProgress(second);
            } else if (intent.getAction().equals("android.mediaplayer.updateProgress")) {
                int progress = intent.getIntExtra("progress", -1);
                if (progress != -1) {
                    mSeekBar.setProgress(mBinder.getCurrentPosition());
                    leftTextView.setText(ShowTime(mBinder.getCurrentPosition()));
                }
            }

        }
    }

    class MusicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mList == null)
                return 0;
            return mList.size();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position).isPlaying())
                return 1;
            else
                return 0;
        }

        @Override
        public MedieModel getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder;
            if (convertView == null) {
                mViewHolder = new ViewHolder();
                mViewHolder.mTextView = new TextView(MusicPlayerActivity.this);
                convertView = mViewHolder.mTextView;
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            mViewHolder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            mViewHolder.mTextView.setPadding(10, 20, 10, 20);
            mViewHolder.mTextView.setText(getItem(position).getUrl());
            if (getItemViewType(position) == 1) {
                mViewHolder.mTextView.setTextColor(Color.RED);
            } else {
                getItem(position).setPlaying(false);
            }
            return convertView;
        }

        class ViewHolder {
            TextView mTextView;
        }
    }
}
