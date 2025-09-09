# Capstone Backend (BE)

A Spring Boot REST API application for managing users and tasks with MySQL database and JPA configuration.

## Features

- **User Management**: Create, read, update, and delete users
- **Task Management**: Create, read, update, and delete tasks
- **Task Assignment**: Assign tasks to users
- **Status Tracking**: Track task status (TODO, IN_PROGRESS, DONE)
- **Priority Management**: Set task priorities (LOW, MEDIUM, HIGH)
- **MySQL Database**: Production-ready database with JPA/Hibernate
- **REST API**: RESTful endpoints with proper HTTP status codes
- **Validation**: Input validation with appropriate error messages

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 8.0**
- **Maven**
- **H2 Database** (for testing)

## Project Structure

```
src/
├── main/
│   ├── java/com/capstone/be/
│   │   ├── BeApplication.java          # Main application class
│   │   ├── controller/                 # REST controllers
│   │   │   ├── UserController.java     # User endpoints
│   │   │   └── TaskController.java     # Task endpoints
│   │   ├── entity/                     # JPA entities
│   │   │   ├── User.java              # User entity
│   │   │   └── Task.java              # Task entity
│   │   └── repository/                # Data access layer
│   │       ├── UserRepository.java    # User repository
│   │       └── TaskRepository.java    # Task repository
│   └── resources/
│       └── application.properties     # Application configuration
└── test/                              # Test files
```

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **MySQL 8.0+**

## Database Setup

1. Install MySQL and create a database:
```sql
CREATE DATABASE capstone_db;
CREATE USER 'capstone_user'@'localhost' IDENTIFIED BY 'capstone_password';
GRANT ALL PRIVILEGES ON capstone_db.* TO 'capstone_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Update database configuration in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/capstone_db
spring.datasource.username=capstone_user
spring.datasource.password=capstone_password
```

## Running the Application

1. **Clone the repository**:
```bash
git clone <repository-url>
cd BE
```

2. **Build the application**:
```bash
mvn clean install
```

3. **Run the application**:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/username/{username}` | Get user by username |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

### Task Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| GET | `/api/tasks/user/{userId}` | Get tasks by user ID |
| GET | `/api/tasks/status/{status}` | Get tasks by status |
| GET | `/api/tasks/priority/{priority}` | Get tasks by priority |
| POST | `/api/tasks` | Create new task |
| POST | `/api/tasks/{taskId}/assign/{userId}` | Assign task to user |
| PUT | `/api/tasks/{id}` | Update task |
| PUT | `/api/tasks/{id}/status` | Update task status |
| DELETE | `/api/tasks/{id}` | Delete task |

## Example API Usage

### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sample Task",
    "description": "This is a sample task",
    "status": "TODO",
    "priority": "HIGH"
  }'
```

### Assign Task to User
```bash
curl -X POST http://localhost:8080/api/tasks/1/assign/1
```

## Testing

Run tests using Maven:
```bash
mvn test
```

Tests use H2 in-memory database for isolation.

## Development

1. **IDE Setup**: Import as Maven project in your favorite IDE
2. **Hot Reload**: Use `mvn spring-boot:run` for automatic restart on file changes
3. **Database Console**: Access H2 console at `http://localhost:8080/h2-console` (test profile)

## Configuration

Key configuration options in `application.properties`:

- **Server Port**: `server.port=8080`
- **Database URL**: `spring.datasource.url`
- **JPA DDL**: `spring.jpa.hibernate.ddl-auto=update`
- **SQL Logging**: `spring.jpa.show-sql=true`

## Contributing

1. Create feature branches for new functionality
2. Write tests for new features
3. Follow existing code conventions
4. Create pull requests for review

## Issues and Project Management

This project is designed to work with:
- **GitHub Issues**: Track bugs and feature requests
- **GitHub Projects**: Organize work in project boards
- **Pull Requests**: Code review and collaboration

## License

This project is part of a capstone project.