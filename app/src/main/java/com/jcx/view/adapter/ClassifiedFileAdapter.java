package com.jcx.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcx.R;
import com.jcx.util.FileClassification;

import java.util.List;

/**
 * Created by Cui on 16-4-27.
 */
public class ClassifiedFileAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<FileClassification.MyFile> filesList;
    private AsynLoadImg asynLoadImg;
    private boolean isExecute=false;
    private ViewHolder holder;

    public ClassifiedFileAdapter(Context context, List<FileClassification.MyFile> filesList){
        this.filesList=filesList;
        System.out.println("classfiedFileAdapter.size():"+filesList.size());
        mInflater=LayoutInflater.from(context);
        asynLoadImg =new AsynLoadImg(context);
    }
    @Override
    public int getCount() {
        return filesList.size();
    }

    @Override
    public Object getItem(int position) {
        return filesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        holder=null;

        if (convertView==null){
            convertView=mInflater.inflate(R.layout.classificed_file_list_item,parent,false);
            holder=new ViewHolder();

            holder.fileIcon= (ImageView) convertView.findViewById(R.id.iv_file_icon);
            holder.filename= (TextView) convertView.findViewById(R.id.tv_file_name);

            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }

        if (filesList.get(position).getFileType().equals(FileClassification.FileType.picture)) {
            holder.fileIcon.setTag(filesList.get(position).getFile_path());
            asynLoadImg.loadImage(holder.fileIcon,"pic",filesList.get(position).getFile_path());
        }else if (filesList.get(position).getFileType().equals(FileClassification.FileType.apk)) {
            holder.fileIcon.setImageDrawable(filesList.get(position).getApk_icon());
        }else if (filesList.get(position).getFileType().equals(FileClassification.FileType.video)) {
            holder.fileIcon.setImageResource(R.drawable.file_movie);
        }else if (filesList.get(position).getFileType().equals(FileClassification.FileType.music)) {
            holder.fileIcon.setImageResource(R.drawable.file_music);
        }else if (filesList.get(position).getFileType().equals(FileClassification.FileType.document)) {
            String file_name=filesList.get(position).getFile_name().toLowerCase();
            if (file_name.endsWith(".pdf"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_pdf);
            }else if (file_name.endsWith(".xls")||file_name.endsWith(".xlsx"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_exel);
            }else if (file_name.endsWith(".doc")||file_name.endsWith(".docx"))
            {
                holder.fileIcon.setImageResource(R.drawable.file_docx);
            }else {
                holder.fileIcon.setImageResource(R.drawable.file_text);
            }
        }else if (filesList.get(position).getFileType().equals(FileClassification.FileType.zip)) {
            holder.fileIcon.setImageResource(R.drawable.file_zip);
        }else if (filesList.get(position).getFileType().equals(FileClassification.FileType.offineWebPage)) {
            holder.fileIcon.setImageResource(R.drawable.file_html);
        }
        holder.filename.setText(filesList.get(position).getFile_name());
        return convertView;
    }
    public static final class ViewHolder{
        ImageView fileIcon;
        TextView filename;
    }
}
