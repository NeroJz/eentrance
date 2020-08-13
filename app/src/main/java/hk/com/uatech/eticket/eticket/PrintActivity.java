package hk.com.uatech.eticket.eticket;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrintActivity extends AppCompatActivity implements ReceiveListener {


    private Printer mPrinter = null;
    private int printerName = Printer.TM_M30;
    private int printerLang = Printer.MODEL_CHINESE;
    private FilterOption mFilterOption = null;

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    /*
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("PrinterName", deviceInfo.getDeviceName());
                    item.put("Target", deviceInfo.getTarget());
                    mPrinterList.add(item);
                    mPrinterListAdapter.notifyDataSetChanged();initializeObject
                    */
                    String tmpName = deviceInfo.getTarget();
                }
            });
        }
    };

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(printerName,
                    printerLang,
                    PrintActivity.this);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "Printer", mContext);

            String ex = e.getMessage();
            return false;
        }

        mPrinter.setReceiveEventListener(PrintActivity.this);

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
            String printerNameStr = "TCP:192.168.0.101";

            mPrinter.connect(printerNameStr, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "connect", mContext);
            String tmp = e.getMessage();


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
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    // ShowMsg.showException(e, "endTransaction", mContext);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    //ShowMsg.showException(e, "disconnect", mContext);
                }
            });
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Look at this dialog!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        runPrintReceiptSequence();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
*/

        runPrintReceiptSequence();




    }


    private boolean runPrintDepartmentReceipt(String department, List<String> items, List<String> remarks) {
        if (!initializeObject()) {
            return false;
        }

        if (!createDepartmentData(department, items, remarks)) {
            finalizeObject();
            return false;
        }



        return true;
    }

    private boolean runPrintReceiptSequence() {
        Context context =  PrintActivity.this;// getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, "PT1", duration);
        toast.show();

        if (!initializeObject()) {
            return false;
        }

        toast = Toast.makeText(context, "PT2", duration);
        toast.show();

        //if (!createReceiptData()) {
        ArrayList items = new ArrayList();
        ArrayList remarks = new ArrayList();
        items.add("PopCorn x 1");
        items.add("Coke x 2");

        remarks.add("Hello");
        remarks.add("Hello2");

        if (!createDepartmentData("DEPARTMENT", items, remarks)) {
            finalizeObject();
            return false;
        }

        toast = Toast.makeText(context, "PT3", duration);
        toast.show();

        if (!printData()) {
            finalizeObject();
            return false;
        }
        toast = Toast.makeText(context, "PT4", duration);
        toast.show();

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
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy (E) hh:mma");
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

    private boolean createDepartmentData(String department, List<String> items, List<String> remarks) {
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
            mPrinter.addText("12345678\n");

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


            textData.append("        VIP             |             A9          ");

            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addText";
            mPrinter.addText("_______________________________________________\n\n");


            method = "addSymbol";
            mPrinter.addSymbol("12345678", Printer.SYMBOL_PDF417_STANDARD, Printer.LEVEL_0, barcodeWidth, barcodeHeight, 0);

/*
            method = "addText";
            mPrinter.addText("\n\n");

            method="addHLine";
            mPrinter.addHLine(0, 100, Printer.LINE_THIN);
*/

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

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
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String displayFileDateTime = displayFormat.format(currentDt);
            textData.append(displayFileDateTime + "\n\n");



            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            //mPrinter.addText("VIP", 10, 10);
            // mPrinter.addText("VIP", 10, 10);

            /*
            int [] lineIds = null;
            mPrinter.addVLineBegin(30000, Printer.LINE_THIN, lineIds);

            mPrinter.addVLineEnd(lineIds);
*/



/*
            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("THE STORE 123 (555) 555 – 5555\n");
            textData.append("STORE DIRECTOR – John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("400 OHEIDA 3PK SPRINGF  9.99 R\n");
            textData.append("410 3 CUP BLK TEAPOT    9.99 R\n");
            textData.append("445 EMERIL GRIDDLE/PAN 17.99 R\n");
            textData.append("438 CANDYMAKER ASSORT   4.99 R\n");
            textData.append("474 TRIPOD              8.99 R\n");
            textData.append("433 BLK LOGO PRNTED ZO  7.99 R\n");
            textData.append("458 AQUA MICROTERRY SC  6.99 R\n");
            textData.append("493 30L BLK FF DRESS   16.99 R\n");
            textData.append("407 LEVITATING DESKTOP  7.99 R\n");
            textData.append("441 **Blue Overprint P  2.99 R\n");
            textData.append("476 REPOSE 4PCPM CHOC   5.49 R\n");
            textData.append("461 WESTGATE BLACK 25  59.99 R\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            mPrinter.addText("TOTAL    174.81\n");
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("Purchased item total number\n");
            textData.append("Sign Up and Save !\n");
            textData.append("With Preferred Saving Card\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);
*/


            /*
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);
*/

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

    private boolean createReceiptData() {
        String method = "";
        //Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

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
            mPrinter.addText("12345");
            mPrinter.addHLine(0, 100, 0);


            method = "addBarcode";
            mPrinter.addBarcode("01209457",

                    Printer.BARCODE_CODE39 ,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);


            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("THE STORE 123 (555) 555 – 5555\n");
            textData.append("STORE DIRECTOR – John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("400 OHEIDA 3PK SPRINGF  9.99 R\n");
            textData.append("410 3 CUP BLK TEAPOT    9.99 R\n");
            textData.append("445 EMERIL GRIDDLE/PAN 17.99 R\n");
            textData.append("438 CANDYMAKER ASSORT   4.99 R\n");
            textData.append("474 TRIPOD              8.99 R\n");
            textData.append("433 BLK LOGO PRNTED ZO  7.99 R\n");
            textData.append("458 AQUA MICROTERRY SC  6.99 R\n");
            textData.append("493 30L BLK FF DRESS   16.99 R\n");
            textData.append("407 LEVITATING DESKTOP  7.99 R\n");
            textData.append("441 **Blue Overprint P  2.99 R\n");
            textData.append("476 REPOSE 4PCPM CHOC   5.49 R\n");
            textData.append("461 WESTGATE BLACK 25  59.99 R\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            mPrinter.addText("TOTAL    174.81\n");
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("Purchased item total number\n");
            textData.append("Sign Up and Save !\n");
            textData.append("With Preferred Saving Card\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addBarcode";
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            // ShowMsg.showException(e, method, mContext);
            return false;
        }

        textData = null;

        return true;
    }


    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                //ShowMsg.showResult(code, makeErrorMessage(status), mContext);

                //dispPrinterWarnings(status);

                //updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
