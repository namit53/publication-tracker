# University Research Publication Tracker

Spring Boot application for viewing and searching university research publications.

## Local development and tests

H2 is used automatically when no datasource environment variables are set:

```bash
./mvnw spring-boot:run
./mvnw test
```

## PostgreSQL configuration

Set the following environment variables before starting the application. Do not
commit database credentials to the repository.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/publication_tracker
export SPRING_DATASOURCE_USERNAME=your_postgres_user
export SPRING_DATASOURCE_PASSWORD=your_postgres_password
./mvnw spring-boot:run
```

On Windows PowerShell, use the equivalent commands:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/publication_tracker"
$env:SPRING_DATASOURCE_USERNAME = "your_postgres_user"
$env:SPRING_DATASOURCE_PASSWORD = "your_postgres_password"
.\mvnw.cmd spring-boot:run
```

The PostgreSQL database must already exist. JPA is configured with
`spring.jpa.hibernate.ddl-auto=update`, so the application's tables are created
or updated from the entity model at startup.

## OpenAlex import

With the application running, trigger an import of UPES publications with:

```bash
curl -X POST http://localhost:8080/publications/import
```

The importer uses institution `I5847235`, retrieves up to 100 works per page,
and follows additional pages automatically. It saves the title, authors,
publication year, DOI, journal, and citation count. Records already present by
DOI (or by title and year when no DOI is available) are skipped. Works without
a title are skipped because the existing publication structure requires a
meaningful title.

The endpoint returns JSON containing `imported` and `skipped` counts. If
OpenAlex is unavailable or returns an error, it responds with HTTP 500 and an
error message. The institution, base URL, and page size can be overridden with
`OPENALEX_INSTITUTION_ID`, `OPENALEX_BASE_URL`, and `OPENALEX_PER_PAGE`.
