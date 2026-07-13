// web/components/template-selector.tsx
'use client'

import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/page-shell';
import { newsletterApi } from '@/lib/newsletter-api'; // Assuming newsletterApi has template functions

interface Template {
  id: string;
  name: string;
  preview?: string; // URL to a preview image or a simple description
  content?: string; // Actual HTML content of the template
}

interface TemplateSelectorProps {
  onSelectTemplate: (templateId: string, templateContent: string) => void;
  initialTemplateId?: string;
}

export function TemplateSelector({ onSelectTemplate, initialTemplateId }: TemplateSelectorProps) {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState<string>(initialTemplateId || 'default');
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchTemplates = async () => {
      setLoading(true);
      try {
        // TODO: Replace with actual API call to fetch templates
        // const fetchedTemplates = await newsletterApi.getTemplates(); 
        const fetchedTemplates: Template[] = [
          { id: 'default', name: 'Default Newsletter', preview: '/previews/default-newsletter.png', content: '<p>This is the default newsletter template.</p>' },
          { id: 'promotional', name: 'Promotional Offer', preview: '/previews/promotional-offer.png', content: '<h2>Special Offer!</h2><p>Get 50% off today!</p>' },
          { id: 'announcement', name: 'Product Announcement', preview: '/previews/product-announcement.png', content: '<h3>New Product Launch!</h3><p>Introducing our latest innovation.</p>' },
        ];
        setTemplates(fetchedTemplates);
        
        // Set initial selected template if provided and available
        if (initialTemplateId && fetchedTemplates.some(t => t.id === initialTemplateId)) {
          setSelectedTemplateId(initialTemplateId);
          const initialTemplate = fetchedTemplates.find(t => t.id === initialTemplateId);
          if (initialTemplate && initialTemplate.content) {
              onSelectTemplate(initialTemplateId, initialTemplate.content);
          }
        } else if (fetchedTemplates.length > 0) {
          // Default to the first template if no initial is set or found
          setSelectedTemplateId(fetchedTemplates[0].id);
          if (fetchedTemplates[0].content) {
            onSelectTemplate(fetchedTemplates[0].id, fetchedTemplates[0].content);
          }
        }

      } catch (error) {
        toast.error('Failed to load templates.');
        console.error('Error fetching templates:', error);
        // Add a fallback default template if API fails
        setTemplates([{ id: 'default-fallback', name: 'Default (Fallback)', content: '<p>A fallback template.</p>' }]);
        setSelectedTemplateId('default-fallback');
        onSelectTemplate('default-fallback', '<p>A fallback template.</p>');
      } finally {
        setLoading(false);
      }
    };

    fetchTemplates();
  }, [onSelectTemplate, initialTemplateId]);

  const handleSelect = (templateId: string) => {
    const template = templates.find(t => t.id === templateId);
    if (template && template.content) {
      setSelectedTemplateId(templateId);
      onSelectTemplate(templateId, template.content);
    } else {
      toast.warn('Selected template content not available.');
    }
  };

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Select Email Template</h3>
      {loading ? (
        <p>Loading templates...</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {templates.map((template) => (
            <Card 
              key={template.id}
              className={`p-4 cursor-pointer transition-all duration-300 ease-in-out ${
                selectedTemplateId === template.id
                  ? 'border-2 border-blue-500 shadow-md bg-blue-50'
                  : 'border border-gray-200 hover:shadow-lg hover:bg-gray-100'
              }`}
              onClick={() => handleSelect(template.id)}
            >
              {template.preview && (
                <div className="mb-3 aspect-video overflow-hidden rounded-md bg-gray-200">
                  {/* Placeholder for image - replace with actual image component or Next/Image */}
                  <img src={template.preview} alt={`${template.name} preview`} className="w-full h-full object-cover"/>
                </div>
              )}
              <h4 className="font-medium mb-1">{template.name}</h4>
              {template.id === selectedTemplateId && (
                <p className="text-xs text-blue-600 font-semibold">Selected</p>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
