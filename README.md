# Identity Reconciliation Service

This is a Spring Boot application that provides an identity reconciliation service. The service is designed to identify and consolidate customer contact information.

**Deployed Application Link**: [https://identityservice-uhbn.onrender.com](https://identityservice-uhbn.onrender.com)

## Core Functionality

The service exposes a single API endpoint, `/identify`, which receives customer information (email and/or phone number). It processes this information to:
1.  **Create new contacts**: If no existing contact matches the provided email or phone number, a new primary contact is created.
2.  **Consolidate contacts**: If the provided information matches one or more existing contacts, the service links them together. It designates the oldest contact as the `PRIMARY` contact and marks others as `SECONDARY`.
3.  **Return a consolidated view**: The API always returns a single, consolidated profile for the customer, including:
    *   The ID of the primary contact.
    *   A list of all unique emails associated with the customer.
    *   A list of all unique phone numbers associated with the customer.
    *   A list of all secondary contact IDs.

## API Endpoint

### `POST /identify`

This endpoint is used to identify a user and consolidate their contact information.

#### Request Body

The request body should be a JSON object containing either an `email`, a `phoneNumber`, or both.

```json
{
  "email": "mcfly@hillvalley.edu",
  "phoneNumber": "123456"
}
```

| Field         | Type   | Description                            |
|---------------|--------|----------------------------------------|
| `email`       | String | The email address of the contact.      |
| `phoneNumber` | String | The phone number of the contact.       |


#### Response Body

The response will be a JSON object containing the consolidated contact information.

```json
{
    "contact": {
        "primaryContactId": 1,
        "emails": [
            "lorraine@hillvalley.edu",
            "mcfly@hillvalley.edu"
        ],
        "phoneNumbers": [
            "987654",
            "123456"
        ],
        "secondaryContactIds": [
            23
        ]
    }
}
```

## Data Model

The core entity is `Contact`, which has the following main attributes:
- `id`: Primary key.
- `phoneNumber`: The contact's phone number.
- `email`: The contact's email address.
- `linkedId`: If the contact is `SECONDARY`, this links to the `id` of the `PRIMARY` contact.
- `linkPrecedence`: An enum (`PRIMARY` or `SECONDARY`) indicating the contact's role.
- `createdAt`: Timestamp of creation, used to determine the oldest contact.

## Technology Stack

- **Framework**: Spring Boot
- **Language**: Java 21
- **Database**: PostgreSQL
- **Build Tool**: Maven

## How to Run Locally

### Prerequisites

- Java 21 or later
- Maven
- PostgreSQL Database

### Configuration

1.  The application connects to a PostgreSQL database using credentials from environment variables. Set the following environment variables:
    - `DB_URL`: The JDBC URL of your PostgreSQL database (e.g., `jdbc:postgresql://localhost:5432/identity_db`)
    - `DB_USERNAME`: Your database username.
    - `DB_PASSWORD`: Your database password.

2.  The application uses `spring.jpa.hibernate.ddl-auto=update`, so the `contact` table will be created or updated automatically on startup.

### Running the Application

1.  Clone the repository.
2.  Navigate to the project root directory.
3.  Run the application using the Maven wrapper:

    **On Windows:**
    ```bash
    mvnw spring-boot:run
    ```

    **On macOS/Linux:**
    ```bash
    ./mvnw spring-boot:run
    ```

The service will start on `http://localhost:8080`.
