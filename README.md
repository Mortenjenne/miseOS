# miseOS
**Kitchen Management System**

MiseOS is a backend platform for professional kitchens — digitizing the workflow from a line cook's first dish proposal to a published guest menu, ingredient orders, and shopping lists.

The name references *mise en place* — the culinary principle of having everything in its place before service begins.

---

## Problem Statement

Menu planning in many professional kitchens relies on fragmented, analog processes:

- Dish proposals from each station (Hot, Cold, Salad, Bakery) arrive verbally or on loose notes with no overview
- Head chefs manually type handwritten notes into Word templates to produce printable menus
- Printed menus cannot be updated quickly and require manual translation for international guests
- Ingredient ordering is disconnected from the menu — no direct link between what is on the menu and what needs to be ordered

MiseOS solves this by providing one digital workspace where data is entered once and flows through the entire process.

---

## Core Features

**Dish Suggestion Flow** — Line cooks submit weekly proposals for their station. The head chef reviews, approves, or rejects them from a single dashboard.

**Weekly Menu Builder** — Approved dishes are placed into a Monday–Friday grid by station. The head chef publishes the final menu when all slots are filled and translated.

**Automated Translation** — Batch translation via DeepL (Danish → English) so international guests see their language without manual effort from kitchen staff.

**Ingredient Request System** — Line cooks link ingredient orders directly to approved dishes. The head chef approves pending requests before they flow through to the shopping list.

**AI-Powered Shopping List** — Approved ingredient requests are normalized using Gemini AI — merging multilingual and variant names (e.g. "onions", "løg", "onio" → "Løg") and aggregating quantities before generating the final order list.

**Menu Inspiration (SSE)** — Streams AI-generated dish suggestions based on current weather forecast and station equipment, delivered in real-time to the dashboard.

**Public Guest View** — Displays the current week's published menu with allergen information and language toggle (Danish/English).

---

## Technology

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build | Maven |
| Framework | Javalin 7 |
| Persistence | Hibernate 7 / JPA, PostgreSQL |
| Auth | JWT (in progress) |
| AI | Google Gemini (suggestions + normalization) |
| Translation | DeepL API |
| Weather | OpenMeteo API |
| Testing | JUnit 5, REST-assured, Testcontainers |

---

## API Documentation

Full endpoint reference will be available at:
**[https://mortenjenne.github.io/portfolio/projects/miseos/](https://mortenjenne.github.io/portfolio/projects/miseos/)**

Covers all REST endpoints across:
`/dishes` · `/dish-suggestions` · `/weekly-menus` · `/ingredient-requests` · `/shopping-lists` · `/users` · `/stations` · `/allergens` · `/menu-inspirations`

---

## Development Log

Weekly reflections on architecture decisions, challenges, and what was learned.
Follow along at **[https://mortenjenne.github.io/portfolio/posts/](https://mortenjenne.github.io/portfolio/posts/)**

| Week | Topic |
|---|---|
| 1 | Project setup and domain modeling |
| 2 | Entity design and persistence layer |
| 3 | Service layer and business rules |
| 4 | AI integration — Gemini and DeepL |
| 5 | DAO patterns and query filtering |
| 6 | Controllers, routes, and exception handling |
| 7 | REST-assured integration tests and WebSocket notifications *(in progress)* |

---

## Status

**In active development** — Core API complete and tested. JWT authentication in progress. WebSocket notifications planned.
