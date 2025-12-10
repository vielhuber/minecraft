package me.david.test;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({ "DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Test extends JavaPlugin implements Listener {

    private Donneraxt donneraxt;
    private Machtklinge machtklinge;
    private Hirsch hirsch;
    private Horde horde;
    private Reset reset;
    private Fight fight;

    @Override
    public void onEnable() {
        donneraxt = new Donneraxt();
        donneraxt.register(this);

        machtklinge = new Machtklinge();
        machtklinge.register(this);

        hirsch = new Hirsch();
        hirsch.register(this);

        horde = new Horde();
        horde.register(this, hirsch, donneraxt, machtklinge);

        reset = new Reset();
        reset.register(this);

        fight = new Fight();
        fight.register(this);
    }

}
