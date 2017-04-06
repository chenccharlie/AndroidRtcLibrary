package cceh.androidrtclibrary.signaling;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link SignalingService} which uses {@link com.pubnub.api.Pubnub} apis.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/5/17.
 */
public class PubnubSignalingService
    extends Callback
    implements SignalingService {

  private final Pubnub pubnub;
  private final Map<String, SignalHandler> signalHandlers;

  private Object signalHandlerLock;

  public PubnubSignalingService(
      String pubKey,
      String subKey) {
    this.pubnub = new Pubnub(pubKey, subKey);
    this.signalHandlers = new HashMap<>();
    this.signalHandlerLock = new Object();
  }

  @Override
  public void listenOn(String userId, SignalHandler signalHandler) throws SignalingException {
    synchronized (signalHandlerLock) {
      if (signalHandlers.containsKey(userId)) {
        throw new SignalingException("UserId: " + userId + " is already listened on.");
      }
      signalHandlers.put(userId, signalHandler);
      try {
        pubnub.subscribe(userId, this);
      } catch (PubnubException e) {
        throw new SignalingException("Failed on listening to: " + userId, e);
      }
    }
  }

  @Override
  public void stopListening(String userId) {
    pubnub.unsubscribe(userId);
  }

  @Override
  public void sendSignal(String peerUserId, JSONObject signal) {
    pubnub.publish(peerUserId, signal, this);
  }

  @Override
  public void connectCallback(String channel, Object message) {
    synchronized (signalHandlerLock) {
      if (signalHandlers.containsKey(channel)) {
        signalHandlers.get(channel).onConnected(channel);
      }
    }
  }

  @Override
  public void disconnectCallback(String channel, Object message) {
    synchronized (signalHandlerLock) {
      if (signalHandlers.containsKey(channel)) {
        signalHandlers.get(channel).onDisconnected(channel);
        signalHandlers.remove(channel);
      }
    }
  }

  @Override
  public void successCallback(String channel, Object message) {
    synchronized (signalHandlerLock) {
      if (!(message instanceof JSONObject)) return; // Ignore if not valid JSON.
      if (signalHandlers.containsKey(channel)) {
        signalHandlers.get(channel).onSignalReiceived(channel, (JSONObject) message);
      }
    }
  }
}
