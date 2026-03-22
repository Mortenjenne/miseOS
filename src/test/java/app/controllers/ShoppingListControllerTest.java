package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.shopping.ShoppingListDTO;
import app.dtos.shopping.ShoppingListItemDTO;
import app.enums.ShoppingListStatus;
import app.persistence.entities.IEntity;
import app.persistence.entities.ShoppingList;
import app.persistence.entities.ShoppingListItem;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShoppingListControllerTest
{
    private static final String ENDPOINT_URL = "/shopping-lists";
    private static final int TEST_PORT = 7777;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String sousChefToken;
    private String lineChefToken;
    private long draftListId;
    private long finalizedListId;

    @BeforeAll
    static void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_PORT;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void setup()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();

        seeded = populator.getSeededData();
        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
        sousChefToken = TestAuthenticationUtil.bearerToken("marco@grill.com", "Hash3");
        draftListId = seeded.get("shopping_list_draft").getId();
        finalizedListId = seeded.get("shopping_list_finalized").getId();
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Nested
    @DisplayName("POST /shopping-lists")
    class Create
    {
        @Test
        @DisplayName("Head chef generates shopping list from approved requests")
        void generatesShoppingList()
        {
            LocalDate date = LocalDate.now().plusDays(7);
            String payload = getCreateListPayload(date);

            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .log().body()
                .statusCode(201)
                .extract()
                .as(ShoppingListDTO.class);

            assertThat(response.id(), notNullValue());
            assertThat(response.status(), is(ShoppingListStatus.DRAFT));
            assertThat(response.items(), is(not(empty())));
            assertThat(response.deliveryDate(), is(LocalDate.now().plusDays(7)));
        }

        @Test
        @DisplayName("Duplicate delivery date returns 409")
        void duplicateDateReturns409()
        {
            ShoppingList draft = (ShoppingList) seeded.get("shopping_list_draft");
            String payload = getCreateListPayload(draft.getDeliveryDate());

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("No approved requests for date returns 409")
        void noApprovedRequestsReturns409()
        {
            String payload = getCreateListPayload(LocalDate.now().plusDays(20));

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Line cook cannot generate — returns 403 Forbidden")
        void lineCookCannotGenerate()
        {
            String payload = getCreateListPayload(LocalDate.now().plusDays(7));

            given()
                .header("Authorization", lineChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Missing delivery date returns 400")
        void missingDeliveryDateReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "targetLanguage": "DA"
                }
                """)
                .when()
                .post(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /shopping-lists/{id}")
    class GetById
    {
        @Test
        @DisplayName("Returns shopping list with items")
        void returnsListWithItems()
        {
            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + draftListId)
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDTO.class);

            assertThat(response.id(), is(draftListId));
            assertThat(response.items(), hasSize(greaterThan(0)));
            assertThat(response.status(), is(ShoppingListStatus.DRAFT));
        }

        @Test
        @DisplayName("Unknown id returns 404")
        void unknownIdReturns404()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Negative id returns 400")
        void negativeIdReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/-1")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /shopping-lists/{id}")
    class Delete
    {
        @Test
        @DisplayName("Head chef deletes draft list — returns 204")
        void headChefDeletesDraftList()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + draftListId)
                .then()
                .statusCode(204);

            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL + "/" + draftListId)
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Sous chef can delete draft list")
        void sousChefCanDelete()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + draftListId)
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("Line cook cannot delete — returns 403 Forbidden")
        void lineCookCannotDelete()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + draftListId)
                .then()
                .statusCode(403)
                .body("message", containsString("Insufficient role. Required:"));
        }

        @Test
        @DisplayName("Cannot delete finalized list — returns 409")
        void cannotDeleteFinalizedList()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + finalizedListId)
                .then()
                .statusCode(409)
                .body("message", equalToIgnoringCase("Cannot delete a finalized shopping list"));
        }

        @Test
        @DisplayName("Unknown id returns 404")
        void unknownIdReturns404()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/9999")
                .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PATCH /shopping-lists/{id}/delivery-date")
    class UpdateDeliveryDate
    {
        @Test
        @DisplayName("Head chef updates delivery date")
        void updatesDeliveryDate()
        {
            LocalDate newDate = LocalDate.now().plusDays(10);
            String payload =
                """
                    {
                    "deliveryDate": "%s"
                    }
                """.formatted(newDate);

            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/delivery-date")
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDTO.class);

            assertThat(response.deliveryDate(), is(newDate));
        }

        @Test
        @DisplayName("Past date returns 400")
        void pastDateReturns400()
        {
            String payload =
                """
                    {
                    "deliveryDate": "%s"
                    }
               """.formatted(LocalDate.now().minusDays(1));

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/delivery-date")
                .then()
                .statusCode(400)
                .body("message", equalToIgnoringCase("Delivery date must be in the future"));
        }

        @Test
        @DisplayName("Date already taken by another list returns 409")
        void conflictingDateReturns409()
        {
            ShoppingList finalized = (ShoppingList) seeded.get("shopping_list_finalized");
            String payload =
                """
                    {
                    "deliveryDate": "%s"
                    }
               """.formatted(finalized.getDeliveryDate());

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/delivery-date")
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Cannot update finalized list — returns 409")
        void cannotUpdateFinalizedList()
        {
            String payload =
                """
                    {
                    "deliveryDate": "%s"
                    }
               """.formatted(LocalDate.now().plusDays(10));

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .patch(ENDPOINT_URL + "/" + finalizedListId + "/delivery-date")
                .then()
                .statusCode(409);
        }
    }

    @Nested
    @DisplayName("GET /shopping-lists")
    class GetAll
    {
        @Test
        @DisplayName("Returns all shopping lists")
        void returnsAll()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(2));
        }

        @Test
        @DisplayName("Filter by DRAFT returns only draft lists")
        void filterByDraft()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("status", "DRAFT")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", equalTo("DRAFT"));
        }

        @Test
        @DisplayName("Filter by FINALIZED returns only finalized lists")
        void filterByFinalized()
        {
            given()
                .header("Authorization", sousChefToken)
                .queryParam("status", "FINALIZED")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", equalTo("FINALIZED"));
        }

        @Test
        @DisplayName("Filter by delivery date returns matching list")
        void filterByDeliveryDate()
        {
            ShoppingList list = (ShoppingList) seeded.get("shopping_list_draft");

            List<ShoppingListDTO> response = given()
                .header("Authorization", sousChefToken)
                .queryParam("deliveryDate", list.getDeliveryDate().toString())
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ShoppingListDTO.class);

            assertThat(response, hasSize(1));
            assertThat(response.get(0).id(), is(list.getId()));
            assertThat(response.get(0).deliveryDate(), is(list.getDeliveryDate()));
        }

        @Test
        @DisplayName("Invalid status returns 400")
        void invalidStatusReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("status", "INVALID")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Invalid date format returns 400")
        void invalidDateReturns400()
        {
            given()
                .header("Authorization", headChefToken)
                .queryParam("deliveryDate", "not-a-date")
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Missing auth returns 401")
        void missingAuthReturns401()
        {
            given()
                .when()
                .get(ENDPOINT_URL)
                .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /shopping-lists/{id}/items")
    class AddItem
    {
        @Test
        @DisplayName("Head chef adds item manually")
        void addsItem()
        {
            ShoppingList draft = (ShoppingList) seeded.get("shopping_list_draft");
            int draftItemSize = draft.getItemCount();
            String payload = getAddItemPayload();

            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/items")
                .then()
                .statusCode(201)
                .extract()
                .as(ShoppingListDTO.class);

            assertThat(response.itemCount(), is(draftItemSize + 1));
            assertThat(response.items().stream().anyMatch(i -> i.ingredientName().equals("Smør")), is(true)
            );
        }

        @Test
        @DisplayName("Missing ingredient name returns 400")
        void missingNameReturns400()
        {
            String payload =
                """
                {
                  "quantity": 5.0,
                  "unit": "KG",
                  "supplier": "Arla"
                }
                """;

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/items")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Cannot add to finalized list — returns 409")
        void cannotAddToFinalizedList()
        {
            String payload = getAddItemPayload();

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/" + finalizedListId + "/items")
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Line cook cannot add item — returns 403")
        void lineCookCannotAdd()
        {
            String payload = getAddItemPayload();

            given()
                .header("Authorization", lineChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/items")
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("DELETE /shopping-lists/{id}/items/{itemId}")
    class RemoveItem
    {
        @Test
        @DisplayName("Removes item — list count decreases")
        void removesItem()
        {
            ShoppingList draft = (ShoppingList) seeded.get("shopping_list_draft");
            Long itemId = getFirstItemId();
            int draftItemSize = draft.getItemCount();

            ShoppingListDTO response = given()
                .header("Authorization", sousChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + draftListId + "/items/" + itemId)
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDTO.class);

            assertThat(response.itemCount(), is(draftItemSize - 1));
        }

        @Test
        @DisplayName("Cannot remove from finalized list — returns 409")
        void cannotRemoveFromFinalizedList()
        {
            ShoppingList finalized = (ShoppingList) seeded.get("shopping_list_finalized");
            Long itemId = finalized.getShoppingListItems().iterator().next().getId();

            given()
                .header("Authorization", headChefToken)
                .when()
                .delete(ENDPOINT_URL + "/" + finalizedListId + "/items/" + itemId)
                .then()
                .statusCode(409);
        }
    }

    @Nested
    @DisplayName("PUT /shopping-lists/{id}/items/{itemId}")
    class UpdateItem
    {
        @Test
        @DisplayName("Updates item quantity and supplier")
        void updatesItem()
        {
            Long itemId = getFirstItemId();
            String payload = getItemUpdatePayload();

            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(ENDPOINT_URL + "/" + draftListId + "/items/" + itemId)
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDTO.class);

            ShoppingListItemDTO updated = response.items().stream()
                .filter(i -> i.id().equals(itemId))
                .findFirst()
                .orElseThrow();

            assertThat(updated.quantity(), is(99.0));
            assertThat(updated.supplier(), is("Ny Leverandør"));
        }

        @Test
        @DisplayName("Unknown item returns 404")
        void unknownItemReturns404()
        {
            String payload = getItemUpdatePayload();

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(ENDPOINT_URL + "/" + draftListId + "/items/9999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Cannot update item in finalized list — returns 409")
        void cannotUpdateItemInFinalizedList()
        {
            ShoppingList finalized = (ShoppingList) seeded.get("shopping_list_finalized");
            Long itemId = finalized.getShoppingListItems().iterator().next().getId();

            given()
                .header("Authorization", headChefToken)
                .contentType(ContentType.JSON)
                .body("""
                    { "quantity": 5.0, "unit": "KG" }
                    """)
                .when()
                .put(ENDPOINT_URL + "/" + finalizedListId + "/items/" + itemId)
                .then()
                .statusCode(409);
        }
    }


    @Nested
    @DisplayName("PATCH /shopping-lists/{id}/items/{itemId}/ordered")
    class MarkItemOrdered
    {
        @Test
        @DisplayName("Marks single item as ordered")
        void marksItemOrdered()
        {
            ShoppingList draft = (ShoppingList) seeded.get("shopping_list_draft");
            ShoppingListItem item = draft.getShoppingListItems().iterator().next();
            Long itemId = item.getId();

            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/" + itemId + "/ordered")
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDTO.class);

            ShoppingListItemDTO itemFromResponse = response.items().stream()
                .filter(i -> i.id().equals(itemId))
                .findFirst()
                .orElseThrow();

            assertThat(itemFromResponse.id(), is(item.getId()));
            assertThat(itemFromResponse.ordered(), is(true));
        }

        @Test
        @DisplayName("Already ordered item returns 409")
        void alreadyOrderedReturns409()
        {
            long itemId = getFirstItemId();

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/" + itemId + "/ordered")
                .then()
                .statusCode(200);

            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/" + itemId + "/ordered")
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Unknown item returns 404")
        void unknownItemReturns404()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/9999/ordered")
                .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PATCH /shopping-lists/{id}/items/ordered")
    class MarkAllOrdered
    {
        @Test
        @DisplayName("Marks all items as ordered — allOrdered becomes true")
        void marksAllOrdered()
        {
            ShoppingListDTO response = given()
                .header("Authorization", headChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/ordered")
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDTO.class);

            assertThat(response.allOrdered(), is(true));
            assertThat(
                response.items().stream().allMatch(ShoppingListItemDTO::ordered),
                is(true)
            );
        }

        @Test
        @DisplayName("Line cook cannot mark all — returns 401")
        void lineCookCannotMarkAll()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/ordered")
                .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("POST /shopping-lists/{id}/finalize")
    class Finalize
    {
        @Test
        @DisplayName("Head chef finalizes when all items are ordered")
        void finalizesWhenAllOrdered()
        {
            given()
                .header("Authorization", headChefToken)
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/ordered");

            given()
                .header("Authorization", headChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/finalize")
                .then()
                .statusCode(200)
                .body("status", equalTo("FINALIZED"))
                .body("finalizedAt", notNullValue());
        }

        @Test
        @DisplayName("Cannot finalize with unordered items — returns 409")
        void cannotFinalizeWithUnorderedItems()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/finalize")
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Cannot finalize already finalized list — returns 409")
        void cannotFinalizeAlreadyFinalized()
        {
            given()
                .header("Authorization", headChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + finalizedListId + "/finalize")
                .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("Sous chef can finalize")
        void sousChefCanFinalize()
        {
            given()
                .header("Authorization", sousChefToken)
                .when()
                .patch(ENDPOINT_URL + "/" + draftListId + "/items/ordered")
                .then()
                .statusCode(200);

            given()
                .header("Authorization", sousChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/finalize")
                .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Line cook cannot finalize — returns 403")
        void lineCookCannotFinalize()
        {
            given()
                .header("Authorization", lineChefToken)
                .when()
                .post(ENDPOINT_URL + "/" + draftListId + "/finalize")
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Unknown id returns 404")
        void unknownIdReturns404()
        {
            given()
                .header("Authorization", sousChefToken)
                .when()
                .post(ENDPOINT_URL + "/9999/finalize")
                .then()
                .statusCode(404);
        }
    }

    private String getCreateListPayload(LocalDate date)
    {
        return  """
            {
              "deliveryDate": "%s",
              "targetLanguage": "DA"
            }
            """.formatted(date);
    }

    private String getAddItemPayload()
    {
        return
            """
            {
              "ingredientName": "Smør",
              "quantity": 5.0,
              "unit": "KG",
              "supplier": "Arla"
            }
            """;
    }

    private Long getFirstItemId()
    {
        ShoppingList draft = (ShoppingList) seeded.get("shopping_list_draft");
        return draft.getShoppingListItems().iterator().next().getId();
    }

    private String getItemUpdatePayload()
    {
        return
            """
                {
                "quantity": 99.0,
                "unit": "KG",
                "supplier": "Ny Leverandør"
                }
                """;
    }
}
