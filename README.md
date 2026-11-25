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
