package org.conveyance.main;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.mobiocean.R;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          21-09-16         Initial creation
 ****************************************************************************/

public class RCustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public LinearLayout tripend, nextcustomer,returnback;

    public interface DialogResponse{
         void response(String response);
    }

    public RCustomDialog(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.r_custom_dialog);
        tripend = (LinearLayout) findViewById(R.id.tripend);
        nextcustomer = (LinearLayout) findViewById(R.id.nextcustomer);
        returnback = (LinearLayout) findViewById(R.id.returnback);
        tripend.setOnClickListener(this);
        nextcustomer.setOnClickListener(this);
        returnback.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        DialogResponse dialogResponse=(DialogResponse) c;
        switch (v.getId()) {
            case R.id.tripend:
                dialogResponse.response("TRIPEND");
                break;
            case R.id.nextcustomer:
                dialogResponse.response("NEXTCUSTOMER");
                break;
            case R.id.returnback:
                dialogResponse.response("RETURN");
                break;
            default:
                break;
        }
        dismiss();
    }

}
