package app.services.impl;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.security.AuthenticatedUser;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.integrations.weather.IWeatherClient;
import app.mappers.StationMapper;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.IAiService;
import app.services.IMenuInspirationService;
import app.utils.ValidationUtil;

import java.util.List;
import java.util.function.Consumer;

public class MenuInspirationService implements IMenuInspirationService
{
    private final IAiService aiService;
    private final IUserReader userReader;
    private final IWeatherClient weatherClient;

    public MenuInspirationService(IAiService aiService, IUserReader userReader, IWeatherClient weatherClient)
    {
        this.aiService = aiService;
        this.userReader = userReader;
        this.weatherClient = weatherClient;
    }

    public List<AiDishSuggestionDTO> getDailyInspiration(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());

        User user = userReader.getByID(authUser.userId());
        Station station = user.getStation();
        StationDTO stationDTO = StationMapper.toDTO(station);

        WeatherForecastDTO weatherForecastDTO = weatherClient.getWeatherForecast();

        return aiService.getAiDishSuggestion(weatherForecastDTO, stationDTO);
    }

    @Override
    public void streamDailyInspiration(AuthenticatedUser authUser, Consumer<String> statusConsumer, Consumer<AiDishSuggestionDTO> dishConsumer, Runnable onComplete, Consumer<Throwable> errorConsumer)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());

        User user = userReader.getByID(authUser.userId());
        Station station = user.getStation();
        ValidationUtil.validateNotNull(station, "Station");

        statusConsumer.accept("Analyserer køkkenstationens udstyr...");
        StationDTO stationDTO = StationMapper.toDTO(station);
        statusConsumer.accept("Henter vejrdata for din lokation...");
        WeatherForecastDTO weatherForecastDTO = weatherClient.getWeatherForecast();
        statusConsumer.accept("Genererer menuforslag baseret på bæredygtighed...");
        aiService.getStreamingDishSuggestions(weatherForecastDTO, stationDTO, dishConsumer, onComplete, errorConsumer);
    }

}
