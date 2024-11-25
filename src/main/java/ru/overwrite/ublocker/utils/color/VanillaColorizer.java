package ru.overwrite.ublocker.utils.color;

import ru.overwrite.ublocker.utils.Utils;

public class VanillaColorizer implements Colorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return Utils.translateAlternateColorCodes('&', message);
    }
}
