# 🤖 QQQ Platform Agent Customization Guide

This guide is specifically designed for AI agents to help users customize and deploy the QQQ Business Platform.

## 🎯 Agent Objectives

When helping users customize this platform, focus on:
1. **Understanding their business needs** (industry, audience, features)
2. **Implementing changes systematically** (brand → content → functionality)
3. **Maintaining quality** (tests pass, no regressions)
4. **Ensuring deployability** (production-ready configuration)

## 🧠 Platform Knowledge for Agents

### Core Architecture Understanding
```
Frontend (Next.js) ←→ Backend API (Spring Boot) ←→ Database (PostgreSQL)
     ↓                       ↓                         ↓
Authentication (Keycloak) ← QBit Modules → Migrations (Liquibase)
```

**Key Files Agents Should Know**:
- `web/app/page.tsx` - Homepage content
- `web/app/layout.tsx` - Site-wide configuration
- `web/components/` - Reusable UI components
- `platform-app/src/main/resources/application.yml` - Backend config
- `deploy/compose/docker-compose.yml` - Infrastructure setup
- `web/tests/` - E2E validation tests

### QBit System (Extensibility Points)
Each QBit is a business capability module:
- **newsletter**: Email marketing and subscriptions
- **content**: CMS and blog management  
- **identity**: User profiles and authentication
- **crm**: Contact and relationship management

**Adding New Capabilities**: Create new QBits in `qbits/[name]/`

## 🔧 Common Customization Patterns

### Pattern 1: Brand Identity Transformation
```typescript
// 1. Update theme colors (web/app/globals.css)
:root {
  --primary: #your-brand-color;
  --logo-url: url('/your-logo.svg');
}

// 2. Replace content (web/app/page.tsx)
const heroTitle = "Your Business Value Proposition"
const description = "Your unique selling proposition"

// 3. Update metadata (web/app/layout.tsx)  
export const metadata = {
  title: "Your Business Name",
  description: "Your business description"
}
```

### Pattern 2: Feature Configuration
```yaml
# Backend feature toggles (application.yml)
platform:
  features:
    newsletter: true
    crm: true
    ecommerce: false  # Example extension
  integrations:
    payment: stripe   # Example
    analytics: plausible
```

### Pattern 3: Content Customization
```typescript
// Homepage sections (web/app/page.tsx)
const features = [
  [IconName, 'Your Feature', 'Your description'],
  // Add/modify features
]

const testimonials = [
  { quote: "Your testimonial", author: "Customer Name", role: "Their Role" }
]
```

## 🛠️ Agent Workflow for Customization

### Phase 1: Discovery & Planning
```typescript
interface CustomizationPlan {
  businessType: string;        // "consulting", "saas", "ecommerce"  
  brandIdentity: {
    colors: string[];           // Primary, secondary colors
    typography: string;         // Font selections
    logoStyle: string;          // Modern, classic, minimal
  };
  contentNeeds: {
    homepage: string[];         // Key messages, CTAs
    features: Feature[];        // Core capabilities to highlight
    testimonials: Testimonial[];
  };
  functionalityNeeds: {
    enabledQBits: string[];     // Which modules to activate
    customIntegrations: string[];  // Third-party services
    additionalPages: string[];  // About, pricing, contact
  };
}
```

### Phase 2: Implementation Order
1. **Brand Identity** (visual changes first - immediate user feedback)
2. **Content Updates** (messaging and copy)
3. **Feature Configuration** (enable/disable capabilities)
4. **Custom Functionality** (new features if needed)
5. **Production Setup** (deployment configuration)

### Phase 3: Validation Protocol
```bash
# Must pass before delivering to user
npm run test:e2e  # All E2E tests pass
npm run build     # Production build succeeds
docker-compose up -d  # Infrastructure starts correctly
curl http://localhost:3004  # Site accessible
```

## 📋 Agent Task Templates

### Task: "Make this look like a [INDUSTRY] business"
1. **Analyze industry requirements**:
   - Color schemes typical for industry
   - Messaging tone (professional, creative, technical)
   - Feature priorities (CRM for B2B, newsletter for content)

2. **Apply systematic changes**:
   ```typescript
   // Industry-specific configurations
   const industryConfigs = {
     consulting: { primary: '#2563eb', tone: 'professional', focus: 'crm' },
     creative: { primary: '#7c3aed', tone: 'inspiring', focus: 'content' },
     saas: { primary: '#059669', tone: 'technical', focus: 'newsletter' }
   }
   ```

3. **Validate changes**: Run E2E tests, verify visual consistency

### Task: "Add [FEATURE] functionality"
1. **Determine scope**:
   - New QBit module? (`qbits/feature/`)
   - Frontend component? (`web/components/feature.tsx`)
   - API endpoint? (`platform-api/controllers/`)
   - Database changes? (`db/migrations/`)

2. **Implementation pattern**:
   ```bash
   # Create backend capability
   qbits/[feature]/
   ├── src/main/java/[...]/FeatureQBitProducer.java
   ├── src/main/java/[...]/service/FeatureService.java
   └── README.md
   
   # Add API endpoints  
   platform-api/src/main/java/[...]/FeatureController.java
   
   # Create frontend interface
   web/app/[feature]/page.tsx
   web/components/[feature].tsx
   
   # Add tests
   web/tests/[feature].spec.ts
   ```

3. **Integration**: Update navigation, permissions, documentation

### Task: "Deploy to production"
1. **Environment preparation**:
   ```bash
   # Production environment file
   cp .env.example .env.production
   # Update with real domains, secrets, service endpoints
   ```

2. **Security checklist**:
   - Change default passwords (Keycloak admin)
   - Configure SSL certificates
   - Set up proper CORS origins
   - Enable production logging

3. **Deployment validation**:
   - Health checks pass
   - Authentication flows work
   - Newsletter/email integration functional
   - Performance acceptable (< 2s load times)

## 🚨 Agent Safety Guidelines

### Always Validate Changes
```bash
# Before making changes
git add -A && git commit -m "Pre-change snapshot"

# After making changes
npm run test:e2e  # Must pass
npm run build     # Must succeed

# If tests fail, investigate and fix before continuing
```

### Preserve Working Functionality  
- Never modify core QQQ framework files
- Always extend via QBit modules
- Keep authentication system intact
- Maintain API backward compatibility

### User Experience Priority
- Changes should improve, not degrade UX
- Maintain responsive design on all devices
- Ensure accessibility standards
- Keep load times under 3 seconds

## 📊 Success Metrics for Agents

### Technical Success
- ✅ All 26 E2E tests pass
- ✅ Production build completes successfully  
- ✅ No TypeScript/Java compilation errors
- ✅ Docker Compose starts all services

### User Experience Success  
- ✅ Site loads in < 3 seconds
- ✅ Mobile responsive design maintained
- ✅ Authentication flows work smoothly
- ✅ Newsletter signup functional
- ✅ Admin dashboard accessible

### Business Success
- ✅ Brand identity clearly communicated
- ✅ Value proposition evident on homepage
- ✅ Contact/conversion paths clear
- ✅ Professional appearance maintained

## 🔍 Debugging Guide for Agents

### Common Issues & Solutions

**Frontend not loading**:
```bash
cd web
rm -rf .next node_modules/.cache
npm install
npm run dev -- --port 3004
```

**Backend API errors**:
```bash
# Check logs
docker-compose logs qqq-server
# Restart with clean state
docker-compose down --volumes
docker-compose up -d
```

**Authentication not working**:
```bash
# Verify Keycloak
curl http://localhost:18081/realms/qqq-platform/.well-known/openid-configuration
# Check environment variables
cat web/.env.local
```

**Newsletter form not submitting**:
```bash
# Test backend directly
curl -X POST http://localhost:18082/api/public/v1/newsletter/subscribe \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

### When to Ask for Help
- User requests functionality outside QBit extension model
- Need to integrate with specific third-party services not documented
- Performance issues that require infrastructure changes
- Security requirements beyond standard authentication

## 📝 Documentation Standards

When making changes, always update:
1. **Inline comments** for complex logic
2. **README files** for new modules  
3. **API documentation** for new endpoints
4. **E2E tests** for new user flows
5. **This guide** for new patterns discovered

---

**🎯 Agent Success**: The user has a fully functional, professionally customized business platform that meets their specific needs and can be deployed to production with confidence.