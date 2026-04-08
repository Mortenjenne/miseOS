package app.dtos.takeaway;

import java.time.LocalDate;
import java.util.List;

public record TakeAwaySummaryDTO(
    LocalDate date,
    int totalOfferedPortions,
    int totalSoldPortions,
    int totalRemainingPortions,
    int totalOrders,
    List<TakeAwayOfferSummaryDTO> summaryPerOffer
) {

}
