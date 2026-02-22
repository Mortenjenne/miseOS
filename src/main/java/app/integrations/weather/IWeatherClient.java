package app.integrations.weather;

import app.dtos.weather.WeatherForecastDTO;

public interface IWeatherClient
{
    WeatherForecastDTO getWeatherForecast();
}
