package app.controllers.impl;

import app.controllers.IAllergenController;
import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;
import app.services.IAllergenService;
import app.utils.RequestUtil;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;

public class AllergenController implements IAllergenController
{
    private final IAllergenService allergenService;

    public AllergenController(IAllergenService allergenService)
    {
        this.allergenService = allergenService;
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = RequestUtil.requirePathId(ctx,"id");
        ctx.status(200).json(allergenService.getAllergenById(id));
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(200).json(allergenService.getAllAllergens());
    }

    @Override
    public void create(Context ctx)
    {
        AllergenCreateRequestDTO dto = ctx.bodyValidator(AllergenCreateRequestDTO.class)
            .check(Objects::nonNull, "Request body cant be null")
            .check(a -> a.nameDA() != null && !a.nameDA().isBlank(), "Name DA is required")
            .check(a -> a.nameEN() != null && !a.nameEN().isBlank(), "Name EN is required")
            .check(a -> a.descriptionDA() != null && !a.descriptionDA().isBlank(), "Description DA is required")
            .check(a -> a.descriptionEN() != null && !a.descriptionEN().isBlank(), "Description EN is required")
            .check(a -> a.displayNumber() > 0, "Display number must be positive")
            .get();

        AllergenDTO allergenDTO = allergenService.registerAllergen(dto);
        ctx.status(201).json(allergenDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long allergenId = RequestUtil.requirePathId(ctx, "id");

        AllergenUpdateRequestDTO dto = ctx.bodyValidator(AllergenUpdateRequestDTO.class)
            .check(Objects::nonNull, "Request body cant be null")
            .check(a -> a.nameDA() != null && !a.nameDA().isBlank(), "Name DA is required")
            .check(a -> a.nameEN() != null && !a.nameEN().isBlank(), "Name EN is required")
            .check(a -> a.descriptionDA() != null && !a.descriptionDA().isBlank(), "Description DA is required")
            .check(a -> a.descriptionEN() != null && !a.descriptionEN().isBlank(), "Description EN is required")
            .check(a -> a.displayNumber() > 1, "Display number must be positive")
            .get();

        AllergenDTO allergenDTO = allergenService.updateAllergen(allergenId, dto);
        ctx.status(200).json(allergenDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long allergenId = RequestUtil.requirePathId(ctx, "id");

        boolean isDeleted = allergenService.deleteAllergen(allergenId);
        ctx.status(isDeleted ? 204 : 404);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = RequestUtil.requirePathString(ctx, "name");
        AllergenDTO dto = allergenService.getAllergenByNameDA(name);
        ctx.status(200).json(dto);
    }

    @Override
    public void seedEUAllergens(Context ctx)
    {
        List<AllergenDTO> allergens = allergenService.seedEUAllergens();
        ctx.status(201).json(allergens);
    }
}
