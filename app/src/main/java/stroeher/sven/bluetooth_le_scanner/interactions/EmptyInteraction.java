package stroeher.sven.bluetooth_le_scanner.interactions;

import stroeher.sven.bluetooth_le_scanner.information.InformationList;

/**
 * Does nothing. Needed to check if the last regular interaction in the interaction queue is working correctly.
 */
class EmptyInteraction extends BluetoothInteraction {

    private Interactions interactions;

    /**
     * Creates an empty interaction.
     *
     * @param interactions The instance of interactions
     */
    EmptyInteraction(Interactions interactions) {
        this.interactions = interactions;
        setTimer(500);
    }

    /**
     * {@inheritDoc}
     *
     * @return True.
     */
    @Override
    boolean isFinished() {
        return true;
    }

    /**
     * {@inheritDoc}
     * Finishes this interaction.
     *
     * @return True.
     */
    @Override
    boolean execute() {
        interactions.interactionFinished();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param value The received data.
     * @return Null.
     */
    @Override
    InformationList interact(byte[] value) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @return Null.
     */
    @Override
    InformationList finish() {
        return null;
    }
}
