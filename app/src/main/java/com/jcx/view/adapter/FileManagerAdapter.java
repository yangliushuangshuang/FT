package com.jcx.view.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jcx.R;
import com.jcx.util.GetFileSize;

import java.io.File;

/**
 * Created by Cui on 16-4-1.
 */
public class FileManagerAdapter extends BaseAdapter {

    private Context context;
    private File[] filelist;
    private ListView listView;
    public static Parcelable state;
    private LayoutInflater inflater;
    private AsynLoadImg asynLoadImg;
    private boolean isExecute=false;
    private ViewHolder holder;
    private GetFileSize getFileSize;

    public FileManagerAdapter(Context context, File[] filelist, ListView listView)
    {
        this.context=context;
        this.filelist=filelist;
        this.listView=listView;
        getFileSize=new GetFileSize();
        inflater=LayoutInflater.from(context);
        asynLoadImg =new AsynLoadImg(context);
        listView.setOnScrollListener(new fixPosition());
    }


    public void upDate(File[] filelist){
        this.filelist=filelist;
        notifyDataSetChanged();//通知adapter list中的数据发生了改变，重置视图
    }

    @Override
    public int getCount() {
        return filelist == null ?0:filelist.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        holder=null;

        if (convertView==null){
            convertView=inflater.inflate(R.layout.allfiles_list_item,parent,false);
            holder=new ViewHolder();

            holder.fileIcon= (ImageView) convertView.findViewById(R.id.file_icon);
            holder.filename= (TextView) convertView.findViewById(R.id.file_name);
            holder.fileSize= (TextView) convertView.findViewById(R.id.file_size);

            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }

        //如果是文件夹
        if (filelist[position].isDirectory())
        {
            if (filelist[position].listFiles().length==0&&filelist[position].listFiles()==null)
            {
                holder.fileIcon.setImageResource(R.drawable.folder_fav);
            }else{
                holder.fileIcon.setImageResource(R.drawable.folder);
            }
        }else{//如果是文件

            String file_name=filelist[position].getName().toLowerCase();

            if (file_name.endsWith(".txt"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_text);
            }else if (file_name.endsWith(".doc")||file_name.endsWith(".docx"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_docx);
            }else if (file_name.endsWith(".xls")||file_name.endsWith(".xlsx"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_exel);
            }else if (file_name.endsWith(".mp3")||file_name.endsWith(".wav"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_music);
            }else if (file_name.endsWith(".mp4")||file_name.endsWith(".3gp")||file_name.endsWith(".wmv")||file_name.endsWith(".rm")||file_name.endsWith(".rmvb")||file_name.endsWith(".mov")||file_name.endsWith(".avi"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_movie);
            }else if (file_name.endsWith(".pdf"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_pdf);
            }else if (file_name.endsWith(".jpg")||file_name.endsWith(".png")||file_name.endsWith(".jpeg")||file_name.endsWith(".bmp"))
            {
                isExecute=true;
                holder.fileIcon.setTag(filelist[position].getAbsolutePath());
                asynLoadImg.loadImage(holder.fileIcon, "pic",filelist[position].getAbsolutePath());
            }else if (file_name.endsWith(".rar")||file_name.endsWith(".zip"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_zip);
            }else if (file_name.endsWith(".apk"))
            {
                isExecute=true;
                holder.fileIcon.setTag(filelist[position].getAbsolutePath());
                asynLoadImg.loadImage(holder.fileIcon, "apk",filelist[position].getAbsolutePath());
            }else if (file_name.endsWith(".db"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_database);
            }else if (file_name.endsWith(".html")||file_name.endsWith(".xml"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_html);
            }else
            {
                holder.fileIcon.setImageResource(R.drawable.file_default);
            }
        }
        holder.filename.setText(filelist[position].getName());
//        holder.fileSize.setTag(filelist[position].getAbsolutePath());
//        getFileSize.asyncLoadFileSize(holder.fileSize, filelist[position].getAbsolutePath());

        return convertView;
    }

    public static final class ViewHolder{
        ImageView fileIcon;
        TextView filename;
        TextView fileSize;
    }

    /**
     * 监听手指在屏幕上滑动的事件
     */
    public class fixPosition implements AbsListView.OnScrollListener{
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState)
            {
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://当正在滑动时
                    if (isExecute) {
                        asynLoadImg.lock();
                        isExecute=false;
                    }
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE://当滑动停止时
                    state=view.onSaveInstanceState();
                    if (isExecute) {
                        asynLoadImg.unLock();
                        isExecute=false;
                    }
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://当手指还在屏幕上时
                    if (isExecute) {
                        asynLoadImg.lock();
                        isExecute=false;
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    }
}
