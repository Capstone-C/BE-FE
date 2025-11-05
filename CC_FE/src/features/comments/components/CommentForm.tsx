import { useState, type ChangeEvent } from 'react';
import { useAddComment, useUpdateComment } from '../hooks/useCommentMutations';

type Props =
  | { mode: 'create'; postId: number; parentId?: number | null }
  | { mode: 'edit'; postId: number; commentId: number; initial: string };

export default function CommentForm(props: Props) {
  const [value, setValue] = useState(props.mode === 'edit' ? props.initial : '');
  const onChange = (e: ChangeEvent<HTMLTextAreaElement>) => setValue(e.target.value);

  const add = props.mode === 'create' ? useAddComment(props.postId) : null;
  const edit = props.mode === 'edit' ? useUpdateComment(props.postId, props.commentId) : null;

  const onSubmit = async () => {
    if (!value.trim()) return;
    if (props.mode === 'create') {
      await add!.mutateAsync({ content: value, parentId: 'parentId' in props ? props.parentId ?? null : null });
      setValue('');
    } else {
      await edit!.mutateAsync({ content: value });
    }
  };

  return (
    <div className="space-y-2">
      <textarea className="border p-2 w-full h-24" value={value} onChange={onChange} placeholder="댓글을 입력하세요" />
      <div className="flex gap-2">
        <button className="border px-3 py-1" onClick={onSubmit}>
          {props.mode === 'create' ? '등록' : '수정'}
        </button>
      </div>
    </div>
  );
}
