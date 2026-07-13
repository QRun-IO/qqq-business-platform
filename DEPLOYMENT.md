# Production Deployment Guide

This guide provides comprehensive instructions for deploying the QQQ Business Platform to production environments with enterprise-grade reliability and security.

## Prerequisites

### Infrastructure Requirements

**Minimum Server Specifications:**
- CPU: 4 cores (8 recommended)
- RAM: 8GB (16GB recommended)
- Storage: 50GB SSD (100GB recommended)
- Network: 1Gbps connection with static IP

**Software Dependencies:**
- Docker Engine 20.10+ or compatible container runtime
- Docker Compose 2.0+ for multi-container orchestration
- SSL/TLS certificates for HTTPS enforcement
- Domain name with DNS management access

### Security Prerequisites

**Certificates and Credentials:**
- SSL/TLS certificate (Let's Encrypt or commercial CA)
- Database passwords (minimum 32 characters, mixed case, numbers, symbols)
- JWT signing keys (cryptographically secure random generation)
- SMTP credentials for email delivery
- Administrative account credentials

## Environment Configuration

### Docker Compose Production Setup

1. Copy the base configuration:
   ```bash
   cp deploy/compose/docker-compose.yml docker-compose.prod.yml
   ```

2. Create production environment file:
   ```bash
   # .env.production
   POSTGRES_PASSWORD=your_secure_database_password
   POSTGRES_DB=qqq_platform_prod
   KEYCLOAK_ADMIN_PASSWORD=your_secure_keycloak_password
   NEXTAUTH_SECRET=your_64_character_random_string
   NEXTAUTH_URL=https://yourdomain.com/api/auth
   SMTP_HOST=your.smtp.server.com
   SMTP_USERNAME=your_smtp_username
   SMTP_PASSWORD=your_smtp_password
   ```

3. Update service configurations for production:
   ```yaml
   # docker-compose.prod.yml
   services:
     postgres:
       environment:
         - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
       volumes:
         - postgres_data:/var/lib/postgresql/data
         - ./backups:/backups
     
     keycloak:
       environment:
         - KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD}
         - KC_HOSTNAME=auth.yourdomain.com
         - KC_PROXY=edge
   ```

### SSL/TLS Certificate Configuration

**Using Let's Encrypt with Caddy:**

1. Create Caddyfile for reverse proxy:
   ```caddyfile
   yourdomain.com {
     reverse_proxy localhost:3004
   }
   
   api.yourdomain.com {
     reverse_proxy localhost:18082
   }
   
   auth.yourdomain.com {
     reverse_proxy localhost:18081
   }
   ```

2. Add Caddy service to Docker Compose:
   ```yaml
   caddy:
     image: caddy:2-alpine
     ports:
       - "80:80"
       - "443:443"
     volumes:
       - ./Caddyfile:/etc/caddy/Caddyfile
       - caddy_data:/data
       - caddy_config:/config
   ```

**Using nginx with custom certificates:**

1. Configure nginx virtual hosts:
   ```nginx
   server {
     listen 443 ssl http2;
     server_name yourdomain.com;
     
     ssl_certificate /etc/ssl/certs/yourdomain.com.crt;
     ssl_certificate_key /etc/ssl/private/yourdomain.com.key;
     
     location / {
       proxy_pass http://localhost:3004;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
     }
   }
   ```

### Database Configuration

**External PostgreSQL Setup:**

1. Create production database:
   ```sql
   CREATE DATABASE qqq_platform_prod;
   CREATE USER qqq_platform_app WITH PASSWORD 'secure_password';
   CREATE USER qqq_platform_ddl WITH PASSWORD 'secure_password';
   GRANT ALL PRIVILEGES ON DATABASE qqq_platform_prod TO qqq_platform_ddl;
   ```

2. Configure connection security:
   ```bash
   # postgresql.conf
   ssl = on
   ssl_cert_file = '/path/to/server.crt'
   ssl_key_file = '/path/to/server.key'
   
   # pg_hba.conf
   hostssl all all 0.0.0.0/0 md5
   ```

3. Update application configuration:
   ```yaml
   # application-prod.yml
   spring:
     datasource:
       url: jdbc:postgresql://your-db-server:5432/qqq_platform_prod
       username: qqq_platform_app
       password: ${DATABASE_PASSWORD}
   ```

### Email Service Configuration

**SMTP Provider Setup (SendGrid example):**

```yaml
# application-prod.yml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

**Amazon SES Configuration:**

```yaml
spring:
  mail:
    host: email-smtp.us-east-1.amazonaws.com
    port: 587
    username: ${AWS_SMTP_USERNAME}
    password: ${AWS_SMTP_PASSWORD}
```

## Deployment Procedures

### Initial Deployment

1. **Server Preparation:**
   ```bash
   # Update system packages
   sudo apt update && sudo apt upgrade -y
   
   # Install Docker
   curl -fsSL https://get.docker.com -o get-docker.sh
   sh get-docker.sh
   
   # Configure firewall
   sudo ufw allow 22,80,443/tcp
   sudo ufw enable
   ```

2. **Application Deployment:**
   ```bash
   # Clone repository
   git clone https://github.com/KofTwentyTwo/qqq-business-platform.git
   cd qqq-business-platform
   
   # Configure environment
   cp .env.example .env.production
   # Edit .env.production with production values
   
   # Build and start services
   docker-compose -f docker-compose.prod.yml --env-file .env.production up -d
   ```

3. **Database Initialization:**
   ```bash
   # Run migrations
   docker-compose -f docker-compose.prod.yml --env-file .env.production up migrate
   
   # Verify database connectivity
   docker exec qqq-platform-postgres psql -U qqq_platform_app -d qqq_platform_prod -c "SELECT version();"
   ```

### Health Verification

**Automated Health Checks:**

```bash
#!/bin/bash
# health-check.sh

# Check API server
if curl -f http://localhost:18082/api/public/v1/health > /dev/null 2>&1; then
  echo "API server: HEALTHY"
else
  echo "API server: FAILED"
  exit 1
fi

# Check frontend application
if curl -f http://localhost:3004 > /dev/null 2>&1; then
  echo "Frontend: HEALTHY"
else
  echo "Frontend: FAILED"
  exit 1
fi

# Check authentication server
if curl -f http://localhost:18081/realms/qqq-platform/.well-known/openid-configuration > /dev/null 2>&1; then
  echo "Authentication: HEALTHY"
else
  echo "Authentication: FAILED"
  exit 1
fi

echo "All services healthy"
```

**End-to-End Validation:**

```bash
cd web
npm run test:e2e:production
```

## Monitoring and Maintenance

### Application Monitoring

**Docker Container Monitoring:**

```bash
# Monitor container health
docker stats

# View application logs
docker-compose -f docker-compose.prod.yml logs -f

# Check container status
docker-compose -f docker-compose.prod.yml ps
```

**Application Performance Monitoring:**

1. Configure application metrics:
   ```yaml
   # application-prod.yml
   management:
     endpoints:
       web:
         exposure:
           include: health,metrics,prometheus
     endpoint:
       health:
         show-details: always
   ```

2. Set up Prometheus monitoring:
   ```yaml
   # docker-compose.prod.yml
   prometheus:
     image: prom/prometheus
     ports:
       - "9090:9090"
     volumes:
       - ./prometheus.yml:/etc/prometheus/prometheus.yml
   ```

### Backup Procedures

**Database Backup Automation:**

```bash
#!/bin/bash
# backup-database.sh

BACKUP_DIR="/var/backups/qqq-platform"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# PostgreSQL backup
docker exec qqq-platform-postgres pg_dump -U qqq_platform_app -d qqq_platform_prod > $BACKUP_DIR/database_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/database_$DATE.sql

# Remove backups older than 30 days
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete

echo "Database backup completed: $BACKUP_DIR/database_$DATE.sql.gz"
```

**Application Data Backup:**

```bash
#!/bin/bash
# backup-application.sh

# Backup uploaded files
tar -czf /var/backups/qqq-platform/files_$(date +%Y%m%d).tar.gz ./data/uploads/

# Backup configuration
tar -czf /var/backups/qqq-platform/config_$(date +%Y%m%d).tar.gz .env.production docker-compose.prod.yml

# Backup Docker volumes
docker run --rm -v qqq_postgres_data:/source -v /var/backups/qqq-platform:/backup alpine tar -czf /backup/volumes_$(date +%Y%m%d).tar.gz -C /source .
```

### Security Maintenance

**Regular Security Updates:**

```bash
#!/bin/bash
# security-updates.sh

# Update base system
sudo apt update && sudo apt upgrade -y

# Update Docker images
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# Clean up unused images
docker image prune -f

# Restart services to apply updates
docker-compose -f docker-compose.prod.yml restart
```

**SSL Certificate Renewal:**

```bash
# Automated renewal with Let's Encrypt
0 2 * * * /usr/bin/certbot renew --quiet && docker-compose -f /path/to/docker-compose.prod.yml restart caddy
```

### Performance Optimization

**Database Performance Tuning:**

```sql
-- postgresql.conf optimizations
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
```

**Application Performance Configuration:**

```yaml
# application-prod.yml
server:
  tomcat:
    max-threads: 200
    min-spare-threads: 10
spring:
  jpa:
    hibernate:
      jdbc:
        batch_size: 20
    properties:
      hibernate:
        jdbc:
          batch_versioned_data: true
```

## Disaster Recovery

### Backup Restoration

**Database Recovery:**

```bash
# Stop application services
docker-compose -f docker-compose.prod.yml stop qqq-server

# Restore database from backup
gunzip -c /var/backups/qqq-platform/database_20231201_120000.sql.gz | docker exec -i qqq-platform-postgres psql -U qqq_platform_app -d qqq_platform_prod

# Restart services
docker-compose -f docker-compose.prod.yml start
```

**Complete System Recovery:**

```bash
# Restore configuration files
tar -xzf /var/backups/qqq-platform/config_20231201.tar.gz

# Restore application data
tar -xzf /var/backups/qqq-platform/files_20231201.tar.gz

# Restore Docker volumes
docker volume create qqq_postgres_data
docker run --rm -v qqq_postgres_data:/target -v /var/backups/qqq-platform:/backup alpine tar -xzf /backup/volumes_20231201.tar.gz -C /target

# Redeploy application
docker-compose -f docker-compose.prod.yml up -d
```

### High Availability Setup

**Load Balancer Configuration:**

```nginx
upstream qqq_backend {
    server 10.0.1.10:3004;
    server 10.0.1.11:3004;
    server 10.0.1.12:3004;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    location / {
        proxy_pass http://qqq_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Database Clustering:**

```yaml
# PostgreSQL primary-replica setup
version: '3.8'
services:
  postgres-primary:
    image: postgres:16-alpine
    environment:
      POSTGRES_REPLICATION_USER: replicator
      POSTGRES_REPLICATION_PASSWORD: replica_password
    volumes:
      - ./postgresql-primary.conf:/etc/postgresql/postgresql.conf
  
  postgres-replica:
    image: postgres:16-alpine
    environment:
      PGUSER: postgres
      POSTGRES_PRIMARY_HOST: postgres-primary
      POSTGRES_REPLICATION_USER: replicator
      POSTGRES_REPLICATION_PASSWORD: replica_password
```

This deployment guide provides the foundation for enterprise-grade production deployment with appropriate security, monitoring, and maintenance procedures.