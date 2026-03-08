package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;
import app.dtos.station.StationDTO;
import app.dtos.weather.WeatherForecastDTO;
import app.exceptions.UnauthorizedActionException;
import app.integrations.weather.IWeatherClient;
import app.mappers.StationMapper;
import app.persistence.daos.interfaces.IUserReader;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import app.utils.ValidationUtil;

import java.util.List;

public class MenuInspirationService
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

    public List<AiDishSuggestionDTO> getDailyInspiration(Long chefId)
    {
        ValidationUtil.validateId(chefId);
        User user = userReader.getByID(chefId);

        if(!user.isKitchenStaff())
        {
            throw new UnauthorizedActionException("Only staff can access daily menu inspirations");
        }

        Station station = user.getStation();
        StationDTO stationDTO = StationMapper.toDTO(station);

        WeatherForecastDTO weatherForecastDTO = weatherClient.getWeatherForecast();

        return aiService.getAiDishSuggestion(weatherForecastDTO, stationDTO);
    }
}
