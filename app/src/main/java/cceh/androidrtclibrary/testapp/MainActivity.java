package cceh.androidrtclibrary.testapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import cceh.androidrtclibrary.R;
import cceh.androidrtclibrary.RtcClient;
import cceh.androidrtclibrary.RtcEventListener;
import cceh.androidrtclibrary.network.XirSysStunTurnServerProvider;
import cceh.androidrtclibrary.signaling.PubnubSignalingService;

/**
 * Testapp main activity which allows listening on a user id and make calls with it.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/7/17.
 */
public class MainActivity
    extends Activity
    implements RtcEventListener {

  private static final int CAMERA_PERMISSIONS_REQUEST = 1;
  private static final int STORAGE_PERMISSIONS_REQUEST = 2;
  private static final String VIDEO_TRACK_ID = "video_track";
  private static final String LOCAL_MEDIA_STREAM_ID = "local_media_stream";

  private static final String[] PERMISSIONS_STORAGE = {
      "android.permission.READ_EXTERNAL_STORAGE",
      "android.permission.WRITE_EXTERNAL_STORAGE"
  };

  @Nullable private String username;
  @Nullable private String peerUsername;
  @Nullable private RtcClient rtcClient;
  private VideoSource localVideoSource;
  private VideoRenderer.Callbacks localRender;
  private VideoRenderer.Callbacks remoteRender;
  private GLSurfaceView videoView;
  private MediaStream localMediaStream;
  private PubnubSignalingService pubnubSignalingService;
  private XirSysStunTurnServerProvider xirSysStunTurnServerProvider;
  private PeerConnectionFactory pcFactory;

  @Override
  protected void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);
    setContentView(R.layout.main_activity);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    checkPermissions();
    initializeLocalVideoAndVideoSurface();

    username = null;
    peerUsername = null;
    rtcClient = null;
    startListening();
  }

  @Override
  protected void onPause() {
    super.onPause();
    this.videoView.onPause();
    this.localVideoSource.stop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.videoView.onResume();
    this.localVideoSource.restart();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (rtcClient != null) {
      this.rtcClient.close();
    }
    this.localMediaStream.dispose();
    this.localVideoSource.dispose();
    this.pcFactory.dispose();
  }

  public void login(View view) {
    EditText textLoginName = (EditText) findViewById(R.id.text_login_name);
    String enteredUserName = textLoginName.getText().toString();
    if (enteredUserName.isEmpty()) {
      return;
    }
    LinearLayout loginBox = (LinearLayout) findViewById(R.id.login_box);
    loginBox.setVisibility(View.GONE);
    this.username = enteredUserName;
    startListening();
  }

  public void makeCall(View view) {
    EditText textPeerName = (EditText) findViewById(R.id.text_peer_name);
    peerUsername = textPeerName.getText().toString();
    rtcClient.connectTo(peerUsername);

    Button callButton = (Button) findViewById(R.id.button_call);
    callButton.setEnabled(false);
    Button endCallButton = (Button) findViewById(R.id.button_end_call);
    endCallButton.setEnabled(true);
  }

  public void endCall(View view) {
    if (peerUsername != null) {
      rtcClient.disconnectFrom(peerUsername);

      peerUsername = null;
      Button endCallButton = (Button) findViewById(R.id.button_end_call);
      endCallButton.setEnabled(false);
    }
  }

  private void initializeLocalVideoAndVideoSurface() {
    videoView = (GLSurfaceView) findViewById(R.id.gl_surface);
    VideoRendererGui.setView(videoView, null);

    PeerConnectionFactory.initializeAndroidGlobals(
        this,  // Context
        true,  // Audio Enabled
        true,  // Video Enabled
        true,  // Hardware Acceleration Enabled
        null); // Render EGL Context

    String frontFacingCam = VideoCapturerAndroid.getNameOfFrontFacingDevice();
    VideoCapturer capturer = VideoCapturerAndroid.create(frontFacingCam);

    pcFactory = new PeerConnectionFactory();
    localVideoSource = pcFactory.createVideoSource(capturer, MediaParams.defaultVideoConstraints());
    VideoTrack localVideoTrack = pcFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);

    remoteRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
    localRender = VideoRendererGui.create(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);

    localMediaStream = pcFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);
    localMediaStream.addTrack(localVideoTrack);

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        localMediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
      }
    });
  }

  private void startListening() {
    if (username == null) {
      promptLogin();
      return;
    }

    pubnubSignalingService = new PubnubSignalingService(
        Constants.PUBNUM_PUB_KEY,
        Constants.PUBNUM_SUB_KEY);
    xirSysStunTurnServerProvider = new XirSysStunTurnServerProvider(
        Constants.XIRSYS_DOMAIN,
        Constants.XIRSYS_APPLICATION,
        Constants.XIRSYS_ROOM,
        Constants.XIRSYS_IDENTITY,
        Constants.XIRSYS_SECRET,
        Constants.XIRSYS_SECURE);
    rtcClient = new RtcClient(
        username,
        localMediaStream,
        pubnubSignalingService,
        xirSysStunTurnServerProvider,
        this);
  }

  private void promptLogin() {
    LinearLayout loginBox = (LinearLayout) findViewById(R.id.login_box);
    loginBox.setVisibility(View.VISIBLE);
  }

  private void setupChatBox() {
    LinearLayout chatBox = (LinearLayout) findViewById(R.id.chat_box);
    chatBox.setAlpha(1);
    EditText textPeerName = (EditText) findViewById(R.id.text_peer_name);
    textPeerName.setEnabled(true);
    Button buttonCall = (Button) findViewById(R.id.button_call);
    buttonCall.setEnabled(true);
  }

  private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA")
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{"android.permission.CAMERA"},
          CAMERA_PERMISSIONS_REQUEST);
    }

    if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE")
        != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(
          this,
          PERMISSIONS_STORAGE,
          STORAGE_PERMISSIONS_REQUEST
      );
    }
  }

  // RtcEventListener
  @Override
  public void onClientReady() {
    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setupChatBox();
      }
    });
  }

  @Override
  public void onClientDied(Throwable e) {
    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, "RtcClient died, quiting activity.", Toast.LENGTH_SHORT).show();
      }
    });
    finish();
  }

  @Override
  public void onConnected(final String peerId) {
    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, "Connected to user: " + peerId, Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onDisconnected(final String peerId) {
    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, "Disconnected from user: " + peerId, Toast.LENGTH_SHORT).show();
        Button callButton = (Button) findViewById(R.id.button_call);
        callButton.setEnabled(true);
      }
    });
  }

  @Override
  public void onRemoteStreamAdded(String peerId, final MediaStream remoteStream) {
    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          if (remoteStream.videoTracks.size() == 0) return;
          remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
          VideoRendererGui.update(remoteRender, 0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
          VideoRendererGui.update(localRender, 72, 65, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
        }
        catch (Exception e){ e.printStackTrace(); }
      }
    });
  }

  @Override
  public void onRemoteStreamRemoved(String peerId, MediaStream mediaStream) {}
  // RtcEventListener Ends
}
