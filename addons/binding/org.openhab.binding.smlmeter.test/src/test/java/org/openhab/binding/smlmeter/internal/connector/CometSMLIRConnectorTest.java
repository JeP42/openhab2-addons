package org.openhab.binding.smlmeter.internal.connector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CometSMLIRConnectorTest {

    @Mock
    private Socket socket;

    @InjectMocks
    CometCOM1IRConnector cometConnector = new CometCOM1IRConnector("", 1);

    @Before
    public void setup() throws Exception {
        InputStream systemResourceAsStream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream("EMH-eHZ-HW8E2A5L0EQ2P.sml");
        doReturn(systemResourceAsStream).when(socket).getInputStream();
    }

    @Test
    public void loadSnapshot() throws Exception {

        long tickBeforeTheTest = System.currentTimeMillis();

        MeterSnapshot snapshot = new SmlReader().read(cometConnector.getRawSmlFile());

        assertTrue(tickBeforeTheTest < snapshot.getTime());
        assertEquals("EMH", snapshot.getVendorId());
        assertEquals("06-45-4D-48-01-02-71-5A-72-7E", snapshot.getDeviceId());
        assertEquals(new BigDecimal("3072.0"), snapshot.getObis1570());
        assertEquals(new BigDecimal("28069.1772"), snapshot.getObis180());
        assertEquals(new BigDecimal("28069.1772"), snapshot.getObis181());
        assertEquals(new BigDecimal("35352.5595"), snapshot.getObis280());
        assertEquals(new BigDecimal("35352.5595"), snapshot.getObis281());
    }
}
