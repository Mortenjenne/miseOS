# MiseOS User Stories

## Epic 1: User Management & Authentication

### US-01: User Registration
As a Head Chef  
I want to register Line Cooks in the system  
So that they can access their station and submit menu suggestions

**Acceptance Criteria:**
- Given I am logged in as Head Chef
- When I create a new Line Cook account
- Then I can assign them to a specific station (Salad, Cold/Starter, Hot, Bakery)
- And they receive login credentials
- And they can only access features for their role

### US-02: User Login
As a Line Cook or Head Chef  
I want to log in to the system  
So that I can access my role-specific features

**Acceptance Criteria:**
- Given I have valid credentials
- When I enter my email and password
- Then I receive a JWT token
- And I am redirected to my role-appropriate dashboard
- And my session persists until token expiration

---

## Epic 2: Menu Planning & Suggestions

### US-03: Suggest Dish for Weekly Menu
As a Line Cook  
I want to suggest dishes for next week's menu  
So that my creative ideas can be considered by the Head Chef

**Acceptance Criteria:**
- Given I am logged in as a Line Cook
- When I create a new dish suggestion
- Then I must provide: dish name, description, station, allergen information
- And the suggestion is marked as "Pending" status
- And the Head Chef can see it in their review queue

### US-04: Review and Approve Dish Suggestions
As a Head Chef  
I want to review dish suggestions from Line cooks  
So that I can approve, reject, or modify them for the weekly menu

**Acceptance Criteria:**
- Given I am logged in as Head Chef
- When I view pending dish suggestions
- Then I can see suggestions grouped by station
- And I can approve a suggestion (adds it to the weekly menu)
- Or reject a suggestion with optional feedback
- Or edit the suggestion before approving
- And the Line Cook is notified of the decision

### US-05: View Weekly Menu
As a Line Cook  
I want to view the current and upcoming weekly menus  
So that I know what dishes I need to prepare

**Acceptance Criteria:**
- Given I am logged in as a Line Cook
- When I navigate to the weekly menu view
- Then I see all approved dishes for my station
- And I can filter by week (current, next, previous)
- And each dish shows: name, description, allergens

---

## Epic 3: Ingredient Management

### US-06: Submit Ingredient Request
As a Line Cook  
I want to request ingredients for my approved dishes  
So that I have everything needed to execute the menu

**Acceptance Criteria:**
- Given I have an approved dish on the weekly menu
- When I create an ingredient request
- Then I must specify: ingredient name, quantity, unit, preferred supplier (optional)
- And the request is linked to the specific dish or a general request
- And the request status is set to "Pending"
- And I can view all my ingredient requests and their status

### US-07: Approve Ingredient Requests
As a Head Chef  
I want to review and approve ingredient requests  
So that I can control purchasing and ensure budget is met

**Acceptance Criteria:**
- Given I am logged in as Head Chef
- When I view pending ingredient requests
- Then I can see requests grouped by Line Cook and dish
- And I can approve or reject each request
- And I can modify quantity or add notes
- And approved requests are automatically added to the shopping list

### US-08: Generate Shopping List
As a Head Chef  
I want to generate an aggregated shopping list from approved ingredient requests  
So that I can place consolidated orders with suppliers that arrive exactly when we need them

**Acceptance Criteria:**
- Given multiple ingredient requests have been approved
- When I generate a shopping list for a specific Delivery Date (e.g., Monday, Feb 9th)
- Then the system pulls all approved requests regardless of which day they are served
- And identical ingredients are consolidated (summed quantities)
- And the list is grouped by category or supplier
- And I can mark items as "Ordered" when complete

---

## Epic 4: Public Menu Access & Translation

### US-09: View Menu as Guest
As a Guest (unauthenticated user)  
I want to view the current week's menu  
So that I know what food is available

**Acceptance Criteria:**
- Given I visit the public menu page
- When I view the menu
- Then I see all approved dishes for the current week
- And each dish shows: name, description, allergens, station
- And I do not need to log in
- And I cannot see pending or rejected suggestions

### US-010: Toggle Menu Language
As a Guest  
I want to switch between Danish and English  
So that I can read the menu in my preferred language

**Acceptance Criteria:**
- Given I am viewing the public menu
- When I click the language toggle (DA/EN)
- Then all dish names and descriptions are translated
- And allergen labels are translated
- And the selected language persists during my session

### US-11: View Allergen Information
As a Guest  
I want to clearly see allergen information for each dish  
So that I can avoid foods that affect my health

**Acceptance Criteria:**
- Given I am viewing the menu
- When I look at a dish
- Then allergens are prominently displayed with icons or numbers
- And common allergens (gluten, dairy, nuts, etc.) are clearly marked


### US-12: Translate Menu 
As a Head Chef 
I want to provide translations for dish names and descriptions
So that international guests can understand the menu.

**Acceptance Criteria:**

- Given a dish is approved.
- When I edit the dish details.
- Then I can input both a Danish and an English version of the text.
- And the Public Menu (US-10) will toggle between these values.

---

## Epic 5: Menu History & Analytics (Nice-to-Have)

### US-13: View Menu History
As a Head Chef  
I want to view past weekly menus  
So that I can reference successful dishes and avoid repetition

**Acceptance Criteria:**
- Given I am logged in as Head Chef
- When I navigate to menu history
- Then I can filter by date range, station, or dish name
- And I can see which Line Cook suggested each dish
- And I can clone past dishes to new menu suggestions

### US-14: Track Dish Popularity
As a Head Chef  
I want to see which dishes appear most frequently  
So that I can identify customer favorites

**Acceptance Criteria:**
- Given historical menu data exists
- When I view dish analytics
- Then I see a list of most-used dishes
- And I can see approval rate per Line Cook
- And I can see seasonal patterns

---

## Epic 6: Weather Integration (The Cook's Assistant)

### US-15: Weather-Informed Planning
As a Line Cook 
I want to see the weather forecast for the upcoming week while making suggestions
So that I can suggest dishes that match the climate

**Acceptance Criteria:**

- Given the system is connected to a Weather API (e.g., OpenWeatherMap).
- When I am on the "Submit Suggestion" page.
- Then I see the 5-7 day forecast for the week I am planning.
- And the system displays an "Inspiration Tip" (e.g., "Forecast is 24°C: Consider cold soups or salads" or "Forecast is 2°C: Good time for stews").

---

