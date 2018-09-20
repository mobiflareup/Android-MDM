package org.conveyance.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobiocean.R;

import org.conveyance.model.RExtraAllowanceModel;

import java.util.ArrayList;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          23-09-16         Initial creation
 ****************************************************************************/

public class RExtraAllowanceAdapter extends RecyclerView.Adapter<RExtraAllowanceAdapter.MyHolder> {
    private ArrayList<RExtraAllowanceModel> extraAllowanceModels = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public RExtraAllowanceAdapter(Context context, ArrayList<RExtraAllowanceModel> extraAllowanceModels) {
        this.extraAllowanceModels = extraAllowanceModels;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void UpdateDataChange(ArrayList<RExtraAllowanceModel> extraAllowanceModels) {
        this.extraAllowanceModels = extraAllowanceModels;
        notifyDataSetChanged();
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.r_allowance_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        RExtraAllowanceModel allowanceModel = extraAllowanceModels.get(position);
        holder.tvSno.setText("" + allowanceModel.getSno());
        holder.tvRemarks.setText(allowanceModel.getRemark());
        holder.tvAmount.setText(allowanceModel.getClaimedAmt());
        if (!TextUtils.isEmpty(allowanceModel.getFilePath())) {

            holder.tvProof.setText(allowanceModel.getFilePath().substring(allowanceModel.getFilePath().lastIndexOf("/") + 1));
        } else {
            holder.tvProof.setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return extraAllowanceModels.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView tvSno, tvRemarks, tvAmount, tvProof;

        public MyHolder(View itemView) {
            super(itemView);
            tvSno = (TextView) itemView.findViewById(R.id.tvSno);
            tvRemarks = (TextView) itemView.findViewById(R.id.tvRemarks);
            tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
            tvProof = (TextView) itemView.findViewById(R.id.tvProof);
        }
    }

}
