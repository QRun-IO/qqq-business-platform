// web/app/admin/newsletter/subscribers/page.tsx
'use client'

import { useState, useEffect, useMemo } from 'react';
import toast from 'react-hot-toast';
import { Card } from '@/components/page-shell';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { newsletterApi } from '@/lib/newsletter-api'; // Assume this has subscriber functions
import { Search, Filter, Edit, Trash } from 'lucide-react';

interface Subscriber {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  status: 'pending' | 'confirmed' | 'unsubscribed' | 'suppressed';
  subscribedAt?: string;
  confirmedAt?: string;
  unsubscribedAt?: string;
  // Add other relevant fields like segments, source, etc.
}

export default function SubscribersList() {
  const [subscribers, setSubscribers] = useState<Subscriber[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [filterStatus, setFilterStatus] = useState<string>(''); // e.g., 'confirmed', 'unsubscribed'

  useEffect(() => {
    const fetchSubscribers = async () => {
      setLoading(true);
      try {
        // TODO: Replace with actual API call to fetch subscribers
        // const fetchedSubscribers = await newsletterApi.getSubscribers();
        const fetchedSubscribers: Subscriber[] = [
          { id: 'sub-1', email: 'alice@example.com', firstName: 'Alice', status: 'confirmed', subscribedAt: '2023-01-15T10:00:00Z', confirmedAt: '2023-01-15T10:05:00Z' },
          { id: 'sub-2', email: 'bob@example.com', lastName: 'Smith', status: 'unsubscribed', subscribedAt: '2023-02-20T11:00:00Z', unsubscribedAt: '2023-03-10T14:30:00Z' },
          { id: 'sub-3', email: 'charlie@sample.net', status: 'confirmed', subscribedAt: '2023-03-01T09:00:00Z', confirmedAt: '2023-03-01T09:02:00Z' },
          { id: 'sub-4', email: 'david@test.org', status: 'pending', subscribedAt: '2023-04-05T16:00:00Z' },
          { id: 'sub-5', email: 'eve@mail.com', firstName: 'Eve', status: 'suppressed', subscribedAt: '2023-05-10T08:00:00Z' },
        ];
        setSubscribers(fetchedSubscribers);
      } catch (err) {
        setError('Failed to load subscribers.');
        toast.error('Failed to load subscribers.');
        console.error('Error fetching subscribers:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSubscribers();
  }, []);

  const filteredSubscribers = useMemo(() => {
    return subscribers.filter((subscriber) => {
      const emailMatch = subscriber.email.toLowerCase().includes(searchTerm.toLowerCase());
      const statusMatch = filterStatus ? subscriber.status === filterStatus : true;
      return emailMatch && statusMatch;
    });
  }, [subscribers, searchTerm, filterStatus]);

  const handleEdit = (subscriberId: string) => {
    toast.warn(`Edit functionality for subscriber ${subscriberId} not implemented yet.`);
    // TODO: Implement edit subscriber modal or navigation
  };

  const handleDelete = (subscriberId: string) => {
    // In a real app, this would involve a confirmation modal
    toast('Deleting subscriber (mock action)...', { icon: '🔥' });
    setSubscribers(subscribers.filter(sub => sub.id !== subscriberId));
    // TODO: Call API to delete subscriber: newsletterApi.deleteSubscriber(subscriberId);
    toast.success('Subscriber deleted (mock action).');
  };
  
  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterStatus(e.target.value);
  };

  const availableStatuses = ['confirmed', 'unsubscribed', 'pending', 'suppressed'];

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-6">Subscriber Management</h1>

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
        <div className="relative w-full md:w-1/3">
          <Input
            type="text"
            placeholder="Search by email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
        </div>
        
        <div>
          <Label htmlFor="statusFilter" className="mr-2">Filter by Status:</Label>
          <select
            id="statusFilter"
            value={filterStatus}
            onChange={handleFilterChange}
            className="p-2 border rounded bg-white"
          >
            <option value="">All Statuses</option>
            {availableStatuses.map(status => (
              <option key={status} value={status}>{status.charAt(0).toUpperCase() + status.slice(1)}</option>
            ))}
          </select>
        </div>

        {/* TODO: Add import/export buttons and segment management */}
        <div className='flex gap-2'>
          <Button variant="outline" onClick={() => toast('Import subscribers not implemented.')}>
            Import
          </Button>
          <Button variant="outline" onClick={() => toast('Export subscribers not implemented.')}>
            Export
          </Button>
          <Button onClick={() => toast('Segment management not implemented.')}>
            Manage Segments
          </Button>
        </div>
      </div>

      {loading && <p>Loading subscribers...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && (
        <Card className="overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Subscribed</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredSubscribers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-4 whitespace-nowrap text-center text-gray-500">
                    No subscribers found matching your criteria.
                  </td>
                </tr>
              ) : (
                filteredSubscribers.map((subscriber) => (
                  <tr key={subscriber.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{subscriber.email}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {(subscriber.firstName || '') + ' ' + (subscriber.lastName || '')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full capitalize ${
                        subscriber.status === 'confirmed' ? 'bg-green-100 text-green-800' :
                        subscriber.status === 'unsubscribed' ? 'bg-red-100 text-red-800' :
                        subscriber.status === 'pending' ? 'bg-yellow-100 text-yellow-800' :
                        subscriber.status === 'suppressed' ? 'bg-gray-100 text-gray-800' : ''
                      }`}>
                        {subscriber.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {subscriber.subscribedAt ? new Date(subscriber.subscribedAt).toLocaleDateString() : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                      <Button variant="ghost" size="sm" onClick={() => handleEdit(subscriber.id)} className="text-blue-600 hover:text-blue-900">
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="sm" onClick={() => handleDelete(subscriber.id)} className="text-red-600 hover:text-red-900">
                        <Trash className="h-4 w-4" />
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
