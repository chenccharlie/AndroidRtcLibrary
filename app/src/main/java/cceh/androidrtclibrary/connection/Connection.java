package cceh.androidrtclibrary.connection;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import cceh.androidrtclibrary.signaling.SignalMessages;
import cceh.androidrtclibrary.signaling.SignalingException;
import cceh.androidrtclibrary.signaling.SignalingService;

/**
 * Maintains information of the {@link org.webrtc.PeerConnection} to another peer.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public class Connection implements
    PeerConnection.Observer,
    SdpObserver {

  /** Handler which processes status changes on a {@link Connection}. */
  public interface ConnectionHandler {

    /** Called when a remote stream was added from the peer. */
    void onRemoteStreamAdded(String peerId, MediaStream mediaStream);

    /** Called when a remote stream was removed by the peer. */
    void onRemoteStreamRemoved(String peerId, MediaStream mediaStream);

    /** Called when connection status to the peer changes. */
    void onConnectionStateChanged(String peerId, Status oldStatus, Status newStatus);
  }

  /** The status of a {@link Connection}. */
  public enum Status {
    // General
    NEW,
    STREAMING,
    DISCONNECTED,

    // Caller status
    STARTED_WAITING_CALL,
    CALLING_WAITING_ANSWER,
    ANSWER_RECEIVED_WAITING_STREAM,

    // Receiver status
    RECEIVED_WAITING_ANSWER,
    ANSWERED_WAITING_STREAM
  }

  private static final String TAG = "Connection";

  private final String userId;
  private final String peerId;
  private final MediaStream localMediaStream;
  private final SignalingService signalingService;
  private final ConnectionParams connectionParams;
  private final ConnectionHandler connectionHandler;
  private final PeerConnection peerConnection;
  private final Object lock;

  private Status status;

  public Connection(
      String userId,
      String peerId,
      MediaStream localMediaStream,
      SignalingService signalingService,
      ConnectionParams connectionParams,
      ConnectionHandler connectionHandler) {
    this.userId = userId;
    this.peerId = peerId;
    this.localMediaStream = localMediaStream;
    this.signalingService = signalingService;
    this.connectionParams = connectionParams;
    this.connectionHandler = connectionHandler;
    this.status = Status.NEW;
    this.lock = new Object();

    this.peerConnection = new PeerConnectionFactory().createPeerConnection(
        this.connectionParams.getIceServers(),
        this.connectionParams.getConnectionConstraints(),
        this);
    this.peerConnection.addStream(this.localMediaStream);
  }

  public void connect() {
    offerToConnect();
  }

  public void disconnect() {
    destroyConnection();

    try {
      signalingService.sendSignal(peerId, SignalMessages.createDisconnectMessage(userId));
    } catch (SignalingException e) {
      Log.w(TAG, "Failed to send disconnect signal.", e);
    }
  }

  public Status getStatus() {
    synchronized (lock) {
      return status;
    }
  }

  public void handleIncomingSignal(JSONObject signal) {
    try {
      String signalType = signal.getString(SignalMessages.SIGNAL_TYPE);
      switch (signalType) {
        case SignalMessages.TYPE_OFFER:
          handleOffer(signal.getJSONObject(SignalMessages.SIGNAL_CONTENT));
          break;
        case SignalMessages.TYPE_ANSWER:
          handleAnswer(signal.getJSONObject(SignalMessages.SIGNAL_CONTENT));
          break;
        case SignalMessages.TYPE_ICE_CANDIDATE:
          handleRemoteIceCandidate(signal.getJSONObject(SignalMessages.SIGNAL_CONTENT));
          break;
        case SignalMessages.TYPE_DISCONNECT:
          handleDisconnectMessage();
          break;
        default:
          Log.w(TAG, "Unhandled signal type received: " + signalType);
      }
    } catch (JSONException e) {
      Log.w(TAG, "Error when handling incoming signal.", e);
    }
  }

  private void offerToConnect() {
    synchronized (lock) {
      setStatus(Status.STARTED_WAITING_CALL);
      this.peerConnection.createOffer(this, connectionParams.getConnectionConstraints());
    }
  }

  private void handleOffer(JSONObject offer) throws JSONException {
    synchronized (lock) {
      if (outgoingCalling() || disconnected() || streaming()) {
        Log.w(TAG, "Receiving offer in unexpected status: " + status);
        return;
      }

      setStatus(Status.RECEIVED_WAITING_ANSWER);
      this.peerConnection.setRemoteDescription(this, extractSdp(offer));
      this.peerConnection.createAnswer(this, connectionParams.getConnectionConstraints());
    }
  }

  private void handleAnswer(JSONObject answer) throws JSONException {
    synchronized (lock) {
      if (!status.equals(Status.CALLING_WAITING_ANSWER)) {
        Log.w(TAG, "Receiving answer in unexpected status: " + status);
        return;
      }

      setStatus(Status.ANSWER_RECEIVED_WAITING_STREAM);
      this.peerConnection.setRemoteDescription(this, extractSdp(answer));
    }
  }

  private void handleRemoteIceCandidate(JSONObject iceCandidate) throws JSONException {
    synchronized (lock) {
      if (this.peerConnection.getRemoteDescription() == null) {
        Log.w(TAG, "Receiving remote ice candidate when remote sdp is null. Status: " + status);
        return;
      }
      this.peerConnection.addIceCandidate(extractIceCandidate(iceCandidate));
    }
  }

  private void handleDisconnectMessage() {
    destroyConnection();
  }

  private SessionDescription extractSdp(JSONObject content) throws JSONException {
    String sdpType = content.getString(SignalMessages.SDP_TYPE);
    String sdpDescription = content.getString(SignalMessages.SDP_DESCRIPTION);
    return new SessionDescription(
        SessionDescription.Type.fromCanonicalForm(sdpType),
        sdpDescription);
  }

  private IceCandidate extractIceCandidate(JSONObject iceCandidate) throws JSONException {
    int sdpMLineIndex = iceCandidate.getInt(SignalMessages.CANDIDATE_SDP_M_LINE_INDEX);
    String sdpMid = iceCandidate.getString(SignalMessages.CANDIDATE_SDP_MID);
    String sdp = iceCandidate.getString(SignalMessages.CANDIDATE_SDP);
    return new IceCandidate(sdpMid, sdpMLineIndex, sdp);
  }

  private void destroyConnection() {
    synchronized (lock) {
      if (disconnected()) return; // Already disconnected.

      setStatus(Status.DISCONNECTED);
      this.peerConnection.removeStream(this.localMediaStream);
      this.peerConnection.close();
    }
  }

  private boolean streaming() {
    synchronized (lock) {
      return status.equals(Status.STREAMING);
    }
  }

  private boolean disconnected() {
    synchronized (lock) {
      return status.equals(Status.DISCONNECTED);
    }
  }

  private boolean outgoingCalling() {
    synchronized (lock) {
      return status.equals(Status.STARTED_WAITING_CALL)
          || status.equals(Status.CALLING_WAITING_ANSWER)
          || status.equals(Status.ANSWER_RECEIVED_WAITING_STREAM);
    }
  }

  private void setStatus(Status newStatus) {
    synchronized (lock) {
      Status oldStatus = status;
      status = newStatus;
      connectionHandler.onConnectionStateChanged(peerId, oldStatus, newStatus);
    }
  }

  // PeerConnection.Observer
  @Override
  public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

  @Override
  public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
    if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
      disconnect();
    }
  }

  @Override
  public void onIceConnectionReceivingChange(boolean b) {}

  @Override
  public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

  @Override
  public void onIceCandidate(IceCandidate iceCandidate) {
    try {
      signalingService.sendSignal(peerId,
          SignalMessages.createIceCandidateMessage(userId, iceCandidate));
    } catch (SignalingException e) {
      Log.w(TAG, "Failed to send Ice candidate information.", e);
    }
  }

  @Override
  public void onAddStream(MediaStream mediaStream) {
    synchronized (lock) {
      setStatus(Status.STREAMING);
      connectionHandler.onRemoteStreamAdded(peerId, mediaStream);
    }
  }

  @Override
  public void onRemoveStream(MediaStream mediaStream) {
    connectionHandler.onRemoteStreamRemoved(peerId, mediaStream);
    disconnect();
  }

  @Override
  public void onDataChannel(DataChannel dataChannel) {}

  @Override
  public void onRenegotiationNeeded() {}
  // PeerConnection.Observer Ends

  // SdpObserver
  @Override
  public void onCreateSuccess(SessionDescription sdp) {
    synchronized (lock) {
      this.peerConnection.setLocalDescription(this, sdp);

      try {
        switch (status) {
          case STARTED_WAITING_CALL: // Created an Offer sdp
            setStatus(Status.CALLING_WAITING_ANSWER);
            signalingService.sendSignal(peerId, SignalMessages.createOfferMessage(userId, sdp));
            break;
          case RECEIVED_WAITING_ANSWER: // Created an Answer sdp
            setStatus(Status.ANSWERED_WAITING_STREAM);
            signalingService.sendSignal(peerId, SignalMessages.createAnswerMessage(userId, sdp));
            break;
          default:
            Log.w(TAG, "Connection in illeagal state when session is created: " + status);
        }
      } catch (SignalingException e) {
        Log.e(TAG, "Failed when creating signal message.", e);
        disconnect();
      }
    }
  }

  @Override
  public void onSetSuccess() {}

  @Override
  public void onCreateFailure(String s) {
    Log.e(TAG, "Failed to create session: " + s);
    disconnect();
  }

  @Override
  public void onSetFailure(String s) {
    Log.e(TAG, "Failed to set SessionDescription: " + s);
    disconnect();
  }
  // SdpObserver Ends
}
