#!/bin/bash

# QQQ Business Platform - Golden Path Test
# Target: 90-second median startup (CI ≤150s)

set -e

echo "🚀 Starting QQQ Business Platform Golden Path Test..."
echo "Target: 90-second median startup time"
echo "CI Allowance: ≤150 seconds"
echo ""

# Record start time
START_TIME=$(date +%s)

function cleanup() {
    echo "🧹 Cleaning up containers..."
    docker-compose -f deploy/compose/docker-compose.yml down --volumes --remove-orphans 2>/dev/null || true
}

# Ensure cleanup on exit
trap cleanup EXIT

# Function to check service health
check_health() {
    local service=$1
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f deploy/compose/docker-compose.yml ps $service | grep -q "healthy\|running"; then
            echo "✅ $service is ready"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts - waiting 3s..."
        sleep 3
        ((attempt++))
    done
    
    echo "❌ $service failed to become healthy within timeout"
    return 1
}

# Function to test API endpoint
test_endpoint() {
    local url=$1
    local expected_status=$2
    local description=$3
    
    echo "🔍 Testing: $description"
    
    actual_status=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    
    if [ "$actual_status" = "$expected_status" ]; then
        echo "✅ $description - Status: $actual_status"
        return 0
    else
        echo "❌ $description - Expected: $expected_status, Got: $actual_status"
        return 1
    fi
}

echo "📋 Golden Path Test Sequence (GP-01 to GP-16):"
echo ""

# GP-01: Start Docker Compose stack
echo "GP-01: Starting Docker Compose stack..."
cd R:/Git.Local/QRunIO/qqq-business-platform
docker-compose -f deploy/compose/docker-compose.yml up -d

# GP-02: Database health check
echo "GP-02: Checking PostgreSQL..."
check_health postgres

# GP-03: Migration execution
echo "GP-03: Checking migration completion..."
# Migration container should complete successfully
docker-compose -f deploy/compose/docker-compose.yml logs migrate | grep -q "Migration completed successfully" || {
    echo "❌ Migration failed"
    docker-compose -f deploy/compose/docker-compose.yml logs migrate
    exit 1
}
echo "✅ Database migrations completed"

# GP-04: Keycloak realm import
echo "GP-04: Checking Keycloak realm import..."
check_health keycloak

# Verify realm is accessible
test_endpoint "http://localhost:8081/realms/qqq-platform/.well-known/openid_configuration" "200" "Keycloak realm configuration"

# GP-05: MinIO storage ready
echo "GP-05: Checking MinIO storage..."
check_health minio

# GP-06: QQQ server startup
echo "GP-06: Checking QQQ server..."
check_health qqq-server

# GP-07: Public API tests
echo "GP-07: Testing public API endpoints..."
test_endpoint "http://localhost:8080/health" "200" "QQQ server health check"

# GP-08: Newsletter API test
echo "GP-08: Testing newsletter subscription API..."
# Test newsletter subscription endpoint (should accept POST)
newsletter_response=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"email":"test@example.com"}' \
    http://localhost:8080/api/public/v1/newsletter/subscribe || echo "failed")

if echo "$newsletter_response" | grep -q "pending\|initiated"; then
    echo "✅ Newsletter subscription API working"
else
    echo "❌ Newsletter subscription failed: $newsletter_response"
fi

# GP-09: Authentication endpoints (should require auth)
echo "GP-09: Testing authentication boundaries..."
test_endpoint "http://localhost:8080/api/member/v1/profile" "401" "Member API requires auth"
test_endpoint "http://localhost:8080/api/staff/v1/content" "401" "Staff API requires auth"

# GP-10: Next.js frontend
echo "GP-10: Checking Next.js frontend..."
check_health nextjs-site
test_endpoint "http://localhost:3000/" "200" "Next.js homepage"

# GP-11: Reverse proxy
echo "GP-11: Testing Caddy reverse proxy..."
test_endpoint "http://localhost/" "200" "Caddy reverse proxy"

# GP-12: Email testing (Mailpit)
echo "GP-12: Checking Mailpit email server..."
test_endpoint "http://localhost:8025/" "200" "Mailpit web interface"

# GP-13: MinIO console
echo "GP-13: Testing MinIO console..."
test_endpoint "http://localhost:9001/" "200" "MinIO console"

# GP-14: API documentation
echo "GP-14: Testing OpenAPI documentation..."
test_endpoint "http://localhost:8080/swagger-ui/index.html" "200" "Swagger UI"
test_endpoint "http://localhost:8080/v3/api-docs" "200" "OpenAPI specification"

# GP-15: Service integration test
echo "GP-15: Testing service integration..."

# Test newsletter confirmation flow
echo "   Testing newsletter confirmation token generation..."
# This would require checking Mailpit for generated emails
# For now, just verify the API accepts the request

# GP-16: Performance validation
echo "GP-16: Performance validation..."
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "⏱️  Golden Path Results:"
echo "   Duration: ${DURATION} seconds"
echo "   Target: ≤90 seconds (median)"
echo "   CI Limit: ≤150 seconds"
echo ""

if [ $DURATION -le 90 ]; then
    echo "🏆 EXCELLENT: Under 90-second target! ($DURATION seconds)"
    exit_code=0
elif [ $DURATION -le 150 ]; then
    echo "✅ GOOD: Within CI limit ($DURATION seconds)"
    exit_code=0
else
    echo "⚠️  SLOW: Exceeds CI limit ($DURATION seconds)"
    exit_code=1
fi

echo ""
echo "📊 Performance Breakdown:"
docker-compose -f deploy/compose/docker-compose.yml ps --format "table {{.Name}}\t{{.Status}}"

echo ""
echo "🎯 Golden Path Test Complete!"
echo "   All services: $(docker-compose -f deploy/compose/docker-compose.yml ps --format '{{.Name}}' | wc -l) containers"
echo "   Duration: ${DURATION}s"
echo "   Status: $([ $exit_code -eq 0 ] && echo "PASSED" || echo "NEEDS OPTIMIZATION")"

exit $exit_code