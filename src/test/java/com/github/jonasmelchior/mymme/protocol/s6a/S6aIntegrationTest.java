package com.github.jonasmelchior.mymme.protocol.s6a;

import com.github.jonasmelchior.mymme.service.AuthenticationVectorsReceivedEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class S6aIntegrationTest {

    @Inject
    S6aService s6aService;

    private static final CountDownLatch latch = new CountDownLatch(1);
    private static String receivedImsi = null;

    void onAuthVectors(@Observes AuthenticationVectorsReceivedEvent event) {
        receivedImsi = event.getImsi();
        latch.countDown();
    }

    @Test
    public void testAuthenticationInformationRequest() throws InterruptedException {
        String testImsi = "001010000000001";
        
        // Trigger the AIR
        s6aService.sendAuthenticationInformationRequest(testImsi);
        
        // Wait for AIA to be received and processed (max 10 seconds)
        boolean received = latch.await(10, TimeUnit.SECONDS);
        
        assertTrue(received, "AIA was not received within timeout");
        System.out.println("Successfully received AIA for IMSI: " + receivedImsi);
    }
}
