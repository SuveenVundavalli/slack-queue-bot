package ml.slack.queue.bot.organizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponsePayload {

    public ResponsePayload(String text) {
        this.setResponseType("in_channel");
        this.text = text;
    }

    public ResponsePayload(String responseType, String text) {
        this.responseType = responseType;
        this.text = text;
    }

    @JsonProperty("response_type")
    private String responseType = "in_channel";
    private String text;

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "ResponsePayload{" +
                "responseType='" + responseType + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
