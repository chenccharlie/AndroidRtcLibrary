package cceh.androidrtclibrary.signaling;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

/**
 * Implementation of {@link SignalingService} which uses {@link com.pubnub.api.Pubnub} apis.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/5/17.
 */
public class PubnubSignalingService
    extends Callback
    implements SignalingService {

  private final Pubnub pubnub;
  private final SignalHandler signalHandler;

  public PubnubSignalingService(
      String pubKey,
      String subKey,
      SignalHandler signalHandler) {
    this.pubnub = new Pubnub(pubKey, subKey);
    this.signalHandler = signalHandler;
  }

  @Override
  public void listenOn(String userId) throws SignalingException {
    try {
      pubnub.subscribe(userId, this);
    } catch (PubnubException e) {
      throw new SignalingException("Failed on listening to: " + userId, e);
    }
  }

  @Override
  public void stopListening() {
    pubnub.unsubscribeAll();
  }

  @Override
  public void sendSignal(String peerUserId, JSONObject signal) {
    pubnub.publish(peerUserId, signal, this);
  }

  @Override
  public void connectCallback(String channel, Object message) {
    signalHandler.onConnected(channel);
  }

  @Override
  public void disconnectCallback(String channel, Object message) {
    signalHandler.onDisconnected(channel);
  }

  @Override
  public void successCallback(String channel, Object message) {
    if (!(message instanceof JSONObject)) return; // Ignore if not valid JSON.
    signalHandler.onSignalReiceived(channel, (JSONObject) message);
  }
}
