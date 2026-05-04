package com.github.jonasmelchior.mymme.protocol.s1ap;

import io.netty.channel.ChannelHandlerContext;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class S1apServiceTest {

    @Inject
    S1apService s1apService;

    @InjectMock
    com.github.jonasmelchior.mymme.protocol.nas.NasService nasService;

    @InjectMock
    com.github.jonasmelchior.mymme.repository.UeRepository ueRepository;

    @Test
    public void testHandleS1SetupRequest() {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        
        // S1 Setup Request Hex
        byte[] s1SetupRequest = new byte[] {
            (byte)0x00, (byte)0x11, (byte)0x00, (byte)0x2a, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, 
            (byte)0x3b, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0xf1, (byte)0x10, (byte)0x00, 
            (byte)0x00, (byte)0x40, (byte)0x00, (byte)0x00, (byte)0x3c, (byte)0x00, (byte)0x06, (byte)0x00, 
            (byte)0x65, (byte)0x4e, (byte)0x42, (byte)0x30, (byte)0x31, (byte)0x00, (byte)0x40, (byte)0x00, 
            (byte)0x07, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0xf1, (byte)0x10, 
            (byte)0x00, (byte)0x89, (byte)0x00, (byte)0x01, (byte)0x80
        };

        when(ctx.writeAndFlush(any())).thenReturn(mock(io.netty.channel.ChannelFuture.class));

        assertDoesNotThrow(() -> s1apService.handleIncomingMessage(ctx, s1SetupRequest));
    }

    @Test
    public void testHandleUplinkNasTransportAuthResponse() {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

        // Uplink NAS Transport Hex (Authentication Response)
        // Procedure Code: 13, Direction: Initiating Message (1)
        // This is a simplified hex for testing decoder changes
        byte[] uplinkNasTransport = new byte[] {
            (byte)0x00, (byte)0x0d, (byte)0x40, (byte)0x23, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, 
            (byte)0x08, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x1a, 
            (byte)0x00, (byte)0x0a, (byte)0x09, (byte)0x07, (byte)0x53, (byte)0x08, (byte)0x11, (byte)0x22, 
            (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x00, (byte)0x64, (byte)0x40, (byte)0x08, 
            (byte)0x00, (byte)0x00, (byte)0xf1, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, 
            (byte)0x43, (byte)0x40, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0xf1, (byte)0x10, (byte)0x00, 
            (byte)0x01
        };

        when(ueRepository.findByEnbUeS1apId(anyInt())).thenReturn(java.util.Optional.of(new com.github.jonasmelchior.mymme.data.UeContext()));

        assertDoesNotThrow(() -> s1apService.handleIncomingMessage(ctx, uplinkNasTransport));
    }
}
