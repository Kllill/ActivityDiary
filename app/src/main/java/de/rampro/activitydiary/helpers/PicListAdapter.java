package de.rampro.activitydiary.helpers;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

import de.rampro.activitydiary.R;

public class PicListAdapter extends BaseAdapter {

        private List<String> list;
        private ListView listview;
    private Context mContext;
    public PicListAdapter(List<String> list, Context context) {
            super();
            this.list = list;

        mContext=context;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (listview == null) {
                listview = (ListView) parent;
            }

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.detail_recycler_item, null);
                holder = new ViewHolder();
                holder.iv = (ImageView) convertView.findViewById(R.id.picture);
                holder.item_tv_title = (TextView) convertView.findViewById(R.id.detailText);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final String path = list.get(position);
            holder.item_tv_title.setText(path+"");
            holder.iv.setImageBitmap(BitmapFactory.decodeFile(path));

            return convertView;
        }


    class ViewHolder {
            ImageView iv;
            TextView item_tv_title;
        }

}
