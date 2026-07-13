/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  async rewrites() {
    return [
      {
        source: '/api/public/:path*',
        destination: `${process.env.API_BASE_URL_INTERNAL || 'http://localhost:8080'}/api/public/:path*`,
      },
      {
        source: '/api/member/:path*', 
        destination: `${process.env.API_BASE_URL_INTERNAL || 'http://localhost:8080'}/api/member/:path*`,
      },
      {
        source: '/api/staff/:path*',
        destination: `${process.env.API_BASE_URL_INTERNAL || 'http://localhost:8080'}/api/staff/:path*`,
      }
    ];
  },
  async headers() {
    return [
      {
        source: '/api/:path*',
        headers: [
          {
            key: 'Cache-Control', 
            value: 'no-store, must-revalidate',
          },
        ],
      },
    ];
  },
  images: {
    domains: ['localhost', 'minio', 'storage.qrun.io'],
    formats: ['image/webp', 'image/avif'],
  },
  poweredByHeader: false,
  compress: true,
  generateEtags: false,
}

module.exports = nextConfig