# Use Testcontainers for Integration Tests

## Context and Problem Statement

Developers need to run integration tests on any device without manually setting up a database.

## Considered Options

* Dockerfile or Docker-Compose for PostgreSQL for Tests
* Embedded DB (H2)
* Testcontainer
* Test DB Instance

## Decision Outcome

**Chosen option**: Testcontainer

**Reasons**:

1. Embedded DBs are fast but may behave differently than the production database and can have SQL incompatibilities.

2. Maintaining a separate test DB instance is ideal for fidelity but too costly in terms of setup and maintenance.

3. Dockerfile/Compose solutions require developers to manually start and stop containers, adding overhead.

4. Testcontainers automatically start a real database in a Docker container for the duration of the test and shut it down afterward, ensuring reproducible, environment-independent integration tests.

## Consequences

* **Advantages**
  * Integration tests run against a real database environment, minimizing SQL and behavior discrepancies with production.
  * Tests are reproducible across all developer machines and CI environments.
  * Testcontainers automatically clean up after tests, keeping environments isolated and clean.
  * Reduces the need for managing separate test database servers or environment variables.

* **Limitations**
  * It might take more time to run the tests to setup the container.
  * Docker is necessary to run the integration tests.

* **Considerations**
  * Developers might have to clean up the data if not rolling back for a transaction or if sharing the container throughout a test class.