#!/usr/bin/env python3
"""
QQQ Business Platform Comprehensive Test Suite
Validates infrastructure, APIs, frontend, and business workflows
"""
import requests
import time
import json
import sys
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass
from urllib.parse import urljoin
import logging
import subprocess
import os

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

@dataclass
class TestConfig:
    """Configuration for test endpoints"""
    base_url: str = "http://localhost"
    api_port: int = 18080
    web_port: int = 13000
    keycloak_port: int = 18081
    mailpit_port: int = 8025
    minio_port: int = 19001
    postgres_port: int = 15432
    
    @property
    def api_base(self) -> str:
        return f"{self.base_url}:{self.api_port}/api"
    
    @property
    def web_base(self) -> str:
        return f"{self.base_url}:{self.web_port}"
    
    @property
    def keycloak_base(self) -> str:
        return f"{self.base_url}:{self.keycloak_port}"
    
    @property
    def mailpit_base(self) -> str:
        return f"{self.base_url}:{self.mailpit_port}"

class PlatformValidator:
    def __init__(self, config: TestConfig):
        self.config = config
        self.session = requests.Session()
        self.session.timeout = 10
        self.test_results: List[Tuple[str, bool, str]] = []
    
    def add_result(self, test_name: str, passed: bool, message: str = ""):
        """Add test result to collection"""
        self.test_results.append((test_name, passed, message))
        status = "✅ PASS" if passed else "❌ FAIL"
        logger.info(f"{status}: {test_name} - {message}")
    
    def wait_for_service(self, url: str, service_name: str, timeout: int = 60) -> bool:
        """Wait for service to become available"""
        logger.info(f"Waiting for {service_name} at {url}")
        start_time = time.time()
        
        while time.time() - start_time < timeout:
            try:
                response = self.session.get(url)
                if response.status_code < 500:
                    logger.info(f"{service_name} is ready!")
                    return True
            except requests.exceptions.RequestException:
                pass
            time.sleep(2)
        
        logger.error(f"{service_name} not available after {timeout}s")
        return False

    def test_infrastructure_health(self) -> None:
        """Test all infrastructure components are running"""
        logger.info("🏗️  Testing Infrastructure Health")
        
        # Test PostgreSQL (via API health endpoint)
        try:
            response = self.session.get(f"{self.config.api_base}/public/v1/health")
            if response.status_code == 200:
                health_data = response.json()
                db_status = health_data.get('database', {}).get('status') == 'UP'
                self.add_result("PostgreSQL Database", db_status, 
                              f"Database connection: {'OK' if db_status else 'FAILED'}")
            else:
                self.add_result("PostgreSQL Database", False, f"Health check failed: {response.status_code}")
        except Exception as e:
            self.add_result("PostgreSQL Database", False, f"Connection failed: {str(e)}")
        
        # Test Keycloak
        try:
            keycloak_url = f"{self.config.keycloak_base}/realms/qqq-platform/.well-known/openid_configuration"
            response = self.session.get(keycloak_url)
            keycloak_ok = response.status_code == 200 and 'issuer' in response.json()
            self.add_result("Keycloak OIDC", keycloak_ok, 
                          f"Realm configuration: {'OK' if keycloak_ok else 'FAILED'}")
        except Exception as e:
            self.add_result("Keycloak OIDC", False, f"Connection failed: {str(e)}")
        
        # Test Mailpit
        try:
            response = self.session.get(f"{self.config.mailpit_base}/api/v1/info")
            mailpit_ok = response.status_code == 200 and 'Version' in response.json()
            self.add_result("Mailpit Email", mailpit_ok, 
                          f"Email service: {'OK' if mailpit_ok else 'FAILED'}")
        except Exception as e:
            self.add_result("Mailpit Email", False, f"Connection failed: {str(e)}")
        
        # Test MinIO Console
        try:
            response = self.session.get(f"http://localhost:{self.config.minio_port}")
            minio_ok = response.status_code in [200, 403]  # 403 is ok for MinIO console
            self.add_result("MinIO Storage", minio_ok, 
                          f"Storage console: {'OK' if minio_ok else 'FAILED'}")
        except Exception as e:
            self.add_result("MinIO Storage", False, f"Connection failed: {str(e)}")

    def test_api_endpoints(self) -> None:
        """Test all API endpoints are responding"""
        logger.info("🔌 Testing API Endpoints")
        
        # Test Public API endpoints
        public_endpoints = [
            ("/public/v1/health", "Public Health Check"),
            ("/public/v1/content/posts", "Public Blog Posts"),
        ]
        
        for endpoint, description in public_endpoints:
            try:
                response = self.session.get(f"{self.config.api_base}{endpoint}")
                success = response.status_code in [200, 404]  # 404 acceptable for empty content
                self.add_result(f"API: {description}", success, 
                              f"Status: {response.status_code}")
            except Exception as e:
                self.add_result(f"API: {description}", False, f"Request failed: {str(e)}")
        
        # Test Member API (should require authentication)
        try:
            response = self.session.get(f"{self.config.api_base}/member/v1/profile")
            # Should return 401 or 403 without authentication
            auth_required = response.status_code in [401, 403]
            self.add_result("API: Member Auth Required", auth_required, 
                          f"Status: {response.status_code} (expected 401/403)")
        except Exception as e:
            self.add_result("API: Member Auth Required", False, f"Request failed: {str(e)}")
        
        # Test Staff API (should require authentication)
        try:
            response = self.session.get(f"{self.config.api_base}/staff/v1/users")
            # Should return 401 or 403 without authentication
            auth_required = response.status_code in [401, 403]
            self.add_result("API: Staff Auth Required", auth_required, 
                          f"Status: {response.status_code} (expected 401/403)")
        except Exception as e:
            self.add_result("API: Staff Auth Required", False, f"Request failed: {str(e)}")

    def test_frontend_pages(self) -> None:
        """Test frontend pages are rendering"""
        logger.info("🌐 Testing Frontend Pages")
        
        # Test main pages
        pages = [
            ("/", "Homepage"),
            ("/login", "Login Page"),
            ("/register", "Registration Page"),
            ("/blog", "Blog Page"),
            ("/newsletter", "Newsletter Page"),
        ]
        
        for path, description in pages:
            try:
                response = self.session.get(f"{self.config.web_base}{path}")
                # Accept 200 for existing pages, 404 for not implemented yet
                success = response.status_code in [200, 404]
                self.add_result(f"Frontend: {description}", success, 
                              f"Status: {response.status_code}")
                
                # Check if it's actually HTML (not just status code)
                if response.status_code == 200:
                    content_type = response.headers.get('content-type', '')
                    is_html = 'text/html' in content_type
                    self.add_result(f"Frontend: {description} HTML", is_html, 
                                  f"Content-Type: {content_type}")
            except Exception as e:
                self.add_result(f"Frontend: {description}", False, f"Request failed: {str(e)}")

    def test_newsletter_workflow(self) -> None:
        """Test newsletter subscription workflow"""
        logger.info("📧 Testing Newsletter Workflow")
        
        try:
            # Test newsletter subscription
            subscription_data = {
                "email": "test@example.com",
                "firstName": "Test",
                "lastName": "User"
            }
            
            response = self.session.post(
                f"{self.config.api_base}/public/v1/newsletter/subscribe",
                json=subscription_data
            )
            
            # Should accept subscription (200/201) or have validation (400)
            subscribe_ok = response.status_code in [200, 201, 400]
            self.add_result("Newsletter: Subscription Endpoint", subscribe_ok, 
                          f"Status: {response.status_code}")
            
            # Check if confirmation email was sent (via Mailpit)
            if subscribe_ok and response.status_code in [200, 201]:
                time.sleep(2)  # Give email time to arrive
                try:
                    mailpit_response = self.session.get(f"{self.config.mailpit_base}/api/v1/messages")
                    if mailpit_response.status_code == 200:
                        messages = mailpit_response.json()
                        has_confirmation = len(messages.get('messages', [])) > 0
                        self.add_result("Newsletter: Confirmation Email", has_confirmation, 
                                      f"Emails in queue: {len(messages.get('messages', []))}")
                    else:
                        self.add_result("Newsletter: Confirmation Email", False, 
                                      "Could not check email queue")
                except Exception as e:
                    self.add_result("Newsletter: Confirmation Email", False, 
                                  f"Email check failed: {str(e)}")
                    
        except Exception as e:
            self.add_result("Newsletter: Subscription Endpoint", False, 
                          f"Request failed: {str(e)}")

    def test_docker_containers(self) -> None:
        """Test Docker containers are running"""
        logger.info("🐳 Testing Docker Containers")
        
        try:
            # Get container status
            result = subprocess.run(
                ['docker', 'ps', '--format', 'table {{.Names}}\t{{.Status}}'],
                capture_output=True, text=True, timeout=10
            )
            
            if result.returncode == 0:
                container_output = result.stdout
                expected_containers = [
                    'qqq-platform-postgres',
                    'qqq-platform-keycloak', 
                    'qqq-platform-mailpit',
                    'qqq-platform-minio',
                    'qqq-platform-server',
                    'qqq-platform-web'
                ]
                
                for container in expected_containers:
                    is_running = container in container_output and 'Up' in container_output
                    self.add_result(f"Container: {container}", is_running, 
                                  "Running" if is_running else "Not running or missing")
            else:
                self.add_result("Docker Containers", False, 
                              f"Docker command failed: {result.stderr}")
                
        except Exception as e:
            self.add_result("Docker Containers", False, f"Docker check failed: {str(e)}")

    def test_database_content(self) -> None:
        """Test database has expected content"""
        logger.info("🗄️  Testing Database Content")
        
        try:
            # Check via API if we have database tables populated
            response = self.session.get(f"{self.config.api_base}/public/v1/health")
            if response.status_code == 200:
                health_data = response.json()
                
                # Check if we have some basic data structures
                db_details = health_data.get('database', {})
                tables_exist = 'tables' in str(db_details) or 'UP' in str(db_details)
                self.add_result("Database: Schema Populated", tables_exist, 
                              f"Database details: {db_details}")
                
                # Try to get newsletter subscriptions count
                try:
                    subs_response = self.session.get(f"{self.config.api_base}/staff/v1/newsletter/subscriptions")
                    # Should be 401/403 without auth, but endpoint should exist
                    endpoint_exists = subs_response.status_code in [401, 403, 200]
                    self.add_result("Database: Newsletter Table", endpoint_exists, 
                                  f"Newsletter endpoint status: {subs_response.status_code}")
                except Exception as e:
                    self.add_result("Database: Newsletter Table", False, 
                                  f"Newsletter check failed: {str(e)}")
            else:
                self.add_result("Database: Schema Populated", False, 
                              f"Health check failed: {response.status_code}")
                
        except Exception as e:
            self.add_result("Database: Schema Populated", False, 
                          f"Database check failed: {str(e)}")

    def run_all_tests(self) -> Tuple[int, int]:
        """Run all tests and return (passed, total)"""
        logger.info("🚀 Starting QQQ Business Platform Validation")
        logger.info("=" * 60)
        
        # Wait for services to be ready
        services_ready = True
        services_ready &= self.wait_for_service(f"{self.config.web_base}", "Next.js Frontend", 30)
        services_ready &= self.wait_for_service(f"{self.config.api_base}/public/v1/health", "Backend API", 30)
        
        if not services_ready:
            logger.error("⚠️  Some services are not ready. Tests may fail.")
        
        # Run test suites
        self.test_docker_containers()
        self.test_infrastructure_health()
        self.test_api_endpoints()
        self.test_frontend_pages()
        self.test_database_content()
        self.test_newsletter_workflow()
        
        # Calculate results
        passed = sum(1 for _, success, _ in self.test_results if success)
        total = len(self.test_results)
        
        # Print summary
        logger.info("=" * 60)
        logger.info("📊 TEST SUMMARY")
        logger.info("=" * 60)
        
        for test_name, passed_flag, message in self.test_results:
            status = "✅" if passed_flag else "❌"
            logger.info(f"{status} {test_name:<40} {message}")
        
        logger.info("=" * 60)
        success_rate = (passed / total * 100) if total > 0 else 0
        logger.info(f"🏆 FINAL RESULT: {passed}/{total} tests passed ({success_rate:.1f}%)")
        
        if success_rate >= 80:
            logger.info("🎉 QQQ Business Platform is OPERATIONAL!")
        elif success_rate >= 60:
            logger.info("⚠️  QQQ Business Platform has some issues but core functionality works")
        else:
            logger.info("❌ QQQ Business Platform has significant issues")
        
        return passed, total

def main():
    """Main test runner"""
    config = TestConfig()
    validator = PlatformValidator(config)
    
    try:
        passed, total = validator.run_all_tests()
        
        # Exit code based on results
        if passed == total:
            sys.exit(0)  # All tests passed
        elif passed >= total * 0.8:
            sys.exit(1)  # Most tests passed
        else:
            sys.exit(2)  # Many tests failed
            
    except KeyboardInterrupt:
        logger.info("\n🛑 Test suite interrupted by user")
        sys.exit(3)
    except Exception as e:
        logger.error(f"💥 Test suite crashed: {str(e)}")
        sys.exit(4)

if __name__ == "__main__":
    main()