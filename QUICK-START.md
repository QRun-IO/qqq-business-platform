# ⚡ 5-Minute Quick Start Guide

Get your QQQ Business Platform running in 5 minutes with this step-by-step guide.

## 🚀 Prerequisites (30 seconds)

Make sure you have:
- [Docker Desktop](https://docs.docker.com/get-docker/) installed and running
- [Git](https://git-scm.com/downloads) installed
- Terminal/Command Prompt access

## 📦 Step 1: Get the Platform (1 minute)

```bash
# Clone the repository
git clone [your-repository-url] my-business-platform
cd my-business-platform

# Verify you have the required files
ls deploy/compose/docker-compose.yml  # Should exist
```

## 🔧 Step 2: Start Everything (2 minutes)

```bash
# Start all services with one command
docker-compose -f deploy/compose/docker-compose.yml up -d

# Watch the startup progress (optional)
docker-compose -f deploy/compose/docker-compose.yml logs -f
```

**What's starting:**
- PostgreSQL database
- Keycloak authentication server  
- MinIO file storage
- Mailpit email testing
- QQQ API server
- Database migrations

## ⏰ Step 3: Wait for Ready (90 seconds)

The platform takes about 90 seconds to fully initialize. You can check progress:

```bash
# Check if all services are healthy
docker-compose -f deploy/compose/docker-compose.yml ps

# Test the API (should return {"status":"UP"})
curl http://localhost:18082/api/public/v1/health
```

## 🌐 Step 4: Start the Frontend (1 minute)

```bash
# Navigate to the web directory
cd web

# Install dependencies (first time only)
npm install

# Start the development server
npm run dev -- --port 3004
```

## ✅ Step 5: Access Your Platform (30 seconds)

Open your browser to: **http://localhost:3004**

You should see a beautiful business platform homepage!

### Test Key Features:
1. **Newsletter Signup**: Try the email form at the bottom
2. **Authentication**: Click "Sign in" to see login flow  
3. **Admin Access**: Go to http://localhost:3004/admin
4. **API Documentation**: Visit http://localhost:18082/swagger-ui.html

## 🎯 Success Checklist

- [ ] Homepage loads at http://localhost:3004
- [ ] Newsletter form accepts email addresses
- [ ] "Sign in" redirects to authentication page
- [ ] Admin area requires login (/admin)
- [ ] API health check returns "UP" status

## 🆘 Troubleshooting

### "Cannot connect" errors:
```bash
# Restart all services
docker-compose -f deploy/compose/docker-compose.yml down
docker-compose -f deploy/compose/docker-compose.yml up -d
```

### "Port already in use":
```bash
# Check what's using the ports
netstat -tulpn | grep :3004  # Frontend
netstat -tulpn | grep :18082  # Backend

# Kill processes or use different ports
npm run dev -- --port 3005  # Use port 3005 instead
```

### Frontend shows errors:
```bash
cd web
rm -rf .next node_modules/.cache
npm install
npm run dev -- --port 3004
```

### Database connection issues:
```bash
# Reset the database
docker-compose -f deploy/compose/docker-compose.yml down --volumes
docker-compose -f deploy/compose/docker-compose.yml up -d
```

## 📱 What's Next?

Your platform is now running! Here are immediate next steps:

### 1. Customize Your Brand (10 minutes)
- Edit `web/app/page.tsx` for homepage content
- Update colors in `web/app/globals.css`  
- Replace logo in `web/public/`

### 2. Configure Authentication
- Access Keycloak: http://localhost:18081 (admin/admin)
- Create your admin user account
- Set up your organization details

### 3. Test Email Features  
- Check Mailpit: http://localhost:8025
- Send test newsletters
- Verify signup confirmations

### 4. Explore Admin Features
- User management at http://localhost:3004/admin
- Content management system
- Analytics and reporting

## 🚀 Ready for Production?

When you're ready to deploy:

1. **Get a domain name** and point it to your server
2. **Set up SSL** (Let's Encrypt recommended)
3. **Configure email** (SendGrid, Mailgun, etc.)
4. **Update environment variables** for production
5. **Run on a VPS** (DigitalOcean, AWS, etc.)

See the full `README.md` for detailed production deployment instructions.

---

**🎉 Congratulations!** You now have a full-featured business platform running locally. This is the same foundation used by professional businesses - customize it to make it yours!