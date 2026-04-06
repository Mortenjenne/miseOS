package app.persistence.daos.interfaces.readers;

import app.dtos.menu.RecentMenuDishDTO;
import app.persistence.daos.interfaces.generic.IEntityReader;
import app.persistence.entities.WeeklyMenu;

import java.util.List;

public interface IWeeklyMenuReader extends IEntityReader<WeeklyMenu, Long>
{
    List<RecentMenuDishDTO> findRecentPublishedMenuDishesByStation(Long stationId, int year, int fromWeek, int toWeek);
}
