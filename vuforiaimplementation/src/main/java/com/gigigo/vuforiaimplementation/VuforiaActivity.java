package com.gigigo.vuforiaimplementation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4ox.app.FragmentActivity;
import android.support.v4ox.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.gigigo.ggglogger.GGGLogImpl;
import com.gigigo.ggglogger.LogLevel;
import com.gigigo.imagerecognitioninterface.ImageRecognitionConstants;
import com.gigigo.vuforiacore.sdkimagerecognition.icloudrecognition.CloudRecognitionActivityLifeCycleCallBack;
import com.gigigo.vuforiacore.sdkimagerecognition.icloudrecognition.ICloudRecognitionCommunicator;
import com.gigigo.vuforiaimplementation.credentials.ParcelableVuforiaCredentials;
import com.vuforia.Trackable;

public class VuforiaActivity extends FragmentActivity
        implements ICloudRecognitionCommunicator {

    private static final String RECOGNIZED_IMAGE_INTENT = "com.gigigo.imagerecognition.intent.action.RECOGNIZED_IMAGE";

    //basics for any vuforia activity
    //private View mView;
    private static CloudRecognitionActivityLifeCycleCallBack mCloudRecoCallBack;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        GGGLogImpl.log("VuforiaActivity.onCreate");
        initVuforiaKeys(getIntent());
    }

    //region implements CloudRecoCommunicator ands initializations calls
    private void initVuforiaKeys(Intent intent) {
        Bundle b = intent.getBundleExtra(ImageRecognitionVuforiaImpl.IMAGE_RECOGNITION_CREDENTIALS);
        ParcelableVuforiaCredentials parcelableVuforiaCredentials = b.getParcelable(ImageRecognitionVuforiaImpl.IMAGE_RECOGNITION_CREDENTIALS);

        mCloudRecoCallBack = new CloudRecognitionActivityLifeCycleCallBack(this, this,
                parcelableVuforiaCredentials.getClientAccessKey(),
                parcelableVuforiaCredentials.getClientSecretKey(),
                parcelableVuforiaCredentials.getLicenseKey(), false);

    }

    /**
     * @Deprecated
     */
    @Deprecated
    private void setThemeColorScheme() {
        if (this.mCloudRecoCallBack != null) {
            try {
                this.mCloudRecoCallBack.setUIPointColor(ContextCompat.getColor(this, R.color.ir_scan_point_color));
                this.mCloudRecoCallBack.setUIScanLineColor(ContextCompat.getColor(this, R.color.ir_scan_line_color));
            } catch (IllegalArgumentException e) {
                GGGLogImpl.log(e.getMessage(), LogLevel.ERROR);
            }
        }
    }

    View mVuforiaView;

    @Override
    public void setContentViewTop(View vuforiaView) {

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_vuforia, null);
        scanLine = view.findViewById(R.id.scan_line);
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.layoutContentVuforiaGL);
        relativeLayout.addView(vuforiaView, 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)

            markFakeFeaturePoint = new MarkFakeFeaturePoint(this);
        relativeLayout.addView(markFakeFeaturePoint);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        addContentView(view, vlp);


        //region Button Close
        view.findViewById(R.id.btnCloseVuforia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //endregion
        mVuforiaView = vuforiaView;
        setThemeColorScheme();

        startBoringAnimation();
        scanlineStart();

    }

    //region New not GPU animation
    MarkFakeFeaturePoint markFakeFeaturePoint;
    private View scanLine;
    private TranslateAnimation scanAnimation;

    private void startBoringAnimation() {
        scanLine.setVisibility(View.VISIBLE);
        // Create animators for y axe
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            int yMax = 1920;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                yMax = mVuforiaView.getDisplay().getHeight();
            }

            yMax = (int)(yMax *0.9);// 174;

            ObjectAnimator oay = ObjectAnimator.ofFloat(scanLine, "translationY", 0, yMax);
            oay.setRepeatCount(Animation.INFINITE);
            oay.setDuration(2000);
            oay.setRepeatMode(Animation.REVERSE);

            oay.setInterpolator(new LinearInterpolator());
            oay.start();

            //for draw points near scanline
            markFakeFeaturePoint.setObjectAnimator(oay);
        }

        //scanAnimation.

    }

    private void scanlineStart() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanLine.setVisibility(View.VISIBLE);
                scanLine.setAnimation(scanAnimation);
            }
        });
    }

    private void scanlineStop() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanLine.setVisibility(View.GONE);
                scanLine.clearAnimation();
            }
        });
    }
    //endregion

    @Override
    public void onVuforiaResult(Trackable trackable, String uniqueId) {
        scanlineStop();
        sendRecognizedPatternToClient(uniqueId);
        finish();
    }

    private void sendRecognizedPatternToClient(String uniqueId) {
        Intent i = new Intent();
        i.putExtra(ImageRecognitionConstants.VUFORIA_PATTERN_ID, uniqueId);
        String appId = getApplicationContext().getPackageName();
        i.putExtra(appId, appId);
        i.setAction(RECOGNIZED_IMAGE_INTENT);
        this.sendBroadcast(i);
    }
}
