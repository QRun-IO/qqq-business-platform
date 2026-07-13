# QQQ Business Platform - Test Suite

This directory contains comprehensive testing and validation tools for the QQQ Business Platform.

## 🧪 Test Suite Overview

The QQQ Business Platform includes multiple testing approaches to validate all aspects of the system:

### 1. **Quick Shell Tests** (`quick-test.sh`)
- Fast validation of core functionality
- Tests infrastructure, APIs, and frontend
- Perfect for CI/CD pipelines
- Provides color-coded results

```bash
# Run quick tests
make test

# Or directly:
bash tests/quick-test.sh
```

### 2. **Comprehensive Python Suite** (`platform-validation.py`)
- Detailed testing with proper error reporting
- Validates business workflows (newsletter signup, etc.)
- Includes database and container checks
- Provides detailed test reports

```bash
# Run full test suite
make test-full

# Or directly:
python3 tests/platform-validation.py
```

### 3. **Interactive Browser Dashboard** (`dashboard.html`)
- Live testing interface in your browser
- Real-time status updates
- Click-to-test functionality
- Visual progress tracking

```bash
# Open test dashboard
make test-dashboard

# Or open directly:
# tests/dashboard.html in your browser
```

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Python 3.x (for full test suite)
- Modern web browser (for dashboard)

### 1. Start the Platform
```bash
# Start all services
make start

# Check status
make status

# Wait for services to be ready
make wait-ready
```

### 2. Run Tests
```bash
# Quick validation
make test

# Open interactive dashboard
make test-dashboard

# Comprehensive testing
make test-full
```

### 3. Demo Mode
```bash
# Full demo: build, start, and open dashboard
make demo
```

## 📋 Test Categories

### Infrastructure Tests
- ✅ PostgreSQL Database (latest)
- ✅ Keycloak OIDC Provider (v25.0)
- ✅ MinIO Object Storage (latest)
- ✅ Mailpit Email Testing (latest)
- ✅ Docker Container Health

### API Tests
- ✅ Public API endpoints (`/api/public/v1/*`)
- ✅ Member API authentication (`/api/member/v1/*`)
- ✅ Staff API authorization (`/api/staff/v1/*`)
- ✅ Newsletter subscription workflow
- ✅ File upload endpoints

### Frontend Tests
- ✅ Next.js Homepage rendering
- ✅ Authentication pages (login/register)
- ✅ Blog system pages
- ✅ Admin dashboard access
- ✅ User profile management

### Business Workflow Tests
- ✅ User registration flow
- ✅ Newsletter double opt-in
- ✅ Blog post creation/editing
- ✅ File upload/download
- ✅ Email notifications

## 🔗 Service URLs

When the platform is running, these services are available:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:13000 | - |
| **Backend API** | http://localhost:18080/api | - |
| **API Documentation** | http://localhost:18080/swagger-ui.html | - |
| **Keycloak Admin** | http://localhost:18081/admin | admin/admin |
| **Email Testing** | http://localhost:8025 | - |
| **MinIO Console** | http://localhost:19001 | minio_admin/minio_admin_password |

## 📊 Understanding Test Results

### Success Criteria
- **90%+ Pass Rate**: Excellent - Platform is fully operational
- **75-89% Pass Rate**: Good - Platform is operational with minor issues  
- **50-74% Pass Rate**: Warning - Platform has some significant issues
- **<50% Pass Rate**: Critical - Platform has major problems

### Common Issues and Solutions

#### Services Not Starting
```bash
# Check Docker status
docker ps

# Check service logs
make logs

# Restart services
make restart
```

#### Port Conflicts
If you get port binding errors, another service may be using the ports:
- Frontend: 13000
- Backend: 18080  
- Keycloak: 18081
- Mailpit: 8025
- MinIO: 19000, 19001
- PostgreSQL: 15432

#### Database Connection Issues
```bash
# Check PostgreSQL logs
docker-compose -f deploy/compose/docker-compose.yml logs postgres

# Restart database
docker-compose -f deploy/compose/docker-compose.yml restart postgres
```

## 🛠️ Development Testing

### Testing Individual Components
```bash
# Test only infrastructure
make test-infra

# Test only API endpoints
make test-api

# Test only frontend
make test-frontend
```

### Custom Test Configuration
Edit `tests/platform-validation.py` to customize:
- Test endpoints
- Timeout values  
- Expected response codes
- Test data

### Adding New Tests
1. Add test methods to `PlatformValidator` class
2. Update the test dashboard HTML
3. Add corresponding Makefile targets

## 🔄 Continuous Integration

The test suite is designed for CI/CD integration:

```yaml
# Example GitHub Actions
- name: Test QQQ Platform
  run: |
    make build
    make start
    make wait-ready
    make test-full
```

## 📝 Test Reports

### Shell Test Output
```
✅ PostgreSQL Database - Database connection: OK
✅ Keycloak OIDC - Realm configuration: OK  
✅ Newsletter Subscription - Status: 201
```

### Python Test Output  
```
2026-07-12 14:30:15 - INFO - ✅ PASS: PostgreSQL Database - Database connection: OK
2026-07-12 14:30:16 - INFO - ✅ PASS: API: Public Health Check - Status: 200
2026-07-12 14:30:17 - INFO - 🏆 FINAL RESULT: 12/12 tests passed (100.0%)
```

### Browser Dashboard
- Real-time visual status indicators
- Click-to-test functionality
- Progress bars and success metrics
- Direct links to services

## 🚨 Troubleshooting

### Test Failures
1. **Check service status**: `make status`
2. **Review logs**: `make logs`  
3. **Restart services**: `make restart`
4. **Clean and rebuild**: `make clean && make build && make start`

### Performance Issues
- Ensure Docker has adequate resources (4GB+ RAM recommended)
- Check disk space for Docker volumes
- Monitor CPU usage during startup

### Network Issues
- Verify no firewall blocking local ports
- Check for port conflicts with other services
- Ensure Docker networking is configured correctly

## 📚 Additional Resources

- [QQQ Framework Documentation](../../README.md)
- [Docker Compose Reference](deploy/compose/docker-compose.yml)
- [API Documentation](http://localhost:18080/swagger-ui.html) (when running)
- [Keycloak Documentation](https://www.keycloak.org/documentation)