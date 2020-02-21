package hk.com.uatech.eticket.eticket;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;

public class PrinterUtils {

    @Nullable
    public static Printer initializeObject(final Activity activity, int printerName, int printerLang) {
        Printer printer = null;
        try {
            printer = new Printer(printerName,
                    printerLang,
                    activity);
        } catch (Exception e) {
            String ex = e.getMessage();
            return null;
        }
        printer.setReceiveEventListener(new com.epson.epos2.printer.ReceiveListener() {
            @Override
            public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {
                //System.out.println("setReceiveEventListener 4 - Called");
                activity.runOnUiThread(new Runnable() { // This runnable is created
                    @Override                  // from lambda by SAM convention
                    public void run() {
                        new Runnable() {       // This Runnable is instantiated
                            @Override          // inside the lambda but never runs.
                            public void run() {
                                System.out.println("run");
                            }
                        };
                    }
                });
            }
        });


        return printer;
    }

    public static boolean connectPrinter(Printer printer, String ip) {
        if (printer == null) {
            System.out.println("ConnectPrinter - Return 1");
            return false;
        }

        try {
            String printerNameStr = "TCP:" + ip;
            printer.connect(printerNameStr, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            //ShowMsg.showException(e, "connect", mContext);
            String tmp = e.getMessage();
            //System.out.println("connectPrinter5 - Return 2");
            System.out.println(e.getMessage());
            return false;
        }


        return true;
    }


    public static void disconnectPrinter(Activity activity, Printer printer) {

        System.out.println("disconnectPrinter - 0");
        if (printer == null) {
            System.out.println("disconnectPrinter - 1");
            return;
        }

        try {
            printer.endTransaction();
        } catch (final Exception e) {
            System.out.println("disconnectPrinter - 2");
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                }
            });
        }

        try {
            System.out.println("disconnectPrinter - 2a");
            PrinterStatusInfo aa = printer.getStatus();
            int ttt = aa.getConnection();


            System.out.print("ttt: " + String.valueOf(ttt));
            printer.disconnect();
        } catch (Epos2Exception ee) {
            System.out.println("disconnectPrinter - 2ee");
            System.out.println(String.valueOf(ee.getErrorStatus()));

        } catch (final Exception e) {
            System.out.println("disconnectPrinter - 2e");
            System.out.println(e.getMessage());

            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    //ShowMsg.showException(e, "disconnect", mContext);
                }
            });
        }

        System.out.println("disconnectPrinter - 3");
        finalizeObject(printer);
        System.out.println("disconnectPrinter - 4");
    }

    public static void finalizeObject(Printer printer) {
        if (printer == null) {
            System.out.println("finalizeObject - return");
            return;
        }


        try {
            printer.clearCommandBuffer();

            printer.setReceiveEventListener(null);


            printer = null;
        } catch (Exception efinal) {


        }
    }


    public static boolean beginTran(Printer printer) {
        boolean isBeginTransaction = false;

        try {
            printer.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            //ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (!isBeginTransaction) {
            try {
                printer.disconnect();

            } catch (Epos2Exception e) {
                // Do nothing
               // System.out.println("beginTran1 - Return 3");
                return false;
            }
        }

        return true;
    }

    public static void endTran(Activity activity,  Printer printer) {

        try {
            printer.endTransaction();

            printer.clearCommandBuffer();
        } catch (final Exception e) {
            //System.out.println("endTran1 - 1");
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    // ShowMsg.showException(e, "endTransaction", mContext);
                }
            });
        }
    }
}
