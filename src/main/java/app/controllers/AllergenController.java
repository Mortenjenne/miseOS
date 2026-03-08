package app.controllers;

import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;
import app.services.IAllergenService;
import app.utils.SecurityUtil;
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
        Long id = requirePathId(ctx, "id");
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
        Long userId = SecurityUtil.requireUserId(ctx);

        AllergenCreateRequestDTO dto = ctx.bodyValidator(AllergenCreateRequestDTO.class)
            .check(Objects::nonNull, "Request body cant be null")
            .check(a -> a.nameDA() != null && !a.nameDA().isBlank(), "Name DA is required")
            .check(a -> a.nameEN() != null && !a.nameEN().isBlank(), "Name EN is required")
            .check(a -> a.descriptionDA() != null && !a.descriptionDA().isBlank(), "Description DA is required")
            .check(a -> a.descriptionEN() != null && !a.descriptionEN().isBlank(), "Description EN is required")
            .check(a -> a.displayNumber() > 1, "Display number must be positive")
            .get();

        AllergenDTO allergenDTO = allergenService.registerAllergen(userId, dto);
        ctx.status(201).json(allergenDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long allergenId = requirePathId(ctx, "id");
        Long userId = SecurityUtil.requireUserId(ctx);

        AllergenUpdateRequestDTO dto = ctx.bodyValidator(AllergenUpdateRequestDTO.class)
        .check(Objects::nonNull, "Request body cant be null")
            .check(a -> a.nameDA() != null && !a.nameDA().isBlank(), "Name DA is required")
            .check(a -> a.nameEN() != null && !a.nameEN().isBlank(), "Name EN is required")
            .check(a -> a.descriptionDA() != null && !a.descriptionDA().isBlank(), "Description DA is required")
            .check(a -> a.descriptionEN() != null && !a.descriptionEN().isBlank(), "Description EN is required")
            .check(a -> a.displayNumber() > 1, "Display number must be positive")
        .get();

        AllergenDTO allergenDTO = allergenService.updateAllergen(allergenId, userId, dto);
        ctx.status(200).json(allergenDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long allergenId = requirePathId(ctx, "id");
        Long userId = SecurityUtil.requireUserId(ctx);

        boolean isDeleted = allergenService.deleteAllergen(allergenId, userId);
        ctx.status(204).json(isDeleted);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        AllergenDTO dto = allergenService.getAllergenByNameDA(name);
        ctx.status(200).json(dto);
    }

    @Override
    public void seedEUAllergens(Context ctx)
    {
        Long userId = SecurityUtil.requireUserId(ctx);
        List<AllergenDTO> allergens = allergenService.seedEUAllergens(userId);
        ctx.status(201).json(allergens);
    }

    private Long requirePathId(Context ctx, String param)
    {
        return ctx.pathParamAsClass(param, Long.class)
            .check(i -> i > 0, "ID must be positive")
            .get();
    }
}
