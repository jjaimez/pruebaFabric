package com.tecpro.pruebafabric;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;
    private Button b,b2,b3;
    InterstitialAd mInterstitialAd;


    MyTwitterApiClient client;
    long uid;
    boolean follow,unfollow,running, pressed,onActionActivity;
    String text;
    int succesUnfollow, succesFollow;
    ProgressDialog dialog;
    private NotificationManager nm;
    private static final int ID_NOTIFICACION = 1;
    NotificationCompat.Builder mBuilder;


    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "oloJdvViss9uzQudPIv3fzeuI";
    private static final String TWITTER_SECRET = "w3D6uITrlEu62K1uIDYMUSUlyaCa7y1PQvtOyT7jLDOxQCE3AT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.bar, null);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.colorBar));
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MyIntentService.ACTION_FIN);
        filter.addAction(MyIntentService.ACTION_ERROR);
        filter.addAction(MyIntentService.ACTION_NOCONNECTION);
        ProgressReceiver rcv = new ProgressReceiver();
        registerReceiver(rcv, filter);

        setReferences();

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                client = new MyTwitterApiClient(Twitter.getSessionManager().getActiveSession());
                uid = result.data.getUserId();
                loginButton.setVisibility(View.GONE);
                b.setVisibility(View.VISIBLE);
                b2.setVisibility(View.VISIBLE);
                b3.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView2)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView3)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView4)).setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        if(!checkConnectivity()) {
            noConnectionMsg();
        }
    }



    private void setReferences(){
        b = (Button) findViewById(R.id.buttonUnfollow);
        b2 =  (Button) findViewById(R.id.buttonFollow);
        b3 = (Button) findViewById(R.id.buttonBoth);
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        follow = false;
        unfollow = false;
        text = getResources().getString(R.string.actionCompleted)+"\n";
        running = false;
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        pressed = false;
        onActionActivity = true;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1344220021901214/6586983689");
        mInterstitialAd.setAdListener(new AdListener() {
            // Listen for when user closes ad
            public void onAdClosed() {
                // When user closes ad end this activity (go back to first activity)
                finish();
            }
        });

        requestNewInterstitial();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }



    public void v(View v2) {

    if (!running) {
        switch (v2.getId()) {
            case R.id.buttonUnfollow:
                unfollow = true;
                follow = false;
                break;
            case R.id.buttonFollow:
                follow = true;
                unfollow = false;
                break;
            case R.id.buttonBoth:
                follow = true;
                unfollow = true;
                break;
        }
        if(checkConnectivity()) {
            pressed = true;
            dialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.Loading), getResources().getString(R.string.pleaseWait), true);
            running = true;
            Intent i = new Intent(MainActivity.this, MyIntentService.class);
            i.putExtra("follow",follow);
            i.putExtra("unfollow",unfollow);
            i.putExtra("uid",uid);
            startService(i);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.offline));
            alertDialog.setMessage(getResources().getString(R.string.needConeccion));
            alertDialog.setIcon(R.drawable.error);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        }
        }
    }


    private void endAction() {
        dialog.dismiss();
        if (unfollow){
            text += (getResources().getString(R.string.stoppeFollowing));
            text += succesUnfollow + "\n";
        }
        if (follow){
            text += (getResources().getString(R.string.following));
            text += succesFollow + "\n";
        }
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.actionEnded));
        alertDialog.setMessage(text);
        alertDialog.setIcon(R.drawable.tick);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                nm.cancel(ID_NOTIFICACION);
                text = getResources().getString(R.string.actionCompleted)+"\n";
                pressed = false;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

        alertDialog.show();
    }

    /**
     *
     * @return true si hay conexion a internet
     */
    private boolean checkConnectivity()
    {
        boolean enabled = true;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() || !info.isAvailable()))
        {
            enabled = false;
        }
        return enabled;
    }


    public void pgWeb(View view) {
        Uri uri;
        PackageManager pm = this.getPackageManager();
        try {
            pm.getPackageInfo("com.facebook.katana", 0);
            // http://stackoverflow.com/a/24547437/1048340
            uri = Uri.parse("fb://facewebmodal/f?href=" + "https://www.facebook.com/TecProSoftware");
        } catch (PackageManager.NameNotFoundException e) {
            uri = Uri.parse("https://www.facebook.com/TecProSoftware");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    /**
     * recive el mensaje de finalizacion del servicio
     */
    public class ProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MyIntentService.ACTION_FIN)) {
                succesFollow = intent.getIntExtra(MyIntentService.succesFollowString,0);
                succesUnfollow = intent.getIntExtra(MyIntentService.succesUnfollowString,0);
                running = false;
                endAction();
            }
            if(intent.getAction().equals(MyIntentService.ACTION_ERROR)) {
                running = false;
                errorMsg();
            }
            if(intent.getAction().equals(MyIntentService.ACTION_NOCONNECTION)) {
                running = false;
                noConnectionMsg();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onActionActivity = false;
        if (pressed) {
            mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_twitter)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.processProgress));

            Intent notIntent = this.getIntent();
            PendingIntent contIntent =
                    PendingIntent.getActivity(
                            this, 0, notIntent, 0);

            mBuilder.setContentIntent(contIntent);
            // Start a lengthy operation in a background thread
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            while (running) {


                                mBuilder.setProgress(0, 0, true);

                                if (onActionActivity) {
                                    nm.cancel(ID_NOTIFICACION);
                                } else {
                                    nm.notify(ID_NOTIFICACION, mBuilder.build());
                                }

                                try {
                                    // Sleep for 5 seconds
                                    Thread.sleep(5 * 1000);
                                } catch (InterruptedException e) {}
                            }
                            mBuilder.setContentText(getResources().getString(R.string.processComplete))

                                    .setProgress(0, 0, false);
                            nm.notify(ID_NOTIFICACION, mBuilder.build());

                        }
                    }
            ).start();
        }

    }

    @Override
    protected void onResume() {
        onActionActivity = true;
        if (pressed) {
            nm.cancel(ID_NOTIFICACION);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (pressed) {
            nm.cancel(ID_NOTIFICACION);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("BFA6DD15B78933CC6B82B57599DF3EDC")
                .build();

        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            // Listen for when user closes ad
            public void onAdClosed() {
                // When user closes ad end this activity (go back to first activity)
                finish();
            }
        });
    }

    public void noConnectionMsg(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.offline));
        alertDialog.setMessage(getResources().getString(R.string.needConeccion));
        alertDialog.setIcon(R.drawable.error);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                nm.cancel(ID_NOTIFICACION);
                pressed = false;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
        alertDialog.show();
    }

    public void errorMsg(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.error));
        alertDialog.setMessage(getResources().getString(R.string.errorOccurred));
        alertDialog.setIcon(R.drawable.error);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                nm.cancel(ID_NOTIFICACION);
                pressed = false;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
        alertDialog.show();
    }

}






