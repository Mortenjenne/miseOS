package app.services;

import app.persistence.daos.interfaces.*;

public class WeeklyMenuService
{
    private final IWeeklyMenuDAO menuDAO;
    private final IDishReader dishReader;
    private final IUserReader userReader;
    private final IStationReader stationReader;
    private final IDishTranslationService dishTranslationService;


    public WeeklyMenuService(IWeeklyMenuDAO menuDAO, IDishReader dishReader, IUserReader userReader, IStationReader stationReader, IDishTranslationService dishTranslationService)
    {
        this.menuDAO = menuDAO;
        this.dishReader = dishReader;
        this.userReader = userReader;
        this.stationReader = stationReader;
        this.dishTranslationService = dishTranslationService;
    }

    public WeeklyMenuDTO createMenu(Long creatorId, int week, int year)
    {
        dishReader.
    }
}
