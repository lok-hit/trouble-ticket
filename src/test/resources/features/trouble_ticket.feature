Feature: Trouble Ticket API

  Background:
    Given a valid JWT token for tenant "tenant-001"

  # ── Create ────────────────────────────────────────────────────────────────

  Scenario: Create a new trouble ticket
    When I send POST "/api/v1/troubleTicket" with body:
      """
      {
        "externalId": "EXT-001",
        "serviceId": 100001,
        "description": "No internet connection",
        "status": "new",
        "note": "Reported by API partner"
      }
      """
    Then the response status is 201
    And the response has field "id"
    And the response field "externalId" equals "EXT-001"
    And the response field "status" equals "acknowledged"
    And the response field "notes[0].text" equals "Reported by API partner"

  Scenario: Creating a ticket with the same externalId returns existing ticket (idempotency)
    Given a trouble ticket exists with externalId "EXT-DUP" for tenant "tenant-001"
    When I send POST "/api/v1/troubleTicket" with body:
      """
      {
        "externalId": "EXT-DUP",
        "serviceId": 100001,
        "description": "Duplicate",
        "status": "new",
        "note": "Second attempt"
      }
      """
    Then the response status is 200
    And the response field "externalId" equals "EXT-DUP"

  Scenario: Creating a ticket with invalid status returns 400
    When I send POST "/api/v1/troubleTicket" with body:
      """
      {
        "externalId": "EXT-BAD",
        "serviceId": 100001,
        "description": "Bad status",
        "status": "inProgress",
        "note": "Should fail"
      }
      """
    Then the response status is 400
    And the response field "code" equals "VALIDATION_ERROR"

  Scenario: Creating a ticket without auth returns 401
    Given no authentication token
    When I send POST "/api/v1/troubleTicket" with body:
      """
      {
        "externalId": "EXT-NOAUTH",
        "serviceId": 100001,
        "description": "No auth",
        "status": "new",
        "note": "Note"
      }
      """
    Then the response status is 401

  # ── List ──────────────────────────────────────────────────────────────────

  Scenario: List trouble tickets returns only tenant's tickets
    Given a trouble ticket exists with externalId "EXT-L1" for tenant "tenant-001"
    Given a trouble ticket exists with externalId "EXT-L2" for tenant "tenant-001"
    Given a trouble ticket exists with externalId "EXT-OTHER" for tenant "tenant-999"
    When I send GET "/api/v1/troubleTicket"
    Then the response status is 200
    And the response is a list with at least 2 items
    And the response list does not contain externalId "EXT-OTHER"

  # ── Get by id ─────────────────────────────────────────────────────────────

  Scenario: Get existing ticket by id
    Given a trouble ticket exists with externalId "EXT-GET" for tenant "tenant-001"
    When I send GET "/api/v1/troubleTicket/{lastCreatedId}"
    Then the response status is 200
    And the response field "externalId" equals "EXT-GET"
    And the response has field "notes"

  Scenario: Get ticket belonging to another tenant returns 404
    Given a trouble ticket exists with externalId "EXT-OWN" for tenant "tenant-999"
    When I send GET "/api/v1/troubleTicket/{lastCreatedId}"
    Then the response status is 404

  # ── Close ─────────────────────────────────────────────────────────────────

  Scenario: Close an existing ticket
    Given a trouble ticket exists with externalId "EXT-CLOSE" for tenant "tenant-001"
    When I send PATCH "/api/v1/troubleTicket/{lastCreatedId}" with body:
      """
      { "status": "closed" }
      """
    Then the response status is 200
    And the response field "status" equals "closed"

  Scenario: Patch with status other than closed returns 400
    Given a trouble ticket exists with externalId "EXT-BAD-PATCH" for tenant "tenant-001"
    When I send PATCH "/api/v1/troubleTicket/{lastCreatedId}" with body:
      """
      { "status": "resolved" }
      """
    Then the response status is 400

  # ── Add Note ──────────────────────────────────────────────────────────────

  Scenario: Add a note to existing ticket
    Given a trouble ticket exists with externalId "EXT-NOTE" for tenant "tenant-001"
    When I send POST "/api/v1/troubleTicket/{lastCreatedId}/note" with body:
      """
      { "text": "Follow-up note from partner" }
      """
    Then the response status is 201
    And the response field "text" equals "Follow-up note from partner"
    And the response has field "id"
    And the response has field "date"

  Scenario: Add note to ticket of another tenant returns 404
    Given a trouble ticket exists with externalId "EXT-NOTE-DENY" for tenant "tenant-999"
    When I send POST "/api/v1/troubleTicket/{lastCreatedId}/note" with body:
      """
      { "text": "Should be denied" }
      """
    Then the response status is 404
