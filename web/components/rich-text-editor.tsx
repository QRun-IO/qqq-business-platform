'use client'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { Bold, Italic, List, ListOrdered, Quote, Redo2, Undo2 } from 'lucide-react'

export function RichTextEditor({value,onChange,onInsertReady}:{value:string;onChange:(html:string)=>void;onInsertReady?:(insert:(html:string)=>void)=>void}){
  const editor=useEditor({extensions:[StarterKit],content:value,immediatelyRender:false,onUpdate:({editor})=>onChange(editor.getHTML()),onCreate:({editor})=>onInsertReady?.(html=>editor.chain().focus().insertContent(html).run())})
  const tools=[['Bold',Bold,()=>editor?.chain().focus().toggleBold().run(),editor?.isActive('bold')],['Italic',Italic,()=>editor?.chain().focus().toggleItalic().run(),editor?.isActive('italic')],['Bullet list',List,()=>editor?.chain().focus().toggleBulletList().run(),editor?.isActive('bulletList')],['Numbered list',ListOrdered,()=>editor?.chain().focus().toggleOrderedList().run(),editor?.isActive('orderedList')],['Quote',Quote,()=>editor?.chain().focus().toggleBlockquote().run(),editor?.isActive('blockquote')]] as const
  return <div className="overflow-hidden rounded-xl border border-black/10 bg-white text-[#111218]"><div className="flex flex-wrap gap-1 border-b border-black/10 bg-[#f5f5f2] p-2">{tools.map(([label,Icon,action,active])=><button type="button" aria-label={label} title={label} onClick={action} className={`rounded-lg p-2 ${active?'bg-[#173f35] text-white':'hover:bg-black/5'}`} key={label}><Icon size={16}/></button>)}<span className="mx-1 w-px bg-black/10"/><button type="button" aria-label="Undo" onClick={()=>editor?.chain().focus().undo().run()} className="rounded-lg p-2 hover:bg-black/5"><Undo2 size={16}/></button><button type="button" aria-label="Redo" onClick={()=>editor?.chain().focus().redo().run()} className="rounded-lg p-2 hover:bg-black/5"><Redo2 size={16}/></button></div><EditorContent editor={editor} className="prose prose-editor max-w-none"/></div>
}
export default RichTextEditor
