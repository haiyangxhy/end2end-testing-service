# End-to-End Testing Platform Database

## Quick Start

### Prerequisites
- PostgreSQL 12+ installed and running
- psql command line tool available in PATH

### Windows Deployment

1. **Run the simple deployment script:**
   ```cmd
   deploy-simple.bat
   ```

2. **Verify the deployment:**
   ```cmd
   verify-simple.bat
   ```

### Manual Deployment

1. **Create database:**
   ```sql
   CREATE DATABASE testplatform;
   ```

2. **Initialize schema:**
   ```cmd
   psql -h localhost -U postgres -d testplatform -f init-simple.sql
   ```

3. **Import sample data (optional):**
   ```cmd
   psql -h localhost -U postgres -d testplatform -f sample-data-simple.sql
   ```

## Configuration

### Database Connection
- **Host:** localhost
- **Port:** 5432
- **Database:** testplatform
- **Username:** postgres
- **Password:** root

### Spring Boot Configuration
Update your `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/testplatform
    username: postgres
    password: root
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: false
```

## Database Structure

### Core Tables
- `users` - User accounts and authentication
- `user_sessions` - Active user sessions
- `test_environments` - Test environment configurations
- `global_variables` - Global test variables
- `test_suites` - Test suite definitions
- `test_cases` - Individual test cases
- `test_suite_cases` - Test suite and case associations
- `test_executions` - Test execution records
- `test_case_executions` - Individual test case execution results
- `test_reports` - Generated test reports
- `scheduled_tasks` - Scheduled test tasks
- `task_execution_history` - Task execution history

### Key Features
- **UUID Primary Keys** - All tables use UUID for primary keys
- **Audit Fields** - created_at, updated_at timestamps
- **JSONB Support** - Flexible configuration storage
- **Comprehensive Indexing** - Optimized for performance
- **Data Validation** - Check constraints for data integrity
- **Automatic Triggers** - Auto-update timestamps

## Sample Data

The sample data includes:
- 1 admin user (admin/admin123)
- 1 default test environment
- 5 global variables
- 3 test suites (API, UI, Business)
- 6 test cases with different types
- Sample test executions and reports
- 3 scheduled tasks

## Troubleshooting

### Common Issues

1. **Connection Failed**
   - Check PostgreSQL is running
   - Verify username/password
   - Check firewall settings

2. **Permission Denied**
   - Ensure user has CREATE DATABASE privilege
   - Check database ownership

3. **Encoding Issues**
   - Use the simple scripts (init-simple.sql, sample-data-simple.sql)
   - Avoid Chinese characters in SQL files

### Verification Commands

```sql
-- Check tables
\dt

-- Check data
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM test_suites;

-- Check indexes
SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public';

-- Check functions
SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = 'public';
```

## Maintenance

### Backup
```cmd
pg_dump -h localhost -U postgres -d testplatform > backup.sql
```

### Restore
```cmd
psql -h localhost -U postgres -d testplatform < backup.sql
```

### Cleanup
```sql
-- Clean expired sessions
SELECT cleanup_expired_sessions();

-- Update scheduled tasks
SELECT update_scheduled_task_next_run();
```

## Files

- `init-simple.sql` - Main database initialization script
- `sample-data-simple.sql` - Sample data for testing
- `deploy-simple.bat` - Windows deployment script
- `verify-simple.bat` - Windows verification script
- `config.env` - Configuration template
- `README-SIMPLE.md` - This documentation
