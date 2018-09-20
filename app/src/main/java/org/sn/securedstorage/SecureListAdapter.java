package org.sn.securedstorage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class SecureListAdapter extends ArrayAdapter<SecureFileModel>{

    private ArrayList<SecureFileModel> models;
    private Context context;
    private LayoutInflater layoutInflater;

    public SecureListAdapter(Context context, ArrayList<SecureFileModel> models) {
        super(context, android.R.layout.select_dialog_item, models);
        this.context = context;
        this.models = models;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(android.R.layout.select_dialog_item, parent, false);
            holder = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (TextView) convertView.getTag();
        }
        final SecureFileModel model = models.get(position);
        holder.setCompoundDrawablesWithIntrinsicBounds(model.icon, 0, 0, 0);
        int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
        holder.setCompoundDrawablePadding(dp5);
        holder.setText(model.file);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SecureFolderActivity)context).adapterOnClick(model.file);
            }
        });
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((SecureFolderActivity)context).adapterOnLongClick(model.file);
                return true;
            }
        });
        return convertView;
    }
}