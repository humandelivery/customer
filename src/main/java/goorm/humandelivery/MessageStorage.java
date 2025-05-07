package goorm.humandelivery;

import goorm.humandelivery.dto.*;

import java.util.LinkedList;
import java.util.Queue;

class MessageStorage {
    private final Queue<DrivingSummaryResponse> drivingSummaryResponseQueue = new LinkedList<>();
    private final Queue<TaxiInfo> taxiInfoQueue = new LinkedList<>();
    private final Queue<DrivingSummaryResponse> taxiLocationQueue = new LinkedList<>();

    public synchronized void storeTaxiResult(DrivingSummaryResponse result) {
        drivingSummaryResponseQueue.offer(result);
    }

    public synchronized void storeTaxiInfo(TaxiInfo info) {
        taxiInfoQueue.offer(info);
    }


    public synchronized DrivingSummaryResponse retrieveTaxiResult() {
        return drivingSummaryResponseQueue.poll();
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

        if (currentState == ClientState.COMPLETED) {
            while (!drivingSummaryResponseQueue.isEmpty()) {
                DrivingSummaryResponse result = retrieveTaxiResult();
                if (result != null) {
                    System.out.println("운행 결과: " + result);
                }
            }
        }
    }
}
