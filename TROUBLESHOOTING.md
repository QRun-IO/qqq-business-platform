# Troubleshooting Guide

This guide provides systematic diagnostic and resolution procedures for common issues encountered during deployment, development, and operation of the QQQ Business Platform.

## Initial Diagnostic Steps

Before investigating specific issues, perform these preliminary checks:

1. **Service Health Verification**:
   ```bash
   docker-compose -f deploy/compose/docker-compose.yml ps
   ```

2. **Container Log Review**:
   ```bash
   docker-compose -f deploy/compose/docker-compose.yml logs --tail=50
   ```

3. **Network Connectivity Test**:
   ```bash
   curl -I http://localhost:3004
   curl -I http://localhost:18082/api/public/v1/health
   ```

4. **Resource Availability Check**:
   ```bash
   docker system df
   docker stats --no-stream
   ```

## Application Startup Issues

### Container Fails to Start

**Symptoms**: Docker containers exit immediately or fail to initialize

**Diagnostic Commands**:
```bash
# Check container status and exit codes
docker-compose -f deploy/compose/docker-compose.yml ps -a

# Review specific service logs
docker-compose -f deploy/compose/docker-compose.yml logs [service-name]

# Inspect container configuration
docker inspect [container-id]
```

**Common Causes and Solutions**:

1. **Port Conflicts**:
   ```bash
   # Identify process using required ports
   netstat -tulpn | grep -E :(3004|18082|18081|15432)
   
   # Windows alternative
   netstat -ano | findstr -E :(3004|18082|18081|15432)
   ```
   
   **Resolution**: Stop conflicting processes or modify port assignments in docker-compose.yml

2. **Insufficient System Resources**:
   ```bash
   # Check available memory and disk
   free -h
   df -h
   ```
   
   **Resolution**: Increase Docker Desktop memory allocation or free system resources

3. **Missing Environment Variables**:
   ```bash
   # Verify environment configuration
   docker-compose -f deploy/compose/docker-compose.yml config
   ```
   
   **Resolution**: Ensure all required environment variables are defined

### Database Connection Failures

**Symptoms**: Application logs show database connection errors

**Diagnostic Procedure**:
```bash
# Test PostgreSQL container health
docker exec qqq-platform-postgres pg_isready -U postgres

# Attempt direct database connection
docker exec -it qqq-platform-postgres psql -U platform_app -d qqq_platform

# Check database configuration
docker exec qqq-platform-postgres cat /var/lib/postgresql/data/postgresql.conf
```

**Resolution Steps**:

1. **Reset Database State**:
   ```bash
   docker-compose -f deploy/compose/docker-compose.yml down --volumes
   docker-compose -f deploy/compose/docker-compose.yml up -d postgres
   sleep 30
   docker-compose -f deploy/compose/docker-compose.yml up migrate
   ```

2. **Verify Credentials**:
   Check `deploy/compose/config/postgres-init.sql` for user creation scripts
   Ensure application configuration matches database credentials

3. **Network Connectivity**:
   ```bash
   # Test internal network connectivity
   docker exec qqq-server ping qqq-platform-postgres
   ```

### Authentication Service Issues

**Symptoms**: Login redirects fail or authentication errors occur

**Diagnostic Commands**:
```bash
# Verify Keycloak service health
curl -I http://localhost:18081/realms/qqq-platform/.well-known/openid-configuration

# Check Keycloak startup logs
docker-compose -f deploy/compose/docker-compose.yml logs keycloak

# Test realm configuration
curl http://localhost:18081/realms/qqq-platform/.well-known/openid-configuration | jq .
```

**Common Issues**:

1. **Realm Import Failure**:
   ```bash
   # Verify realm configuration file
   cat deploy/compose/config/keycloak-realm.json | jq .
   
   # Manual realm import
   docker exec -it qqq-keycloak /opt/keycloak/bin/kc.sh import --file /opt/keycloak/data/import/realm.json
   ```

2. **URL Configuration Mismatch**:
   Verify `NEXTAUTH_URL` in frontend configuration matches deployed domain
   Check Keycloak client redirect URIs include correct application URLs

## Frontend Application Issues

### Page Loading Failures

**Symptoms**: Blank pages, JavaScript errors, or styling issues

**Diagnostic Steps**:
```bash
cd web

# Check TypeScript compilation
npm run type-check

# Verify build process
npm run build

# Clear cache and restart
rm -rf .next node_modules/.cache
npm install
npm run dev -- --port 3004
```

**Browser Developer Tools Analysis**:
1. Console tab: JavaScript errors and warnings
2. Network tab: Failed resource requests
3. Sources tab: TypeScript compilation issues

### API Integration Problems

**Symptoms**: Forms not submitting, data not loading, 500 errors

**API Connectivity Test**:
```bash
# Test backend API directly
curl -X POST http://localhost:18082/api/public/v1/newsletter/subscribe \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# Check API endpoint availability
curl http://localhost:18082/swagger-ui.html
```

**CORS Configuration Issues**:
```bash
# Test preflight request
curl -H "Origin: http://localhost:3004" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://localhost:18082/api/public/v1/newsletter/subscribe -v
```

**Resolution**: Verify CORS configuration in `platform-app/src/main/java/.../PlatformSecurityConfiguration.java`

### Authentication Flow Problems

**Symptoms**: Login loops, session errors, redirect failures

**NextAuth Configuration Verification**:
```bash
# Test NextAuth endpoints
curl http://localhost:3004/api/auth/providers
curl http://localhost:3004/api/auth/session

# Check environment variables
cat web/.env.local
```

**Common Configuration Errors**:
1. `NEXTAUTH_URL` mismatch with deployment URL
2. Keycloak client configuration missing redirect URIs
3. Session secret not configured or invalid

## Backend Service Issues

### API Server Startup Failures

**Symptoms**: Spring Boot application fails to start or health checks fail

**Log Analysis**:
```bash
# Review application startup logs
docker-compose -f deploy/compose/docker-compose.yml logs qqq-server

# Check Java application health
curl http://localhost:18082/actuator/health | jq .
```

**Common Spring Boot Issues**:

1. **Configuration Binding Errors**:
   ```bash
   # Validate application.yml syntax
   cd platform-app/src/main/resources
   python -c "import yaml; yaml.safe_load(open('application.yml'))"
   ```

2. **Database Migration Failures**:
   ```bash
   # Check Liquibase changelog
   docker exec qqq-platform-postgres psql -U platform_ddl -d qqq_platform -c "\dt"
   
   # Review migration logs
   docker-compose -f deploy/compose/docker-compose.yml logs migrate
   ```

3. **Bean Configuration Conflicts**:
   Review Spring Boot configuration classes for duplicate bean definitions
   Check classpath for conflicting dependencies

### Performance Issues

**Symptoms**: Slow response times, timeouts, high resource usage

**Performance Monitoring**:
```bash
# Monitor container resource usage
docker stats

# Check database performance
docker exec qqq-platform-postgres psql -U platform_app -d qqq_platform \
  -c "SELECT query, calls, total_time, mean_time FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"

# Profile Java application
docker exec qqq-server jps -v
```

**Database Optimization**:
```sql
-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM newsletter_subscription WHERE subscription_state = 'CONFIRMED';

-- Check index usage
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE tablename = 'newsletter_subscription';
```

## Infrastructure Issues

### Docker Compose Problems

**Symptoms**: Services fail to communicate, networking errors

**Network Diagnostics**:
```bash
# List Docker networks
docker network ls

# Inspect network configuration
docker network inspect qqq-business-platform_default

# Test inter-service connectivity
docker exec qqq-server ping qqq-platform-postgres
docker exec qqq-server nslookup qqq-platform-postgres
```

**Volume Mount Issues**:
```bash
# Check volume mounts
docker inspect qqq-platform-postgres | jq '.[0].Mounts'

# Verify file permissions
docker exec qqq-platform-postgres ls -la /var/lib/postgresql/data
```

### SSL/TLS Certificate Issues

**Symptoms**: HTTPS connections fail, certificate warnings

**Certificate Validation**:
```bash
# Check certificate validity
openssl x509 -in /path/to/certificate.crt -text -noout

# Test SSL connection
openssl s_client -connect yourdomain.com:443 -servername yourdomain.com

# Verify certificate chain
curl -I https://yourdomain.com
```

**Let's Encrypt Renewal**:
```bash
# Test certificate renewal
certbot renew --dry-run

# Force renewal
certbot renew --force-renewal

# Check certificate status
certbot certificates
```

## Monitoring and Alerting

### Health Check Implementation

**Automated Monitoring Script**:
```bash
#!/bin/bash
# monitor.sh

SERVICES=("http://localhost:3004" "http://localhost:18082/api/public/v1/health" "http://localhost:18081")
ERROR_COUNT=0

for SERVICE in "${SERVICES[@]}"; do
  if ! curl -f "$SERVICE" > /dev/null 2>&1; then
    echo "ERROR: $SERVICE is not responding"
    ((ERROR_COUNT++))
  else
    echo "OK: $SERVICE is healthy"
  fi
done

if [ $ERROR_COUNT -gt 0 ]; then
  echo "Health check failed: $ERROR_COUNT services down"
  exit 1
else
  echo "All services healthy"
  exit 0
fi
```

### Log Aggregation

**Centralized Logging Setup**:
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  qqq-server:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
  
  postgres:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

**Log Analysis Commands**:
```bash
# Search for specific errors
docker-compose logs | grep -i "error\|exception\|failed"

# Monitor real-time logs
docker-compose logs -f --tail=100

# Export logs for analysis
docker-compose logs > platform-logs-$(date +%Y%m%d).log
```

## Recovery Procedures

### Service Recovery

**Graceful Service Restart**:
```bash
# Restart specific service
docker-compose -f deploy/compose/docker-compose.yml restart qqq-server

# Rolling restart of all services
for service in postgres keycloak qqq-server; do
  docker-compose -f deploy/compose/docker-compose.yml restart $service
  sleep 30
done
```

**Emergency Recovery**:
```bash
# Complete platform reset
docker-compose -f deploy/compose/docker-compose.yml down
docker system prune -f
docker-compose -f deploy/compose/docker-compose.yml up -d
```

### Data Recovery

**Database Recovery from Backup**:
```bash
# Stop application services
docker-compose -f deploy/compose/docker-compose.yml stop qqq-server

# Restore database
gunzip -c /path/to/backup.sql.gz | docker exec -i qqq-platform-postgres psql -U postgres -d qqq_platform

# Restart services
docker-compose -f deploy/compose/docker-compose.yml start qqq-server
```

## Preventive Measures

### Regular Maintenance

**Weekly Maintenance Checklist**:
1. Review application and system logs for errors
2. Verify backup completion and test restore procedures
3. Update Docker images for security patches
4. Monitor disk usage and clean up old logs
5. Test critical business functions end-to-end

**Monthly Maintenance Tasks**:
1. Review and update SSL certificates
2. Analyze performance metrics and optimize queries
3. Update system packages and security patches
4. Review user access and permissions
5. Validate disaster recovery procedures

This troubleshooting guide provides systematic approaches to identifying and resolving common issues while maintaining platform stability and performance.