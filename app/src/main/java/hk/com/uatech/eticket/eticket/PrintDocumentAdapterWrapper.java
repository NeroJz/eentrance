package hk.com.uatech.eticket.eticket;

import android.app.Activity;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;


/**
 * Created by alex_ on 28/08/2017.
 */

public class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {

    private final PrintDocumentAdapter delegate;
    private final Activity activity;
    public PrintDocumentAdapterWrapper(PrintDocumentAdapter adapter, Activity activity){
        super();
        this.delegate = adapter;
        this.activity = activity;
    }

    public void onFinish(){
        delegate.onFinish();
        //insert hook here

        activity.finish();

    }

    public void onWrite (PageRange[] pages,
                         ParcelFileDescriptor destination,
                         CancellationSignal cancellationSignal,
                         WriteResultCallback callback) {
        delegate.onWrite(pages, destination, cancellationSignal, callback);
    }

    public void onLayout (PrintAttributes oldAttributes,
                          PrintAttributes newAttributes,
                          CancellationSignal cancellationSignal,
                          LayoutResultCallback callback,
                          Bundle extras) {
        delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
    }

    //override all other methods with a trivial implementation calling to the delegate
}
