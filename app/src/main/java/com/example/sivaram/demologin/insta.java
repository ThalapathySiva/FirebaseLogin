package com.example.sivaram.demologin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class insta extends AppCompatActivity implements View.OnClickListener {
    private Button btnConnect;
    private InstagramApp mApp;
    TextView userId,userName;
    ImageView userProfile;
    private InstagramSession mSession;
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(insta.this, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        mSession = new InstagramSession(getApplicationContext());
        userId=findViewById(R.id.textViewUserId);
        userName=findViewById(R.id.textViewUsername);
        userProfile=findViewById(R.id.userProfilePic);
        btnConnect.setOnClickListener((View.OnClickListener) this);
        setUpViews();
    }
    private void setUpViews() {
        mApp = new InstagramApp(this, Constants.INSTA_CLIENT_ID,
                Constants.INSTA_CLIENT_SECRET, Constants.INSTA_CALLBACK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                // tvSummary.setText("Connected as " + mApp.getUserName());
                btnConnect.setText("Logout");
                // userInfoHashmap = mApp.
                mApp.fetchUserName(handler);
                setInstragramData(true);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT)
                        .show();
            }
        });


        if (mApp.hasAccessToken()) {
            // tvSummary.setText("Connected as " + mApp.getUserName());
            btnConnect.setText("LogOut");
            mApp.fetchUserName(handler);
/*
            Log.w("Id",mApp.getId());
            Log.w("Name",mApp.getName());
            Log.w("Token",mApp.getTOken());
            Log.w("UserName",mApp.getUserName());
            Log.w("Profile",mApp.getProfilePicture());*/
            setInstragramData(true);
        }

    }
    public void setInstragramData(boolean isLogin)
    {
        if (isLogin && mSession.getLoginType().equals("insta")) {
//        Log.w("InstraData",userInfoHashmap.get(InstagramApp.TAG_ID));
            Log.w("ID",mApp.getName());
            userId.setText(mApp.getId());
            userName.setText(mApp.getName());
            Picasso.with(this).load(mApp.getProfilePicture()).into(userProfile);
        }

    }
    @Override
    public void onBackPressed()
    {
        finishAffinity();
    }
    @Override
    public void onClick(View view) {

        if (view == btnConnect) {
            connectOrDisconnectUser();
        }
    }
    private void connectOrDisconnectUser() {
        if (mSession.getAccessToken() != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    insta.this);
            builder.setMessage("Logout ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    mApp.resetAccessToken();
                                    // btnConnect.setVisibility(View.VISIBLE);
                                    btnConnect.setText("Login With Instagram");
                                    // tvSummary.setText("Not connected");
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            mApp.authorize();
        }
    }

}
