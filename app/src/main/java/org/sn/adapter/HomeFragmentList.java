package org.sn.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.browser.gingerbox.BrowserMainActivity;
import com.mobiocean.R;
import com.mobiocean.ui.AttendanceActivity;
import com.mobiocean.ui.CalendarSyncActivity;
import com.mobiocean.ui.ContactListMenuActivity;
import com.mobiocean.ui.Conveyance;
import com.mobiocean.ui.Home;

import org.conveyance.main.RMainActivity;
import org.sn.beans.HomeFragmentItemBean;
import org.sn.securedstorage.SecureFolderActivity;

import java.util.ArrayList;

/**
 * @author Narayanan
 * @version V 0.0.5
 */

public class HomeFragmentList extends BaseAdapter {

    Context context;
    ArrayList<HomeFragmentItemBean> items;
    private LayoutInflater inflater = null;

    public HomeFragmentList(Context context, ArrayList<HomeFragmentItemBean> items) {
        this.items = items;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        View rowView;

        rowView = inflater.inflate(R.layout.home_fragment_item, null);
        holder.tv = (TextView) rowView.findViewById(R.id.item_txt);
        holder.img = (ImageView) rowView.findViewById(R.id.item_img);

        final HomeFragmentItemBean bean = items.get(position);

        holder.tv.setText(bean.text);
        holder.img.setImageResource(bean.img);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bean.text.equalsIgnoreCase("Profile")) {
                    ((Home) context).displayView(1);
                } else if (bean.text.equalsIgnoreCase("Backup Data")) {
                    ((Home) context).displayView(2);
                } else if (bean.text.equalsIgnoreCase("SOS")) {
                    ((Home) context).displayView(3);
                } else if (bean.text.equalsIgnoreCase("Travel\nAllowance")) {
                    Intent list = new Intent(context, RMainActivity.class);
                    context.startActivity(list);
                } else if (bean.text.equalsIgnoreCase("Conveyance")) {
                    Intent list = new Intent(context, Conveyance.class);
                    context.startActivity(list);
                } else if (bean.text.equalsIgnoreCase("Attendance")) {
                    Intent list = new Intent(context, AttendanceActivity.class);
                    context.startActivity(list);
                } else if (bean.text.equalsIgnoreCase("Calendar")) {
                    Intent list = new Intent(context, CalendarSyncActivity.class);
                    context.startActivity(list);
                } else if (bean.text.equalsIgnoreCase("Contacts")) {
                    Intent list = new Intent(context, ContactListMenuActivity.class);
                    context.startActivity(list);
                } else if (bean.text.equalsIgnoreCase("Browser")) {
                    Intent list = new Intent(context, BrowserMainActivity.class);
                    context.startActivity(list);
                } else if (bean.text.equalsIgnoreCase("Secure Storage")) {
                    Intent list = new Intent(context, SecureFolderActivity.class);
                    context.startActivity(list);
                }
            }
        });

        return rowView;
    }

}