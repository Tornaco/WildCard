package dev.nick.app.pinlock.secure;

public interface VividDotListener {
    /**
     * Called when a pin key has been added to dot list.
     *
     * @param added The added pin key.
     */
    abstract void onPinKeyAdded(PinKey added);

    /**
     * Called when the input pin key has been rejected because we hit the max pin code count.
     */
    abstract void onOutOfBound();
}
