import { PropsWithChildren } from 'react';

export default function Badge({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return (
    <span
      className={`inline-flex items-center rounded-md bg-gray-100 px-2 py-0.5 text-xs text-gray-700 border ${className}`}
    >
      {children}
    </span>
  );
}
