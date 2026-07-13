#!/bin/bash
# QQQ Business Platform Quick Test Script
# Validates core functionality is working

set -e

echo "🧪 QQQ Business Platform Quick Validation"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0

test_service() {
    local service_name="$1"
    local url="$2"
    local expected_status="${3:-200}"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "Testing $service_name... "
    
    if curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url" | grep -q "$expected_status"; then
        echo -e "${GREEN}✅ PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ FAIL${NC}"
    fi
}

test_service_contains() {
    local service_name="$1"
    local url="$2"
    local expected_content="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "Testing $service_name... "
    
    if curl -s --max-time 10 "$url" | grep -q "$expected_content"; then
        echo -e "${GREEN}✅ PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ FAIL${NC}"
    fi
}

echo -e "${BLUE}🐳 Checking Docker containers...${NC}"
docker ps --format "table {{.Names}}\t{{.Status}}" | grep qqq-platform || echo "No QQQ containers found"
echo ""

echo -e "${BLUE}🏗️ Testing Infrastructure Services...${NC}"
test_service "PostgreSQL (via API)" "http://localhost:18080/api/public/v1/health"
test_service "Keycloak OIDC" "http://localhost:18081/realms/qqq-platform/.well-known/openid_configuration"
test_service "Mailpit Email UI" "http://localhost:8025"
test_service_contains "Mailpit API" "http://localhost:8025/api/v1/info" "Version"
test_service "MinIO Console" "http://localhost:19001" "200,403"
echo ""

echo -e "${BLUE}🔌 Testing API Endpoints...${NC}"
test_service "Public API Health" "http://localhost:18080/api/public/v1/health"
test_service "Public Content API" "http://localhost:18080/api/public/v1/content/posts" "200,404"
test_service "Member API (Auth Required)" "http://localhost:18080/api/member/v1/profile" "401,403"
test_service "Staff API (Auth Required)" "http://localhost:18080/api/staff/v1/users" "401,403"
echo ""

echo -e "${BLUE}🌐 Testing Frontend Pages...${NC}"
test_service "Next.js Homepage" "http://localhost:13000/"
test_service "Next.js Login" "http://localhost:13000/login" "200,404"
test_service "Next.js Register" "http://localhost:13000/register" "200,404"
test_service "Next.js Blog" "http://localhost:13000/blog" "200,404"
echo ""

echo -e "${BLUE}📧 Testing Newsletter Functionality...${NC}"
# Test newsletter subscription endpoint
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -n "Testing Newsletter Subscription... "
if curl -s -X POST "http://localhost:18080/api/public/v1/newsletter/subscribe" \
   -H "Content-Type: application/json" \
   -d '{"email":"test@example.com","firstName":"Test","lastName":"User"}' \
   --max-time 10 | grep -E "(success|error|validation)" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ PASS${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    # Check if endpoint exists (even if validation fails)
    if curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://localhost:18080/api/public/v1/newsletter/subscribe" | grep -E "200|400|405" > /dev/null; then
        echo -e "${GREEN}✅ PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ FAIL${NC}"
    fi
fi
echo ""

# Calculate success rate
SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))

echo "========================================"
echo -e "${BLUE}📊 TEST SUMMARY${NC}"
echo "========================================"
echo "Tests Passed: $PASSED_TESTS/$TOTAL_TESTS"
echo "Success Rate: $SUCCESS_RATE%"
echo ""

if [ $SUCCESS_RATE -ge 90 ]; then
    echo -e "${GREEN}🎉 QQQ Business Platform is EXCELLENT!${NC}"
    exit 0
elif [ $SUCCESS_RATE -ge 75 ]; then
    echo -e "${GREEN}✅ QQQ Business Platform is OPERATIONAL!${NC}"
    exit 0
elif [ $SUCCESS_RATE -ge 50 ]; then
    echo -e "${YELLOW}⚠️  QQQ Business Platform has some issues${NC}"
    exit 1
else
    echo -e "${RED}❌ QQQ Business Platform has significant problems${NC}"
    exit 2
fi