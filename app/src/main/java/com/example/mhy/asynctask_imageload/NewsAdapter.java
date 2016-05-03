package com.example.mhy.asynctask_imageload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mhy on 2016/4/21.
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private Context context;
    private List<NewsBean> mList;
    private ImageLoader imageLoader;
    private int mStart,mEnd;
    public static String[] URLS;
    private boolean firstIn;

    public NewsAdapter(Context context, List<NewsBean> mList, ListView mListView){
        this.context = context;
        this.mList = mList;
        //保证在ListView中的Item加载过程中只创建一个ImageLoader对象来达到图片缓存的作用
        imageLoader = new ImageLoader(mListView);
        URLS = new String[mList.size()];
        for(int i=0; i < mList.size(); i++){
            URLS[i] = mList.get(i).newsIconUrl;
        }
        firstIn = true;
        //给滚动事件加载事件监听器
        mListView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_layout,null);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.content = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.image.setImageResource(R.mipmap.ic_launcher);
        String url = mList.get(position).newsIconUrl;
        viewHolder.image.setTag(url);
        //使用线程Thread加载图片
        //imageLoader.showImageByThread(viewHolder.image,url);
        //使用AsyncTask加载图片
        imageLoader.showImageByAsyncTask(viewHolder.image, url);
        viewHolder.title.setText(mList.get(position).newsTitle);
        viewHolder.content.setText(mList.get(position).newsContent);

        return convertView;
    }

    //当ListView中的Scroll状态改变的时候
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //当Scroll为停止滑动状态时
        if(scrollState == SCROLL_STATE_IDLE){
            //加载可见项
            imageLoader.loadImages(mStart, mEnd);
        }else{
            //停止任务
            imageLoader.cancelAllTasks();
        }
    }

    //当ListView中的Scroll滚动的时候调用
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        //当首次加载并且已加载View对象时
        if(firstIn && visibleItemCount > 0) {
            imageLoader.loadImages(mStart, mEnd);
            firstIn = false;
        }
    }

    class ViewHolder{
        ImageView image;
        TextView title;
        TextView content;
    }

}
