package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler> clients;

    public ConnectionsImpl(){
        this.clients = new ConcurrentHashMap<>();
    }

    public void addClient(int connectionId, ConnectionHandler client){
        clients.put(connectionId, client);
    }

    @Override @SuppressWarnings("unchecked")
    public boolean send(int connectionId, T msg) {
        ConnectionHandler client = clients.get(connectionId);
        client.send(msg);
        return true;
    }

    @Override @SuppressWarnings("unchecked")
    public void broadcast(T msg) {
        clients.forEach((id, client) -> client.send(msg));
    }

    @Override
    public void disconnect(int connectionId) {
        try {
            clients.get(connectionId).close();
        } catch (IOException e) {
            System.out.println("disconnected");
        }
        clients.remove(connectionId);
    }
}
