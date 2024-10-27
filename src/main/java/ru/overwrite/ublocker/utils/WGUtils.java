package ru.overwrite.ublocker.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WGUtils {

    public static List<String> getRegions(Location location) {
        List<String> regions = new ArrayList<>();
        if (getApplicableRegions(location) == null || getApplicableRegions(location).size() == 0) {
            return regions;
        }
        for (ProtectedRegion region : getApplicableRegions(location)) {
            regions.add(region.getId());
        }
        return regions;
    }

    public static ApplicableRegionSet getApplicableRegions(Location location) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null || regionManager.getRegions().isEmpty())
            return null;
        return regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
    }

}
