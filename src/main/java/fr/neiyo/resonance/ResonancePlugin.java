package fr.neiyo.resonance;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import fr.neiyo.resonance.api.ResonanceProvider;
import fr.neiyo.resonance.core.ResonanceManager;

import javax.annotation.Nonnull;

public class ResonancePlugin extends JavaPlugin {

    public ResonancePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ResonanceProvider.register(new ResonanceManager());
    }
}