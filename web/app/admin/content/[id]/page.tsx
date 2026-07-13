import { PostEditor } from '@/components/post-editor'
export default function EditPostPage({params}:{params:{id:string}}){return <PostEditor id={params.id}/>}
