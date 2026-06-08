package fr.neiyo.resonance.api;

import java.util.concurrent.atomic.AtomicReference;

public final class ResonanceProvider {

    private static final AtomicReference<IResonanceManager> INSTANCE = new AtomicReference<>();

    private ResonanceProvider() {}

    public static void register(IResonanceManager manager) {
        INSTANCE.set(manager);
    }

    public static IResonanceManager get() {
        IResonanceManager manager = INSTANCE.get();
        if (manager == null) {
            throw new IllegalStateException("ResonanceManager system not registered");
        }
        return manager;
    }
}