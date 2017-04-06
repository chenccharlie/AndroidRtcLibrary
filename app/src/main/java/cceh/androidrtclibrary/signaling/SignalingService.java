package cceh.androidrtclibrary.signaling;

import org.json.JSONObject;

/**
 * The interface for the signaling service used for setting up WebRtc connection.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/5/17.
 */
public interface SignalingService {

  /**
   * Callback to be registered with {@link SignalingService} to handle incoming signals after
   * {@link SignalingService#listenOn(String, SignalHandler)} a user id.
   */
  interface SignalHandler {

    /** Called when successfully listened to the user id. */
    void onConnected(String userId);

    /** Called when stops listening to a user id. */
    void onDisconnected(String userId);

    /** Called when a signal is received. */
    void onSignalReiceived(String userId, JSONObject signal);
  }

  /** Listens for signals sent to the specified user id. */
  void listenOn(String userId, SignalHandler signalHandler) throws SignalingException;

  /** Stops listening to signals to a user id. */
  void stopListening(String userId);

  /** Send signal message to a peer. */
  void sendSignal(String peerUserId, JSONObject signal);
}
