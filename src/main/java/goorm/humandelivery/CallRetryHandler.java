package goorm.humandelivery;

import goorm.humandelivery.dto.CallRequest;
import goorm.humandelivery.dto.ClientState;
import goorm.humandelivery.dto.ClientStatusContext;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CallRetryHandler {
    private static final int MAX = 3;
    private static final int RETRY_DELAY_MS = 5; // 5초
    private CallRequest lastCallRequest;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void setLastCallRequest(CallRequest request) {
        this.lastCallRequest = request;
    }

    public void retry(StompSession session) {
        if (lastCallRequest == null) {
            System.out.println("재시도할 요청이 없습니다.");
            return;
        }

        int currentRetry = lastCallRequest.getRetryCount();
        if (currentRetry >= MAX) {
            currentRetry = 0;
        }

        int delaySeconds = RETRY_DELAY_MS;
        System.out.println(delaySeconds + "초 후 재시도합니다.");
        ClientStatusContext context = new ClientStatusContext();
        context.setState(ClientState.WAITING);


        final int nextRetryCount = currentRetry + 1;

        scheduler.schedule(() -> {
            CallRequest retryRequest = new CallRequest(
                    lastCallRequest.getExpectedOrigin(),
                    lastCallRequest.getExpectedDestination(),
                    lastCallRequest.getTaxiType(),
                    nextRetryCount
            );
            lastCallRequest = retryRequest;
            session.send("/app/call/request", retryRequest);
            System.out.println("콜 재시도 요청 전송 (" + nextRetryCount + "회차)");
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public CallRequest getLastCallRequest() {
        return lastCallRequest;
    }

    public void shutdown() {
        System.out.println("콜 재시도 스케줄러 종료 중...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("강제 종료");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
