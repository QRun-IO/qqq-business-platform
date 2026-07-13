// Simplified newsletter editor component
'use client'

import { useState } from 'react';

interface NewsletterEditorProps {
  initialContent?: string;
  onChange?: (content: string) => void;
}

export function NewsletterEditor({ initialContent = '', onChange }: NewsletterEditorProps) {
  const [content, setContent] = useState(initialContent);

  const handleContentChange = (value: string) => {
    setContent(value);
    if (onChange) {
      onChange(value);
    }
  };

  return (
    <div className="editor-wrapper">
      <div className="toolbar-container border border-gray-300 rounded-t-md p-2 bg-gray-50">
        <span className="text-sm text-gray-600">Newsletter Editor</span>
      </div>
      <textarea
        className="w-full min-h-[300px] border border-gray-300 rounded-b-md p-4 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-vertical"
        value={content}
        onChange={(e) => handleContentChange(e.target.value)}
        placeholder="Enter your newsletter content here..."
      />
    </div>
  );
}