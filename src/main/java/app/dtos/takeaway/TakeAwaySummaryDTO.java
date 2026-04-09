package app.dtos.takeaway;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record TakeAwaySummaryDTO(
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    int totalOfferedPortions,
    Long totalSoldPortions,
    int totalRemainingPortions,
    Long totalOrders,
    List<TakeAwayOfferSummaryDTO> summaryPerOffer
) {

}
