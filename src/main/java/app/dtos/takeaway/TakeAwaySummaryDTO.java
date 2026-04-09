package app.dtos.takeaway;

import java.time.LocalDate;
import java.util.List;

public record TakeAwaySummaryDTO(
    LocalDate date,
    int totalOfferedPortions,
    Long totalSoldPortions,
    int totalRemainingPortions,
    Long totalOrders,
    List<TakeAwayOfferSummaryDTO> summaryPerOffer
) {

}
