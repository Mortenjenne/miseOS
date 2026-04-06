package app.utils;

import app.dtos.weather.WeatherForecastDTO;
import app.enums.WeatherType;

import java.util.List;

public class WeatherForecastBuilder
{
    private WeatherForecastBuilder(){}

    public static String getWeatherForecast(WeatherForecastDTO weatherForecastDTO)
    {
        StringBuilder sb = new StringBuilder();

        if (weatherForecastDTO == null || weatherForecastDTO.dailyWeatherDTO() == null)
        {
            return "No weather data available! Please use season instead";
        }

        List<String> days = weatherForecastDTO.dailyWeatherDTO().days();
        String temperatureUnit = weatherForecastDTO.weatherUnitsDTO().temperatureUnit();
        String measuringUnit = weatherForecastDTO.weatherUnitsDTO().precipitationUnit();

        sb.append("Weather forecast next 7 days in ").append(weatherForecastDTO.timeZone()).append(" :");

        for (int i = 0; i < days.size(); i++)
        {
            double maxTemp = weatherForecastDTO.dailyWeatherDTO().maxTemperatures().get(i);
            double minTemp = weatherForecastDTO.dailyWeatherDTO().minTemperatures().get(i);
            double rain = weatherForecastDTO.dailyWeatherDTO().precipitationSums().get(i);
            int weatherCode = weatherForecastDTO.dailyWeatherDTO().weatherCodes().get(i);
            String weatherType = fromCode(weatherCode).name();

            sb.append(String.format("[%s: %s%s/%s%s, precipitation sum: %s %s, weather type: %s] ",
                days.get(i), maxTemp, temperatureUnit, minTemp, temperatureUnit, rain, measuringUnit, weatherType));
        }

        double averageMaxTemperature = weatherForecastDTO.dailyWeatherDTO().maxTemperatures().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);

        sb.append(String.format("Average week maximum temperature: %f", averageMaxTemperature));

        return sb.toString();
    }

    private static WeatherType fromCode(int code) {

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
