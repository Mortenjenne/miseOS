package app.utils;

import app.persistence.entities.Allergen;

import java.util.List;

public class EUAllergens
{
    public static List<Allergen> getAll()
    {
        return List.of(
        new Allergen(
            "Gluten",
            "Gluten",
            "Korn der indeholder gluten: hvede, rug, byg, havre, spelt, kamut",
            "Cereals containing gluten: wheat, rye, barley, oats, spelt, kamut",
            1
        ),
        new Allergen(
            "Krebsdyr",
            "Crustaceans",
            "Krebsdyr og produkter heraf: rejer, krabbe, hummer, krebs",
            "Crustaceans and products thereof: shrimp, crab, lobster, crayfish",
            2
        ),
        new Allergen(
            "Æg",
            "Eggs",
            "Æg og produkter heraf",
            "Eggs and products thereof",
            3
        ),
        new Allergen(
            "Fisk",
            "Fish",
            "Fisk og produkter heraf",
            "Fish and products thereof",
            4
        ),
        new Allergen(
            "Jordnødder",
            "Peanuts",
            "Jordnødder og produkter heraf",
            "Peanuts and products thereof",
            5
        ),
        new Allergen(
            "Soja",
            "Soybeans",
            "Sojabønner og produkter heraf",
            "Soybeans and products thereof",
            6
        ),
        new Allergen(
            "Mælk",
            "Milk",
            "Mælk og produkter heraf (herunder laktose)",
            "Milk and products thereof (including lactose)",
            7
        ),
        new Allergen(
            "Nødder",
            "Nuts",
            "Træfrugt: mandler, hasselnødder, valnødder, cashewnødder, pekannødder, pistacienødder, macadamianødder",
            "Tree nuts: almonds, hazelnuts, walnuts, cashews, pecans, pistachios, macadamia",
            8
        ),
        new Allergen(
            "Selleri",
            "Celery",
            "Selleri og produkter heraf",
            "Celery and products thereof",
            9
        ),
        new Allergen(
            "Sennep",
            "Mustard",
            "Sennep og produkter heraf",
            "Mustard and products thereof",
            10
        ),
        new Allergen(
            "Sesamfrø",
            "Sesame",
            "Sesamfrø og produkter heraf",
            "Sesame seeds and products thereof",
            11
        ),
        new Allergen(
            "Svovldioxid og sulfitter",
            "Sulphites",
            "Svovldioxid og sulfitter i koncentrationer på mere end 10mg/kg",
            "Sulphur dioxide and sulphites at concentrations of more than 10mg/kg",
            12
        ),
        new Allergen(
            "Lupin",
            "Lupin",
            "Lupin og produkter heraf",
            "Lupin and products thereof",
            13
        ),
        new Allergen(
            "Bløddyr",
            "Molluscs",
            "Bløddyr og produkter heraf: muslinger, østers, blæksprutter, snegle",
            "Molluscs and products thereof: clams, mussels, oysters, scallops, squid, octopus",
            14
        )
        );
    }
}
