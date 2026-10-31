package com.lemnt.trade.infrastructure.fulfillment;

import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.fulfillment.model.ShipmentTrack;
import com.lemnt.trade.infrastructure.fulfillment.gateway.ShipmentTrackGatewayImpl;
import com.lemnt.trade.infrastructure.fulfillment.tracking.ShipmentTrackClient;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentTrackGatewayTest {
    final Shipment shipment = new Shipment("TR1", "SHIP1", "HASH", "SF", "TRACK1",
            "测试收件人", "13800000000", "测试地址", LocalDateTime.now(), null);

    @Test
    void missingOrAmbiguousProviderCannotPretendThereAreNoTracks() {
        assertThrows(IllegalStateException.class,
                () -> new ShipmentTrackGatewayImpl(List.of()).tracks(shipment));
        var client = mock(ShipmentTrackClient.class);
        when(client.carrierCode()).thenReturn("SF");
        assertThrows(IllegalStateException.class,
                () -> new ShipmentTrackGatewayImpl(List.of(client, client)).tracks(shipment));
        verify(client, never()).tracks(any());
    }

    @Test
    void providerReceivesPersistedWaybillAndNullResponseIsNotSuccess() {
        var client = mock(ShipmentTrackClient.class);
        when(client.carrierCode()).thenReturn("SF");
        var gateway = new ShipmentTrackGatewayImpl(List.of(client));
        when(client.tracks(shipment)).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> gateway.tracks(shipment));
        var track = new ShipmentTrack(LocalDateTime.now(), "已揽收");
        when(client.tracks(shipment)).thenReturn(List.of(track));
        assertEquals(List.of(track), gateway.tracks(shipment));
        verify(client, times(2)).tracks(shipment);
    }
}
