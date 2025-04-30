package goorm.humandelivery.dto;

public class ClientStatusContext {

    private ClientState currentState = ClientState.WAITING;

    public synchronized ClientState getState() {
        return currentState;
    }

    public synchronized void setState(ClientState newState) {
        System.out.println("[상태 변경] " + currentState + " -> " + newState);
        currentState = newState;
    }
}
