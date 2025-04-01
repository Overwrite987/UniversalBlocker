package ru.overwrite.ublocker.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public class WGUtils {

    public ObjectList<String> getRegions(Location location) {
        ObjectList<String> regions = new ObjectArrayList<>();
        if (getApplicableRegions(location) == null || getApplicableRegions(location).size() == 0) {
            return regions;
        }
        for (ProtectedRegion region : getApplicableRegions(location)) {
            regions.add(region.getId());
        }
        return regions;
    }

    private ApplicableRegionSet getApplicableRegions(Location location) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null || regionManager.getRegions().isEmpty())
            return null;
        return regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
    }

}
