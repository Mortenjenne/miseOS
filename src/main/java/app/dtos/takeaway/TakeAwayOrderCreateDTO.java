package app.dtos.takeaway;

import java.util.List;

public record TakeAwayOrderCreateDTO(
    List<TakeAwayOrderLineCreateDTO> takeAwayOrderLines
)
{
}
