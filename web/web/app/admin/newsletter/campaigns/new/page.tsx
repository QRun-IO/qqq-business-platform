// web/app/admin/newsletter/campaigns/new/page.tsx
'use client'

import { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import toast from 'react-hot-toast';
import { NewsletterEditor } from '@/components/newsletter-editor';
import { TemplateSelector } from '@/components/template-selector';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { newsletterApi } from '@/lib/newsletter-api';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';
import { DatePicker } from '@/components/ui/date-picker'; // Assuming a DatePicker component exists

type CampaignFormValues = {
  subject: string;
  senderName: string;
  senderEmail: string;
  templateId?: string;
  content: string;
  scheduleDate?: Date | null;
  scheduleTime?: Date | null;
};

const DEFAULT_TEMPLATE_CONTENT = '<p>This is the default newsletter content.</p>';

export default function NewCampaign() {
  const { handleSubmit, control, setValue, watch, formState: { errors, isSubmitting } } = useForm<CampaignFormValues>({
    defaultValues: {
      subject: '',
      senderName: '',
      senderEmail: '',
      templateId: 'default',
      content: DEFAULT_TEMPLATE_CONTENT,
      scheduleDate: undefined,
      scheduleTime: undefined,
    },
  });

  const [currentTemplateContent, setCurrentTemplateContent] = useState<string>(DEFAULT_TEMPLATE_CONTENT);
  const currentTemplateId = watch('templateId');
  const scheduleDate = watch('scheduleDate');
  const scheduleTime = watch('scheduleTime');

  const handleTemplateSelect = (templateId: string, templateContent: string) => {
    setValue('templateId', templateId);
    setValue('content', templateContent);
    setCurrentTemplateContent(templateContent);
    toast.info(`Template "${templateId}" selected.`);
  };

  const combineDateTime = (): Date | null => {
    if (!scheduleDate || !scheduleTime) return null;

    const year = scheduleDate.getFullYear();
    const month = scheduleDate.getMonth();
    const day = scheduleDate.getDate();
    const hours = scheduleTime.getHours();
    const minutes = scheduleTime.getMinutes();
    
    const combinedDate = new Date(year, month, day, hours, minutes);
    return combinedDate;
  };

  const onSubmit = async (data: CampaignFormValues) => {
    const scheduledDateTime = combineDateTime();

    const campaignData = {
      ...data,
      scheduleDate: scheduledDateTime ? scheduledDateTime.toISOString() : null,
      // Here you might also want to send the content directly from the editor state
      content: currentTemplateContent, 
    };

    try {
      console.log('Submitting campaign data:', campaignData);
      // TODO: Integrate with backend API to create campaign
      // Use newsletterApi.createCampaign(campaignData)
      await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate API call
      toast.success('Campaign created successfully!');
      // Redirect or show success message
    } catch (error: any) {
      toast.error(error.message || 'Failed to create campaign.');
    }
  };

  return (
    <div className="container mx-auto p-4">
      <div className="flex items-center mb-6">
        <Button asChild variant="ghost" size="icon">
          <Link href="/admin/newsletter/campaigns">
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <h1 className="text-3xl font-bold ml-4">Create New Campaign</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Subject */}
          <div>
            <Label htmlFor="subject">Subject</Label>
            <Controller
              name="subject"
              control={control}
              rules={{ required: 'Subject is required' }}
              render={({ field }) => (
                <Input id="subject" placeholder="Campaign Subject" {...field} aria-invalid={!!errors.subject} />
              )}
            />
            {errors.subject && <p className="text-red-500 text-sm mt-1">{errors.subject.message}</p>}
          </div>
          
          {/* Sender Name */}
          <div>
            <Label htmlFor="senderName">Sender Name</Label>
            <Controller
              name="senderName"
              control={control}
              rules={{ required: 'Sender name is required' }}
              render={({ field }) => (
                <Input id="senderName" placeholder="Your Name / Company Name" {...field} aria-invalid={!!errors.senderName} />
              )}
            />
            {errors.senderName && <p className="text-red-500 text-sm mt-1">{errors.senderName.message}</p>}
          </div>

          {/* Sender Email */}
          <div className="md:col-span-2">
            <Label htmlFor="senderEmail">Sender Email</Label>
            <Controller
              name="senderEmail"
              control={control}
              rules={{ required: 'Sender email is required', pattern: { value: /.+@.+\..+/, message: 'Invalid email format' } }}
              render={({ field }) => (
                <Input id="senderEmail" placeholder="your.email@example.com" {...field} aria-invalid={!!errors.senderEmail} />
              )}
            />
            {errors.senderEmail && <p className="text-red-500 text-sm mt-1">{errors.senderEmail.message}</p>}
          </div>
        </div>

        <div className="space-y-4">
          <TemplateSelector 
             onSelectTemplate={handleTemplateSelect} 
             initialTemplateId={currentTemplateId} // Pass current ID to keep selection consistent
          />
        </div>

        <div className="space-y-4">
          <Label htmlFor="content">Content</Label>
          <Controller
            name="content"
            control={control}
            rules={{ required: 'Content is required' }}
            render={({ field }) => (
              <NewsletterEditor
                initialContent={field.value} 
                onChange={(newContent: string) => {
                  field.onChange(newContent); // Update form state
                  setCurrentTemplateContent(newContent); // Also update local state for editor
                }}
              />
            )}
          />
          {errors.content && <p className="text-red-500 text-sm mt-1">{errors.content.message}</p>}
        </div>

        {/* Scheduling Section */}
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Scheduling</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <Label htmlFor="scheduleDate">Date</Label>
              <Controller
                name="scheduleDate"
                control={control}
                render={({ field }) => (
                  <DatePicker 
                    selected={field.value} 
                    onChange={field.onChange} 
                    placeholderText="Select date"
                    className="w-full" // Apply Tailwind classes for styling
                  />
                )}
              />
            </div>
            <div>
              <Label htmlFor="scheduleTime">Time</Label>
              <Controller
                name="scheduleTime"
                control={control}
                render={({ field }) => (
                   <DatePicker 
                    selected={field.value} 
                    onChange={field.onChange} 
                    showTimeSelect 
                    showTimeSelectOnly 
                    timeIntervals={15} 
                    timeCaption="Time" 
                    dateFormat="h:mm aa" 
                    placeholderText="Select time"
                    className="w-full" // Apply Tailwind classes for styling
                  />
                )}
              />
            </div>
          </div>
          {(!scheduleDate || !scheduleTime) && (scheduleDate || scheduleTime) && <p className="text-red-500 text-sm mt-1">Both date and time are required for scheduling.</p>}
        </div>

        <div className="flex justify-end gap-4">
          <Button variant="outline" type="button" onClick={() => alert('Save draft functionality not implemented yet.')}>
            Save Draft
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Creating...' : 'Create Campaign'}
          </Button>
        </div>
      </form>
    </div>
  );
}
