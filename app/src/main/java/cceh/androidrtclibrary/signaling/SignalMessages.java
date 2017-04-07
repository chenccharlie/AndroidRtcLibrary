package cceh.androidrtclibrary.signaling;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Factory class for generating signal messages.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public class SignalMessages {
  public static final String SENDER = "sender";
  public static final String SIGNAL_TYPE = "signal_type";
  public static final String SIGNAL_CONTENT = "signal_content";
  public static final String SDP_TYPE = "sdp_type";
  public static final String SDP_DESCRIPTION = "sdp_description";
  public static final String CANDIDATE_SDP_M_LINE_INDEX = "candidate_sdp_m_line_index";
  public static final String CANDIDATE_SDP_MID = "candidate_sdp_mid";
  public static final String CANDIDATE_SDP = "candidate_sdp";

  public static final String TYPE_OFFER = "type_offer";
  public static final String TYPE_ANSWER = "type_answer";
  public static final String TYPE_ICE_CANDIDATE = "type_ice_candidate";
  public static final String TYPE_DISCONNECT = "type_disconnect";

  public static JSONObject createOfferMessage(String userId, SessionDescription sdp)
      throws SignalingException {
    try {
      JSONObject message = new JSONObject();
      message.put(SENDER, userId);
      message.put(SIGNAL_TYPE, TYPE_OFFER);
      JSONObject content = new JSONObject();
      content.put(SDP_TYPE, sdp.type.canonicalForm());
      content.put(SDP_DESCRIPTION, sdp.description);
      message.put(SIGNAL_CONTENT, content);
      return message;
    } catch (JSONException e) {
      throw new SignalingException(e);
    }
  }

  public static JSONObject createAnswerMessage(String userId, SessionDescription sdp)
      throws SignalingException {
    try {
      JSONObject message = new JSONObject();
      message.put(SENDER, userId);
      message.put(SIGNAL_TYPE, TYPE_ANSWER);
      JSONObject content = new JSONObject();
      content.put(SDP_TYPE, sdp.type.canonicalForm());
      content.put(SDP_DESCRIPTION, sdp.description);
      message.put(SIGNAL_CONTENT, content);
      return message;
    } catch (JSONException e) {
      throw new SignalingException(e);
    }
  }

  public static JSONObject createIceCandidateMessage(String userId, IceCandidate iceCandidate)
      throws SignalingException {
    try {
      JSONObject message = new JSONObject();
      message.put(SENDER, userId);
      message.put(SIGNAL_TYPE, TYPE_ICE_CANDIDATE);
      JSONObject content = new JSONObject();
      content.put(CANDIDATE_SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
      content.put(CANDIDATE_SDP_MID, iceCandidate.sdpMid);
      content.put(CANDIDATE_SDP, iceCandidate.sdp);
      message.put(SIGNAL_CONTENT, content);
      return message;
    } catch (JSONException e) {
      throw new SignalingException(e);
    }
  }

  public static JSONObject createDisconnectMessage(String userId)
      throws SignalingException {
    try {
      JSONObject message = new JSONObject();
      message.put(SENDER, userId);
      message.put(SIGNAL_TYPE, TYPE_DISCONNECT);
      return message;
    } catch (JSONException e) {
      throw new SignalingException(e);
    }
  }
}
