package com.example.sivaram.demologin;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GithubActivity extends AppCompatActivity {
    private static final String REDIRECT_URL_CALLBACK="https://demologin-6aa65.firebaseapp.com/__/auth/handler";
    private final String TAG=getClass().getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Button sign;
    private TextView detail;
    private LinearLayout layout;
    private boolean signed;
    private WebView webView;
    private SecureRandom random = new SecureRandom();
    private ImageView imageView;

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_github);
        sign=findViewById(R.id.sign);
        detail=findViewById(R.id.tv);
        layout=findViewById(R.id.ll);
        imageView=findViewById(R.id.iv);
        firebaseAuth=FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =firebaseAuth.getCurrentUser();
                if (user!=null){
                    signed=true;
                    layout.setVisibility(View.VISIBLE);
                    detail.setText(user.getDisplayName()+"\n"+user.getEmail());
                    Picasso.with(GithubActivity.this).load(user.getPhotoUrl()).into(imageView);
                    sign.setText("Signout");
                    Toast.makeText(GithubActivity.this,"Signed in",Toast.LENGTH_SHORT).show();
                }
                else {
                    signed=false;
                    layout.setVisibility(View.GONE);
                    sign.setText("Signin");
                    Toast.makeText(GithubActivity.this,"Signed out",Toast.LENGTH_SHORT).show();
                }

            }
        };

        Uri uri=getIntent().getData();
        if (uri!=null && uri.toString().startsWith(REDIRECT_URL_CALLBACK)){
            String code=uri.getQueryParameter("code");
            String state =uri.getQueryParameter("state");
            if (code!=null && state!=null)
                senPost(code,state);
            }
        }

    private void senPost(String code, String state) {
        OkHttpClient okHttpClient=new OkHttpClient();
        FormBody formBody =new FormBody.Builder()
                .add("client_id","752f88b7e3986c9323a6")
                .add("client_secret","5122de23da4a1b16f81305163b149ac2bf99e567")
                .add("code",code)
                .add("redirect_uri",REDIRECT_URL_CALLBACK)
                .add("state",state)
                .build();
        Request request =new Request.Builder()
                .url("https://github/login/oauth/access_token")
                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Toast.makeText(GithubActivity.this,"Error"+e.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responcebody=response.body().string();
                String[] splitted =responcebody.split("=|&");
                if (splitted[0].equalsIgnoreCase("access_token"))
                    signInWithToken(splitted[1]);
                else
                    Toast.makeText(GithubActivity.this,"splitted[0]"+splitted[0],Toast.LENGTH_SHORT).show();


            }
        });

    }

    private void signInWithToken(String s) {
        AuthCredential credential= GithubAuthProvider.getCredential(s);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            task.getException().printStackTrace();
                            Toast.makeText(GithubActivity.this,"Auth failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void signout(View view) {

        if (!signed){
            HttpUrl httpUrl=new HttpUrl.Builder()
                    .scheme("http")
                    .host("github.com")
                    .addPathSegment("login")
                    .addPathSegment("oauth")
                    .addPathSegment("authorize")
                    .addQueryParameter("cliend_id","752f88b7e3986c9323a6")
                    .addQueryParameter("redirect_uri",REDIRECT_URL_CALLBACK)
                    .addQueryParameter("state", getRandomString())
                    .addQueryParameter("scope","user:email")
                    .build();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()));
            startActivity(intent);
        }
        else {
            firebaseAuth.signOut();
        }
    }

    private String getRandomString() {

        return new BigInteger(130,random).toString(32);
    }
}
