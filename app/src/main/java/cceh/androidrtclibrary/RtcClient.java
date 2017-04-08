package cceh.androidrtclibrary;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;

import java.util.HashMap;
import java.util.Map;

import cceh.androidrtclibrary.connection.Connection;
import cceh.androidrtclibrary.connection.ConnectionParams;
import cceh.androidrtclibrary.network.NetworkException;
import cceh.androidrtclibrary.network.StunTurnServerProvider;
import cceh.androidrtclibrary.signaling.SignalMessages;
import cceh.androidrtclibrary.signaling.SignalingException;
import cceh.androidrtclibrary.signaling.SignalingService;

/**
 * The client which manages peer connections.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/5/17.
 */
public class RtcClient implements
    SignalingService.SignalHandler,
    StunTurnServerProvider.Callback,
    Connection.ConnectionHandler {
  private static final String TAG = "RtcClient";

  private final String userId;
  private final MediaStream localMediaStream;
  private final SignalingService signalingService;
  private final StunTurnServerProvider stunTurnServerProvider;
  private final RtcEventListener rtcEventListener;
  private final Map<String, Connection> connections;
  private final Object lock;

  private boolean signalingServiceInitialized;
  private boolean stunTurnServerProviderInitialized;
  private boolean clientInitialized;

  public RtcClient(
      String userId,
      MediaStream localMediaStream,
      SignalingService signalingService,
      StunTurnServerProvider stunTurnServerProvider,
      RtcEventListener rtcEventListener) {
    this.userId = userId;
    this.localMediaStream = localMediaStream;
    this.signalingService = signalingService;
    this.stunTurnServerProvider = stunTurnServerProvider;
    this.rtcEventListener = rtcEventListener;
    this.connections = new HashMap<>();
    this.lock = new Object();

    this.signalingServiceInitialized = false;
    this.stunTurnServerProviderInitialized = false;
    this.clientInitialized = false;
    initializeClient();
  }

  public void connectTo(String peerId) {
    if (!clientInitialized) {
      return;
    }
    getConnectionTo(peerId).connect();
  }

  public void disconnectFrom(String peerId) {
    if (!clientInitialized) {
      return;
    }
    getConnectionTo(peerId).disconnect();
  }

  public void close() {
    for (Connection connection : connections.values()) {
      connection.disconnect();
    }
    connections.clear();
    signalingService.stopListening(userId);
  }

  private void initializeClient() {
    try {
      signalingService.listenOn(userId, this);
      stunTurnServerProvider.fetchServers(this);
    } catch (SignalingException e) {
      rtcEventListener.onClientDied(new RtcException(e));
    }
  }

  private void checkInitStatus() {
    if (!clientInitialized && signalingServiceInitialized && stunTurnServerProviderInitialized) {
      clientInitialized = true;
      rtcEventListener.onClientReady();
    }
  }

  private void dispatchSignal(JSONObject signal) {
    if (!clientInitialized) {
      return;
    }

    try {
      String senderId = signal.getString(SignalMessages.SENDER);
      getConnectionTo(senderId).handleIncomingSignal(signal);
    } catch (JSONException e) {
      Log.w(TAG, "Failed parsing signal: " + signal);
    }
  }

  private Connection getConnectionTo(String peerId) {
    try {
      if (!connections.containsKey(peerId)) {
        connections.put(peerId, new Connection(
            userId,
            peerId,
            localMediaStream,
            signalingService,
            new ConnectionParams(stunTurnServerProvider.getServers()),
            this));
      }
      return connections.get(peerId);
    } catch (NetworkException e) {
      throw new IllegalStateException("Should have initialized, unexpected network exception..", e);
    }
  }

  // SignalingService.SignalHandler
  @Override
  public void onConnected(String userId) {
    signalingServiceInitialized = true;
    checkInitStatus();
  }

  @Override
  public void onDisconnected(String userId) {
    rtcEventListener.onClientDied(new RtcException("SignalingService disconnected."));
  }

  @Override
  public void onSignalReiceived(String userId, JSONObject signal) {
    dispatchSignal(signal);
  }
  // SignalingService.SignalHandler Ends

  // StunTurnServerProvider.Callback
  @Override
  public void onServersFetched() {
    stunTurnServerProviderInitialized = true;
    checkInitStatus();
  }

  @Override
  public void onServerFetchFails(NetworkException e) {
    rtcEventListener.onClientDied(new RtcException("Failed fetching STUN TURN servers.", e));
  }
  // StunTurnServerProvider.Callback Ends

  // Connection.ConnectionHandler
  @Override
  public void onConnectionStateChanged(
      String peerId, Connection.Status oldStatus, Connection.Status newStatus) {
    if (newStatus.equals(Connection.Status.ANSWER_RECEIVED)
        || newStatus.equals(Connection.Status.ANSWERED)) {
      rtcEventListener.onConnected(peerId);
    } else if (newStatus.equals(Connection.Status.DISCONNECTED)) {
      rtcEventListener.onDisconnected(peerId);
      connections.remove(peerId);
    }
  }

  @Override
  public void onRemoteStreamAdded(String peerId, MediaStream mediaStream) {
    rtcEventListener.onRemoteStreamAdded(peerId, mediaStream);
  }

  @Override
  public void onRemoteStreamRemoved(String peerId, MediaStream mediaStream) {
    rtcEventListener.onRemoteStreamRemoved(peerId, mediaStream);
  }
  // Connection.ConnectionHandler Ends
}
