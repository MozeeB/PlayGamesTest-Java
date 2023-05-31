package com.cikup.logingoogleplayservicegame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.quickgame.android.sdk.QuickGameManager;
import com.quickgame.android.sdk.bean.QGRoleInfo;
import com.quickgame.android.sdk.bean.QGUserData;
import com.quickgame.android.sdk.model.QGUserHolder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button loginBtn;
    private Button logoutBtn;
    private Button userCenterBtn;
    private LinearLayout beforeLogin;
    private LinearLayout afterLogin;
    private TextView username;
    private  TextView playerID;
    QuickGameManager sdkInstance = QuickGameManager.getInstance();

//    PackageManager pm = getPackageManager();
//    boolean isPC = pm.hasSystemFeature("com.google.android.play.feature.HPE_EXPERIENCE");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sdkInstance.onCreate(this);

        DemoSDKCallback sdkCallback = new DemoSDKCallback();
        sdkInstance.init(this, "your product code here", sdkCallback);

//        sdkInstance.freeLogin(this);
        sdkInstance.trackAdjustEvent("xoizir");

        loginBtn = findViewById(R.id.loginBtn);
        logoutBtn = findViewById(R.id.logOutBtn);
        userCenterBtn = findViewById(R.id.userCenterBtn);
        beforeLogin = findViewById(R.id.beforeLoginLL);
        afterLogin = findViewById(R.id.afterLoginLL);
        username = findViewById(R.id.nameTV);
        playerID = findViewById(R.id.playerIDTV);

        initSDK();
        initAction();
    }

    private void initSDK() {
        PlayGamesSdk.initialize(this);

        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(this);

        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
//                beforeLogin.setVisibility(View.GONE);
//                afterLogin.setVisibility(View.VISIBLE);
                // Continue with Play Games Services
                PlayGames.getPlayersClient(this).getCurrentPlayer().addOnCompleteListener(mTask -> {
                            // Get PlayerID with mTask.getResult().getPlayerId()
                            username.setText("Welcome Back!, " + mTask.getResult().getDisplayName());
                            playerID.setText("Player ID: " + mTask.getResult().getPlayerId());
                            Log.d("user is data valid", String.valueOf(mTask.getResult().isDataValid()));
                            Log.d("user list", String.valueOf(mTask.getResult().getCurrentPlayerInfo().getFriendsListVisibilityStatus()));


                        }
                );

                gamesSignInClient
                        .requestServerSideAccess("357466661153-mjl9ti99fpaq4i44rc2q0e72nvf034cj.apps.googleusercontent.com", /* forceRefreshToken= */ false)
                        .addOnCompleteListener( task -> {
                            if (task.isSuccessful()) {
                                String serverAuthToken = task.getResult();
                                // Send authentication code to the backend game server to be
                                // exchanged for an access token and used to verify the player
                                // via the Play Games Services REST APIs.
                                Log.d("Token cek", serverAuthToken);
                                Toast.makeText(MainActivity.this, "token play game:" + serverAuthToken, Toast.LENGTH_LONG).show();
                            } else {
                                // Failed to retrieve authentication code.
                                Toast.makeText(MainActivity.this, "token play game: failed", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
//                beforeLogin.setVisibility(View.VISIBLE);
//                afterLogin.setVisibility(View.GONE);
                // Disable your integration with Play Games Services or show a
                // login button to ask  players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().
                gamesSignInClient.signIn();
            }
        });
    }

    private void initAction() {
        loginBtn.setOnClickListener(view -> doLogin());
        logoutBtn.setOnClickListener(view -> doLogout());
        userCenterBtn.setOnClickListener(view -> showUserCenter());
    }

    private void doLogin() {
        sdkInstance.login(this);
    }

    private void doLogout() {
        sdkInstance.logout(this);
    }

    private void showUserCenter() {
        sdkInstance.enterUserCenter(this);
    }

    /**
     * sdk login callback
     */
    private class DemoSDKCallback implements QuickGameManager.SDKCallback {

        @Override
        public void onLoginFinished(QGUserData userData, QGUserHolder loginState) {
            if (loginState.getStateCode() == QGUserHolder.LOGIN_SUCCESS) {
//                //show float view
                sdkInstance.showFloatView(MainActivity.this);

                beforeLogin.setVisibility(View.GONE);
                afterLogin.setVisibility(View.VISIBLE);

                QGRoleInfo roleInfo = new QGRoleInfo();
                roleInfo.setRoleId("1001");
                roleInfo.setRoleLevel("10");
                roleInfo.setRoleName("Mozeeb");
                roleInfo.setServerName("ServerTest");
                roleInfo.setVipLevel("14");
                roleInfo.setServerId("12345");

                //report role info
                sdkInstance.submitRoleInfo(userData.getUid(), roleInfo);
//                gameSignIn();
                Toast.makeText(MainActivity.this, "login success,uid:" + userData.getUid() + " displayUid>:" + userData.getdisplayUid(), Toast.LENGTH_LONG).show();
            } else if (loginState.getStateCode() == QGUserHolder.LOGIN_CANCEL) {
                Toast.makeText(MainActivity.this, "login cancel", Toast.LENGTH_LONG).show();
            } else if (loginState.getStateCode() == QGUserHolder.LOGIN_FAILED) {
                Toast.makeText(MainActivity.this, "login fail", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onInitFinished(boolean isSuccess) {
            if (isSuccess) {
                Toast.makeText(MainActivity.this, "channelId:" + sdkInstance.getChannelId() + "init success->", Toast .LENGTH_LONG).show();
                loginBtn.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, "init fail", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLogout() {
            MainActivity.this.runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "logout", Toast.LENGTH_LONG).show();
                MainActivity.this.findViewById(R.id.afterLoginLL).setVisibility(View.GONE);
                MainActivity.this.findViewById(R.id.beforeLoginLL).setVisibility(View.VISIBLE);
            });
        }

        @Override
        public void onGooglePlaySub(String goodsId, String sdkOrder, boolean isAutoRenewing, boolean isAcknowledged) {
            Log.d(TAG, "goodsId=" + goodsId + "&&sdkOrder=" + sdkOrder);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        sdkInstance.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sdkInstance.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sdkInstance.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sdkInstance.onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdkInstance.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sdkInstance.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sdkInstance.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}