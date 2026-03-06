package app.controllers;

import app.dtos.allergen.AllergenCreateRequestDTO;
import app.dtos.allergen.AllergenDTO;
import app.dtos.allergen.AllergenUpdateRequestDTO;
import app.services.IAllergenService;
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
        ctx.status(200);
        ctx.json(allergenService.getAllergenById(id));
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(200);
        ctx.json(allergenService.getAllAllergens());
    }

    @Override
    public void create(Context ctx)
    {
        AllergenCreateRequestDTO dto = ctx.bodyValidator(AllergenCreateRequestDTO.class)
            .check(Objects::nonNull, "Request cant be null")
            .check(a -> a.nameDA().isBlank(), "Name should be filled")
            .get();

        Long userId = ctx.attribute("userId");

        AllergenDTO allergenDTO = allergenService.registerAllergen(userId, dto);
        ctx.status(201);
        ctx.json(allergenDTO);
    }

    @Override
    public void update(Context ctx)
    {
        Long allergenId = requirePathId(ctx, "id");
        Long userId = ctx.attribute("userId");

        AllergenUpdateRequestDTO dto = ctx.bodyValidator(AllergenUpdateRequestDTO.class)
        .check(Objects::nonNull, "Request cant be null")
        .check(a -> a.nameDA().isBlank(), "Name should be filled")
        .get();

        AllergenDTO allergenDTO = allergenService.updateAllergen(allergenId, userId, dto);
        ctx.status(200);
        ctx.json(allergenDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        Long allergenId = requirePathId(ctx, "id");
        Long userId = ctx.attribute("userId");

        boolean isDeleted = allergenService.deleteAllergen(allergenId, userId);
        ctx.status(204);
        ctx.json(isDeleted);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        AllergenDTO dto = allergenService.getAllergenByNameDA(name);
        ctx.status(200);
        ctx.json(dto);
    }

    @Override
    public void seedEUAllergens(Context ctx)
    {
        Long userId = ctx.attribute("userId");
        List<AllergenDTO> allergens = allergenService.seedEUAllergens(userId);
        ctx.status(201);
        ctx.json(allergens);
    }

    private Long requirePathId(Context ctx, String param)
    {
        return ctx.pathParamAsClass(param, Long.class)
            .check(i -> i > 0, "ID must be positive")
            .get();
    }
}
