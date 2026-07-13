# 🚀 QQQ Business Platform - Complete Self-Hosted Business Solution

A production-ready, fully-integrated business platform that combines the best of Squarespace, ConvertKit, and HubSpot into a single, self-hosted solution.

## ✨ What You Get

**🌐 Professional Website & CMS**
- Beautiful, responsive design with premium aesthetics
- Content management system for blogs and pages  
- SEO-optimized with modern frontend (Next.js 14)

**📧 Newsletter & Email Marketing**
- Double opt-in newsletter system
- Campaign management and analytics
- Email template editor with rich content

**🔐 User Management & Authentication**
- Enterprise-grade OIDC authentication via Keycloak
- Role-based access control (Member/Staff/Admin)
- User profiles and account management

**📊 CRM & Contact Management**
- Contact and company tracking
- Deal pipeline management
- Activity logging and relationship mapping

**🛡️ Admin Dashboard**
- System analytics and user management
- Content moderation and publishing
- Platform configuration and monitoring

## 🚀 Quick Start (5 Minutes)

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend development)
- Git

### 1. Clone & Start
```bash
git clone [your-repo-url]
cd qqq-business-platform

# Start all services
docker-compose -f deploy/compose/docker-compose.yml up -d

# Wait ~90 seconds for full startup
```

### 2. Access Your Platform
- **Website**: http://localhost:3004
- **Admin Panel**: http://localhost:3004/admin  
- **API Docs**: http://localhost:18082/swagger-ui.html
- **Email Testing**: http://localhost:8025
- **Auth Server**: http://localhost:18081

### 3. First Login
- Click "Sign in" → "Continue securely"  
- Use Keycloak admin: `admin` / `admin`
- Create your first business user account

## 📋 Complete Setup Guide

### Environment Configuration
1. **Frontend Setup**:
   ```bash
   cd web
   npm install
   cp .env.example .env.local
   # Edit .env.local with your domains
   npm run dev -- --port 3004
   ```

2. **Backend Services** (via Docker):
   ```bash
   # All services in one command
   docker-compose -f deploy/compose/docker-compose.yml up -d
   
   # Or individually:
   docker-compose up -d postgres keycloak mailpit minio
   docker-compose up migrate  # Run database migrations
   docker-compose up -d qqq-server  # Start API server
   ```

3. **Verify Installation**:
   ```bash
   # Run comprehensive health check
   npm run test:e2e
   
   # Or manual verification
   curl http://localhost:18082/api/public/v1/health
   curl http://localhost:3004
   ```

## 🎨 Customization Guide

### 1. Brand Identity
**Colors & Styling** (`web/app/globals.css`):
```css
:root {
  --primary: your-brand-color;
  --secondary: your-accent-color;
  --background: your-bg-color;
}
```

**Logo & Images** (`web/public/`):
- Replace `logo.svg` with your logo
- Add `favicon.ico` and social sharing images
- Update `web/app/layout.tsx` metadata

**Typography** (`web/app/layout.tsx`):
```javascript
import { YourFont } from 'next/font/google'
```

### 2. Content Customization
**Homepage** (`web/app/page.tsx`):
- Update hero section copy
- Modify feature descriptions  
- Replace testimonials and social proof
- Adjust pricing tiers

**Navigation** (`web/components/header.tsx`):
- Add/remove menu items
- Update company name and links

**Footer** (`web/components/footer.tsx`):
- Add your contact information
- Update legal pages and policies

### 3. Business Configuration
**Keycloak Setup**:
1. Access http://localhost:18081 (admin/admin)
2. Configure your domain and email settings
3. Set up custom themes and branding
4. Configure social login providers

**Email Configuration** (`platform-app/src/main/resources/application.yml`):
```yaml
spring:
  mail:
    host: your-smtp-server
    username: your-email
    password: your-password
```

**Database Customization**:
- Add custom fields: `db/migrations/`
- Extend data models: `qbits/*/src/main/java/`
- Create new QBits for additional functionality

## 🏗️ Architecture Overview

```
┌─ Frontend (Next.js) ──── Port 3004
│  ├─ Public Pages (/, /blog, /pricing)
│  ├─ Auth Pages (/auth/login, /auth/register)  
│  ├─ Member Portal (/member/*)
│  └─ Admin Dashboard (/admin/*)
│
├─ API Server (Spring Boot) ──── Port 18082
│  ├─ Public API (/api/public/v1/*)
│  ├─ Member API (/api/member/v1/*)
│  └─ Staff API (/api/staff/v1/*)
│
├─ Authentication (Keycloak) ──── Port 18081
├─ Database (PostgreSQL) ──── Port 15432
├─ File Storage (MinIO) ──── Port 19001
└─ Email Testing (Mailpit) ──── Port 8025
```

## 🔧 Development Workflow

### Making Changes
1. **Frontend**: Edit files in `web/` - auto-reloads via `npm run dev`
2. **Backend**: Modify `platform-app/` or `qbits/` - rebuild with Maven
3. **Database**: Add migrations to `db/migrations/`
4. **Tests**: Update E2E tests in `web/tests/`

### Adding New Features
1. **QBit Extensions**: Create new modules in `qbits/`
2. **API Endpoints**: Extend controllers in `platform-api/`
3. **Frontend Pages**: Add to `web/app/` directory
4. **Database Schema**: Create Liquibase changesets

### Testing
```bash
# E2E tests (frontend + backend integration)
cd web && npm run test:e2e

# Backend unit tests  
./mvnw test

# Manual testing
./scripts/quick-validation.sh
```

## 🚀 Production Deployment

### Option 1: Docker Compose (Recommended)
```bash
# Production environment
cp deploy/compose/docker-compose.yml docker-compose.prod.yml
# Edit domains, SSL, and external services
docker-compose -f docker-compose.prod.yml up -d
```

### Option 2: Kubernetes
```bash
# Use provided Helm charts
helm install qqq-platform deploy/helm/
```

### Option 3: Traditional Hosting
```bash
# Build production artifacts
npm run build          # Frontend
./mvnw package          # Backend JAR
# Deploy JAR + static files to your hosting
```

### Essential Production Steps
1. **Domain & SSL**: Configure reverse proxy (Caddy/nginx)
2. **Environment Variables**: Set production secrets
3. **Database**: Use managed PostgreSQL service
4. **Email**: Configure SMTP provider (SendGrid, AWS SES)
5. **File Storage**: Use S3 or compatible service
6. **Monitoring**: Set up health checks and logging
7. **Backups**: Configure automated database backups

## 📚 Additional Resources

- **API Documentation**: http://localhost:18082/swagger-ui.html
- **QBit Development**: See `qbits/*/README.md` 
- **Testing Guide**: `tests/README.md`
- **Deployment Guide**: `deploy/README.md`
- **Troubleshooting**: `TROUBLESHOOTING.md`

## 🤖 For AI Agents

This platform is designed to be AI-agent friendly for customization and extension:

1. **Clear Module Structure**: Each component is well-separated
2. **Comprehensive Tests**: E2E tests validate all changes
3. **Type Safety**: TypeScript frontend, Java backend
4. **Standard Patterns**: Follows established conventions
5. **Documentation**: Inline comments and README files

**Agent Workflow**:
1. Understand requirements via user conversation
2. Identify affected modules (frontend/backend/database)
3. Make incremental changes with tests
4. Validate via E2E test suite
5. Deploy and verify functionality

## ⚖️ License

Apache-2.0 - See LICENSE file for details

---

**🎯 Result**: A complete, customizable business platform that you own and control, ready to be deployed and adapted to your specific business needs.