# MiseOS User Stories

## Epic 1: User Management & Authentication

### US-01: User Registration
As a new user  
I want to register an account in the system  
So that I can access the application features

**Acceptance Criteria**
- Given I am not registered
- When I submit my email, password and name
- Then a user account is created
- And my default role is Customer
- And I can log in to the system

---

### US-02: Login with JWT
As a user (Head Chef, Sous Chef, Line Cook, Customer)  
I want to log in with email and password  
So that I can access protected features

**Acceptance Criteria**
- Given I have valid credentials
- When I send a login request
- Then I receive a JWT token
- And I can use the token in `Authorization: Bearer <token>`
- And requests with invalid credentials return 401

---

### US-03: Role Assignment
As a Head Chef  
I want to assign roles to users  
So that they can access features based on their role

**Acceptance Criteria**
- Given I am logged in as Head Chef
- When I assign a role to a user
- Then the user receives the assigned role
- And the user can only access features allowed for that role
- And unauthorized users cannot assign roles

---

### US-04: Station Assignment
As a Head Chef  
I want to assign kitchen staff to specific stations  
So that responsibilities in the kitchen are clearly defined

**Acceptance Criteria**
- Given I am logged in as Head Chef
- When I assign a user to a station
- Then the user is linked to that station
- And the user can access resources related to that station

---

### US-05: Own Profile Management
As a kitchen staff member  
I want to update my own profile, email and password  
So that my account stays current and secure

**Acceptance Criteria**
- Given I am logged in
- When I request `GET /users/me`
- Then I see my own profile without knowing my ID
- When I change my email, only my own email can be changed
- When I change my password, I must provide my current password
- And a line cook cannot modify another user's profile

---

### US-06: Protected Endpoints Require Auth
As a system  
I want to protect secured endpoints  
So that only authenticated users can access them

**Acceptance Criteria**
- Given an endpoint is not `ANYONE`
- When request has no token
- Then response is 401
- When request has invalid or expired token
- Then response is 401
- When token is valid but role is not allowed
- Then response is 403

---

## Epic 2: Dish Suggestions

### US-07: Submit Dish Suggestion
As a kitchen staff member  
I want to suggest a dish for an upcoming week's menu  
So that my ideas can be reviewed by management

**Acceptance Criteria**
- Given I am logged in as kitchen staff
- When I create a dish suggestion with name, description, station, allergens, target week and year
- Then the suggestion is created with status `PENDING`
- And Head Chef/Sous Chef can see it
- And submission is blocked if the deadline has passed

---

### US-08: Review Dish Suggestions
As a Head Chef or Sous Chef  
I want to review pending dish suggestions  
So that I can approve or reject them for the weekly menu

**Acceptance Criteria**
- Given I am logged in as Head Chef or Sous Chef
- When I view dish suggestions
- Then I see pending suggestions
- And I can filter by status, week, year, and station
- And I can approve a suggestion (creates dish in dish bank)
- And I can reject a suggestion with feedback
- And the submitting cook gets a notification

---

### US-09: Update Own Suggestion
As a kitchen staff member  
I want to update a suggestion I submitted  
So that I can fix mistakes or improve it

**Acceptance Criteria**
- Given I am creator of a `PENDING` suggestion
- When I edit name, description, or allergens
- Then suggestion is updated
- And only creator or management can update
- And approved/rejected suggestions cannot be updated

---

### US-10: Submission Deadline Enforcement
As a Head Chef  
I want the system to enforce a submission deadline  
So that there is enough review time before ordering

**Acceptance Criteria**
- Given current date is past deadline for target week
- When a staff member submits suggestion
- Then request is rejected
- And response includes deadline info

---

## Epic 3: Dish Bank

### US-11: View Available Dishes
As a Head Chef or Sous Chef  
I want to browse the dish bank  
So that I can plan weekly menus

**Acceptance Criteria**
- Given I am logged in as Head Chef or Sous Chef
- When I request dishes
- Then I see dishes with optional filters for station and active status
- And grouped views are available for menu planning

---

### US-12: Manage Dish Lifecycle
As a Head Chef or Sous Chef  
I want to activate, deactivate, or delete dishes  
So that only relevant dishes are used

**Acceptance Criteria**
- Given I am logged in as Head Chef or Sous Chef
- When I deactivate a dish
- Then it no longer appears in available flows
- When I reactivate dish
- Then it becomes available again
- And deletion is blocked if dish is used in menu history

---

### US-13: Search Dishes
As a kitchen staff member  
I want to search for dishes by name  
So that I can quickly find dishes

**Acceptance Criteria**
- Given I am logged in
- When I search with at least 2 characters
- Then partial matches are returned
- And inactive dishes are excluded

---

## Epic 4: Ingredient Requests

### US-14: Submit Ingredient Request
As a kitchen staff member  
I want to request ingredients for approved dishes or general stock  
So that kitchen has required items

**Acceptance Criteria**
- Given I am logged in as kitchen staff
- When I create request with name, quantity, unit, delivery date and type
- Then request status is `PENDING`
- And date must be valid
- And dish-specific request must match station rules
- And management gets notification

---

### US-15: Approve Ingredient Requests
As a Head Chef or Sous Chef  
I want to approve or reject ingredient requests  
So that purchasing stays controlled

**Acceptance Criteria**
- Given I am logged in as Head Chef or Sous Chef
- When I view requests
- Then I can filter by status/date/type/station
- And I can approve with optional quantity adjustment and note
- And requester gets notification
- And only pending requests can be approved/rejected

---

## Epic 5: Shopping Lists

### US-16: Generate Shopping List
As a Head Chef or Sous Chef  
I want to generate one shopping list from approved requests  
So that ordering is easier

**Acceptance Criteria**
- Given approved requests exist for a delivery date
- When I generate list
- Then ingredients are aggregated
- And list status is `DRAFT`
- And only one list per delivery date can exist
- And if AI normalization fails, list is still created

---

### US-17: Mark Items Ordered and Finalize
As a Head Chef or Sous Chef  
I want to mark items ordered and finalize the list  
So that team knows what is purchased

**Acceptance Criteria**
- Given list is `DRAFT`
- When I mark items ordered
- Then ordered state updates correctly
- When all items are ordered and I finalize
- Then list becomes `FINALIZED`
- And finalized lists cannot be edited

---

### US-18: Add Manual Item to Shopping List
As a Head Chef or Sous Chef  
I want to add manual items  
So that missing items can still be ordered

**Acceptance Criteria**
- Given list is `DRAFT`
- When I add item with name/quantity/unit/supplier
- Then item is added
- And item count increases
- And cannot add items to finalized list

---

## Epic 6: Weekly Menus

### US-19: Create and Plan Weekly Menu
As a Head Chef or Sous Chef  
I want to create weekly menu and assign dishes to slots  
So that kitchen has clear plan

**Acceptance Criteria**
- Given I am management
- When I create menu for week/year
- Then menu status is `DRAFT`
- And only one menu per week is allowed
- And I can add dish slots and placeholders

---

### US-20: Translate and Publish Weekly Menu
As a Head Chef or Sous Chef  
I want to translate and publish weekly menu  
So that guests can read it

**Acceptance Criteria**
- Given I am management
- When I run translation for full menu or slot
- Then dish names/descriptions are translated
- When I publish menu
- Then menu must not be empty
- And required translations must exist
- And published menu becomes visible publicly

---

### US-21: View Public Menu
As a guest  
I want to view published weekly menu without login  
So that I can see available food

**Acceptance Criteria**
- Given I visit menu endpoints without token
- When I request current or selected week
- Then only published menus are returned
- And dish info includes name, description, allergens, station
- And draft content is hidden

---

## Epic 7: Real-Time Notifications

### US-22: Admin Dashboard Badge Updates
As a Head Chef or Sous Chef  
I want live pending counters  
So that I can react quickly without refresh

**Acceptance Criteria**
- Given I am connected via WebSocket with valid token
- When new suggestions/requests arrive
- Then badge counts update in real time
- And snapshot endpoint can bootstrap initial count

---

### US-23: Staff Review Notifications
As a kitchen staff member  
I want direct notification when my item is reviewed  
So that I immediately know outcome

**Acceptance Criteria**
- Given I am connected via WebSocket
- When my suggestion/request is approved or rejected
- Then I receive direct notification with decision details
- And other staff do not receive my private review notification

---

## Epic 8: AI Menu Inspiration

### US-24: Get Weather-Informed Dish Suggestions
As a kitchen staff member  
I want AI dish inspiration based on weather and station  
So that I get ideas for new dishes

**Acceptance Criteria**
- Given I am logged in and assigned to station
- When I request inspiration
- Then I receive suggested dishes with Danish names and descriptions
- And suggestions reflect weather + station context
- And results are generative (not fixed every time)

---

### US-25: Stream Dish Suggestions
As a kitchen staff member  
I want suggestions streamed progressively  
So that I see output as it is generated

**Acceptance Criteria**
- Given I connect to stream endpoint while logged in
- When generation starts
- Then suggestions arrive one by one
- And stream ends with done marker
- And service failure returns proper error (not empty/broken stream)

---

## Epic 9: Nice-to-Have Features

### US-26: Offer Leftover Takeaway
As a Head Chef or Sous Chef  
I want to offer leftover portions as takeaway after 12:00  
So that food waste is reduced and customers can buy remaining meals

**Acceptance Criteria**
- Given current time is after 12:00
- When management enables takeaway for a dish
- Then a takeaway offer is visible to customers
- And offer includes total available portions
- And offer can be disabled when sold out

---

### US-27: Customer Places Takeaway Order
As a Customer  
I want to order takeaway from leftover offers  
So that I can buy available meals quickly

**Acceptance Criteria**
- Given takeaway offers are active
- When I place an order
- Then quantity is deducted from available portions
- And I cannot order more than available
- And if quantity reaches 0, offer is marked sold out

---

### US-28: Management Overview of Takeaway Sales
As a Head Chef or Sous Chef  
I want to see takeaway totals  
So that I know how many portions were offered and sold

**Acceptance Criteria**
- Given takeaway was offered during the day
- When I open takeaway summary
- Then I can see total offered portions
- And total sold portions
- And remaining/unsold portions

### US-29: Import Daily Guest Count from External Payment Service
As a Head Chef or Sous Chef  
I want the system to fetch total guest payments per day from an external payment service  
So that I can quickly see how many guests dined on previous days

**Acceptance Criteria**
- Given the payment service integration is configured
- When I request guest count for a date or date range
- Then the system returns total guest count per day
- And each day's value includes the source date and total transactions/guests
- And if the external service is unavailable, the system returns a clear error message

---

### US-30: View Guest Count Trend for Ordering Decisions
As a Head Chef or Sous Chef  
I want to view recent daily guest totals in one overview  
So that I can estimate ordering quantities better for upcoming days

**Acceptance Criteria**
- Given daily guest totals are available
- When I open the guest overview
- Then I can see totals for previous days (for example last 7 days)
- And I can compare day-by-day changes
- And the overview is simple to read (table or chart)
- And the data can be used as input when planning ingredient orders
