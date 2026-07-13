# QQQ Business Platform

A comprehensive, self-hosted business platform providing integrated content management, customer relationship management, email marketing, and user authentication capabilities. Built on enterprise-grade technologies with production deployment support.

## Overview

The QQQ Business Platform delivers core business functionality through a unified interface, eliminating the need for multiple SaaS subscriptions while maintaining complete data ownership and control. The platform combines proven open-source technologies in a cohesive architecture designed for scalability and reliability.

**Core Capabilities:**
- Professional website with content management system
- Email marketing with subscriber management and campaign tools
- Customer relationship management with contact and deal tracking
- Multi-tier user authentication and authorization
- Administrative dashboard with analytics and user management
- REST API with public, member, and staff access levels

**Technical Foundation:**
- Frontend: Next.js 14 with TypeScript and responsive design
- Backend: Spring Boot with Java 21 and PostgreSQL
- Authentication: Keycloak with OpenID Connect
- Infrastructure: Docker Compose for development and production
- Testing: Comprehensive end-to-end test suite with automated validation

## Architecture

```
Frontend Application (Next.js)
├── Public Pages (Landing, Blog, Authentication)
├── Member Portal (Exclusive content and profile management)
└── Admin Dashboard (User management and system configuration)

API Server (Spring Boot)
├── Public API (Content, newsletter signup, general information)
├── Member API (User profiles, member content, account management)
└── Staff API (Administrative functions, user management, analytics)

Infrastructure Services
├── Authentication Server (Keycloak)
├── Database (PostgreSQL with Liquibase migrations)
├── File Storage (MinIO S3-compatible)
└── Email Testing (Mailpit for development)
```

## Quick Start

### Prerequisites

- Docker Desktop or compatible container runtime
- Node.js 18 or later for frontend development
- Git for source code management

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/KofTwentyTwo/qqq-business-platform.git
   cd qqq-business-platform
   ```

2. Start the infrastructure services:
   ```bash
   docker-compose -f deploy/compose/docker-compose.yml up -d
   ```

3. Initialize the frontend:
   ```bash
   cd web
   npm install
   npm run dev -- --port 3004
   ```

### Service Access Points

- **Primary Application**: http://localhost:3004
- **API Documentation**: http://localhost:18082/swagger-ui.html
- **Authentication Server**: http://localhost:18081
- **Email Testing Interface**: http://localhost:8025
- **Administrative Interface**: http://localhost:3004/admin

### Initial Configuration

1. Access the authentication server at http://localhost:18081
2. Login with default credentials (admin/admin)
3. Create organizational user accounts
4. Configure email settings in application properties
5. Customize branding and content through the administrative interface

## Development Environment

### Frontend Development

The frontend application uses Next.js with TypeScript and provides hot reloading during development.

```bash
cd web
npm install
npm run dev -- --port 3004
```

Key development commands:
- `npm run build` - Production build
- `npm run test:e2e` - End-to-end test execution
- `npm run type-check` - TypeScript validation

### Backend Development

The backend API server uses Spring Boot with Maven for dependency management.

```bash
cd platform-app
../mvnw spring-boot:run
```

Key development commands:
- `./mvnw test` - Unit test execution
- `./mvnw package` - Build deployment artifact
- `./mvnw verify` - Full build with tests and validation

### Database Management

Database schema changes use Liquibase migrations stored in `db/src/main/resources/db/changelog/`.

Migration execution:
```bash
docker-compose -f deploy/compose/docker-compose.yml up migrate
```

## Customization

### Brand Identity

Update visual identity by modifying:
- `web/app/globals.css` - Color schemes and typography
- `web/public/` - Logo and favicon assets
- `web/app/layout.tsx` - Site metadata and configuration

### Content Management

Content customization involves:
- `web/app/page.tsx` - Homepage content and messaging
- `web/components/` - Reusable interface components
- `platform-app/src/main/resources/` - Backend configuration

### Functional Extensions

The platform supports extension through the QBit module system:
- `qbits/newsletter/` - Email marketing capabilities
- `qbits/content/` - Content management functionality
- `qbits/identity/` - User profile and authentication
- `qbits/crm/` - Customer relationship management

New modules follow the established QBit pattern for consistency and maintainability.

## Production Deployment

### Docker Compose Deployment

For production deployment using Docker Compose:

1. Copy and modify the production configuration:
   ```bash
   cp deploy/compose/docker-compose.yml docker-compose.prod.yml
   ```

2. Update environment variables for production:
   - Database credentials and connection strings
   - Email server configuration (SMTP)
   - Domain names and SSL certificate paths
   - Authentication server settings

3. Deploy with production configuration:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

### Environment Variables

Critical production environment variables:
- `DATABASE_URL` - PostgreSQL connection string
- `SMTP_HOST` - Email server configuration
- `KEYCLOAK_REALM_URL` - Authentication server endpoint
- `NEXTAUTH_SECRET` - Session encryption key
- `NEXTAUTH_URL` - Application callback URL

### SSL Configuration

Configure SSL/TLS through reverse proxy (Caddy, nginx, or cloud load balancer):
- Obtain SSL certificates (Let's Encrypt recommended)
- Configure reverse proxy routing to application ports
- Update CORS and authentication callback URLs

### Monitoring and Maintenance

Production monitoring recommendations:
- Health check endpoints for load balancer configuration
- Database backup procedures and retention policies
- Application log aggregation and analysis
- Performance monitoring and alerting
- Security update procedures for dependencies

## API Reference

### Public API Endpoints

Available without authentication:
- `GET /api/public/v1/content/posts` - Retrieve published content
- `POST /api/public/v1/newsletter/subscribe` - Newsletter subscription
- `GET /api/public/v1/health` - System health status

### Member API Endpoints

Require authenticated user session:
- `GET /api/member/v1/profile` - User profile information
- `PUT /api/member/v1/profile` - Update user profile
- `GET /api/member/v1/content` - Member-exclusive content

### Staff API Endpoints

Require administrative privileges:
- `GET /api/staff/v1/users` - User management operations
- `POST /api/staff/v1/content` - Content creation and management
- `GET /api/staff/v1/analytics` - Platform analytics and reporting

Complete API documentation available at the Swagger UI endpoint when the application is running.

## Testing

### End-to-End Testing

The platform includes comprehensive end-to-end tests covering all user workflows:

```bash
cd web
npm run test:e2e
```

Test categories:
- **Authentication flows** - Login, registration, session management
- **Content management** - Creation, editing, publishing workflows
- **Newsletter functionality** - Subscription, confirmation, management
- **Administrative operations** - User management, system configuration
- **Mobile responsiveness** - Cross-device compatibility

### Integration Testing

Backend integration tests validate API functionality and data persistence:

```bash
./mvnw test
```

### Continuous Integration

GitHub Actions workflow executes automated testing on code changes:
- TypeScript compilation validation
- Unit test execution
- End-to-end test execution against live services
- Docker image build verification

## Security Considerations

### Authentication and Authorization

- OpenID Connect implementation with Keycloak
- Role-based access control (Public, Member, Staff)
- Secure session management with HTTP-only cookies
- CSRF protection on all state-changing operations

### Data Protection

- Database credentials stored in environment variables
- API keys and secrets managed through secure configuration
- HTTPS enforcement in production environments
- Input validation and sanitization on all endpoints

### Infrastructure Security

- Container isolation through Docker networking
- Database access restricted to application services
- File upload validation and storage isolation
- Regular security updates for all dependencies

## Troubleshooting

### Common Issues

**Application fails to start:**
- Verify Docker Desktop is running and has sufficient resources
- Check port availability (3004, 18082, 18081, 15432)
- Review Docker Compose logs for specific service failures

**Database connection errors:**
- Confirm PostgreSQL container is healthy
- Validate database credentials in application configuration
- Check network connectivity between services

**Authentication redirects fail:**
- Verify Keycloak container startup completion
- Confirm authentication URLs match deployed domain
- Check browser developer tools for CORS errors

**Email functionality not working:**
- Configure SMTP settings in application properties
- Use Mailpit for development email testing
- Verify email templates and sender configuration

### Support Resources

- Application logs available through Docker Compose
- Database query capabilities through container exec
- Development server provides detailed error messages
- End-to-end test reports identify specific failure points

## License

Licensed under the Apache License, Version 2.0. See LICENSE file for complete terms and conditions.

## Contributing

Contributions welcome through standard GitHub workflows:
1. Fork the repository
2. Create feature branch from main
3. Implement changes with appropriate tests
4. Submit pull request with clear description
5. Ensure all automated checks pass

Code quality maintained through automated testing, TypeScript validation, and pre-commit hooks.