package cn.boasoft.boabot.adapter;

public class event {
    public final String type;
    public final String data;
    public final String more;

    public event(String type, String data, String more) {
        this.type = type;
        this.data = data;
        this.more = more;
    }
}