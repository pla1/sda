package net.pla1.sda;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class StatusActivity extends Activity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_layout);
        context = this;
        TextView statusField = (TextView) findViewById(R.id.statusField);
        statusField.setText(Utils.getStatus(context));
    }
}
