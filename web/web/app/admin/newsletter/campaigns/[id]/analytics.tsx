// web/app/admin/newsletter/campaigns/[id]/analytics.tsx
'use client'

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import toast from 'react-hot-toast';
import { Card } from '@/components/page-shell';
import { newsletterApi } from '@/lib/newsletter-api'; // Assume this has analytics functions
import { MailOpen, Link2, Users, TrendingUp } from 'lucide-react';

interface CampaignAnalytics {
  campaignId: string;
  subject: string;
  sentAt: string;
  openRate?: number; // Percentage
  clickRate?: number; // Percentage
  uniqueClicks?: number;
  delivered?: number;
  bounced?: number;
  unsubscribed?: number;
  // Add other relevant metrics
}

export default function CampaignAnalyticsPage() {
  const params = useParams();
  const campaignId = params.id as string;
  const [analytics, setAnalytics] = useState<CampaignAnalytics | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!campaignId) return;

    const fetchAnalytics = async () => {
      setLoading(true);
      try {
        // TODO: Replace with actual API call to fetch analytics for a specific campaign
        // const fetchedAnalytics = await newsletterApi.getAnalytics(campaignId);
        const fetchedAnalytics: CampaignAnalytics = {
          campaignId: campaignId,
          subject: 'Sample Campaign Subject',
          sentAt: new Date().toISOString(),
          openRate: 25.5,
          clickRate: 5.2,
          uniqueClicks: 150,
          delivered: 1000,
          bounced: 20,
          unsubscribed: 5,
        };
        setAnalytics(fetchedAnalytics);
      } catch (err) {
        setError(`Failed to load analytics for campaign ${campaignId}.`);
        toast.error('Failed to load analytics.');
        console.error('Error fetching analytics:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, [campaignId]);

  if (loading) return <div className="container mx-auto p-4">Loading analytics...</div>;
  if (error) return <div className="container mx-auto p-4 text-red-500">{error}</div>;
  if (!analytics) return <div className="container mx-auto p-4">No analytics data available.</div>;

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-6">Campaign Analytics: {analytics.subject}</h1>
      <p className="text-sm text-gray-500 mb-6">Sent on: {new Date(analytics.sentAt).toLocaleString()}</p>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Card className="flex items-center justify-between p-6">
          <div>
            <div className="text-sm font-medium text-gray-500 truncate">Delivered</div>
            <div className="text-3xl font-bold">{analytics.delivered ?? 0}</div>
          </div>
          <Users className="h-8 w-8 text-blue-500" />
        </Card>
        
        <Card className="flex items-center justify-between p-6">
          <div>
            <div className="text-sm font-medium text-gray-500 truncate">Open Rate</div>
            <div className="text-3xl font-bold">{analytics.openRate?.toFixed(1) ?? 0}%</div>
          </div>
          <MailOpen className="h-8 w-8 text-green-500" />
        </Card>

        <Card className="flex items-center justify-between p-6">
          <div>
            <div className="text-sm font-medium text-gray-500 truncate">Click Rate</div>
            <div className="text-3xl font-bold">{analytics.clickRate?.toFixed(1) ?? 0}%</div>
          </div>
          <Link2 className="h-8 w-8 text-purple-500" />
        </Card>

        <Card className="flex items-center justify-between p-6">
          <div>
            <div className="text-sm font-medium text-gray-500 truncate">Unique Clicks</div>
            <div className="text-3xl font-bold">{analytics.uniqueClicks ?? 0}</div>
          </div>
          <TrendingUp className="h-8 w-8 text-orange-500" />
        </Card>

        <Card className="flex items-center justify-between p-6">
          <div>
            <div className="text-sm font-medium text-gray-500 truncate">Bounced</div>
            <div className="text-3xl font-bold">{analytics.bounced ?? 0}</div>
          </div>
          <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-red-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2 L0 20 24 20 12 2zM12 7v6 M12 18h.01"/></svg>
        </Card>

        <Card className="flex items-center justify-between p-6">
          <div>
            <div className="text-sm font-medium text-gray-500 truncate">Unsubscribed</div>
            <div className="text-3xl font-bold">{analytics.unsubscribed ?? 0}</div>
          </div>
           <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-gray-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-1a2 2 0 0 1 2-2h1a2 2 0 0 1 2 2z"/><path d="M21 15H3"/><path d="M11 8V3a1 1 0 0 0-1-1H9a1 1 0 0 0-1 1v5"/><path d="M3 12v9a2 2 0 0 0 2 2h7"/></svg>
        </Card>
      </div>

      {/* TODO: Add more detailed charts or tables for click tracking, etc. */}
    </div>
  );
}
