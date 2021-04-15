public class MessageInfo {
    private int time; //simulation cycle of rcssserver
    private String sender;
    private String uttered;

    public MessageInfo(int time, String sender, String uttered) {
        this.time = time;
        this.sender = sender;
        this.uttered = uttered;
    }


    public int getTime() {
        return time;
    }

    public String getSender() {
        return sender;
    }

    public String getUttered() {
        return uttered;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "time=" + time +
                ", sender='" + sender + '\'' +
                ", uttered='" + uttered + '\'' +
                '}';
    }
}
