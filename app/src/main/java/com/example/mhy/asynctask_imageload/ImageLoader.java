package com.example.mhy.asynctask_imageload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by mhy on 2016/4/22.
 */
public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;
    //创建Cache
    private LruCache<String,Bitmap> mCache;
    private ListView mListView;
    private Set<MyAsyncTask> mTask;

    public ImageLoader(ListView listView){

        mListView = listView;
        mTask = new HashSet<>();

        //获得最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //通常情况下区最大内存的四分之一作为缓存区的大小
        int cacheSize = maxMemory/4;
        mCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key,Bitmap value){
                //在每次输入缓存的时候调用　获得每次输入的对象的大小
                return value.getByteCount();
            }
        };
    }

    //将图片增加到缓存
    public void addBitmapToCashe(String url,Bitmap value){
        if(getBitmapFromCache(url) == null){
            mCache.put(url,value);
        }
    }
    //把图片从缓存中读取
    public Bitmap getBitmapFromCache(String url){
        return mCache.get(url);
    }
    /**
     * 使用线程加载图片
     */
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }

        }
    };
    public void showImageByThread(ImageView imageView,final String url){
        mImageView = imageView;
        mUrl = url;
        new Thread(){

            @Override
            public void run(){
                super.run();
                Bitmap bitmap = getBitmapFromURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    private Bitmap getBitmapFromURL(String urlString) {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 使用AsyncTask加载图片
     * @param mImageView
     * @param mUrl
     */
    public void showImageByAsyncTask(ImageView mImageView,String mUrl){
        //通过图片的url从缓存中取出该图片
        Bitmap bitmap = getBitmapFromCache(mUrl);
        //若不在缓存中
        if(bitmap == null){
//            new MyAsyncTask(mImageView,mUrl).execute(mUrl);

            mImageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            mImageView.setImageBitmap(bitmap);
        }

    }

    //取消所有加载项
    public void cancelAllTasks(){
        if(mTask != null){
            for(MyAsyncTask task : mTask){
                task.cancel(false);
            }
        }
    }
    public void loadImages(int start, int end){
        for(int i=start; i < end; i++){
            String url = NewsAdapter.URLS[i];
            //从缓存中取出对应的图片
            Bitmap bitmap = getBitmapFromCache(url);
            //如果缓存中没有，则去加载
            if(bitmap == null){
                MyAsyncTask task = new MyAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }else{
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    private class MyAsyncTask extends AsyncTask<String,Void,Bitmap>{

        private ImageView mImageView;
        private String mUrl;

        public MyAsyncTask(ImageView mImageView,String mUrl){
            this.mImageView = mImageView;
            this.mUrl = mUrl;
        }

        public  MyAsyncTask(String mUrl){
            this.mUrl = mUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            //从网络获取图片
            Bitmap bitmap = getBitmapFromURL(url);
            if(bitmap != null){
                //将不在缓存的图片加入缓存
                addBitmapToCashe(url,bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//            if(mImageView.getTag().equals(mUrl)){
//                mImageView.setImageBitmap(bitmap);
//            }
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if(imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}
