package app.services.impl;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.menu.RecentMenuDishDTO;
import app.dtos.security.AuthenticatedUser;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.integrations.weather.IWeatherClient;
import app.mappers.StationMapper;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.daos.interfaces.readers.IWeeklyMenuReader;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.services.IAiService;
import app.services.IMenuInspirationService;
import app.utils.ValidationUtil;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.function.Consumer;

public class MenuInspirationService implements IMenuInspirationService
{
    private final IAiService aiService;
    private final IUserReader userReader;
    private final IWeatherClient weatherClient;
    private final IWeeklyMenuReader weeklyMenuReader;

    public MenuInspirationService(IAiService aiService, IUserReader userReader, IWeatherClient weatherClient, IWeeklyMenuReader weeklyMenuReader)
    {
        this.aiService = aiService;
        this.userReader = userReader;
        this.weatherClient = weatherClient;
        this.weeklyMenuReader = weeklyMenuReader;
    }

    public List<AiDishSuggestionDTO> getDailyInspiration(AuthenticatedUser authUser)
    {
        ValidationUtil.validateNotNull(authUser, "Authenticated User");
        ValidationUtil.validateId(authUser.userId());

        User user = userReader.getByID(authUser.userId());
        Station station = user.getStation();
        ValidationUtil.validateNotNull(station, "Station");
        StationDTO stationDTO = StationMapper.toDTO(station);
        List<RecentMenuDishDTO> recentMenuDishDTOS = getRecentMenuDishesContext(station.getId());
        WeatherForecastDTO weatherForecastDTO = weatherClient.getWeatherForecast();

        return aiService.getAiDishSuggestion(weatherForecastDTO, stationDTO, recentMenuDishDTOS);
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
        statusConsumer.accept("Henter seneste menuretter fra stationen...");
        List<RecentMenuDishDTO> recentMenuDishDTOS = getRecentMenuDishesContext(station.getId());
        statusConsumer.accept("Forbereder data og bæredygtighedsregler...");
        statusConsumer.accept("Starter generering af menuforslag...");
        aiService.getStreamingDishSuggestions(weatherForecastDTO, stationDTO, recentMenuDishDTOS, dishConsumer, onComplete, errorConsumer);
    }

    private List<RecentMenuDishDTO> getRecentMenuDishesContext(Long stationId)
    {
        int currentWeek = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int currentYear = LocalDate.now().get(IsoFields.WEEK_BASED_YEAR);

        int weeksBack = 2;
        int fromWeek = Math.max(1, currentWeek - weeksBack);

        return weeklyMenuReader.findRecentPublishedMenuDishesByStation(
            stationId,
            currentYear,
            fromWeek,
            currentWeek
        );
    }

}
