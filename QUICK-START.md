# Quick Start Guide

Deploy the QQQ Business Platform in under five minutes with this step-by-step guide.

## Prerequisites

Ensure the following software is installed and operational:
- Docker Desktop (version 20.10 or later)
- Git (version 2.30 or later)
- Node.js (version 18 or later)
- Command line interface (Terminal, PowerShell, or Command Prompt)

## Step 1: Repository Setup

Clone the repository to your local development environment:

```bash
git clone https://github.com/KofTwentyTwo/qqq-business-platform.git
cd qqq-business-platform
```

Verify the repository structure:
```bash
ls -la deploy/compose/docker-compose.yml  # Should exist
```

## Step 2: Infrastructure Startup

Launch all required services using Docker Compose:

```bash
docker-compose -f deploy/compose/docker-compose.yml up -d
```

This command starts the following services:
- PostgreSQL database server
- Keycloak authentication server
- MinIO object storage service
- Mailpit email testing service
- Application API server
- Database migration service

## Step 3: Service Initialization

The platform requires approximately 90 seconds for complete initialization. Monitor startup progress:

```bash
# Check service health status
docker-compose -f deploy/compose/docker-compose.yml ps

# Verify API server availability
curl http://localhost:18082/api/public/v1/health
```

Expected response: `{"status":"UP"}`

## Step 4: Frontend Application

Navigate to the frontend directory and start the development server:

```bash
cd web
npm install
npm run dev -- --port 3004
```

The installation process downloads required dependencies and may take several minutes on first execution.

## Step 5: Access Verification

Open your web browser and navigate to: `http://localhost:3004`

Verify the following functionality:
1. Homepage loads with complete styling and content
2. Navigation menu responds to user interaction
3. Newsletter subscription form accepts email input
4. Authentication system redirects to login interface
5. Administrative area requires proper authorization

## Service Endpoints

After successful deployment, access the following interfaces:

- **Primary Application**: http://localhost:3004
- **API Documentation**: http://localhost:18082/swagger-ui.html
- **Authentication Server**: http://localhost:18081 (admin/admin)
- **Email Testing**: http://localhost:8025
- **Administrative Dashboard**: http://localhost:3004/admin

## Validation Checklist

Confirm successful deployment by testing these critical functions:

**Frontend Validation:**
- [ ] Homepage renders without errors or missing content
- [ ] Newsletter form accepts valid email addresses
- [ ] Authentication flow redirects to Keycloak login
- [ ] Protected routes require proper authorization
- [ ] Mobile interface displays correctly

**Backend Validation:**
- [ ] Health endpoint returns UP status
- [ ] Newsletter API accepts subscription requests
- [ ] Content API returns structured data
- [ ] Authentication endpoints respond correctly
- [ ] Database connections function properly

**Infrastructure Validation:**
- [ ] All Docker containers show healthy status
- [ ] Database accepts connections and queries
- [ ] Email service captures outbound messages
- [ ] File storage service responds to requests
- [ ] Authentication server processes login attempts

## Troubleshooting Common Issues

**Port Conflicts:**
If services fail to start due to port conflicts, identify competing processes:
```bash
# Check port usage on Windows
netstat -ano | findstr :3004
netstat -ano | findstr :18082

# Check port usage on macOS/Linux
lsof -i :3004
lsof -i :18082
```

Stop conflicting processes or modify port assignments in the Docker Compose configuration.

**Container Startup Failures:**
Review service logs for specific error messages:
```bash
docker-compose -f deploy/compose/docker-compose.yml logs [service-name]
```

Common solutions:
- Ensure Docker Desktop has sufficient memory allocation (minimum 4GB recommended)
- Verify disk space availability for container images and volumes
- Check Docker Desktop service status and restart if necessary

**Database Connection Issues:**
Reset the database state if initialization fails:
```bash
docker-compose -f deploy/compose/docker-compose.yml down --volumes
docker-compose -f deploy/compose/docker-compose.yml up -d
```

**Frontend Build Errors:**
Clear Node.js cache and reinstall dependencies:
```bash
cd web
rm -rf node_modules .next
npm install
npm run dev -- --port 3004
```

## Next Steps

With the platform successfully deployed, proceed with customization and configuration:

1. **Brand Customization**: Update colors, typography, and logo assets
2. **Content Management**: Configure homepage messaging and navigation structure
3. **User Administration**: Create organizational accounts and define user roles
4. **Email Configuration**: Set up SMTP settings for production email delivery
5. **Domain Configuration**: Configure custom domain names and SSL certificates

Refer to the main README.md file for detailed customization instructions and production deployment procedures.

## Production Considerations

This quick start guide establishes a development environment. Production deployment requires additional configuration:

- SSL/TLS certificate installation
- External database service configuration
- Email service provider integration
- Domain name and DNS configuration
- Load balancer and scaling considerations
- Backup and disaster recovery procedures

Consult the production deployment section in the main documentation for comprehensive production setup instructions.