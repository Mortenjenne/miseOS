package app.integrations;

import app.enums.WeatherType;

public class WeatherCodeMapper {

    public static WeatherType fromCode(int code) {

        return switch (code) {

            case 0 -> WeatherType.CLEAR_SKY;
            case 1 -> WeatherType.MAINLY_CLEAR;
            case 2 -> WeatherType.PARTLY_CLOUDY;
            case 3 -> WeatherType.OVERCAST;

            case 45,48 -> WeatherType.FOG;

            case 51 -> WeatherType.DRIZZLE_LIGHT;
            case 53 -> WeatherType.DRIZZLE_MODERATE;
            case 55 -> WeatherType.DRIZZLE_HEAVY;

            case 61 -> WeatherType.RAIN_LIGHT;
            case 63 -> WeatherType.RAIN_MODERATE;
            case 65 -> WeatherType.RAIN_HEAVY;

            case 66 -> WeatherType.FREEZING_RAIN_LIGHT;
            case 67 -> WeatherType.FREEZING_RAIN_HEAVY;

            case 71,85 -> WeatherType.SNOW_LIGHT;
            case 73,86 -> WeatherType.SNOW_MODERATE;
            case 75 -> WeatherType.SNOW_HEAVY;

            case 80 -> WeatherType.RAIN_SHOWERS_LIGHT;
            case 81 -> WeatherType.RAIN_SHOWERS_MODERATE;
            case 82 -> WeatherType.RAIN_SHOWERS_HEAVY;

            case 95 -> WeatherType.THUNDERSTORM;
            case 96,99 -> WeatherType.THUNDERSTORM_HAIL;

            default -> WeatherType.MIXED;
        };
    }
}
