package org.openhab.binding.smlmeter.internal.connector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PowerDirectionHelperTest {

    @Mock
    MeterSnapshot previousSnapshot;

    @InjectMocks
    private PowerDirectionHelper powerDirectionHelper = new PowerDirectionHelper();

    @Test
    public void testFirstInvocation() {

        MeterSnapshot snapshot = mock(MeterSnapshot.class);

        assertEquals(PowerDirection.UNKNOWN, new PowerDirectionHelper().getDirectionFor(snapshot));
    }

    @Test
    public void testProduction() {

        Mockito.reset(previousSnapshot);
        Mockito.when(previousSnapshot.getObis180()).thenReturn(BigDecimal.valueOf(10));
        Mockito.when(previousSnapshot.getObis280()).thenReturn(BigDecimal.valueOf(10));

        MeterSnapshot current = new MeterSnapshot();
        current.setObis180(BigDecimal.valueOf(10));
        current.setObis280(BigDecimal.valueOf(11));

        assertEquals(PowerDirection.OUT, powerDirectionHelper.getDirectionFor(current));
    }

    @Test
    public void testConsumption() {

        Mockito.when(previousSnapshot.getObis180()).thenReturn(BigDecimal.valueOf(10));

        MeterSnapshot current = new MeterSnapshot();
        current.setObis180(BigDecimal.valueOf(11));
        current.setObis280(BigDecimal.valueOf(10));

        assertEquals(PowerDirection.IN, powerDirectionHelper.getDirectionFor(current));
    }

    @Test
    public void testNoPower() {

        Mockito.when(previousSnapshot.getObis180()).thenReturn(BigDecimal.valueOf(10));
        Mockito.when(previousSnapshot.getObis280()).thenReturn(BigDecimal.valueOf(10));

        MeterSnapshot current = new MeterSnapshot();
        current.setObis180(BigDecimal.valueOf(10));
        current.setObis280(BigDecimal.valueOf(10));

        assertEquals(PowerDirection.NONE, powerDirectionHelper.getDirectionFor(current));
    }

}
