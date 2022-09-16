package ir.behrooz.loan;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
public class WebActivity extends Activity {
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(getIntent().getExtras().getString("url"));
    }
}
