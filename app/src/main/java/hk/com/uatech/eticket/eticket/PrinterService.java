package hk.com.uatech.eticket.eticket;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PrinterService extends IntentService implements ReceiveListener {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "hk.com.uatech.eticket.eticket.action.FOO";
    private static final String ACTION_BAZ = "hk.com.uatech.eticket.eticket.action.BAZ";

    private Printer mPrinter = null;
    private int printerName = Printer.TM_M30;
    private int printerLang = Printer.MODEL_CHINESE;


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "hk.com.uatech.eticket.eticket.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "hk.com.uatech.eticket.eticket.extra.PARAM2";

    public PrinterService() {

        super("PrinterService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PrinterService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PrinterService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList items = new ArrayList();
        ArrayList remarks = new ArrayList();
        runPrintDepartmentReceipt(PrinterService.this, "1213", "sdafds", "sadfjal", "ip", "departmentt", items, remarks);
        /*
        if (intent != null) {


            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
        */

    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {

        disconnectPrinter();

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }



    /* Method for clients */
    private boolean initializeObject(Context context) {
        try {
            mPrinter = new Printer(printerName,
                    printerLang,
                    this /* EntranceStep2Activity.this*/);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "Printer", mContext);
            return false;
        }

       // mPrinter.setReceiveEventListener(this);

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            String printerNameStr = "TCP:192.168.1.119";
            mPrinter.connect(printerNameStr, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }


    private boolean connectPrinter(String ipaddress) {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            String printerNameStr = "TCP:" + ipaddress; //"TCP:192.168.1.119";
            mPrinter.connect(printerNameStr, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {

        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {

        }

        finalizeObject();
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            ;//print available
        }

        return true;
    }

    private boolean runPrintDepartmentReceipt(Context context, String token, String foodRefNo, String foodOrderId, String ipaddress, String department, List<String> items, List<String> remarks) {
        if (!initializeObject(context)) {
            return false;
        }

        if (!createDepartmentData(token,foodRefNo, foodOrderId,  department, items, remarks)) {
            finalizeObject();
            return false;
        }

        if (!printData(ipaddress)) {
            finalizeObject();
            return false;
        }

        return true;

    }



    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        //dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            //ShowMsg.showMsg(makeErrorMessage(status), mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "sendData", mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }


    private boolean printData(String ipaddress) {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter(ipaddress)) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        //dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            //ShowMsg.showMsg(makeErrorMessage(status), mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "sendData", mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean createConsolidateData() {
        String method = "";
        //Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }


        try {
            method = "addSymbol";
            mPrinter.addSymbol("12345678", Printer.SYMBOL_PDF417_STANDARD, Printer.LEVEL_0, barcodeWidth, barcodeHeight, 22);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            textData.append("Department\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            textData.append("Consolidate Table\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addFeedLine";
            mPrinter.addFeedLine(3);




            textData.append("Item Detail\n");
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());



            method = "addFeedLine";
            mPrinter.addFeedLine(3);


            textData.append("Remark\n");
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            // Print Date
            method = "addFeedLine";
            mPrinter.addFeedLine(3);


            textData.append("Print Date\n");
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);
            textData.delete(0, textData.length());


            Date currentDt = new Date();
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy (E) hh:mma", java.util.Locale.US);
            String displayFileDateTime = displayFormat.format(currentDt);
            textData.append(displayFileDateTime + "\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());



        }
        catch (Exception e) {
            // ShowMsg.showException(e, method, mContext);
            return false;
        }

        textData = null;

        return true;
    }

    private boolean createDepartmentData(String token, String foodRefNo, String foodOrderId, String department, List<String> items, List<String> remarks) {
        String method = "";
        //Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 3;
        final int barcodeHeight = 7;

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            /*
            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
*/


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.TRUE, Printer.PARAM_DEFAULT);

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);

            method = "addText";
            mPrinter.addText(foodRefNo + "\n");

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);


            method = "addText";
            mPrinter.addText("_______________________________________________");

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);


            method = "addFeedLine";
            mPrinter.addFeedLine(2);


            textData.append("        VIP             |             " + foodOrderId + "          ");

            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());



            if ("Consolidate".compareTo(department) == 0) {

                method = "addText";
                mPrinter.addText("_______________________________________________\n\n");

                method = "addSymbol";
                mPrinter.addSymbol(token, Printer.SYMBOL_PDF417_STANDARD, Printer.LEVEL_0, barcodeWidth, barcodeHeight, 0);

/*
            method = "addText";
            mPrinter.addText("\n\n");

            method="addHLine";
            mPrinter.addHLine(0, 100, Printer.LINE_THIN);
*/

                method = "addTextAlign";
                mPrinter.addTextAlign(Printer.ALIGN_LEFT);


            }

            method = "addText";
            mPrinter.addText("_______________________________________________\n");

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("Department\n");


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // Restore the Text Style
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


            textData.append(department + "\n\n\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            // Item
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);

            textData.append("Item Detail\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());



            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);

            if (items.size() > 0) {
                for (int l = 0; l < items.size(); l++) {
                    textData.append(items.get(l) + "\n");
                    method = "addText";
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                }
            }

            textData.append("\n\n\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // Remark
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);

            textData.append("Remark\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


            if (remarks.size() > 0) {
                for (int l = 0; l < remarks.size(); l++) {
                    textData.append(remarks.get(l) + "\n");
                    method = "addText";
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                }
            }

            textData.append("\n\n\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            // Print Date
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);


            textData.append("Print Date\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


            Date currentDt = new Date();
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
            String displayFileDateTime = displayFormat.format(currentDt);
            textData.append(displayFileDateTime + "\n\n");



            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());



            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            // ShowMsg.showException(e, method, mContext);
            String tmp = e.getMessage();
            return false;
        }

        textData = null;





        return true;
    }
}
