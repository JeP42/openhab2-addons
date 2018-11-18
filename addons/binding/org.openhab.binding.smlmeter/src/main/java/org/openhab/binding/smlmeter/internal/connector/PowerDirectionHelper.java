package org.openhab.binding.smlmeter.internal.connector;

public class PowerDirectionHelper {

    private MeterSnapshot previousSnapshot;

    public PowerDirection getDirectionFor(MeterSnapshot currentSnapshot) {
        PowerDirection direction = PowerDirection.UNKNOWN;

        if (previousSnapshot != null) {
            if (this.isIncreasedConsumption(currentSnapshot)) {
                direction = PowerDirection.IN;
            } else if (this.isIncreasedProduction(currentSnapshot)) {
                direction = PowerDirection.OUT;
            } else {
                direction = PowerDirection.NONE;
            }
        }
        this.previousSnapshot = currentSnapshot;
        return direction;
    }

    private boolean isIncreasedConsumption(MeterSnapshot currentSnapshot) {
        return previousSnapshot.getObis180().compareTo(currentSnapshot.getObis180()) < 0;
    }

    private boolean isIncreasedProduction(MeterSnapshot currentSnapshot) {
        return previousSnapshot.getObis280().compareTo(currentSnapshot.getObis280()) < 0;
    }

}
