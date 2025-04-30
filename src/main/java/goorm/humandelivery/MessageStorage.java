package goorm.humandelivery;

import goorm.humandelivery.dto.*;

import java.util.LinkedList;
import java.util.Queue;

class MessageStorage {
    private final Queue<TaxiLocation> taxiLocationQueue = new LinkedList<>();
    private final Queue<TaxiResult> taxiResultQueue = new LinkedList<>();
    private final Queue<TaxiInfo> taxiInfoQueue = new LinkedList<>();

    public synchronized void storeTaxiLocation(TaxiLocation location) {
        taxiLocationQueue.offer(location);
    }

    public synchronized void storeTaxiResult(TaxiResult result) {
        taxiResultQueue.offer(result);
    }

    public synchronized void storeTaxiInfo(TaxiInfo info) {
        taxiInfoQueue.offer(info);
    }

    public synchronized TaxiLocation retrieveTaxiLocation() {
        return taxiLocationQueue.poll();
    }

    public synchronized TaxiResult retrieveTaxiResult() {
        return taxiResultQueue.poll();
    }

    public synchronized TaxiInfo retrieveTaxiInfo() {
        return taxiInfoQueue.poll();
    }

    public void processPendingMessages(ClientStatusContext statusContext) {
        ClientState currentState = statusContext.getState();

        if (currentState == ClientState.READY) {
            while (!taxiInfoQueue.isEmpty()) {
                TaxiInfo info = retrieveTaxiInfo();
                if (info != null) {
                    System.out.println("택시 정보 수신(배차 완료): " + info);
                    statusContext.setState(ClientState.MATCHED);
                }
            }
        }

        if (currentState == ClientState.MATCHED) {
            while (!taxiLocationQueue.isEmpty()) {
                TaxiLocation location = retrieveTaxiLocation();
                if (location != null) {
                    String address = KakaoMap.convertCoordinatesToAddress(
                            location.getLocation().getLatitude(),
                            location.getLocation().getLongitude()
                    );
                    System.out.println("택시 위치: " + address);
                    statusContext.setState(ClientState.MOVING);
                }
            }
        }

        if (currentState == ClientState.COMPLETED) {
            while (!taxiResultQueue.isEmpty()) {
                TaxiResult result = retrieveTaxiResult();
                if (result != null) {
                    System.out.println("운행 결과: " + result);
                }
            }
        }
    }
}
