import { NextRequest, NextResponse } from 'next/server'

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    
    // Validate email format
    if (!body.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(body.email)) {
      return NextResponse.json(
        { error: 'Invalid email format' },
        { status: 400 }
      )
    }

    // Call backend API from server-side (no CORS issues)
    const backendUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:18082'
    const response = await fetch(`${backendUrl}/api/public/v1/newsletter/subscribe`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email: body.email }),
    })

    if (!response.ok) {
      console.error('Backend error:', response.status, await response.text())
      return NextResponse.json(
        { error: 'Subscription failed. Please try again.' },
        { status: 500 }
      )
    }

    const result = await response.json()
    return NextResponse.json(result)
    
  } catch (error) {
    console.error('Newsletter subscription error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}