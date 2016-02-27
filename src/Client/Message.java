package Client;

public class Message {

	private String date;
	private String object;
	private String recipient;
	private String sender;
	private String copy;
	private String body;
	
	Message(String sender, String recipient, String date, String object, String body){
		this.sender = sender;
		this.recipient = recipient;
		this.date = date;
		this.object = object;
		this.body = body;
	}

	public Message() {
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public String getCopy() {
		return copy;
	}

	public void setCopy(String copy) {
		this.copy = copy;
	}
}
