package com.mobiocean.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiocean.interfaces.ePhoneFeatureCtrl;
import com.mobiocean.interfaces.ePhoneMode;
import com.mobiocean.service.CallDetectService;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.eFeatureControl;

import org.sn.database.ContactListTable;

import static com.mobiocean.util.CallHelper.Ds;
import static com.mobiocean.util.Constant.MIN_LUT;

public class OutgoingReceiver extends BroadcastReceiver {

    static final String Outgoing = "OutgoingCall";

    @Override
    public void onReceive(Context context, Intent intent) {

        int allowednoIndex;
        boolean allowednoFound = false;
        int iWeekDay = Ds.structPC.bWeekDay;
        StringBuffer BodyBuffer = new StringBuffer(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
        DeBug.ShowToast(context, "" + BodyBuffer);
        DeBug.ShowLog(Outgoing, BodyBuffer.toString());

        if (!Ds.structPC.bTimeExpired) {

            try {
                if (!CallDetectService.settings.getBoolean("unlocked", true) || !CallDetectService.settings.getBoolean("unlockedSOS", true)) {
                    setResultData(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (BodyBuffer.length() >= 8 && Ds.structPC.bMode == ePhoneMode.PARENT_RESTRICTED) {
                int outWorkingIndex = 0;
                int bodyLength = 0;
                char cLocalMD = 0;

                DeBug.ShowLog(Outgoing, BodyBuffer.toString());

                bodyLength = BodyBuffer.length();
                cLocalMD = BodyBuffer.charAt(0);
                if (cLocalMD == '+')
                    outWorkingIndex = 1;
                for (; outWorkingIndex < bodyLength; ) {
                    cLocalMD = BodyBuffer.charAt(outWorkingIndex);
                    if (!Character.isDigit(cLocalMD)) {
                        BodyBuffer.deleteCharAt(outWorkingIndex);
                        bodyLength--;
                    } else
                        outWorkingIndex++;
                }

                String outcomingNumber = BodyBuffer.toString();
                DeBug.ShowLog(Outgoing, outcomingNumber);
                DeBug.ShowLog(Outgoing, "In parent Mode");

                for (allowednoIndex = 0; allowednoIndex < Ds.structDNCC.bTotalNumbers; allowednoIndex++)
                    if ((Ds.structDNCC.stDefunctNumber[allowednoIndex] != null) && (outcomingNumber.indexOf(Ds.structDNCC.stDefunctNumber[allowednoIndex]) != -1)) {
                        DeBug.ShowLog(Outgoing, "Defunct number found");
                        allowednoFound = true;
                        break;
                    }

                if (!allowednoFound) // if this is not defunct no AND
                {
                    if (((Ds.structNCC.bPhoneFeatureCtrl & ePhoneFeatureCtrl.CALL_NO_OUTCOMING) != ePhoneFeatureCtrl.CALL_NO_OUTCOMING) && !Ds.structNCC.bPresentlyStopped[iWeekDay][1])
                    {
                        boolean ExtNoMatch = true;
                        DeBug.ShowLog(Outgoing, "Entered Outgoing call handling");
                        if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.ALLOWED_NUMBERS_CALL) == eFeatureControl.ALLOWED_NUMBERS_CALL) {
                            ContactListTable blockedNumbersDB = new ContactListTable(context);

                            String Number = outcomingNumber.replace(" ", "");
                            StringBuffer bb = new StringBuffer(Number);

                            if (Number.startsWith("091"))
                                bb.delete(0, 3);
                            else if (Number.startsWith("+91"))
                                bb.delete(0, 3);
                            else if (Number.startsWith("0"))
                                bb.delete(0, 1);
                            else if (Number.startsWith("+"))
                                bb.delete(0, 1);
                            DeBug.ShowLog(Outgoing, " Nummber check for allow " + Number);
                            ExtNoMatch = blockedNumbersDB.canBlockOutgoingCall(bb.toString());
                            DeBug.ShowLog(Outgoing, " Nummber check for allow " + ExtNoMatch);
                        }
                        if (!ExtNoMatch) {
                            int Hr = Ds.structPC.AppHrMin / 100;
                            int Min = Ds.structPC.AppHrMin % 100;
                            DeBug.ShowLog(Outgoing, "Entered Outgoing call drop "+Hr+":"+Min+" res"+((Ds.structNCC.lAllowedTime[iWeekDay][1][Hr] & MIN_LUT[Min]) != 0));
                            DeBug.ShowLog(Outgoing, "Entered Outgoing call drop "+Hr+":"+Min+" res"+((Ds.structFCC.lAllowedTime[iWeekDay][19][Hr] & MIN_LUT[Min]) != 0));
                            if ((Ds.structNCC.lAllowedTime[iWeekDay][1][Hr] & MIN_LUT[Min]) != 0) {
                                setResultData(null);
                                DeBug.ShowLog(Outgoing, "Call Dropped");
                            }
                            if ((Ds.structFCC.lAllowedTime[iWeekDay][19][Hr] & MIN_LUT[Min]) != 0) {
                                setResultData(null);
                                DeBug.ShowLog(Outgoing, "Call Dropped");
                            }
                        }
                    } else {
                        setResultData(null);
                        DeBug.ShowLog(Outgoing, "else Outgoing call not allowed");
                    }
                }
            } else if (Ds.structPC.bMode == ePhoneMode.SCHOOL_RESTRICTED || Ds.structPC.bDateChangedToDefault) {
                setResultData(null);
                DeBug.ShowLog(Outgoing, "Outgoing call not allowed");
            }
        }
    }
}